package com.mfw.atlas.sdk.spring.cloud.discovery;

import com.netflix.loadbalancer.PollingServerListUpdater;

public class MfwServerListUpdater extends PollingServerListUpdater {

    private static long LISTOFSERVERS_CACHE_UPDATE_DELAY = 1000; // msecs;
    private static int LISTOFSERVERS_CACHE_REPEAT_INTERVAL = 10 * 1000; // msecs;

    //每10s更新一次
    public MfwServerListUpdater() {
        super(LISTOFSERVERS_CACHE_UPDATE_DELAY, LISTOFSERVERS_CACHE_REPEAT_INTERVAL);
    }

}
