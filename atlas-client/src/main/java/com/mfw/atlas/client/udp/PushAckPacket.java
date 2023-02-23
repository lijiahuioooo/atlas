package com.mfw.atlas.client.udp;

import java.io.Serializable;
import lombok.Builder;
import lombok.Data;

/**
 * @author KL
 * @Time 2020/10/22 11:30 上午
 */
@Data
@Builder
public class PushAckPacket implements Serializable {
    public String key;
    public long lastRefTime;
    public long responseTime;
    public Object data;
}
