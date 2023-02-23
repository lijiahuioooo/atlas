package com.mfw.atlas.sdk.spring.cloud;

import com.mfw.atlas.client.udp.PushEvent;
import com.mfw.atlas.client.udp.PushEventListener;
import com.mfw.atlas.client.udp.PushPacket;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: zhangyang1
 */
public class ServerPushEventListener implements PushEventListener {

    private MfwDiscoveryProperties mfwDiscoveryProperties;

    private static final Logger log = LoggerFactory
            .getLogger(ServerPushEventListener.class);


    public ServerPushEventListener(MfwDiscoveryProperties mfwDiscoveryProperties) {
        this.mfwDiscoveryProperties = mfwDiscoveryProperties;
    }

    @Override
    public void onEvent(PushEvent event) {
        PushPacket packet = event.getPushPacket();
        String serviceName = packet.getServiceName();
        log.info("ServerPushEventListener,serviceName:{},event:{}", serviceName, event.toString());
        if (StringUtils.isNotBlank(serviceName) && mfwDiscoveryProperties.getConsumers().contains(serviceName)){
            mfwDiscoveryProperties.namingServiceInstance().refreshInstance(serviceName, new ArrayList<String>());
        }
    }
}
