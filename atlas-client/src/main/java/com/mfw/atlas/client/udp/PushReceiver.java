package com.mfw.atlas.client.udp;

import com.mfw.atlas.client.utils.GsonUtils;
import com.mfw.atlas.client.utils.StringUtils;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.Charset;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author KL
 * @Time 2020/10/22 1:57 下午
 */
public class PushReceiver implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(PushReceiver.class);

    private ScheduledExecutorService executorService;

    private static final int UDP_MSS = 64 * 1024;

    private DatagramSocket udpSocket;

    private PushEventDispatcher pushEventDispatcher;

    //default udp port
    private int defaultPort = 13505;

    public PushReceiver(int udpPort){
        init(udpPort);
    }

    public PushReceiver() {
        String udpPort = System.getenv("mfw.discovery.udp.port");
        if (StringUtils.isNotBlank(udpPort)) {
            init(Integer.valueOf(udpPort));
        } else {
            init(defaultPort);
        }
    }

    private void init(int udpPort){
        try {
            udpSocket = new DatagramSocket(udpPort);

            executorService = new ScheduledThreadPoolExecutor(1, new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    Thread thread = new Thread(r);
                    thread.setDaemon(true);
                    thread.setName("atlas.client.pushReceiver");
                    return thread;
                }
            });
            logger.info("[pushReceiver] started at port: {}", udpPort);
            executorService.execute(this);
        } catch (Exception e) {
            logger.error("[pushReceiver] init udp socket failed", e);
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                // byte[] is initialized with 0 full filled by default
                byte[] buffer = new byte[UDP_MSS];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                udpSocket.receive(packet);

                String json = new String(packet.getData(), "UTF-8").trim();
                logger.info("pushReceiver received push data: " + json + " from " + packet.getAddress().toString()
                        + " : " + packet.getPort());

                PushPacket pushPacket = GsonUtils.fromJson(json, PushPacket.class);
                //publish event
                publishPushPacket(new PushEvent(pushPacket));
                // do nothing send ack only
                PushAckPacket pushAckPacket = PushAckPacket.builder()
                        .key(pushPacket.key)
                        .lastRefTime(pushPacket.lastRefTime)
                        .responseTime(System.currentTimeMillis())
                        .build();
                String ack = GsonUtils.toJsonString(pushAckPacket);
                udpSocket.send(new DatagramPacket(ack.getBytes(Charset.forName("UTF-8")),
                        ack.getBytes(Charset.forName("UTF-8")).length, packet.getSocketAddress()));

            } catch (Exception e) {
                logger.error("[pushReceiver] error while receiving push data", e);
            }
        }
    }

    public PushEventDispatcher getPushEventDispatcher() {
        return pushEventDispatcher;
    }

    public void setPushEventDispatcher(PushEventDispatcher pushEventDispatcher) {
        this.pushEventDispatcher = pushEventDispatcher;
    }

    public void publishPushPacket(PushEvent pushEvent) {
        getPushEventDispatcher().publishEvent(pushEvent);
    }

    public int getUDPPort() {
        return udpSocket.getLocalPort();
    }

    public static void main(String[] args) throws InterruptedException {
        PushReceiver udp = new PushReceiver();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        countDownLatch.await();
    }
}
