package com.mfw.atlas.sdk.spring.cloud.client.naming;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ListView;
import com.alibaba.nacos.api.naming.pojo.ServiceInfo;
import com.alibaba.nacos.api.naming.utils.NamingUtils;
import com.alibaba.nacos.api.selector.AbstractSelector;
import com.mfw.atlas.client.constants.RegisterTypeEnum;
import com.mfw.atlas.client.constants.ServiceTypeEnum;
import com.mfw.atlas.client.discoveryclient.DiscoveryClient;
import com.mfw.atlas.client.discoveryclient.RegisterClient;
import com.mfw.atlas.client.model.dto.ConsumerServiceDTO;
import com.mfw.atlas.client.model.dto.ProviderServiceDTO;
import com.mfw.atlas.client.utils.EnvUtils;
import com.mfw.atlas.sdk.spring.cloud.PropertyKeyConst;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.*;

import static com.alibaba.nacos.api.PropertyKeyConst.SERVER_ADDR;
import static com.mfw.atlas.client.constants.GlobalStatusConstants.MFW_PARAMETER_PREFIX;

/**
 * @Author: zhangyang1
 */
public class MfwNamingService implements NamingService {

    private MfwHostReactor hostReactor;
    private RegisterClient registerClient;
    private Properties properties;
    private static final Logger log = LoggerFactory
            .getLogger(MfwNamingService.class);

    public MfwNamingService(Properties properties) {
        init(properties);
    }

    private void init(Properties properties) {
        this.properties = properties;
        registerClient = new RegisterClient(properties.getProperty(SERVER_ADDR),
                properties.getProperty(PropertyKeyConst.CLIENT_VERSION), new HashMap<String, String>());
        Map<String, Object> extendedParams = new HashMap<>();;
        for (Object key:properties.keySet()){
            if(Objects.nonNull(key) && key instanceof String && key.toString().startsWith(MFW_PARAMETER_PREFIX)){
                extendedParams.putIfAbsent((String) key,properties.get(key));
            }
        }

        DiscoveryClient discoveryClient = new DiscoveryClient(properties.getProperty(SERVER_ADDR),
                properties.getProperty(PropertyKeyConst.CLIENT_VERSION), new HashMap<String, String>(), extendedParams);

        hostReactor = new MfwHostReactor(discoveryClient);
    }

    @Override
    public void registerInstance(String serviceName, String ip, int port) throws NacosException {
    }

    @Override
    public void registerInstance(String serviceName, String groupName, String ip, int port) throws NacosException {
    }

    @Override
    public void registerInstance(String serviceName, String ip, int port, String clusterName) throws NacosException {
    }

    @Override
    public void registerInstance(String serviceName, String groupName, String ip, int port, String clusterName) throws NacosException {

    }

    @Override
    public void registerInstance(String serviceName, Instance instance) throws NacosException {
        registerProvider(serviceName, instance);
        registerConsumer();
    }


    private void registerProvider(String serviceName, Instance instance){
        ProviderServiceDTO providerServiceDTO = new ProviderServiceDTO();
        providerServiceDTO.setAppCode(EnvUtils.getAppCode())
                .setInstanceId(EnvUtils.getInstanceId())
                .setCtime(new Date())
                .setEnvType(EnvUtils.getEnvType())
                .setIp(EnvUtils.getIp())
                .setMtime(new Date())
                .setPort(String.valueOf(instance.getPort()))
                .setRegisterType(RegisterTypeEnum.NACOS)
                .setServiceName(serviceName)
                .setServiceType(ServiceTypeEnum.SPRING_CLOUD)
                .setMetadata(JSON.toJSONString(instance.getMetadata()))
                .setServiceVersion("")
                .setProtocol("http")
                .setServiceGroup(Constants.DEFAULT_GROUP);
        registerClient.register(Arrays.asList(providerServiceDTO));
        log.info("registerProvider:{} finished", providerServiceDTO);
    }

