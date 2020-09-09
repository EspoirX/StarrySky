package com.lzx.starrysky

import android.os.Binder
import com.lzx.starrysky.control.PlayerControl
import com.lzx.starrysky.control.PlayerControlImpl

class ServiceBridge : Binder() {
    val register = StarrySkyRegister()
    val playerControl: PlayerControl = PlayerControlImpl()
}