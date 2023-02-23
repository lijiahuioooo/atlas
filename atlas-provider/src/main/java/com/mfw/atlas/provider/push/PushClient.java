package com.mfw.atlas.provider.push;

import com.mfw.atlas.provider.event.AlertEventPublisher;
import java.net.DatagramSocket;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

/**
 * @author KL
 * @Time 2020/10/30 5:02 下午
 */
@Slf4j
public class PushClient implements Callable<Boolean> {

    private volatile int MAX_RETRY_TIMES = 1;
    private static final long ACK_TIMEOUT = 1000L;

    public String key;
    private volatile DatagramSocket udpSocket;
    private volatile ConcurrentHashMap<String, Long> udpSendTimeMap;
    private volatile ConcurrentMap<String, PushAckReceiver.AckEntry> ackMap;
    private volatile ScheduledExecutorService retryPushExecutor;
    private volatile int maxRetryTimes;
    private volatile boolean isAlert;

    public PushClient(String key, DatagramSocket udpSocket, ConcurrentHashMap<String, Long> udpSendTimeMap,
            ConcurrentMap<String, PushAckReceiver.AckEntry> ackMap, ScheduledExecutorService retryPushExecutor) {
        this.key = key;
        this.udpSocket = udpSocket;
        this.udpSendTimeMap = udpSendTimeMap;
        this.ackMap = ackMap;
        this.retryPushExecutor = retryPushExecutor;
        this.maxRetryTimes = MAX_RETRY_TIMES;
    }

    public PushClient(String key, DatagramSocket udpSocket, ConcurrentHashMap<String, Long> udpSendTimeMap,
            ConcurrentMap<String, PushAckReceiver.AckEntry> ackMap, ScheduledExecutorService retryPushExecutor,
            int maxRetryTimes, boolean isAlert) {
        this.key = key;
        this.udpSocket = udpSocket;
        this.udpSendTimeMap = udpSendTimeMap;
        this.ackMap = ackMap;
        this.retryPushExecutor = retryPushExecutor;
        this.maxRetryTimes = maxRetryTimes;
        this.isAlert = isAlert;
    }


    @Override
    public Boolean call() {

        PushAckReceiver.AckEntry ackEntry = ackMap.get(key);
        if (ackEntry == null) {
            udpSendTimeMap.remove(key);
            return true;
        }
        if (ackEntry.getRetryTimes() > this.maxRetryTimes) {
            log.error("[PushClient] max re-push times reached, retry times {}, key: {}", ackEntry.getRetryTimes(),
                    ackEntry.key);
            ackMap.remove(ackEntry.key);
            udpSendTimeMap.remove(ackEntry.key);
            if (isAlert) {
                //发送报警
                AlertEventPublisher.publishEvent("PushClient send udp error, [key: %s,retry: %s]", key, this.maxRetryTimes);
            }
            return true;
        }

        try {
            log.info("[PushClient] send udp packet: {}" ,ackEntry.key);
            udpSendTimeMap.put(ackEntry.key, System.currentTimeMillis());
            udpSocket.send(ackEntry.origin);
            ackEntry.increaseRetryTime();
            //重试策略
            retryPushExecutor.schedule(this, ACK_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("[PushClient] failed to push data: {} to client: {}, error: {}",
                    ackEntry.data, ackEntry.origin.getAddress().getHostAddress(), e);
        }
        return true;
    }

}
