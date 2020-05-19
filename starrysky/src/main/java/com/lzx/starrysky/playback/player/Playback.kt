/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lzx.starrysky.playback.player


import android.support.v4.media.session.PlaybackStateCompat
import com.lzx.starrysky.provider.SongInfo

/**
 * 播放器接口，如果要实现其他播放器，实现该接口即可
 */
interface Playback {

    companion object {
        const val STATE_NONE = PlaybackStateCompat.STATE_NONE      //什么都没开始
        const val STATE_IDLE = PlaybackStateCompat.STATE_NONE      //空闲
        const val STATE_BUFFERING = PlaybackStateCompat.STATE_BUFFERING //正在缓冲
        const val STATE_PLAYING = PlaybackStateCompat.STATE_PLAYING   //正在播放
        const val STATE_PAUSED = PlaybackStateCompat.STATE_PAUSED    //暂停
        const val STATE_STOPPED = PlaybackStateCompat.STATE_STOPPED   //停止
        const val STATE_ERROR = PlaybackStateCompat.STATE_ERROR     //出错
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
         * 播放完成回调
         */
        fun onPlaybackCompletion()

        /**
         * 播放状态改变回调
         */
        fun onPlaybackStatusChanged(songInfo: SongInfo?, state: Int)

        /**
         * 播放出错回调
         */
        fun onPlaybackError(songInfo: SongInfo?, error: String)
    }

    /**
     * 设置回调
     */
    fun setCallback(callback: Callback)
}
