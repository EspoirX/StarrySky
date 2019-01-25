package com.lzx.musiclib;

import android.app.Application;

import com.lzx.starrysky.manager.MusicManager;
import com.lzx.starrysky.notification.NotificationConstructor;


/**
 * create by lzx
 * time:2018/11/9
 */
public class TestApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        MusicManager.initMusicManager(this)
        ;
        NotificationConstructor constructor = new NotificationConstructor.Builder()
                .setCreateSystemNotification(false)
                .bulid();
        MusicManager.getInstance().setNotificationConstructor(constructor);
    }


}
