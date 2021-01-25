package com.lzx.musiclib

import android.content.Context
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner

object LifecycleUtils {
    /**
     * 添加观察
     */
    fun addObserver(context: Context?, observer: LifecycleObserver?) {
        if (context is LifecycleOwner) {
            val owner = context as LifecycleOwner?
            owner?.lifecycle?.addObserver(observer!!)
        }
    }

    /**
     * 移除订阅
     */
    fun removeObserver(context: Context?, observer: LifecycleObserver?) {
        if (context is LifecycleOwner) {
            val owner = context as LifecycleOwner?
            owner?.lifecycle?.removeObserver(observer!!)
        }
    }
}