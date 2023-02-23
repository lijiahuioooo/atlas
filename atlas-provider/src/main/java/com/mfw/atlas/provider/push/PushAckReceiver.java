package com.mfw.atlas.provider.push;

import com.mfw.atlas.client.udp.PushAckPacket;
import com.mfw.atlas.provider.util.GsonUtils;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * @author KL
 * @Time 2020/10/30 4:48 下午
 */
@Slf4j
public class PushAckReceiver implements Runnable {

    private static final long ACK_TIMEOUT_NANOS = TimeUnit.MILLISECONDS.toNanos(1000L);
    private volatile DatagramSocket udpSocket;
    private volatile ConcurrentHashMap<String, Long> udpSendTimeMap;
    private volatile ConcurrentMap<String, AckEntry> ackMap;


    public PushAckReceiver(DatagramSocket udpSocket, ConcurrentHashMap<String, Long> udpSendTimeMap,
            ConcurrentMap<String, AckEntry> ackMap) {
        this.udpSocket = udpSocket;
        this.udpSendTimeMap = udpSendTimeMap;
        this.ackMap = ackMap;
    }

    @Override
    public void run() {
        while (true) {
            byte[] buffer = new byte[1024 * 64];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            try {
                udpSocket.receive(packet);
                String json = new String(packet.getData(), 0, packet.getLength(), Charset.forName("UTF-8")).trim();
                PushAckPacket ackPacket = GsonUtils.fromJson(json, PushAckPacket.class);
                if (ackPacket == null || StringUtils.isEmpty(ackPacket.getKey())) {
                    throw new IllegalStateException("ackPacket illegal: " + ", ack json: " + json);
                }
                String ackKey = ackPacket.getKey();
                long pushCost = System.currentTimeMillis() - udpSendTimeMap.get(ackKey);
                log.info("[pushAckReceiver] received ack key: {} ,cost: {} ms, unacked: {}", ackKey, pushCost, ackMap.size());
                ackMap.remove(ackKey);
                udpSendTimeMap.remove(ackKey);
                if (System.nanoTime() - ackPacket.lastRefTime > ACK_TIMEOUT_NANOS) {
                    log.warn("[pushAckReceiver] ack takes too long from {} ack json: {}", ackKey, json);
                }
            } catch (Throwable e) {
                log.error("[pushAckReceiver] error while receiving ack data", e);
            }
        }
    }

    public static void start(PushAckReceiver pushAckReceiver){
        if (pushAckReceiver == null || pushAckReceiver.udpSocket == null
                || pushAckReceiver.udpSendTimeMap == null
                || pushAckReceiver.ackMap == null) {
            throw new RuntimeException("pushAckReceiver start error");
        }
        Thread inThread = new Thread(pushAckReceiver);
        inThread.setDaemon(true);
        inThread.setName("atlas.pushAckReceiver");
        inThread.start();
    }


    public static class AckEntry {

        public String key;
        public DatagramPacket origin;
        private AtomicInteger retryTimes = new AtomicInteger(0);
        public Map<String, Object> data;

        public AckEntry(String key, DatagramPacket packet) {
            this.key = key;
            this.origin = packet;
        }

        public void increaseRetryTime() {
            retryTimes.incrementAndGet();
        }

        public int getRetryTimes() {
            return retryTimes.get();
        }

    }
}
