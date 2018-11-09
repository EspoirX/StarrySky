package com.lzx.musiclib;

import android.app.Application;

import com.lzx.musiclibrary.manager.MusicLibrary;
import com.lzx.musiclibrary.notification.NotificationCreater;
import com.lzx.musiclibrary.utils.BaseUtil;

/**
 * create by lzx
 * time:2018/11/9
 */
public class TestApplication extends Application {
    private static MusicLibrary musicLibrary;

    @Override
    public void onCreate() {
        super.onCreate();
        if (BaseUtil.getCurProcessName(this).equals("com.lzx.pandora")) {
            NotificationCreater creater = new NotificationCreater.Builder()
                    .setTargetClass("com.lzx.music.MusicTestActivity")
                    .setCreateSystemNotification(true)
                    .build();
            musicLibrary = new MusicLibrary.Builder(this)
                    .setUseMediaPlayer(true)
                    .setNotificationCreater(creater)
                    .build();
            musicLibrary.startMusicService();
        }
    }

    public static MusicLibrary getMusicLibrary() {
        return musicLibrary;
    }
}
