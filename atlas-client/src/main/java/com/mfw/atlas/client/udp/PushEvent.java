package com.mfw.atlas.client.udp;

import java.io.Serializable;
import lombok.Data;

/**
 * @author KL
 * @Time 2020/10/22 11:30 上午
 */
@Data
public class PushEvent implements Serializable {

    private PushPacket pushPacket;

    public PushEvent(PushPacket pushPacket) {
        this.pushPacket = pushPacket;
    }

}
