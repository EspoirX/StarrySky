package com.lzx.musiclib;

import android.app.Application;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Environment;

import com.lzx.musiclib.imageloader.GlideLoader;
import com.lzx.starrysky.manager.MusicManager;
import com.lzx.starrysky.notification.NotificationConstructor;
import com.lzx.starrysky.playback.download.ExoDownload;


/**
 * create by lzx
 * time:2018/11/9
 */
public class TestApplication extends Application {

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

    @Override
    public void onCreate() {
        super.onCreate();
        //初始化
        MusicManager.initMusicManager(this);
        //设置图片加载器
        MusicManager.setImageLoader(new GlideLoader());
        //配置通知栏
        NotificationConstructor constructor = new NotificationConstructor.Builder()
                .setCreateSystemNotification(true)
                .bulid();
        MusicManager.getInstance().setNotificationConstructor(constructor);
        //设置缓存
        String destFileDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/11ExoCacheDir";
        ExoDownload.getInstance().setOpenCache(true); //打开缓存开关
        ExoDownload.getInstance().setShowNotificationWhenDownload(true);
        ExoDownload.getInstance().setCacheDestFileDir(destFileDir); //设置缓存文件夹
    }
}
