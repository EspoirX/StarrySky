package com.lzx.starrysky.common

import java.util.ArrayDeque
import java.util.Queue

abstract class BaseKey<T> {

    private val keyPool = ArrayDeque<T>()

    fun get(): T? {
        var result: T? = keyPool.poll()
        if (result == null) {
            result = create()
        }
        return result
    }

    fun offer(key: T) {
        keyPool.offer(key)
    }

    abstract fun create(): T
}
