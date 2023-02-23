package com.mfw.atlas.client.udp;

/**
 * @author KL
 * @Time 2020/10/22 11:31 上午
 */
public interface PushEventListener {

    /**
     * callback event
     *
     * @param event
     */
    void onEvent(PushEvent event);

}
