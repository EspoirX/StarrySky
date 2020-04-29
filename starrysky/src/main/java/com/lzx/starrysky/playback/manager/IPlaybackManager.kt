package com.lzx.starrysky.playback.manager

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat

import com.lzx.starrysky.notification.INotification
import com.lzx.starrysky.provider.IMediaSourceProvider
import com.lzx.starrysky.provider.SongInfo

interface IPlaybackManager {

    val mediaSessionCallback: MediaSessionCompat.Callback

    /**
     * 是否在播放
     */
    val isPlaying: Boolean

    fun setServiceCallback(serviceCallback: PlaybackServiceCallback)

    fun setMetadataUpdateListener(listener: IMediaSourceProvider.MetadataUpdateListener)

    /**
     * 播放
     * isPlayWhenReady 是否立即播放
     * isActiveTrigger 是否主动触发（除了播放完成后调用，其他都是主动触发）
     */
    fun handlePlayRequest(isPlayWhenReady: Boolean, isActiveTrigger: Boolean)

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
     * currPlayInfo 当前的播放信息
     * isOnlyUpdateActions 是否只更新 action
     * isError 是否发生错误
     * error 错误信息
     */
    fun updatePlaybackState(
        currPlayInfo: SongInfo?,
        isOnlyUpdateActions: Boolean,
        isError: Boolean,
        error: String?
    )

    fun registerNotification(notification: INotification?)

    interface PlaybackServiceCallback {
        fun onPlaybackStart()

        fun onPlaybackStop(isStop: Boolean)

        fun onPlaybackStateUpdated(
            newState: PlaybackStateCompat?, currMetadata:
            MediaMetadataCompat?
        )
    }
}
