package com.mfw.atlas.sdk.spring.cloud.utils;

import java.util.List;

public interface Poller<T> {

    /**
     * Get next element selected by poller.
     *
     * @return next element
     */
    T next();

    /**
     * Update items stored in poller.
     *
     * @param items new item list
     * @return new poller instance
     */
    Poller<T> refresh(List<T> items);
}