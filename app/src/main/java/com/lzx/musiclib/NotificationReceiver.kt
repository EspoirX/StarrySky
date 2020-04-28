package com.lzx.musiclib

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.lzx.starrysky.StarrySky
import com.lzx.starrysky.notification.CustomNotification
import com.lzx.starrysky.notification.INotification

var isFavorite = true

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        when (action) {
            INotification.ACTION_FAVORITE -> {
                val bundle = Bundle()
                bundle.putBoolean("isFavorite", isFavorite)
                isFavorite = !isFavorite
                StarrySky.with().sendCommand(CustomNotification.ACTION_UPDATE_FAVORITE, bundle)
            }
        }
    }
}