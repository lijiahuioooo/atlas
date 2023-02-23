package com.mfw.atlas.sdk.spring.cloud.client.naming;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ServiceInfo;
import com.alibaba.nacos.api.naming.utils.NamingUtils;
import com.mfw.atlas.client.constants.ServiceTypeEnum;
import com.mfw.atlas.client.discoveryclient.DiscoveryClient;
import com.mfw.atlas.client.model.dto.ProviderServiceDTO;
import com.mfw.atlas.sdk.spring.cloud.utils.net.HttpClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import java.util.*;
import java.util.concurrent.*;

import static com.mfw.atlas.sdk.spring.cloud.utils.LogUtils.NAMING_LOGGER;

/**
 * @Author: zhangyang1
 */
public class MfwHostReactor {

    private static final long DEFAULT_DELAY = 5000L;
    private static final long UPDATE_HOLD_INTERVAL = 5000L;

    private ScheduledExecutorService executor;
    private Map<String, ServiceInfo> serviceInfoMap;
    private final Map<String, ScheduledFuture<?>> futureMap = new HashMap<String, ScheduledFuture<?>>();
    private Map<String, Object> updatingMap;
    private DiscoveryClient discoveryClient;

    public static final int DEFAULT_POLLING_THREAD_COUNT =
            Runtime.getRuntime().availableProcessors() > 1 ? Runtime.getRuntime().availableProcessors() / 2 : 1;

    public static final String ENCODING = "UTF-8";

