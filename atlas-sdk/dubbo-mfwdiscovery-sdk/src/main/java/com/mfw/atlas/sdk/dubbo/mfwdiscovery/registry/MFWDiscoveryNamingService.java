package com.mfw.atlas.sdk.dubbo.mfwdiscovery.registry;


import static com.mfw.atlas.sdk.dubbo.mfwdiscovery.utils.common.LogUtils.NAMING_LOGGER;
import static com.mfw.atlas.client.constants.GlobalStatusConstants.MFW_PARAMETER_PREFIX;

import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ListView;
import com.alibaba.nacos.api.naming.pojo.ServiceInfo;
import com.alibaba.nacos.api.selector.AbstractSelector;
import com.mfw.atlas.client.constants.ServiceTypeEnum;
import com.mfw.atlas.client.udp.PushEvent;
import com.mfw.atlas.client.udp.PushEventListener;
import com.mfw.atlas.client.udp.PushPacket;
import com.mfw.atlas.client.discoveryclient.DiscoveryClient;
import com.mfw.atlas.client.model.dto.ProviderServiceDTO;
import com.mfw.atlas.client.utils.EnvUtils;
import com.mfw.atlas.sdk.dubbo.mfwdiscovery.report.ServiceReporter;
import com.mfw.atlas.sdk.dubbo.mfwdiscovery.utils.common.CollectionUtils;
import com.mfw.atlas.sdk.dubbo.mfwdiscovery.utils.common.JacksonUtils;
import com.mfw.atlas.sdk.dubbo.mfwdiscovery.utils.common.StringUtils;
import com.mfw.atlas.sdk.dubbo.mfwdiscovery.utils.common.ValidatorUtils;
import com.mfw.atlas.sdk.dubbo.mfwdiscovery.utils.naming.InitUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * mfw discovery naming service
 * @author fenghua
 */
public class MFWDiscoveryNamingService implements NamingService, PushEventListener {

    private static final long UPDATE_HOLD_INTERVAL = 5000L;
    private static final long DEFAULT_DELAY = 5000L;
    private static final String NAMESPACE_KEY = "namespace";
    private static final String CLIENT_VERSION = "1.0.0";

    private final Map<String, ScheduledFuture<?>> futureMap = new HashMap<>();
    private final Map<String, ServiceInfo> serviceInfoMap = new ConcurrentHashMap<>(16);
    private final Map<String, Object> updatingMap = new ConcurrentHashMap<>();
    private final ScheduledExecutorService executor;

    private DiscoveryClient discoveryClient;
    private ServiceReporter serviceReporter;
    private String serverAddress;
    private EventDispatcher eventDispatcher = new EventDispatcher();

//    public MFWDiscoveryNamingService(String serverList) throws NacosException {
//        Properties properties = new Properties();
//        properties.setProperty(PropertyKeyConst.SERVER_ADDR, serverList);
//        executor = this.createRefreshServiceInstanceExecutor();
//        init(properties);
//    }

    public MFWDiscoveryNamingService(Properties properties) throws NacosException {
        executor = this.createRefreshServiceInstanceExecutor();
        init(properties);
    }

    private void init(Properties properties) throws NacosException {
        ValidatorUtils.checkInitParam(properties);
        initServerAddr(properties);
        initDiscoveryClient(properties);
        initServiceReporter(properties);
        InitUtils.initSerialization();
        InitUtils.initWebRootContext();
    }

    private void initServerAddr(Properties properties) {
        serverAddress = properties.getProperty(PropertyKeyConst.SERVER_ADDR);
    }

    private void initDiscoveryClient(Properties properties) {
        Map<String, String> params = new HashMap<>(2);
        if(properties.containsKey(NAMESPACE_KEY)) {
            params.put(NAMESPACE_KEY, properties.getProperty(NAMESPACE_KEY));
        }

        Map<String, Object> extendedParams = new HashMap<>();;

        for (Object key:properties.keySet()){
            if(Objects.nonNull(key) && key instanceof String && key.toString().startsWith(MFW_PARAMETER_PREFIX)){
                extendedParams.putIfAbsent((String) key,properties.get(key));
            }
        }

        discoveryClient = new DiscoveryClient("http://"+serverAddress, CLIENT_VERSION, params,extendedParams);
    }

