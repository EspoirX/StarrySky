package com.lzx.starrysky.utils

fun <T> Int.isIndexPlayable(queue: List<T>?): Boolean {
    return queue != null && this >= 0 && this < queue.size
}