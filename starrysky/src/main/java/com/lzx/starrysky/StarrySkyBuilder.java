package com.lzx.starrysky;

import android.content.ComponentName;
import android.content.Context;

import com.lzx.starrysky.common.MediaSessionConnection;
import com.lzx.starrysky.playback.queue.MediaQueue;
import com.lzx.starrysky.playback.queue.MediaQueueManager;
import com.lzx.starrysky.provider.MediaQueueProvider;
import com.lzx.starrysky.provider.MediaQueueProviderImpl;
import com.lzx.starrysky.provider.MediaQueueProviderSurface;

public class StarrySkyBuilder {

    private MediaSessionConnection mConnection;
    private MediaQueueProvider mMediaQueueProvider;
    private MediaQueue mMediaQueue;

    private boolean isShowNotificationWhenDownload;

    boolean isOpenNotification;
    boolean isOpenCache;
    String cacheDestFileDir;


    public void setConnection(MediaSessionConnection connection) {
        mConnection = connection;
    }

    public void setMediaQueueProvider(MediaQueueProvider mediaQueueProvider) {
        mMediaQueueProvider = mediaQueueProvider;
    }

    public void setMediaQueue(MediaQueue mediaQueue) {
        mMediaQueue = mediaQueue;
    }

    public void setOpenCache(boolean openCache) {
        isOpenCache = openCache;
    }

    public void setShowNotificationWhenDownload(boolean showNotificationWhenDownload) {
        isShowNotificationWhenDownload = showNotificationWhenDownload;
    }

    public void setCacheDestFileDir(String cacheDestFileDir) {
        this.cacheDestFileDir = cacheDestFileDir;
    }

    public void setOpenNotification(boolean openNotification) {
        isOpenNotification = openNotification;
    }

    StarrySky build(Context context) {
        if (mConnection == null) {
            ComponentName componentName = new ComponentName(context, MusicService.class);
            mConnection = new MediaSessionConnection(context, componentName);
        }
        if (mMediaQueueProvider == null) {
            mMediaQueueProvider = new MediaQueueProviderImpl();
        }
        MediaQueueProviderSurface surface = new MediaQueueProviderSurface(mMediaQueueProvider);

        if (mMediaQueue == null) {
            mMediaQueue = new MediaQueueManager(surface, context);
        }

        return new StarrySky(
                mConnection,
                surface,
                mMediaQueue);
    }
}