    private void initServiceReporter(Properties properties) {
        Map<String, String> params = new HashMap<>(2);
        if(properties.containsKey(NAMESPACE_KEY)) {
            params.put(NAMESPACE_KEY, properties.getProperty(NAMESPACE_KEY));
        }
        ServiceReporter.setRequestEssentials("http://"+serverAddress, CLIENT_VERSION, params);
    }

    private ScheduledThreadPoolExecutor createRefreshServiceInstanceExecutor() {
        int pollingThreadCount = Runtime.getRuntime().availableProcessors() > 1 ?
                Runtime.getRuntime().availableProcessors() / 2 : 1;

        return new ScheduledThreadPoolExecutor(pollingThreadCount, r -> {
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            thread.setName("com.alibaba.mfwdiscovery.client.naming.updater");
            return thread;
        });
    }

    @Override
    public void registerInstance(String serviceName, String ip, int port) {
        registerInstance(serviceName, ip, port, Constants.DEFAULT_CLUSTER_NAME);
    }

    @Override
    public void registerInstance(String serviceName, String groupName, String ip, int port) {
        registerInstance(serviceName, groupName, ip, port, Constants.DEFAULT_CLUSTER_NAME);
    }

    @Override
    public void registerInstance(String serviceName, String ip, int port, String clusterName) {
        registerInstance(serviceName, Constants.DEFAULT_GROUP, ip, port, clusterName);
    }

    @Override
    public void registerInstance(String serviceName, Instance instance) {
        registerInstance(serviceName, Constants.DEFAULT_GROUP, instance);
    }

    @Override
    public void registerInstance(String serviceName, String groupName, String ip, int port, String clusterName) {
        Instance instance = new Instance();
        instance.setIp(ip);
        instance.setPort(port);
        instance.setWeight(1.0);
        instance.setClusterName(clusterName);

        registerInstance(serviceName, groupName, instance);
    }

    @Override
    public void registerInstance(String serviceName, String groupName, Instance instance) {
        // do nothing
    }

    @Override
    public void deregisterInstance(String serviceName, String ip, int port) {
        deregisterInstance(serviceName, ip, port, Constants.DEFAULT_CLUSTER_NAME);
    }

    @Override
    public void deregisterInstance(String serviceName, String groupName, String ip, int port) {
        deregisterInstance(serviceName, groupName, ip, port, Constants.DEFAULT_CLUSTER_NAME);
    }

    @Override
    public void deregisterInstance(String serviceName, String ip, int port, String clusterName) {
        deregisterInstance(serviceName, Constants.DEFAULT_GROUP, ip, port, clusterName);
    }

    @Override
    public void deregisterInstance(String serviceName, Instance instance) {
        deregisterInstance(serviceName, Constants.DEFAULT_GROUP, instance);
    }

    @Override
    public void deregisterInstance(String serviceName, String groupName, String ip, int port, String clusterName) {
        Instance instance = new Instance();
        instance.setIp(ip);
        instance.setPort(port);
        instance.setClusterName(clusterName);

        deregisterInstance(serviceName, groupName, instance);
    }

    @Override
    public void deregisterInstance(String serviceName, String groupName, Instance instance) {
        // do nothing
    }

    @Override
    public List<Instance> getAllInstances(String serviceName) throws NacosException {
        return getAllInstances(serviceName, new ArrayList<>());
    }

    @Override
    public List<Instance> getAllInstances(String serviceName, String groupName) throws NacosException {
        return getAllInstances(serviceName, groupName, new ArrayList<>());
    }

    @Override
    public List<Instance> getAllInstances(String serviceName, boolean subscribe) throws NacosException {
        return getAllInstances(serviceName, new ArrayList<>(), subscribe);
    }

    @Override
    public List<Instance> getAllInstances(String serviceName, String groupName, boolean subscribe) throws NacosException {
        return getAllInstances(serviceName, groupName, new ArrayList<>(), subscribe);
    }

    @Override
    public List<Instance> getAllInstances(String serviceName, List<String> clusters) throws NacosException {
        return getAllInstances(serviceName, clusters, true);
    }

    @Override
    public List<Instance> getAllInstances(String serviceName, String groupName, List<String> clusters) throws NacosException {
        return getAllInstances(serviceName, groupName, clusters, true);
    }

    @Override
    public List<Instance> getAllInstances(String serviceName, List<String> clusters, boolean subscribe) throws NacosException {
        return getAllInstances(serviceName, Constants.DEFAULT_GROUP, clusters, subscribe);
    }

