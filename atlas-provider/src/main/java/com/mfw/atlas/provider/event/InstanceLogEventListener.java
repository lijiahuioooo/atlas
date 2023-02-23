package com.mfw.atlas.provider.event;

import com.mfw.atlas.provider.manager.InstanceLogManager;
import com.mfw.atlas.provider.model.bo.InstanceBO;
import com.mfw.atlas.provider.util.GsonUtils;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * @author KL
 * @Time 2020/10/30 4:48 下午
 */
@Slf4j
@Component
public class InstanceLogEventListener {

    @Autowired
    private InstanceLogManager instanceLogManager;

    private final static ScheduledThreadPoolExecutor logExecutor;

    static {
        logExecutor = new ScheduledThreadPoolExecutor(5, (Runnable r) -> {
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            thread.setName("instanceLogThread");
            return thread;
        });
    }

    @EventListener(InstanceLogEvent.class)
    public void onApplicationEvent(InstanceLogEvent event) {
        log.debug("receive InstanceLogEvent : {} ", GsonUtils.toJsonString(event));
        if (event.getData() == null || !(event.getData() instanceof InstanceBO)){
            return;
        }

        final InstanceBO instanceBO= (InstanceBO) event.getData();
        //记录日志
        logExecutor.schedule(() -> {
            instanceLogManager.insert(instanceBO);
        }, 100, TimeUnit.MILLISECONDS);
    }

}
