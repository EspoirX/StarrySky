package com.example.musiclib.service;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.format.DateUtils;

/**
 * 获取进度，每秒更新一次
 * Created by lzx on 2017/8/8.
 */
public class QuitTimer {
    private MusicPlayService mPlayService;
    private EventCallback<Long> mTimerCallback;
    private Handler mHandler;
    private long mTimerRemain;

    public static QuitTimer getInstance() {
        return SingletonHolder.sInstance;
    }

    private static class SingletonHolder {
        private static final QuitTimer sInstance = new QuitTimer();
    }

    private QuitTimer() {
    }

    public void init(@NonNull MusicPlayService playService, @NonNull Handler handler, @NonNull EventCallback<Long> timerCallback) {
        mPlayService = playService;
        mHandler = handler;
        mTimerCallback = timerCallback;
    }

    public void start(long milli) {
        stop();
        if (milli > 0) {
            mTimerRemain = milli + DateUtils.SECOND_IN_MILLIS;
            mHandler.post(mQuitRunnable);
        } else {
            mTimerRemain = 0;
            mTimerCallback.onEvent(mTimerRemain);
        }
    }

    public void stop() {
        mHandler.removeCallbacks(mQuitRunnable);
    }

    private Runnable mQuitRunnable = new Runnable() {
        @Override
        public void run() {
            mTimerRemain -= DateUtils.SECOND_IN_MILLIS;
            if (mTimerRemain > 0) {
                mTimerCallback.onEvent(mTimerRemain);
                mHandler.postDelayed(this, DateUtils.SECOND_IN_MILLIS);
            } else {
//                try {
//                    mPlayService.quit();
//                } catch (RemoteException e) {
//                    e.printStackTrace();
//                }
            }
        }
    };
}