    @Override
    public List<Instance> getAllInstances(String serviceName, String groupName, List<String> clusters, boolean subscribe) throws NacosException {
        // nacos中没有启用groupName，改造的naming service就不再需要了
        // serviceName = NamingUtils.getGroupedName(serviceName, groupName);
        String clusterStr = StringUtils.join(clusters, ",");

        ServiceInfo service;
        if (subscribe) {
            service = getServiceInfo(serviceName, clusterStr);
        } else {
            service = getServiceInfoDirectly(serviceName, clusterStr);
        }

        List<Instance> list = new ArrayList<>();
        if(!Objects.isNull(service)) {
            list = service.getHosts();
        }
        return list;
    }

    @Override
    public List<Instance> selectInstances(String serviceName, boolean healthy) throws NacosException {
        return selectInstances(serviceName, new ArrayList<>(), healthy);
    }

    @Override
    public List<Instance> selectInstances(String serviceName, String groupName, boolean healthy) throws NacosException {
        return selectInstances(serviceName, groupName, healthy, true);
    }

    @Override
    public List<Instance> selectInstances(String serviceName, boolean healthy, boolean subscribe) throws NacosException {
        return selectInstances(serviceName, new ArrayList<>(), healthy, subscribe);
    }

    @Override
    public List<Instance> selectInstances(String serviceName, String groupName, boolean healthy, boolean subscribe) throws NacosException {
        return selectInstances(serviceName, groupName, new ArrayList<>(), healthy, subscribe);
    }

    @Override
    public List<Instance> selectInstances(String serviceName, List<String> clusters, boolean healthy) throws NacosException {
        return selectInstances(serviceName, clusters, healthy, true);
    }

    @Override
    public List<Instance> selectInstances(String serviceName, String groupName, List<String> clusters, boolean healthy) throws NacosException {
        return selectInstances(serviceName, groupName, clusters, healthy, true);
    }

    @Override
    public List<Instance> selectInstances(String serviceName, List<String> clusters, boolean healthy, boolean subscribe) throws NacosException {
        return selectInstances(serviceName, Constants.DEFAULT_GROUP, clusters, healthy, subscribe);
    }

    @Override
    public List<Instance> selectInstances(String serviceName, String groupName, List<String> clusters, boolean healthy, boolean subscribe) throws NacosException {
        //serviceName = NamingUtils.getGroupedName(serviceName, groupName);
        String clusterStr = StringUtils.join(clusters, ",");

        ServiceInfo service;
        if(subscribe) {
            service = getServiceInfo(serviceName, clusterStr);
        } else {
            service = getServiceInfoDirectly(serviceName, clusterStr);
        }

        return selectInstances(service, healthy);
    }

    @Override
    public Instance selectOneHealthyInstance(String serviceName) throws NacosException {
        return selectOneHealthyInstance(serviceName, new ArrayList<>());
    }

    @Override
    public Instance selectOneHealthyInstance(String serviceName, String groupName) throws NacosException {
        return selectOneHealthyInstance(serviceName, groupName, true);
    }

    @Override
    public Instance selectOneHealthyInstance(String serviceName, boolean subscribe) throws NacosException {
        return selectOneHealthyInstance(serviceName, new ArrayList<>(), subscribe);
    }

    @Override
    public Instance selectOneHealthyInstance(String serviceName, String groupName, boolean subscribe) throws NacosException {
        return selectOneHealthyInstance(serviceName, groupName, new ArrayList<>(), subscribe);
    }

    @Override
    public Instance selectOneHealthyInstance(String serviceName, List<String> clusters) throws NacosException {
        return selectOneHealthyInstance(serviceName, clusters, true);
    }

    @Override
    public Instance selectOneHealthyInstance(String serviceName, String groupName, List<String> clusters) throws NacosException {
        return selectOneHealthyInstance(serviceName, groupName, clusters, true);
    }

    @Override
    public Instance selectOneHealthyInstance(String serviceName, List<String> clusters, boolean subscribe) throws NacosException {
        return selectOneHealthyInstance(serviceName, Constants.DEFAULT_GROUP, clusters, subscribe);
    }

