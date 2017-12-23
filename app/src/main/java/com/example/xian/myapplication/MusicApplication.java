package com.example.xian.myapplication;

import android.app.Application;

import com.lzx.musiclib.manager.MusicManager;

/**
 * Created by xian on 2017/12/23.
 */

public class MusicApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        MusicManager.get().init(this);
    }
}
