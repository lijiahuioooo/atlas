
package com.mfw.atlas.provider.push;

import com.mfw.atlas.provider.config.AtlasEnvProperties;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

/**
 * @author KL
 * @Time 2020/10/22 1:57 下午
 */
@Slf4j
@Component
@Singleton
public class PushService implements SmartLifecycle {
    @Resource
    private AtlasEnvProperties atlasEnvProperties;

    private volatile boolean isRunning = false;

    private final static ThreadPoolExecutor pushExecutor;
    private final static ScheduledExecutorService retryPushExecutor;

    private static DatagramSocket udpSocket;
    private static volatile ConcurrentHashMap<String, Long> udpSendTimeMap = new ConcurrentHashMap<>();
    private static volatile ConcurrentMap<String, PushAckReceiver.AckEntry> ackMap = new ConcurrentHashMap<>();

    static {
        pushExecutor = new ThreadPoolExecutor(
                20,
                60,
                1000 * 60 * 3,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(),
                (Runnable r) -> {
                    Thread thread = new Thread(r);
                    thread.setDaemon(true);
                    thread.setName("instancePushThread");
                    return thread;
                });

        retryPushExecutor = new ScheduledThreadPoolExecutor(10, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setDaemon(true);
                thread.setName("atlas.pushReceiver");
                return thread;
            }
        });
    }


    public void submitPushTask(String ip, int port, String key, String sendData) {
        try {

            PushAckReceiver.AckEntry ackEntry = new PushAckReceiver.AckEntry(key,
                    new DatagramPacket(sendData.getBytes(Charset.forName("UTF-8")),
                            sendData.getBytes(Charset.forName("UTF-8")).length, InetAddress.getByName(ip), port));

            ackMap.put(key, ackEntry);
            //创建pushClient
            PushClient pushClient = new PushClient(key, udpSocket, udpSendTimeMap, ackMap,retryPushExecutor);
            pushExecutor.submit(pushClient);
        } catch (Exception e) {
            log.error("submitPushTask error exception: {}", e.getMessage());
        }
    }

    public void submitPushGatewayTask(String ip, int port, String key, String sendData) {
        try {

            PushAckReceiver.AckEntry ackEntry = new PushAckReceiver.AckEntry(key,
                    new DatagramPacket(sendData.getBytes(Charset.forName("UTF-8")),
                            sendData.getBytes(Charset.forName("UTF-8")).length, InetAddress.getByName(ip), port));

            ackMap.put(key, ackEntry);
            //创建pushClient
            PushClient pushClient = new PushClient(key, udpSocket, udpSendTimeMap, ackMap, retryPushExecutor, 3, true);
            pushExecutor.submit(pushClient);
        } catch (Exception e) {
            log.error("submitPushGatewayTask error exception: {}", e.getMessage());
        }
    }

    @Override
    public void start() {
        isRunning = true;
        try {
            udpSocket = new DatagramSocket(atlasEnvProperties.getDatagramSocketPort());
            PushAckReceiver.start(new PushAckReceiver(udpSocket, udpSendTimeMap, ackMap));
        } catch (SocketException e) {
            log.error("[pushAckReceiver] failed to init push service");
        }
    }

    @Override
    public void stop() {
        isRunning = false;
        if (!pushExecutor.isShutdown()) {
            pushExecutor.shutdown();
        }
        if (!pushExecutor.isShutdown()) {
            pushExecutor.shutdown();
        }
        if (!pushExecutor.isShutdown()) {
            pushExecutor.shutdown();
        }
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

}
