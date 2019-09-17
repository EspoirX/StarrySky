package com.lzx.starrysky.common;

import android.arch.lifecycle.MutableLiveData;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;

public interface IMediaConnection {

    /**
     * MediaBrowserCompat 订阅
     */
    void subscribe(String parentId, MediaBrowserCompat.SubscriptionCallback callback);

    /**
     * MediaBrowserCompat 取消订阅
     */
    void unsubscribe(String parentId, MediaBrowserCompat.SubscriptionCallback callback);

    /**
     * 给服务发消息
     */
    void sendCommand(String command, Bundle parameters);

    /**
     * 获取当前随机模式
     */
    int getShuffleMode();

    /**
     * 获取当前播放模式
     */
    int getRepeatMode();

    /**
     * 获取当前播放的 MediaMetadataCompat
     */
    MediaMetadataCompat getNowPlaying();

    /**
     * 获取 PlaybackStateCompat
     */
    PlaybackStateCompat getPlaybackStateCompat();

    /**
     * 获取 PlaybackStage
     */
    MutableLiveData<PlaybackStage> getPlaybackState();

    /**
     * 获取播放控制器
     */
    MediaControllerCompat.TransportControls getTransportControls();

    /**
     * 获取 MediaControllerCompat
     */
    MediaControllerCompat getMediaController();

    /**
     * 连接
     */
    void connect();

    /**
     * 断开连接
     */
    void disconnect();

    /**
     * 链接监听
     */
    interface OnConnectListener {
        void onConnected();
    }
}
