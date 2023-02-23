package com.mfw.atlas.provider.test.udp;

import com.mfw.atlas.provider.constant.InstanceChangeEnum;
import com.mfw.atlas.provider.event.ServiceChangeEvent;
import com.mfw.atlas.provider.event.ServiceChangeEventListener;
import com.mfw.atlas.provider.manager.InstanceManager;
import com.mfw.atlas.provider.model.bo.InstanceBO;
import com.mfw.atlas.provider.test.AtlasTestApplication;
import com.mfw.atlas.provider.util.GsonUtils;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class UdpSend extends AtlasTestApplication {
    @Autowired
    private ServiceChangeEventListener serviceChangeEventListener;
    @Autowired
    private InstanceManager instanceManager;

    @Test
    public void senUdp() throws InterruptedException {
        List<InstanceBO> bos = instanceManager.getByInstanceIds(Lists.newArrayList("336886-dubbosingleservicedemo-msp-7949db746c-44b57"));
        Assert.assertTrue(bos.size() >= 1);
        log.info("send udp instanceId : {}", GsonUtils.toJsonString(bos));
        ServiceChangeEvent event = new ServiceChangeEvent(this, InstanceChangeEnum.GATEWAY_SYN, bos.get(0));
        serviceChangeEventListener.onApplicationEvent(event);
        Assert.assertTrue(true);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        countDownLatch.await();
    }
}