    private void registerConsumer(){
        String consumerJsonStr = properties.getProperty(PropertyKeyConst.CONSUMERS);
        if (StringUtils.isNotBlank(consumerJsonStr)){
            List<String> consumers = JSON.parseObject(consumerJsonStr, List.class);
            if (!CollectionUtils.isEmpty(consumers)){
                List<ConsumerServiceDTO> consumerServiceDTOS = new ArrayList<>();
                for(String consumerName : consumers){
                    consumerServiceDTOS.add(new ConsumerServiceDTO()
                            .setInstanceId(EnvUtils.getInstanceId())
                            .setProtocol("http")
                            .setServiceName(consumerName)
                            .setServiceType(ServiceTypeEnum.SPRING_CLOUD)
                            .setServiceGroup(Constants.DEFAULT_GROUP)
                            .setRegisterType(RegisterTypeEnum.NACOS)
                            .setEnvType(EnvUtils.getEnvType())
                            .setServiceVersion("")
                    );
                }
                registerClient.subscribe(consumerServiceDTOS);
                log.info("registerConsumer:{} finished", consumerServiceDTOS);
            }
        }
    }

    @Override
    public void registerInstance(String serviceName, String groupName, Instance instance) throws NacosException {

    }

    @Override
    public void deregisterInstance(String serviceName, String ip, int port) throws NacosException {

    }

    @Override
    public void deregisterInstance(String serviceName, String groupName, String ip, int port) throws NacosException {

    }

    @Override
    public void deregisterInstance(String serviceName, String ip, int port, String clusterName) throws NacosException {

    }

    @Override
    public void deregisterInstance(String serviceName, String groupName, String ip, int port, String clusterName) throws NacosException {

    }

    @Override
    public void deregisterInstance(String serviceName, Instance instance) throws NacosException {

    }

    @Override
    public void deregisterInstance(String serviceName, String groupName, Instance instance) throws NacosException {

    }

    @Override
    public List<Instance> getAllInstances(String serviceName) throws NacosException {
        return null;
    }

    @Override
    public List<Instance> getAllInstances(String serviceName, String groupName) throws NacosException {
        return null;
    }

    @Override
    public List<Instance> getAllInstances(String serviceName, boolean subscribe) throws NacosException {
        return null;
    }

    @Override
    public List<Instance> getAllInstances(String serviceName, String groupName, boolean subscribe) throws NacosException {
        return null;
    }

    @Override
    public List<Instance> getAllInstances(String serviceName, List<String> clusters) throws NacosException {
        return null;
    }

    @Override
    public List<Instance> getAllInstances(String serviceName, String groupName, List<String> clusters) throws NacosException {
        return null;
    }

    @Override
    public List<Instance> getAllInstances(String serviceName, List<String> clusters, boolean subscribe) throws NacosException {
        return null;
    }

    @Override
    public List<Instance> getAllInstances(String serviceName, String groupName, List<String> clusters, boolean subscribe) throws NacosException {
        return null;
    }

    @Override
    public List<Instance> selectInstances(String serviceName, boolean healthy) throws NacosException {
        return selectInstances(serviceName, new ArrayList<String>(), healthy);
    }

    @Override
    public List<Instance> selectInstances(String serviceName, String groupName, boolean healthy) throws NacosException {
        return null;
    }

    @Override
    public List<Instance> selectInstances(String serviceName, boolean healthy, boolean subscribe) throws NacosException {
        return null;
    }

    @Override
    public List<Instance> selectInstances(String serviceName, String groupName, boolean healthy, boolean subscribe) throws NacosException {
        return null;
    }

    @Override
    public List<Instance> selectInstances(String serviceName, List<String> clusters, boolean healthy) throws NacosException {
        return selectInstances(serviceName, clusters, healthy, true);
    }

    @Override
    public List<Instance> selectInstances(String serviceName, String groupName, List<String> clusters, boolean healthy) throws NacosException {
        return null;
    }

    @Override
    public List<Instance> selectInstances(String serviceName, List<String> clusters, boolean healthy, boolean subscribe) throws NacosException {
        return selectInstances(serviceName, Constants.DEFAULT_GROUP, clusters, healthy, subscribe);
    }

