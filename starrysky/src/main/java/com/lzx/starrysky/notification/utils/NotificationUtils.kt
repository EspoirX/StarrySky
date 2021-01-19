package com.lzx.starrysky.notification.utils

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.lzx.starrysky.R
import com.lzx.starrysky.SongInfo
import com.lzx.starrysky.notification.INotification
import com.lzx.starrysky.notification.INotification.Companion.CHANNEL_ID
import com.lzx.starrysky.notification.NotificationConfig

/**
 * 通知栏工具类，主要提供一些公共的方法
 */
object NotificationUtils {


    /**
     * 设置content点击事件
     */
    fun createContentIntent(
        context: Context, config: NotificationConfig?,
        songInfo: SongInfo?, bundle: Bundle?, targetClass: Class<*>
    ): PendingIntent {
        //构建 Intent
        val openUI = Intent(context, targetClass)
        openUI.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        openUI.putExtra("notification_entry", INotification.ACTION_INTENT_CLICK)
        songInfo?.let {
            openUI.putExtra("songInfo", it)
        }
        bundle?.let {
            openUI.putExtra("bundleInfo", it)
        }
        //构建 PendingIntent
        @SuppressLint("WrongConstant")
        val pendingIntent: PendingIntent
        val requestCode = INotification.REQUEST_CODE
        val flags = PendingIntent.FLAG_CANCEL_CURRENT
        pendingIntent = when (config?.pendingIntentMode) {
            NotificationConfig.MODE_ACTIVITY -> {
                PendingIntent.getActivity(context, requestCode, openUI, flags)
            }
            NotificationConfig.MODE_BROADCAST -> {
                PendingIntent.getBroadcast(context, requestCode, openUI, flags)
            }
            NotificationConfig.MODE_SERVICE -> {
                PendingIntent.getService(context, requestCode, openUI, flags)
            }
            else -> PendingIntent.getActivity(context, requestCode, openUI, flags)
        }
        return pendingIntent
    }

    /**
     * 兼容8.0
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotificationChannel(
        context: Context,
        manager: NotificationManager
    ) {
        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            val notificationChannel = NotificationChannel(CHANNEL_ID, context.getString(R.string.notification_channel), NotificationManager.IMPORTANCE_LOW)
            notificationChannel.description = context.getString(R.string.notification_channel_description)
            manager.createNotificationChannel(notificationChannel)
        }
    }


    fun createNoCrashNotification(context: Context): Notification {
        val notifyBuilder = if (Build.VERSION.SDK_INT >= 26) {
            val manager = context.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
            createNotificationChannel(context, manager)
            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            builder.setVibrate(longArrayOf(0L))
            builder.setSound(null as Uri?)
            builder.setDefaults(0)
            builder
        } else {
            NotificationCompat.Builder(context)
        }
        return notifyBuilder
            .setContentTitle("防止崩溃notification")
            .setSmallIcon(R.drawable.ic_notification).build()

    }
}

