package com.mfw.atlas.sdk.spring.cloud.registry;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.mfw.atlas.client.utils.EnvUtils;
import com.mfw.atlas.sdk.spring.cloud.MfwDiscoveryProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;


/**
 * @author zhangyang1
 */
public class MfwServiceRegistry{

    private static final Logger log = LoggerFactory.getLogger(MfwServiceRegistry.class);

    MfwDiscoveryProperties mfwDiscoveryProperties;

    private final MfwRegistration registration;

    private final NamingService namingService;

    public MfwServiceRegistry(MfwDiscoveryProperties mfwDiscoveryProperties, MfwRegistration registration) {
        this.mfwDiscoveryProperties = mfwDiscoveryProperties;
        namingService = mfwDiscoveryProperties.namingServiceInstance();
        this.registration = registration;
    }

    @PostConstruct
    public void register() {
        log.info("atlas register start.");
        if (EnvUtils.isEnvValiate()){
            Instance instance = new Instance();
            instance.setIp(this.registration.getHost());
            instance.setPort(this.registration.getPort());
            instance.setMetadata(this.registration.getMetadata());
            try{
                log.info("regist with service name: {}; instance: {}", this.registration.getServiceId(),instance);
                namingService.registerInstance(this.registration.getServiceId(), instance);
            } catch (Exception e) {
                throw new IllegalStateException(
                        "can not register instances, serviceId=" + this.registration.getServiceId()
                                + "; instance=" + instance,e);
            }
        } else {
            log.info("can not regist for lack of env info: {}", EnvUtils.getEnvInfomation());
        }
    }
}
