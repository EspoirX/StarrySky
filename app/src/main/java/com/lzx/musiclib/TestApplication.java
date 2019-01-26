package com.lzx.musiclib;

import android.app.Application;
import android.os.Environment;

import com.lzx.starrysky.manager.MusicManager;
import com.lzx.starrysky.notification.NotificationConstructor;
import com.lzx.starrysky.playback.download.ExoDownload;


/**
 * create by lzx
 * time:2018/11/9
 */
public class TestApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        //初始化
        MusicManager.initMusicManager(this);
        //配置通知栏
        NotificationConstructor constructor = new NotificationConstructor.Builder()
                .setCreateSystemNotification(false)
                .bulid();
        MusicManager.getInstance().setNotificationConstructor(constructor);
        //设置缓存
        String destFileDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/11ExoCacheDir";
        ExoDownload.getInstance().setOpenCache(true); //打开缓存开关
        ExoDownload.getInstance().setCacheDestFileDir(destFileDir); //设置缓存文件夹
    }
}
