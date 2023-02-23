package com.mfw.atlas.sdk.spring.cloud.utils;

public class Pair<T> {

    private final T item;

    private final double weight;

    public Pair(T item, double weight) {
        this.item = item;
        this.weight = weight;
    }

    public T item() {
        return item;
    }

    public double weight() {
        return weight;
    }
}
