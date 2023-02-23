package com.mfw.atlas.provider.event;

import org.springframework.context.ApplicationEvent;

public class InstanceLogEvent extends ApplicationEvent {

    public Object data;

    public InstanceLogEvent(Object source,Object data) {
        super(source);
        this.data=data;
    }

    public Object getData() {
        return data;
    }
}
