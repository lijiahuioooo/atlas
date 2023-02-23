package com.mfw.atlas.provider.test.udp;

import com.alibaba.fastjson.JSON;
import com.mfw.atlas.client.udp.PushPacket;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
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
public class PushClientTest implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(PushClientTest.class);

    private ScheduledExecutorService executorService;

    private static final int UDP_MSS = 64 * 1024;

    private DatagramSocket udpSocket;

    public PushClientTest(int port) {
        try {
            udpSocket = new DatagramSocket(port);

            executorService = new ScheduledThreadPoolExecutor(1, new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    Thread thread = new Thread(r);
                    thread.setDaemon(true);
                    thread.setName("com.mfw.atlas.client.core.PushReceiver");
                    return thread;
                }
            });

            executorService.execute(this);
        } catch (Exception e) {
            logger.error("[PushReceiver] init udp socket failed", e);
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
                logger.info("received ack data: " + json + " from " + packet.getAddress().toString()
                        +" : " +packet.getPort());

            } catch (Exception e) {
                logger.error("[NA] error while receiving push data", e);

            }
        }
    }

    public void sendData() {
        try {
            PushPacket sendData= PushPacket.builder()
                    .type(0)
                    .serviceName("providers:com.example.dubboclient1.service.DemoService:1.0.0:testgroup")
                    .data(PushPacket.PushPacketData.builder()
                            //.serviceName("providers:com.example.dubboclient1.service.DemoService:1.0.0:testgroup")
                            .envType("dev")
                            .build())
                    .lastRefTime(System.currentTimeMillis())
                    .build();

            String sendDataString = JSON.toJSONString(sendData);
            udpSocket.send(new DatagramPacket(sendDataString.getBytes(Charset.forName("UTF-8")),
                    sendDataString.getBytes(Charset.forName("UTF-8")).length,
                    InetAddress.getByName("172.18.24.84"),13505));
            logger.info("send success");
        } catch (Exception e) {
            logger.error("[NA] error while send push data", e);
        }
    }

    public int getUDPPort() {
        return udpSocket.getLocalPort();
    }

    public static void main(String[] args) throws InterruptedException {
        PushClientTest udp = new PushClientTest(59368);
        new Thread(udp).start();
        udp.sendData();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        countDownLatch.await();
    }
}
