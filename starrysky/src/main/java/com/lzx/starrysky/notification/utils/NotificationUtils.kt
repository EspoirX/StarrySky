package com.lzx.starrysky.notification.utils

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.text.TextUtils
import com.lzx.starrysky.MusicService
import com.lzx.starrysky.R
import com.lzx.starrysky.StarrySky
import com.lzx.starrysky.notification.INotification
import com.lzx.starrysky.notification.NotificationConfig
import com.lzx.starrysky.provider.SongInfo

/**
 * 通知栏工具类，主要提供一些公共的方法
 */
object NotificationUtils {
    /**
     * 得到目标界面 Class
     */
    fun getTargetClass(targetClass: String): Class<*>? {
        var clazz: Class<*>? = null
        try {
            if (!TextUtils.isEmpty(targetClass)) {
                clazz = Class.forName(targetClass)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return clazz
    }

    /**
     * 设置content点击事件
     */
    fun createContentIntent(
        mService: MusicService, mBuilder: NotificationConfig?,
        songId: String?, bundle: Bundle?, targetClass: Class<*>
    ): PendingIntent {
        var songInfo: SongInfo? = null
        val songInfos = StarrySky.get().mediaQueueProvider.getSongList()
        for (info in songInfos) {
            if (info.songId == songId) {
                songInfo = info
                break
            }
        }
        val openUI = Intent(mService, targetClass)
        openUI.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        openUI.putExtra("notification_entry", INotification.ACTION_INTENT_CLICK)
        if (songInfo != null) {
            openUI.putExtra("songInfo", songInfo)
        }
        if (bundle != null) {
            openUI.putExtra("bundleInfo", bundle)
        }
        @SuppressLint("WrongConstant")
        val pendingIntent: PendingIntent
        pendingIntent = when (mBuilder?.pendingIntentMode) {
            NotificationConfig.MODE_ACTIVITY -> PendingIntent.getActivity(mService, INotification.REQUEST_CODE, openUI,
                PendingIntent.FLAG_CANCEL_CURRENT)
            NotificationConfig.MODE_BROADCAST -> PendingIntent.getBroadcast(mService, INotification.REQUEST_CODE, openUI,
                PendingIntent.FLAG_CANCEL_CURRENT)
            NotificationConfig.MODE_SERVICE -> PendingIntent.getService(mService, INotification.REQUEST_CODE, openUI,
                PendingIntent.FLAG_CANCEL_CURRENT)
            else -> PendingIntent.getActivity(mService, INotification.REQUEST_CODE, openUI,
                PendingIntent.FLAG_CANCEL_CURRENT)
        }
        return pendingIntent
    }

    /**
     * 兼容8.0
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotificationChannel(
        mService: MusicService, mNotificationManager: NotificationManager
    ) {
        if (mNotificationManager.getNotificationChannel(INotification.CHANNEL_ID) == null) {
            val notificationChannel = NotificationChannel(INotification.CHANNEL_ID,
                mService.getString(R.string.notification_channel),
                NotificationManager.IMPORTANCE_LOW)

            notificationChannel.description =
                mService.getString(R.string.notification_channel_description)

            mNotificationManager.createNotificationChannel(notificationChannel)
        }
    }
}
