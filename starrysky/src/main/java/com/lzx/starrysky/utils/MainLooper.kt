package com.lzx.starrysky.utils

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

    companion object {
        val instance = MainLooper(Looper.getMainLooper())
    }
}