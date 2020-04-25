package com.lzx.musiclib

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.widget.Toast

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (TextUtils.isEmpty(action)) {
            return
        }
        Toast.makeText(context, "这是自定义通知栏点击事件", Toast.LENGTH_SHORT).show()
    }
}