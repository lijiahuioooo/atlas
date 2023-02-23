package com.mfw.atlas.provider.event;

import com.mfw.atlas.client.utils.StringUtils;
import com.mfw.atlas.provider.util.MAlertHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * @author KL
 * @Time 2020/12/8 7:08 下午
 */
@Slf4j
@Component
public class AlertEventListener implements ApplicationListener<AlertEvent> {

    @Autowired
    private MAlertHelper alertHelper;

    @Override
    public void onApplicationEvent(AlertEvent event) {
        if (event != null && StringUtils.isNotEmpty(event.getAlertMsg())) {
            log.debug("receive  AlertEvent:  {}", event.getAlertMsg());
            alertHelper.alertEventMap(event.getAlertMsg());
        }
    }

}
