package com.mfw.atlas.client.udp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

/**
 * @author KL
 * @Time 2020/10/22 11:31 上午
 */
public class PushEventDispatcher {

    private static final Logger logger = LoggerFactory.getLogger(PushEventDispatcher.class);

    private final ExecutorService executor;

    private final BlockingQueue<PushEvent> pushEventQueue = new LinkedBlockingQueue<PushEvent>(1000);

    private final List<PushEventListener> observers = Collections.synchronizedList(new ArrayList<PushEventListener>());

    private final ThreadPoolExecutor notifyExecutor;

    public PushEventDispatcher() {
        notifyExecutor = new ThreadPoolExecutor(
                20,
                100,
                1000 * 60 * 3,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(1000),
                (Runnable r) -> {
                    Thread thread = new Thread(r);
                    thread.setDaemon(true);
                    thread.setName("instancePushThread");
                    return thread;
                });

        executor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(),
                new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread thread = new Thread(r, "atlas.client.pushEventDispatcher");
                        thread.setDaemon(true);
                        return thread;
                    }
                });
        executor.execute(new Notifier());

    }

    public void addListener(PushEventListener listener) {
        logger.info("[pushEventDispatcher LISTENER] adding " + listener.getClass());
        if (this.observers != null) {
            this.observers.add(listener);
        }
    }

    public void removeListener(PushEventListener listener) {
        logger.info("[pushEventDispatcher LISTENER] removing " + listener.getClass());
        if (this.observers != null) {
            Iterator<PushEventListener> iter = this.observers.iterator();
            while (iter.hasNext()) {
                PushEventListener oldListener = iter.next();
                if (oldListener.equals(listener)) {
                    iter.remove();
                }
            }
        }
    }

    public List<PushEventListener> getSubscribeListeners() {
        return new ArrayList<>(observers);
    }

    public Boolean publishEvent(PushEvent pushEvent) {
        logger.info("[pushEventDispatcher publishEvent] : " + pushEvent.toString());
        if (this.pushEventQueue != null) {
            return this.pushEventQueue.add(pushEvent);
        }
        return false;
    }

    public void notifierAllListener(final PushEvent pushEvent) {
        if (pushEvent == null) {
            return;
        }
        List<PushEventListener> listeners = this.getSubscribeListeners();
        try {
            if (!CollectionUtils.isEmpty(listeners)) {
                for (PushEventListener listener : listeners) {
                    if (listener == null) {
                        continue;
                    }
                    listener.onEvent(pushEvent);
                }
            }

        } catch (Exception e) {
            logger.error("[pushEventDispatcher notify error] ,event: {} ,listeners: {}", pushEvent, listeners);
        }
    }

    private class Notifier implements Runnable {

        @Override
        public void run() {
            while (true) {
                PushEvent pushEvent = null;
                try {
                    pushEvent = pushEventQueue.poll(1, TimeUnit.MINUTES);
                } catch (Exception ignore) {
                    logger.debug("[pushEventDispatcher poll ignore]");
                }
                if (pushEvent == null) {
                    continue;
                }
                final PushEvent finalPushEvent = pushEvent;
                notifyExecutor.submit(() -> notifierAllListener(finalPushEvent));
            }
        }


    }
}
