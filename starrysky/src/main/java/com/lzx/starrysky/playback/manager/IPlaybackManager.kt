package com.lzx.starrysky.playback.manager

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat

import com.lzx.starrysky.notification.INotification
import com.lzx.starrysky.provider.MediaQueueProvider

interface IPlaybackManager {

    val mediaSessionCallback: MediaSessionCompat.Callback

    /**
     * 是否在播放
     */
    val isPlaying: Boolean

    fun setServiceCallback(serviceCallback: PlaybackServiceCallback)

    fun setMetadataUpdateListener(listener: MediaQueueProvider.MetadataUpdateListener)

    /**
     * 播放
     */
    fun handlePlayRequest(isPlayWhenReady: Boolean)

    /**
     * 暂停
     */
    fun handlePauseRequest()

    /**
     * 停止
     */
    fun handleStopRequest(withError: String?)

    /**
     * 快进
     */
    fun handleFastForward()

    /**
     * 倒带
     */
    fun handleRewind()

    /**
     * 指定语速 refer 是否已当前速度为基数  multiple 倍率
     */
    fun handleDerailleur(refer: Boolean, multiple: Float)

    /**
     * 更新播放状态
     */
    fun updatePlaybackState(isOnlyUpdateActions: Boolean, error: String?)

    fun registerNotification(notification: INotification)

    interface PlaybackServiceCallback {
        fun onPlaybackStart()

        fun onNotificationRequired()

        fun onPlaybackStop()

        fun onPlaybackStateUpdated(
            newState: PlaybackStateCompat, currMetadata:
            MediaMetadataCompat?
        )

        fun onShuffleModeUpdated(shuffleMode: Int)

        fun onRepeatModeUpdated(repeatMode: Int)
    }
}
