package com.lzx.musiclib;

import android.app.Application;
import android.app.PendingIntent;
import android.content.Intent;

import com.lzx.starrysky.StarrySky;


/**
 * create by lzx
 * time:2018/11/9
 */
public class TestApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        StarrySky.init(this);
    }


    public static String ACTION_PLAY_OR_PAUSE = "ACTION_PLAY_OR_PAUSE";
    public static String ACTION_NEXT = "ACTION_NEXT";
    public static String ACTION_PRE = "ACTION_PRE";
    public static String ACTION_FAVORITE = "ACTION_FAVORITE";
    public static String ACTION_LYRICS = "ACTION_LYRICS";

    private PendingIntent getPendingIntent(String action) {
        Intent intent = new Intent(action);
        intent.setClass(this, NotificationReceiver.class);
        return PendingIntent.getBroadcast(this, 0, intent, 0);
    }


}
