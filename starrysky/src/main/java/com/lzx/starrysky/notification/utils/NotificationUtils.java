package com.lzx.starrysky.notification.utils;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;

import com.lzx.starrysky.MusicService;
import com.lzx.starrysky.R;
import com.lzx.starrysky.model.MusicProvider;
import com.lzx.starrysky.model.SongInfo;
import com.lzx.starrysky.notification.NotificationConstructor;
import com.lzx.starrysky.notification.factory.INotification;

import java.util.List;

import androidx.annotation.RequiresApi;

/**
 * 通知栏工具类，主要提供一些公共的方法
 */
public class NotificationUtils {
    /**
     * 得到目标界面 Class
     */
    public static Class getTargetClass(String targetClass) {
        Class clazz = null;
        try {
            if (!TextUtils.isEmpty(targetClass)) {
                clazz = Class.forName(targetClass);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return clazz;
    }

    /**
     * 设置content点击事件
     */
    public static PendingIntent createContentIntent(MusicService mService, NotificationConstructor mBuilder,
                                                    String songId, Bundle bundle, Class targetClass) {
        SongInfo songInfo = null;
        List<SongInfo> songInfos = MusicProvider.getInstance().getSongInfos();
        for (SongInfo info : songInfos) {
            if (info.getSongId().equals(songId)) {
                songInfo = info;
                break;
            }
        }
        Intent openUI = new Intent(mService, targetClass);
        openUI.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        openUI.putExtra("notification_entry", INotification.ACTION_INTENT_CLICK);
        if (songInfo != null) {
            openUI.putExtra("songInfo", songInfo);
        }
        if (bundle != null) {
            openUI.putExtra("bundleInfo", bundle);
        }
        @SuppressLint("WrongConstant")
        PendingIntent pendingIntent;
        switch (mBuilder.getPendingIntentMode()) {
            case NotificationConstructor.MODE_ACTIVITY:
                pendingIntent = PendingIntent.getActivity(mService, INotification.REQUEST_CODE, openUI, PendingIntent.FLAG_CANCEL_CURRENT);
                break;
            case NotificationConstructor.MODE_BROADCAST:
                pendingIntent = PendingIntent.getBroadcast(mService, INotification.REQUEST_CODE, openUI, PendingIntent.FLAG_CANCEL_CURRENT);
                break;
            case NotificationConstructor.MODE_SERVICE:
                pendingIntent = PendingIntent.getService(mService, INotification.REQUEST_CODE, openUI, PendingIntent.FLAG_CANCEL_CURRENT);
                break;
            default:
                pendingIntent = PendingIntent.getActivity(mService, INotification.REQUEST_CODE, openUI, PendingIntent.FLAG_CANCEL_CURRENT);
                break;
        }
        return pendingIntent;
    }

    /**
     * 兼容8.0
     */
    @RequiresApi(Build.VERSION_CODES.O)
    public static void createNotificationChannel(MusicService mService, NotificationManager mNotificationManager) {
        if (mNotificationManager.getNotificationChannel(INotification.CHANNEL_ID) == null) {
            NotificationChannel notificationChannel =
                    new NotificationChannel(INotification.CHANNEL_ID, mService.getString(R.string.notification_channel),
                            NotificationManager.IMPORTANCE_LOW);

            notificationChannel.setDescription(mService.getString(R.string.notification_channel_description));

            mNotificationManager.createNotificationChannel(notificationChannel);
        }
    }
}