    @Override
    public Instance selectOneHealthyInstance(String serviceName, String groupName, List<String> clusters, boolean subscribe) throws NacosException {
        //serviceName = NamingUtils.getGroupedName(serviceName, groupName);
        String clusterStr = StringUtils.join(clusters, ",");
        if (subscribe) {
            return Balancer.RandomByWeight.selectHost(getServiceInfo(serviceName, clusterStr));
        } else {
            return Balancer.RandomByWeight.selectHost(getServiceInfoDirectly(serviceName, clusterStr));
        }
    }

    @Override
    public void subscribe(String serviceName, EventListener listener) {
        subscribe(serviceName, new ArrayList<>(), listener);
    }

    @Override
    public void subscribe(String serviceName, String groupName, EventListener listener) {
        subscribe(serviceName, groupName, new ArrayList<>(), listener);
    }

    @Override
    public void subscribe(String serviceName, List<String> clusters, EventListener listener) {
        subscribe(serviceName, Constants.DEFAULT_GROUP, clusters, listener);
    }

    @Override
    public void subscribe(String serviceName, String groupName, List<String> clusters, EventListener listener) {
        this.eventDispatcher.addListener(getServiceInfo(serviceName, StringUtils.join(clusters, ",")), StringUtils.join(clusters, ","), listener);
    }

    @Override
    public void unsubscribe(String serviceName, EventListener listener) {
        unsubscribe(serviceName, new ArrayList<>(), listener);
    }

    @Override
    public void unsubscribe(String serviceName, String groupName, EventListener listener) {
        unsubscribe(serviceName, groupName, new ArrayList<>(), listener);
    }

    @Override
    public void unsubscribe(String serviceName, List<String> clusters, EventListener listener) {
        unsubscribe(serviceName, Constants.DEFAULT_GROUP, clusters, listener);
    }

    @Override
    public void unsubscribe(String serviceName, String groupName, List<String> clusters, EventListener listener) {
        this.eventDispatcher.removeListener(serviceName, StringUtils.join(clusters, ","), listener);
    }

    @Override
    public List<ServiceInfo> getSubscribeServices() {
        return this.eventDispatcher.getSubscribeServices();
    }

    @Override
    public ListView<String> getServicesOfServer(int pageNo, int pageSize) {
        return new ListView<>();
    }

    @Override
    public ListView<String> getServicesOfServer(int pageNo, int pageSize, String groupName) {
        return getServicesOfServer(pageNo, pageSize, groupName, null);
    }

    @Override
    public ListView<String> getServicesOfServer(int pageNo, int pageSize, AbstractSelector selector) {
        return getServicesOfServer(pageNo, pageSize, Constants.DEFAULT_GROUP, selector);
    }

    @Override
    public ListView<String> getServicesOfServer(int pageNo, int pageSize, String groupName, AbstractSelector selector) {
        return new ListView<>();
    }

    @Override
    public String getServerStatus() {
        return "UP";
    }

