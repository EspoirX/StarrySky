package com.lzx.starrysky;

import android.content.ComponentName;
import android.content.Context;

import com.lzx.starrysky.common.IMediaConnection;
import com.lzx.starrysky.common.MediaSessionConnection;
import com.lzx.starrysky.playback.queue.MediaQueue;
import com.lzx.starrysky.playback.queue.MediaQueueManager;
import com.lzx.starrysky.provider.MediaQueueProvider;
import com.lzx.starrysky.provider.MediaQueueProviderImpl;
import com.lzx.starrysky.provider.MediaQueueProviderSurface;

public class StarrySkyBuilder {

    //客户端与Service链接管理类
    private IMediaConnection mConnection;
    //媒体信息存储管理类
    private MediaQueueProvider mMediaQueueProvider;
    //播放队列管理类
    private MediaQueue mMediaQueue;
    //通知栏开关
    boolean isOpenNotification;
    //缓存开关
    boolean isOpenCache;
    //缓存文件夹
    String cacheDestFileDir;

    /**
     * 设置链接管理器
     */
    public void setConnection(IMediaConnection connection) {
        mConnection = connection;
    }

    /**
     * 设置媒体信息存储
     */
    public void setMediaQueueProvider(MediaQueueProvider mediaQueueProvider) {
        mMediaQueueProvider = mediaQueueProvider;
    }

    /**
     * 设置播放队列管理
     */
    public void setMediaQueue(MediaQueue mediaQueue) {
        mMediaQueue = mediaQueue;
    }

    /**
     * 设置缓存开关
     */
    public void setOpenCache(boolean openCache) {
        isOpenCache = openCache;
    }

    /**
     * 设置缓存存放文件夹
     */
    public void setCacheDestFileDir(String cacheDestFileDir) {
        this.cacheDestFileDir = cacheDestFileDir;
    }

    /**
     * 设置通知栏开关
     */
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
            mMediaQueue = new MediaQueueManager(surface);
        }

        return new StarrySky(
                mConnection,
                surface,
                mMediaQueue);
    }
}
