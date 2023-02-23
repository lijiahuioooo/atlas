package com.mfw.atlas.client.udp;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author KL
 * @Time 2020/10/22 11:30 上午
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PushPacket implements Serializable {

    /**
     * 0: 全部，1：增量更新。只对网关有用
     */
    public int type;
    public String key;
    public long lastRefTime;
    public String serviceName;
    public String instanceId;
    public PushPacketData data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PushPacketData implements Serializable {

        private String appCode;

        private String envType;

        private String envCode;
    }
}

