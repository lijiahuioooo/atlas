package com.mfw.atlas.provider.event;

import com.mfw.atlas.client.udp.PushPacket;
import com.mfw.atlas.provider.constant.InstanceChangeEnum;
import com.mfw.atlas.provider.constant.InstanceStatusEnum;
import com.mfw.atlas.provider.manager.ConsumerServiceManager;
import com.mfw.atlas.provider.manager.GatewayServiceManager;
import com.mfw.atlas.provider.model.bo.ConsumerServiceBO;
import com.mfw.atlas.provider.model.bo.InstanceBO;
import com.mfw.atlas.provider.model.dto.response.GatewayInstanceDTO;
import com.mfw.atlas.provider.model.po.InstancePO;
import com.mfw.atlas.provider.push.PushService;
import com.mfw.atlas.provider.util.GsonUtils;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * @author KL
 * @Time 2020/10/30 4:48 下午
 */
@Slf4j
@Component
public class ServiceChangeEventListener {

    @Autowired
    private ConsumerServiceManager consumerServiceManager;
    @Autowired
    private GatewayServiceManager gatewayServiceManager;
    @Autowired
    private PushService pushService;
    //default udp port
    private final int defaultUdpPort = 13505;

    @EventListener(ServiceChangeEvent.class)
    public void onApplicationEvent(ServiceChangeEvent event) {
        //log.debug("receive ServiceChangeEvent : {} ", GsonUtils.toJsonString(event));
        if (event.getData() == null || !(event.getData() instanceof InstanceBO)) {
            return;
        }

        final InstanceBO bo = (InstanceBO) event.getData();
        String instanceId = bo.getInstanceId();

        if (event.getType() == InstanceChangeEnum.SDK_GATEWAY_SYN || event.getType() == InstanceChangeEnum.SDK_SYN) {
            //查询订阅此服务的信息
            List<ConsumerServiceBO> sdkServiceList = consumerServiceManager.getByProviderInstanceId(instanceId);
            sdkServiceList.forEach(
                    (service) -> {
                        long lastRefTime = System.nanoTime();
                        String ip = service.getInstancePO().getIp();
                        if(StringUtils.isEmpty(ip) || !service.getInstancePO().getEnvType().equals(bo.getInstancePO().getEnvType())
                            || !InstanceStatusEnum.ENABLE.getCode().equals(service.getInstancePO().getStatus())){
                            log.warn("[ServiceChangeEventListener] not found IP OR env illegal OR status!=1,instance :{}, " +
                                            "consumer instance:{} reversion: {} ,env :{} ,status :{}",
                                    instanceId, service.getInstancePO().getInstanceId(), service.getInstancePO().getReversion(),
                                    service.getInstancePO().getEnvType(), service.getInstancePO().getStatus());
                            return;
                        }
                        String key = getPushKey(service.getConsumerServicePO().getServiceName(), ip, lastRefTime);
                        PushPacket pushData = PushPacket.builder().type(1).key(key)
                                .lastRefTime(lastRefTime)
                                .instanceId(instanceId)
                                .serviceName(service.getConsumerServicePO().getServiceName())
                                .data(getPushPacketData(bo.getInstancePO()))
                                .build();

                        pushService.submitPushTask(ip, defaultUdpPort, key,
                                GsonUtils.toJsonString(pushData));
                    }
            );
        }
        //有变化就通知网关
        if (event.getType() != InstanceChangeEnum.SDK_SYN) {
            List<GatewayInstanceDTO> gateways = gatewayServiceManager.getGatewayInstances();
            gateways.forEach(
                    (gateway) -> {
                        long lastRefTime = System.nanoTime();
                        PushPacket pushData = null;
                        String key = "";
                        if (event.getType().equals(InstanceChangeEnum.GATEWAY_SYN_ALL)) {
                            key = getPushKey(gateway.getPort(), gateway.getIp(), lastRefTime);
                            pushData = PushPacket.builder()
                                    .type(1)
                                    .key(key)
                                    .lastRefTime(lastRefTime)
                                    .build();
                        } else if (bo != null && bo.getInstancePO() != null && gateway.getEnvType().equals(bo.getInstancePO().getEnvType())) {
                            key = getPushKey(bo.getInstancePO().getInstanceId(), gateway.getIp(), lastRefTime);
                            pushData = PushPacket.builder().type(0).key(key).lastRefTime(lastRefTime)
                                    .instanceId(instanceId).serviceName(bo.getInstancePO().getAppCode())
                                    .data(getPushPacketData(bo.getInstancePO()))
                                    .build();
                        }
                        if (pushData != null) {
                            pushService.submitPushGatewayTask(gateway.getIp(), Integer.parseInt(gateway.getPort()), key,
                                    GsonUtils.toJsonString(pushData));
                        }
                    }
            );
        }

    }


    private String getPushKey(String serviceName, String clientIp, long lastRefTime) {
        //并发性如何防重复
        return new StringBuilder().append(serviceName).append(":").append(clientIp).append(":").append(lastRefTime)
                .toString();
    }

    private PushPacket.PushPacketData getPushPacketData(InstancePO instancePO) {
        return PushPacket.PushPacketData.builder()
                .appCode(instancePO.getAppCode())
                .envCode(instancePO.getEnvCode())
                .envType(instancePO.getEnvType())
                .build();
    }

}
