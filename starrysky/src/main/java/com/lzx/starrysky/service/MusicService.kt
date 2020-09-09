package com.lzx.starrysky.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import java.lang.ref.WeakReference

class MusicService : Service() {

    private var bridge: ServiceBridge? = null

    override fun onBind(intent: Intent?): IBinder? {
        bridge = ServiceBridge(WeakReference(this))
        bridge?.start()
        return bridge
    }
}