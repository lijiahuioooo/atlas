package com.mfw.atlas.provider.event;

import javax.annotation.PostConstruct;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Component;

/**
 * @author KL
 * @Time 2020/12/8 8:06 下午
 */
@Component
public class AlertEventPublisher implements ApplicationEventPublisherAware {

    private ApplicationEventPublisher applicationEventPublisher;

    private static ApplicationEventPublisher publisher;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @PostConstruct
    public void init() {
        publisher = applicationEventPublisher;
    }

    public static void publishEvent(String format, Object... args) {
        publisher.publishEvent(new AlertEvent(String.format(format, args)));
    }
}
