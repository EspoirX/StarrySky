package com.lzx.musiclib;

import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.lzx.starrysky.StarrySky;
import com.lzx.starrysky.StarrySkyBuilder;
import com.lzx.starrysky.StarrySkyConfig;
import com.lzx.starrysky.notification.StarrySkyNotificationManager;
import com.lzx.starrysky.playback.offline.StarrySkyCacheManager;
import com.lzx.starrysky.registry.StarrySkyRegistry;


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

    private static class TestConfig extends StarrySkyConfig {
        @Override
        public void applyOptions(@NonNull Context context, @NonNull StarrySkyBuilder builder) {
            super.applyOptions(context, builder);
        }

        @Override
        public void applyMediaValid(@NonNull Context context, StarrySkyRegistry registry) {
            super.applyMediaValid(context, registry);
        }

        @Override
        public StarrySkyNotificationManager.NotificationFactory getNotificationFactory() {
            return super.getNotificationFactory();
        }

        @Override
        public StarrySkyCacheManager.CacheFactory getCacheFactory() {
            return super.getCacheFactory();
        }
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
