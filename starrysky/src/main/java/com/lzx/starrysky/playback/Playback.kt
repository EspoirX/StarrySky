package com.lzx.starrysky.playback

import com.lzx.starrysky.SongInfo

/**
 * 播放器接口，如果要实现其他播放器，实现该接口即可
 */
interface Playback {

    companion object {
        const val STATE_IDLE = 1       //空闲(包括一开始和播放完都是空闲)
        const val STATE_BUFFERING = 2  //正在缓冲
        const val STATE_PLAYING = 3    //正在播放
        const val STATE_PAUSED = 4     //暂停
        const val STATE_STOPPED = 5    //停止
        const val STATE_ERROR = 6      //出错
    }

    /**
     * 获取当前播放状态（上面那几种）
     */
    val playbackState: Int

    /**
     * 是否已经链接播放器
     */
    val isConnected: Boolean

    /**
     * 是否在播放
     */
    val isPlaying: Boolean

    /**
     * 当前播放进度
     */
    val currentStreamPosition: Long

    /**
     * 当前缓冲进度
     */
    val bufferedPosition: Long

    /**
     * 时长
     */
    val duration: Long

    /**
     * 当前播放的songId
     */
    var currentMediaId: String

    /**
     * 当前音量
     */
    var volume: Float

    /**
     * 当前播放的 songInfo
     */
    val currPlayInfo: SongInfo?

    /**
     * 获取 AudioSessionId
     */
    val audioSessionId: Int

    /**
     * 停止
     */
    fun stop()

    /**
     * 播放
     * songInfo 要播放的音频
     * isPlayWhenReady 准备好之后是否要马上播放
     */
    fun play(songInfo: SongInfo, isPlayWhenReady: Boolean)

    /**
     * 暂停
     */
    fun pause()

    /**
     * 转跳进度
     */
    fun seekTo(position: Long)

    /**
     * 快进
     */
    fun onFastForward()

    /**
     * 快退
     */
    fun onRewind()

    /**
     * 指定语速 refer 是否已当前速度为基数  multiple 倍率
     */
    fun onDerailleur(refer: Boolean, multiple: Float)

    /**
     * 获取速度
     */
    fun getPlaybackSpeed(): Float

    interface Callback {
        /**
         * 其他播放状态回调
         */
        fun onPlayerStateChanged(songInfo: SongInfo?, playWhenReady: Boolean, playbackState: Int)

        /**
         * 播放出错回调
         */
        fun onPlaybackError(songInfo: SongInfo?, error: String)

        /**
         * 焦点改变
         */
        fun onFocusStateChange(info: FocusInfo)
    }

    /**
     * 设置回调
     */
    fun setCallback(callback: Callback)
}