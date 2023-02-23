package com.mfw.atlas.sdk.dubbo.mfwdiscovery.report;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.fastjson.JSON;
import com.mfw.atlas.client.constants.RegisterTypeEnum;
import com.mfw.atlas.client.constants.ServiceTypeEnum;
import com.mfw.atlas.client.model.dto.ConsumerServiceDTO;
import com.mfw.atlas.client.model.dto.ProviderServiceDTO;
import com.mfw.atlas.client.discoveryclient.RegisterClient;
import com.mfw.atlas.client.utils.EnvUtils;
import com.mfw.atlas.sdk.dubbo.mfwdiscovery.registry.MFWDiscoveryRegistry;

import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContext;
import java.util.*;

import static com.alibaba.dubbo.common.Constants.PATH_KEY;
import static com.alibaba.dubbo.common.Constants.PROTOCOL_KEY;
import static com.alibaba.dubbo.common.Constants.CATEGORY_KEY;
import static com.alibaba.dubbo.common.Constants.DEFAULT_CATEGORY;

/**
 * 服务上报信息
 *
 * @author fenghua
 */
public class ServiceReporter {
    private static String serverAddress;
    private static String clientVersion;
    private static Map<String, String> baseParams;

    private RegisterClient registerClient;

    private static String serverInstanceId = EnvUtils.getInstanceId();

    private Collection<RegistryConfig> registryConfigs = new ArrayList<>();

    /**
     * 在MFWDiscovery中调用ServiceReport的初始化
     */
    public void report(ApplicationContext applicationContext) {
        Map<String, RegistryConfig> registryConfigMap = applicationContext == null ? null : BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, RegistryConfig.class, false, false);
        registryConfigs = registryConfigMap.values();

        registerClient = new RegisterClient(serverAddress, clientVersion, baseParams);
        reportServiceInfo(MFWDiscoveryRegistry.getRegisterUrls());
        reportSubscribeInfo(MFWDiscoveryRegistry.getSubscribeUrls());
    }

    public static void setRequestEssentials(String serverAddress, String clientVersion, Map<String, String> baseParams) {
        ServiceReporter.serverAddress = serverAddress;
        ServiceReporter.clientVersion = clientVersion;
        ServiceReporter.baseParams = baseParams;
    }

    private void reportServiceInfo(List<URL> registerUrls) {
        List<ProviderServiceDTO> serviceList = new ArrayList<>();

        registerUrls.forEach(url -> {

            URL metaParams = url.addParameter(CATEGORY_KEY, url.getParameter(CATEGORY_KEY, DEFAULT_CATEGORY));
            metaParams = metaParams.addParameter(PROTOCOL_KEY, url.getProtocol());
            metaParams = metaParams.addParameter(PATH_KEY, url.getPath());

            serviceList.add(new ProviderServiceDTO()
                    .setInstanceId(serverInstanceId)
                    .setProtocol(url.getProtocol())
                    .setServiceName(getServiceName(url))
                    .setServiceType(ServiceTypeEnum.DUBBO)
                    .setServiceGroup(url.getParameter("group"))
                    .setServiceVersion(url.getParameter("version"))
                    .setAppCode(EnvUtils.getAppCode())
                    .setEnvType(EnvUtils.getEnvType())
                    .setEnvGroup(EnvUtils.getEnvGroup())
                    .setVersion(EnvUtils.getAppVersionId())
                    .setRegisterType(getRegisterType())
                    .setMetadata(JSON.toJSONString(new HashMap<>(metaParams.getParameters())))
            );
        });

        if(!serviceList.isEmpty()) {
            registerClient.register(serviceList);
        }
    }

    private String getServiceName(URL url) {
        return "providers:" +
                url.getParameter("interface") + ":" +
                url.getParameter("version", "") + ":" +
                url.getParameter("group", "");
    }

    private void reportSubscribeInfo(List<URL> subscribeUrls) {
        List<ConsumerServiceDTO> consumerServiceList = new ArrayList<>();
        subscribeUrls.forEach(url -> {
            consumerServiceList.add(new ConsumerServiceDTO()
                    .setInstanceId(serverInstanceId)
                    .setProtocol(url.getProtocol())
                    .setServiceName(getServiceName(url))
                    .setServiceType(ServiceTypeEnum.DUBBO)
                    .setServiceGroup(url.getParameter("group"))
                    .setServiceVersion(url.getParameter("version"))
                    .setAppCode(EnvUtils.getAppCode())
                    .setEnvType(EnvUtils.getEnvType())
                    .setEnvGroup(EnvUtils.getEnvGroup())
                    .setVersion(EnvUtils.getAppVersionId())
                    .setRegisterType(getRegisterType())
            );
        });
        if(!consumerServiceList.isEmpty()) {
            registerClient.subscribe(consumerServiceList);
        }
    }

    private RegisterTypeEnum getRegisterType() {
        RegisterTypeEnum type = RegisterTypeEnum.MFW;

        for(RegistryConfig config : registryConfigs) {
            URL registryUrl = URL.valueOf(config.getAddress());
            Boolean isRegister = Objects.nonNull(config.isRegister()) ? config.isRegister() : true;
            RegisterTypeEnum configType = RegisterTypeEnum.getByName(registryUrl.getProtocol());
            if(isRegister && !Objects.isNull(configType) && !configType.equals(RegisterTypeEnum.MFW)) {
                type = configType;
                break;
            }
        }

        return type;
    }
}
