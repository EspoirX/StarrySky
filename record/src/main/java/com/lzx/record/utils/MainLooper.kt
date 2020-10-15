package com.lzx.record.utils

import android.os.Handler
import android.os.Looper

class MainLooper private constructor(looper: Looper) : Handler(looper) {

    fun runOnUiThread(runnable: Runnable) {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            runnable.run()
        } else {
            instance.post(runnable)
        }
    }

    fun isInMainThread() = Looper.myLooper() == Looper.getMainLooper()

    companion object {
        val instance = MainLooper(Looper.getMainLooper())
    }
}