    @Override
    public List<Instance> selectInstances(String serviceName, String groupName, List<String> clusters, boolean healthy, boolean subscribe) throws NacosException {
        ServiceInfo serviceInfo;
        if (subscribe) {
            serviceInfo = hostReactor.getServiceInfo(NamingUtils.getGroupedName(serviceName, groupName), StringUtils.join(clusters, ","));
        } else {
            serviceInfo = hostReactor.getServiceInfoDirectlyFromServer(NamingUtils.getGroupedName(serviceName, groupName), StringUtils.join(clusters, ","));
        }
        log.debug("selectInstances,serviceInfo:{}", serviceInfo);
        return selectInstances(serviceInfo, healthy);
    }

    public boolean refreshInstance(String serviceName,List<String> clusters){
        return hostReactor.updateServiceNow(NamingUtils.getGroupedName(serviceName, Constants.DEFAULT_GROUP), StringUtils.join(clusters, ","));
    }

    @Override
    public Instance selectOneHealthyInstance(String serviceName) throws NacosException {
        return null;
    }

    @Override
    public Instance selectOneHealthyInstance(String serviceName, String groupName) throws NacosException {
        return null;
    }

    @Override
    public Instance selectOneHealthyInstance(String serviceName, boolean subscribe) throws NacosException {
        return null;
    }

    @Override
    public Instance selectOneHealthyInstance(String serviceName, String groupName, boolean subscribe) throws NacosException {
        return null;
    }

    @Override
    public Instance selectOneHealthyInstance(String serviceName, List<String> clusters) throws NacosException {
        return null;
    }

    @Override
    public Instance selectOneHealthyInstance(String serviceName, String groupName, List<String> clusters) throws NacosException {
        return null;
    }

    @Override
    public Instance selectOneHealthyInstance(String serviceName, List<String> clusters, boolean subscribe) throws NacosException {
        return null;
    }

    @Override
    public Instance selectOneHealthyInstance(String serviceName, String groupName, List<String> clusters, boolean subscribe) throws NacosException {
        return null;
    }

    @Override
    public void subscribe(String serviceName, EventListener listener) throws NacosException {

    }

    @Override
    public void subscribe(String serviceName, String groupName, EventListener listener) throws NacosException {

    }

    @Override
    public void subscribe(String serviceName, List<String> clusters, EventListener listener) throws NacosException {

    }

    @Override
    public void subscribe(String serviceName, String groupName, List<String> clusters, EventListener listener) throws NacosException {

    }

    @Override
    public void unsubscribe(String serviceName, EventListener listener) throws NacosException {

    }

    @Override
    public void unsubscribe(String serviceName, String groupName, EventListener listener) throws NacosException {

    }

    @Override
    public void unsubscribe(String serviceName, List<String> clusters, EventListener listener) throws NacosException {

    }

    @Override
    public void unsubscribe(String serviceName, String groupName, List<String> clusters, EventListener listener) throws NacosException {

    }

    @Override
    public ListView<String> getServicesOfServer(int pageNo, int pageSize) throws NacosException {
        return null;
    }

    @Override
    public ListView<String> getServicesOfServer(int pageNo, int pageSize, String groupName) throws NacosException {
        return null;
    }

    @Override
    public ListView<String> getServicesOfServer(int pageNo, int pageSize, AbstractSelector selector) throws NacosException {
        return null;
    }

    @Override
    public ListView<String> getServicesOfServer(int pageNo, int pageSize, String groupName, AbstractSelector selector) throws NacosException {
        return null;
    }

    @Override
    public List<ServiceInfo> getSubscribeServices() throws NacosException {
        return null;
    }

    @Override
    public String getServerStatus() {
        return null;
    }



    private List<Instance> selectInstances(ServiceInfo serviceInfo, boolean healthy) {
        List<Instance> list;
        if (serviceInfo == null || CollectionUtils.isEmpty(list = serviceInfo.getHosts())) {
            return new ArrayList<Instance>();
        }

        Iterator<Instance> iterator = list.iterator();
        while (iterator.hasNext()) {
            Instance instance = iterator.next();
            if (healthy != instance.isHealthy() || !instance.isEnabled() || instance.getWeight() <= 0) {
                iterator.remove();
            }
        }

        return list;
    }
}
