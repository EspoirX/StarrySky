package com.lzx.musiclibrary.manager;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;

import com.lzx.musiclibrary.MusicService;
import com.lzx.musiclibrary.aidl.source.IPlayControl;
import com.lzx.musiclibrary.cache.CacheConfig;
import com.lzx.musiclibrary.notification.NotificationCreater;

/**
 * Created by xian on 2018/5/15.
 */

public class MusicLibrary {

    public static String ACTION_MUSICLIBRARY_INIT_FINISH = "ACTION_MUSICLIBRARY_INIT_FINISH";//服务初始化成功action
    public static boolean isInitLibrary = false;
    private Context mContext;
    private boolean isUseMediaPlayer;
    private boolean isAutoPlayNext;
    private boolean isGiveUpAudioFocusManager;

    private NotificationCreater mNotificationCreater;
    private CacheConfig mCacheConfig;
    private Builder mBuilder;

    private MusicLibrary(Builder builder) {
        mBuilder = builder;
        mContext = builder.context;
        isUseMediaPlayer = builder.isUseMediaPlayer;
        isAutoPlayNext = builder.isAutoPlayNext;
        isGiveUpAudioFocusManager = builder.isGiveUpAudioFocusManager;
        mNotificationCreater = builder.mNotificationCreater;
        mCacheConfig = builder.mCacheConfig;
    }

    public static class Builder {
        private Context context;
        private boolean isUseMediaPlayer = false;
        private boolean isAutoPlayNext = true;
        private boolean isGiveUpAudioFocusManager = false;
        private NotificationCreater mNotificationCreater;
        private CacheConfig mCacheConfig;

        public Builder(Context context) {
            this.context = context.getApplicationContext();
        }

        public Builder setUseMediaPlayer(boolean isUseMediaPlayer) {
            this.isUseMediaPlayer = isUseMediaPlayer;
            return this;
        }

        public Builder setAutoPlayNext(boolean autoPlayNext) {
            isAutoPlayNext = autoPlayNext;
            return this;
        }

        public Builder setNotificationCreater(NotificationCreater creater) {
            mNotificationCreater = creater;
            return this;
        }

        public Builder giveUpAudioFocusManager() {
            isGiveUpAudioFocusManager = true;
            return this;
        }

        public Builder setCacheConfig(CacheConfig cacheConfig) {
            if (cacheConfig != null) {
                mCacheConfig = cacheConfig;
            }
            return this;
        }

        boolean isUseMediaPlayer() {
            return isUseMediaPlayer;
        }

        boolean isAutoPlayNext() {
            return isAutoPlayNext;
        }

        boolean isGiveUpAudioFocusManager() {
            return isGiveUpAudioFocusManager;
        }

        NotificationCreater getNotificationCreater() {
            return mNotificationCreater;
        }


        CacheConfig getCacheConfig() {
            return mCacheConfig;
        }

        public MusicLibrary build() {
            return new MusicLibrary(this);
        }
    }

    public void init() {
        init(true);
    }

    public void bindService() {
        init(false);
    }

    private void init(boolean isStartService) {
        if (isInitLibrary) {
            return;
        }
        Intent intent = new Intent(mContext, MusicService.class);
        intent.putExtra("isUseMediaPlayer", isUseMediaPlayer);
        intent.putExtra("isAutoPlayNext", isAutoPlayNext);
        intent.putExtra("isGiveUpAudioFocusManager", isGiveUpAudioFocusManager);
        intent.putExtra("notificationCreater", mNotificationCreater);
        intent.putExtra("cacheConfig", mCacheConfig);
        if (isStartService) {
            ContextCompat.startForegroundService(mContext, intent);
        }
        mContext.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            IPlayControl control = IPlayControl.Stub.asInterface(iBinder);
            MusicManager.get().attachPlayControl(mContext, control);
            MusicManager.get().attachServiceConnection(this);
            MusicManager.get().attachMusicLibraryBuilder(mBuilder);
            isInitLibrary = true;
            //发送一个广播，可通过接受它来知道服务初始化成功
            mContext.sendBroadcast(new Intent(ACTION_MUSICLIBRARY_INIT_FINISH));
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isInitLibrary = false;
        }
    };
}