    public MfwHostReactor(DiscoveryClient discoveryClient) {

        this.discoveryClient = discoveryClient;

        executor = new ScheduledThreadPoolExecutor(DEFAULT_POLLING_THREAD_COUNT, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setDaemon(true);
                thread.setName("com.mfw.atals.sdk.spring.cloud.client.naming.updater");
                return thread;
            }
        });

        this.serviceInfoMap = new ConcurrentHashMap<String, ServiceInfo>(16);
        this.updatingMap = new ConcurrentHashMap<String, Object>();
    }

    public synchronized ScheduledFuture<?> addTask(UpdateTask task) {
        return executor.schedule(task, DEFAULT_DELAY, TimeUnit.MILLISECONDS);
    }

    public ServiceInfo getServiceInfo(final String serviceName, final String clusters) {
        ServiceInfo serviceObj = getCachedServiceInfo(serviceName, clusters);

        if (null == serviceObj) {
            serviceObj = new ServiceInfo(serviceName, clusters);

            serviceInfoMap.put(serviceObj.getKey(), serviceObj);

            updatingMap.put(serviceName, new Object());
            updateServiceNow(serviceName, clusters);
            updatingMap.remove(serviceName);

        } else if (updatingMap.containsKey(serviceName)) {

            if (UPDATE_HOLD_INTERVAL > 0) {
                // hold a moment waiting for update finish
                synchronized (serviceObj) {
                    try {
                        serviceObj.wait(UPDATE_HOLD_INTERVAL);
                    } catch (InterruptedException e) {
                        NAMING_LOGGER.error("[getServiceInfo] serviceName:" + serviceName + ", clusters:" + clusters, e);
                    }
                }
            }
        }

        scheduleUpdateIfAbsent(serviceName, clusters);

        return serviceInfoMap.get(serviceObj.getKey());
    }

    public ServiceInfo getServiceInfoDirectlyFromServer(final String serviceName, final String clusters) throws NacosException {
        ServiceInfo serviceInfo = new ServiceInfo(serviceName, clusters);
        List<ProviderServiceDTO> providerServiceDTOS = getServiceInstanceListFromServer(serviceName, clusters);
        serviceInfo.setLastRefTime(System.currentTimeMillis());
        List<Instance> hosts = null;

        if(Objects.nonNull(providerServiceDTOS)) {
            hosts = new ArrayList<>();
            for (ProviderServiceDTO serviceInstance : providerServiceDTOS) {
                Instance instance = new Instance();
                instance.setInstanceId(serviceInstance.getInstanceId());
                instance.setServiceName(serviceName);
                instance.setClusterName(clusters);
                instance.setIp(serviceInstance.getIp());
                instance.setPort(Integer.parseInt(serviceInstance.getPort()));
                if (StringUtils.isNotBlank(serviceInstance.getMetadata())){
                    instance.setMetadata((Map<String, String>) JSON.parseObject(serviceInstance.getMetadata(), Map.class));
                    Map<String, String> metaData = instance.getMetadata();
                    if(Objects.nonNull(metaData) && Objects.nonNull(metaData.get("weight"))) {
                        String instanceWeight = metaData.get("weight");
                        instance.setWeight(Double.parseDouble(instanceWeight));
                    }
                }
                if (isInstanceHealthy(instance)){
                    hosts.add(instance);
                }
            }
        }
        
        serviceInfo.setHosts(hosts);
        return serviceInfo;
    }

    private Boolean isInstanceHealthy(Instance instance){

        String url = "http://" + instance.getIp() +":"+ instance.getPort();
        HttpClient.HttpResult result = HttpClient.request(url, null, null, ENCODING, HttpMethod.HEAD.name());
        return result.code == HttpStatus.OK.value();
    }

    private List<ProviderServiceDTO> getServiceInstanceListFromServer(String serviceName, String clusterStr) {
        List<ProviderServiceDTO> dtos = discoveryClient.getServiceInstanceList(ServiceTypeEnum.SPRING_CLOUD, NamingUtils.getServiceName(serviceName), clusterStr, NamingUtils.getGroupName(serviceName), "");
        return dtos;
    }

    private ServiceInfo getCachedServiceInfo(String serviceName, String clusters) {

        String key = ServiceInfo.getKey(serviceName, clusters);

        return serviceInfoMap.get(key);
    }

    public void scheduleUpdateIfAbsent(String serviceName, String clusters) {
        if (futureMap.get(ServiceInfo.getKey(serviceName, clusters)) != null) {
            return;
        }

        synchronized (futureMap) {
            if (futureMap.get(ServiceInfo.getKey(serviceName, clusters)) != null) {
                return;
            }

            ScheduledFuture<?> future = addTask(new UpdateTask(serviceName, clusters));
            futureMap.put(ServiceInfo.getKey(serviceName, clusters), future);
        }
    }

    public boolean updateServiceNow(String serviceName, String clusters) {
        ServiceInfo oldService = getCachedServiceInfo(serviceName, clusters);
        try {
            ServiceInfo serviceInfo = getServiceInfoDirectlyFromServer(serviceName, clusters);
            processService(serviceInfo);
            return true;
        } catch (Exception e) {
            NAMING_LOGGER.error("[NA] failed to update serviceName: " + serviceName, e);
            return false;
        } finally {
            if (oldService != null) {
                synchronized (oldService) {
                    oldService.notifyAll();
                }
            }
        }
    }

    public ServiceInfo processService(ServiceInfo serviceInfo) {
        ServiceInfo oldService = serviceInfoMap.get(serviceInfo.getKey());
        if (serviceInfo.getHosts() == null || !serviceInfo.validate()) {
            //empty or error push, just ignore
            return oldService;
        }

        boolean changed = false;

        if (oldService != null) {

            if (oldService.getLastRefTime() > serviceInfo.getLastRefTime()) {
                NAMING_LOGGER.warn("out of date data received, old-t: " + oldService.getLastRefTime()
                        + ", new-t: " + serviceInfo.getLastRefTime());
            }

            serviceInfoMap.put(serviceInfo.getKey(), serviceInfo);

            Map<String, Instance> oldHostMap = new HashMap<String, Instance>(oldService.getHosts().size());
            for (Instance host : oldService.getHosts()) {
                oldHostMap.put(host.toInetAddr(), host);
            }

            Map<String, Instance> newHostMap = new HashMap<String, Instance>(serviceInfo.getHosts().size());
            for (Instance host : serviceInfo.getHosts()) {
                newHostMap.put(host.toInetAddr(), host);
            }

            Set<Instance> modHosts = new HashSet<Instance>();
            Set<Instance> newHosts = new HashSet<Instance>();
            Set<Instance> remvHosts = new HashSet<Instance>();

            List<Map.Entry<String, Instance>> newServiceHosts = new ArrayList<Map.Entry<String, Instance>>(
                    newHostMap.entrySet());
            for (Map.Entry<String, Instance> entry : newServiceHosts) {
                Instance host = entry.getValue();
                String key = entry.getKey();
                if (oldHostMap.containsKey(key) && !StringUtils.equals(host.toString(),
                        oldHostMap.get(key).toString())) {
                    modHosts.add(host);
                    continue;
                }

                if (!oldHostMap.containsKey(key)) {
                    newHosts.add(host);
                }
            }

            for (Map.Entry<String, Instance> entry : oldHostMap.entrySet()) {
                Instance host = entry.getValue();
                String key = entry.getKey();
                if (newHostMap.containsKey(key)) {
                    continue;
                }

                if (!newHostMap.containsKey(key)) {
                    remvHosts.add(host);
                }

            }

            if (newHosts.size() > 0) {
                changed = true;
                NAMING_LOGGER.info("new ips(" + newHosts.size() + ") service: "
                        + serviceInfo.getKey() + " -> " + JSON.toJSONString(newHosts));
            }

            if (remvHosts.size() > 0) {
                changed = true;
                NAMING_LOGGER.info("removed ips(" + remvHosts.size() + ") service: "
                        + serviceInfo.getKey() + " -> " + JSON.toJSONString(remvHosts));
            }

            if (modHosts.size() > 0) {
                changed = true;
                NAMING_LOGGER.info("modified ips(" + modHosts.size() + ") service: "
                        + serviceInfo.getKey() + " -> " + JSON.toJSONString(modHosts));
            }
        } else {
            changed = true;
            NAMING_LOGGER.info("init new ips(" + serviceInfo.ipCount() + ") service: " + serviceInfo.getKey() + " -> " + JSON
                    .toJSONString(serviceInfo.getHosts()));
            serviceInfoMap.put(serviceInfo.getKey(), serviceInfo);
        }

//        MetricsMonitor.getServiceInfoMapSizeMonitor().set(serviceInfoMap.size());

        if (changed) {
            NAMING_LOGGER.info("current ips:(" + serviceInfo.ipCount() + ") service: " + serviceInfo.getKey() +
                    " -> " + JSON.toJSONString(serviceInfo.getHosts()));
        }

        return serviceInfo;
    }

    public class UpdateTask implements Runnable {
        long lastRefTime = Long.MAX_VALUE;
        private String clusters;
        private String serviceName;

        public UpdateTask(String serviceName, String clusters) {
            this.serviceName = serviceName;
            this.clusters = clusters;
        }

        @Override
        public void run() {
            try {
                ServiceInfo serviceObj = serviceInfoMap.get(ServiceInfo.getKey(serviceName, clusters));

                if (serviceObj == null) {
                    updateServiceNow(serviceName, clusters);
                    executor.schedule(this, DEFAULT_DELAY, TimeUnit.MILLISECONDS);
                    return;
                }

                if (serviceObj.getLastRefTime() <= lastRefTime) {
                    updateServiceNow(serviceName, clusters);
                    serviceObj = serviceInfoMap.get(ServiceInfo.getKey(serviceName, clusters));
                }

                executor.schedule(this, DEFAULT_DELAY, TimeUnit.MILLISECONDS);

                lastRefTime = serviceObj.getLastRefTime();
            } catch (Throwable e) {
                NAMING_LOGGER.warn("[NA] failed to update serviceName: " + serviceName, e);
            }

        }
    }
}
