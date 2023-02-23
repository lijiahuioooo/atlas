package com.mfw.atlas.sdk.spring.cloud.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class GenericPoller<T> implements Poller<T> {

    private final AtomicInteger index = new AtomicInteger(0);

    private List<T> items = new ArrayList<T>();

    public GenericPoller(List<T> items) {
        this.items = items;
    }

    @Override
    public T next() {
        return items.get(Math.abs(index.getAndIncrement() % items.size()));
    }

    @Override
    public Poller<T> refresh(List<T> items) {
        return new GenericPoller<T>(items);
    }
}
