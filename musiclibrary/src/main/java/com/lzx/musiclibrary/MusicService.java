package com.lzx.musiclibrary;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;

import com.lzx.musiclibrary.cache.CacheConfig;
import com.lzx.musiclibrary.control.PlayControl;
import com.lzx.musiclibrary.notification.NotificationCreater;

import java.lang.ref.WeakReference;

/**
 * Created by xian on 2018/1/20.
 */

public class MusicService extends Service {

    private PlayControl mBinder;

    private static MusicService mService;

    @Override
    public void onCreate() {
        super.onCreate();
        mService = this;
        if (Build.VERSION.SDK_INT >= 26) {
            int NOTIFICATION_ID = (int) (System.currentTimeMillis() % 10000);
            startForeground(NOTIFICATION_ID, new Notification.Builder(this).build());
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        boolean isUseMediaPlayer = intent.getBooleanExtra("isUseMediaPlayer", false);
        boolean isAutoPlayNext = intent.getBooleanExtra("isAutoPlayNext", true);
        boolean isGiveUpAudioFocusManager = intent.getBooleanExtra("isGiveUpAudioFocusManager", false);
        NotificationCreater notificationCreater = intent.getParcelableExtra("notificationCreater");
        CacheConfig cacheConfig = intent.getParcelableExtra("cacheConfig");
        mBinder = new PlayControl
                .Builder(this)
                .setAutoPlayNext(isAutoPlayNext)
                .setUseMediaPlayer(isUseMediaPlayer)
                .setGiveUpAudioFocusManager(isGiveUpAudioFocusManager)
                .setNotificationCreater(notificationCreater)
                .setCacheConfig(cacheConfig)
                .build();
        return mBinder;
    }

    public PlayControl getBinder() {
        return mBinder;
    }

    public static MusicService getService() {
        return mService;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mBinder != null) {
            mBinder.stopMusic();
            mBinder.releaseMediaSession();
        }
    }

    private static class DelayedStopHandler extends Handler {
        private final WeakReference<MusicService> mWeakReference;

        private DelayedStopHandler(MusicService service) {
            mWeakReference = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            MusicService service = mWeakReference.get();
            if (service != null && service.mBinder.getPlayback() != null) {
                if (service.mBinder.getPlayback().isPlaying()) {
                    return;
                }
                service.stopSelf();
            }
        }
    }
}
