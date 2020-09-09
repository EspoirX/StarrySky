package com.lzx.starrysky

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.widget.Toast

class MusicService : Service() {

    private val register: StarrySkyRegister = StarrySkyRegister()
    override fun onBind(intent: Intent?): IBinder? {
        return register
    }

    override fun onCreate() {
        super.onCreate()
        Handler().postDelayed({
            Toast.makeText(this, "register = " + (register.playback == null), Toast.LENGTH_SHORT).show()
        }, 5000)
    }
}