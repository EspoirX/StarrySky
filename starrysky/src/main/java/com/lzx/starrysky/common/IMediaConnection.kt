package com.lzx.starrysky.common

import android.arch.lifecycle.MutableLiveData
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat

interface IMediaConnection {
    /**
     * MediaBrowserCompat 订阅
     */
    fun subscribe(parentId: String, callback: MediaBrowserCompat.SubscriptionCallback)

    /**
     * MediaBrowserCompat 取消订阅
     */
    fun unsubscribe(parentId: String, callback: MediaBrowserCompat.SubscriptionCallback)

    /**
     * 给服务发消息
     */
    fun sendCommand(command: String, parameters: Bundle)

    /**
     * 获取当前随机模式
     */
    fun getShuffleMode(): Int

    /**
     * 获取当前播放模式
     */
    fun getRepeatMode(): Int

    /**
     * 获取当前播放的 MediaMetadataCompat
     */
    fun getNowPlaying(): MediaMetadataCompat?

    /**
     * 获取 PlaybackStateCompat
     */
    fun getPlaybackStateCompat(): PlaybackStateCompat?

    /**
     * 获取 PlaybackStage
     */
    fun getPlaybackState(): MutableLiveData<PlaybackStage>

    /**
     * 获取播放控制器
     */
    fun getTransportControls(): MediaControllerCompat.TransportControls?

    /**
     * 获取 MediaControllerCompat
     */
    fun getMediaController(): MediaControllerCompat?

    /**
     * 连接
     */
    fun connect()

    /**
     * 断开连接
     */
    fun disconnect()

    fun setOnConnectListener(listener: OnConnectListener?)

    /**
     * 链接监听
     */
    interface OnConnectListener {
        fun onConnected()
    }
}