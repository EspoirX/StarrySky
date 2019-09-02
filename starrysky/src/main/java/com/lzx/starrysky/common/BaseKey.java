package com.lzx.starrysky.common;

import java.util.ArrayDeque;
import java.util.Queue;

abstract public class BaseKey<T> {

    private final Queue<T> keyPool = new ArrayDeque<>();

    public T get() {
        T result = keyPool.poll();
        if (result == null) {
            result = create();
        }
        return result;
    }

    public void offer(T key) {
        keyPool.offer(key);
    }

    public abstract T create();
}
