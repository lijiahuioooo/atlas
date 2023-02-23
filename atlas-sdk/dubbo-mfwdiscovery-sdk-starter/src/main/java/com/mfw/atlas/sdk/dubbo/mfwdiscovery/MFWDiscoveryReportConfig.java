package com.mfw.atlas.sdk.dubbo.mfwdiscovery;

import com.mfw.atlas.client.utils.EnvUtils;
import com.mfw.atlas.sdk.dubbo.mfwdiscovery.report.ServiceReporter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import javax.annotation.Resource;

/**
 * 从springboot应用外部mfw discovery中获取spring context，从而获取其中的application properties信息
 *
 * @author fenghua
 */
@Configuration
@Slf4j
public class MFWDiscoveryReportConfig {
    @Resource
    private ApplicationContext appContext;

    @EventListener(ApplicationReadyEvent.class)
    void ready() {
        log.info("atlas register start.");
        if(EnvUtils.isEnvValiate()) {
            ServiceReporter serviceReporter = new ServiceReporter();
            serviceReporter.report(appContext);
        } else {
            log.info("can not regist for lack of env info: {}", EnvUtils.getEnvInfomation());
        }
    }
}