    @Override
    public void onEvent(PushEvent event) {
        try {
            PushPacket.PushPacketData pushData = event.getPushPacket().getData();
            String serviceName = event.getPushPacket().getServiceName();
            String clusterStr = "";
            if(pushData.getEnvType().equals(EnvUtils.getEnvType()) && serviceInfoMap.containsKey(ServiceInfo.getKey(serviceName, clusterStr))) {
                updateService(serviceName, clusterStr);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<Instance> selectInstances(ServiceInfo serviceInfo, boolean healthy) {
        List<Instance> list;
        if (serviceInfo == null || CollectionUtils.isEmpty(list = serviceInfo.getHosts())) {
            return new ArrayList<>();
        }

        list.removeIf(instance -> healthy != instance.isHealthy() || !instance.isEnabled() || instance.getWeight() <= 0);

        return list;
    }

    @SuppressWarnings("unchecked")
    private ServiceInfo getServiceInfoDirectly(String serviceName, String clusterStr) throws NacosException {
        ServiceInfo serviceInfo = new ServiceInfo(serviceName, clusterStr);
        List<ProviderServiceDTO> serviceInstanceList = getServiceInstanceListFromServer(serviceName, clusterStr);
        serviceInfo.setLastRefTime(System.currentTimeMillis());
        if(Objects.isNull(serviceInstanceList)) {
            throw new NacosException(NacosException.SERVER_ERROR, "service instance null");
        }

        List<Instance> hosts = new ArrayList<>();
        for (ProviderServiceDTO serviceInstance : serviceInstanceList) {
            Instance ins = new Instance();
            ins.setInstanceId(serviceInstance.getInstanceId());
            ins.setServiceName(serviceName);
            ins.setClusterName(clusterStr);
            ins.setIp(serviceInstance.getIp());
            ins.setPort(Integer.parseInt(serviceInstance.getPort()));
            ins.setMetadata((Map<String, String>) JacksonUtils.toObj(serviceInstance.getMetadata(), Map.class));
            hosts.add(ins);
        }

        serviceInfo.setHosts(hosts);

        return serviceInfo;
    }

    private void updateService(String serviceName, String clusterStr) {
        updatingMap.put(serviceName, new Object());
        refreshServiceInstance(serviceName, clusterStr);
        updatingMap.remove(serviceName);
    }

    private ServiceInfo getServiceInfo(String serviceName, String clusterStr) {
        ServiceInfo service = getCachedServiceInfo(serviceName, clusterStr);

        if(Objects.isNull(service)) {
            service = new ServiceInfo(serviceName, clusterStr);
            serviceInfoMap.put(service.getKey(), service);
            updateService(serviceName, clusterStr);
        } else if (updatingMap.containsKey(serviceName)) {

            if (UPDATE_HOLD_INTERVAL > 0) {
                // hold a moment waiting for update finish
                synchronized (service) {
                    try {
                        service.wait(UPDATE_HOLD_INTERVAL);
                    } catch (InterruptedException e) {
                        NAMING_LOGGER
                                .error("[getServiceInfo] serviceName:" + serviceName + ", clusters:" + clusterStr, e);
                    }
                }
            }
        }

        scheduleUpdateIfAbsent(serviceName, clusterStr);

        return serviceInfoMap.get(service.getKey());
    }

    private void refreshServiceInstance(String serviceName, String clusterStr) {
        ServiceInfo oldService = getCachedServiceInfo(serviceName, clusterStr);
        try {

            ServiceInfo serviceInfo = getServiceInfoDirectly(serviceName, clusterStr);
            processRefreshService(serviceInfo);
        } catch (NacosException e) {
            e.printStackTrace();
        } finally {
            if (oldService != null) {
                synchronized (oldService) {
                    oldService.notifyAll();
                }
            }
        }
    }

    private ServiceInfo getCachedServiceInfo(String serviceName, String clusters) {
        String key = ServiceInfo.getKey(serviceName, clusters);
        return serviceInfoMap.get(key);
    }

    private void scheduleUpdateIfAbsent(String serviceName, String clusters) {
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

    private synchronized ScheduledFuture<?> addTask(UpdateTask task) {
        return executor.schedule(task, DEFAULT_DELAY, TimeUnit.MILLISECONDS);
    }

    private List<ProviderServiceDTO> getServiceInstanceListFromServer(String serviceName, String clusterStr) {

        List<ProviderServiceDTO> serviceInstanceList = null;

        try {
            /*
              get service group and version from service name
              例如：providers:com.alibaba.cloud.dubbo.service.DubboMetadataService:1.0.0:sales-service-prod
              group: sales-service-prod
              version: 1.0.0
             */
            String[] servicePart = serviceName.split(":");

            serviceInstanceList = discoveryClient.getServiceInstanceList(ServiceTypeEnum.DUBBO, serviceName, clusterStr,
                    servicePart.length == 4 ? servicePart[3] : "", servicePart[2]);
        } catch(Exception e) {
            e.printStackTrace();
        }

        return serviceInstanceList;
    }

    private void processRefreshService(ServiceInfo serviceInfo) {
        if (!serviceInfo.validate()) {
            return;
        }

        boolean changed = false;

        ServiceInfo oldService = serviceInfoMap.get(serviceInfo.getKey());
        if (oldService != null) {

            if (oldService.getLastRefTime() > serviceInfo.getLastRefTime()) {
                NAMING_LOGGER.warn("out of date data received, old-t: " + oldService.getLastRefTime() + ", new-t: "
                        + serviceInfo.getLastRefTime());
            }

            serviceInfoMap.put(serviceInfo.getKey(), serviceInfo);

            Map<String, Instance> oldHostMap = new HashMap<>(oldService.getHosts().size());
            for (Instance host : oldService.getHosts()) {
                oldHostMap.put(host.toInetAddr(), host);
            }

            Map<String, Instance> newHostMap = new HashMap<>(serviceInfo.getHosts().size());
            for (Instance host : serviceInfo.getHosts()) {
                newHostMap.put(host.toInetAddr(), host);
            }

            Set<Instance> modHosts = new HashSet<>();
            Set<Instance> newHosts = new HashSet<>();
            Set<Instance> remvHosts = new HashSet<>();

            List<Map.Entry<String, Instance>> newServiceHosts = new ArrayList<>(
                    newHostMap.entrySet());
            for (Map.Entry<String, Instance> entry : newServiceHosts) {
                Instance host = entry.getValue();
                String key = entry.getKey();
                if (oldHostMap.containsKey(key) && !StringUtils
                        .equals(host.toString(), oldHostMap.get(key).toString())) {
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
                NAMING_LOGGER.info("new ips(" + newHosts.size() + ") service: " + serviceInfo.getKey() + " -> "
                        + JacksonUtils.toJson(newHosts));
            }

            if (remvHosts.size() > 0) {
                changed = true;
                NAMING_LOGGER.info("removed ips(" + remvHosts.size() + ") service: " + serviceInfo.getKey() + " -> "
                        + JacksonUtils.toJson(remvHosts));
            }

            if (modHosts.size() > 0) {
                changed = true;
                NAMING_LOGGER.info("modified ips(" + modHosts.size() + ") service: " + serviceInfo.getKey() + " -> "
                        + JacksonUtils.toJson(modHosts));
            }

            if (newHosts.size() > 0 || remvHosts.size() > 0 || modHosts.size() > 0) {
                eventDispatcher.serviceChanged(serviceInfo);
            }

        } else {
            changed = true;
            NAMING_LOGGER.info("init new ips(" + serviceInfo.ipCount() + ") service: " + serviceInfo.getKey() + " -> "
                    + JacksonUtils.toJson(serviceInfo.getHosts()));
            serviceInfoMap.put(serviceInfo.getKey(), serviceInfo);
            eventDispatcher.serviceChanged(serviceInfo);
        }

        if (changed) {
            NAMING_LOGGER.info("current ips:(" + serviceInfo.ipCount() + ") service: " + serviceInfo.getKey() + " -> "
                    + JacksonUtils.toJson(serviceInfo.getHosts()));
        }
    }



    public class UpdateTask implements Runnable {

        long lastRefTime = Long.MAX_VALUE;

        private final String clusters;

        private final String serviceName;

        /**
         * the fail situation. 1:can't connect to server 2:serviceInfo's hosts is empty
         */
        private int failCount = 0;

        UpdateTask(String serviceName, String clusters) {
            this.serviceName = serviceName;
            this.clusters = clusters;
        }

        private void incFailCount() {
            int limit = 6;
            if (failCount == limit) {
                return;
            }
            failCount++;
        }

        private void resetFailCount() {
            failCount = 0;
        }

        @Override
        public void run() {
            long delayTime = DEFAULT_DELAY;

            try {
                ServiceInfo serviceObj = serviceInfoMap.get(ServiceInfo.getKey(serviceName, clusters));

                if (serviceObj == null) {
                    refreshServiceInstance(serviceName, clusters);
                    return;
                }

                if (serviceObj.getLastRefTime() <= lastRefTime) {
                    refreshServiceInstance(serviceName, clusters);
                    serviceObj = serviceInfoMap.get(ServiceInfo.getKey(serviceName, clusters));
                }

                lastRefTime = serviceObj.getLastRefTime();

                if (!eventDispatcher.isSubscribed(serviceName, clusters) && !futureMap
                        .containsKey(ServiceInfo.getKey(serviceName, clusters))) {
                    // abort the update task
                    NAMING_LOGGER.info("update task is stopped, service:" + serviceName + ", clusters:" + clusters);
                    return;
                }
                if (CollectionUtils.isEmpty(serviceObj.getHosts())) {
                    incFailCount();
                    return;
                }
                //delayTime = serviceObj.getCacheMillis();
                resetFailCount();
            } catch (Throwable e) {
                incFailCount();
                NAMING_LOGGER.warn("[NA] failed to update serviceName: " + serviceName, e);
            } finally {
                executor.schedule(this, Math.min(delayTime << failCount, DEFAULT_DELAY * 12), TimeUnit.MILLISECONDS);
            }
        }
    }
}
