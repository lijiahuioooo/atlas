package com.mfw.atlas.provider.event;

import org.springframework.context.ApplicationEvent;

/**
 * @author KL
 * @Time 2020/12/8 7:06 下午
 */
public class AlertEvent extends ApplicationEvent {

    private String alertMsg;

    /**
     *
     * @param alertMsg
     */
    public AlertEvent(String alertMsg) {
        super(alertMsg);
        this.alertMsg = alertMsg;
    }

    public String getAlertMsg() {
        return alertMsg;
    }
}
