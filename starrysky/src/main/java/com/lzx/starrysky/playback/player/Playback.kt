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

import com.lzx.starrysky.provider.SongInfo

/**
 * 播放器接口，如果要实现其他播放器，实现该接口即可
 */
interface Playback {

    companion object {
        const val PLAYBACK_STATE_NONE = 100      //什么都没开始
        const val PLAYBACK_STATE_IDLE = 200      //空闲
        const val PLAYBACK_STATE_BUFFERING = 300 //正在缓冲
        const val PLAYBACK_STATE_PLAYING = 400   //正在播放
        const val PLAYBACK_STATE_PAUSED = 500    //暂停
        const val PLAYBACK_STATE_STOPPED = 600   //停止
        const val PLAYBACK_STATE_ERROR = 700     //出错
    }

    val playbackState: Int

    val isConnected: Boolean

    val isPlaying: Boolean

    val currentStreamPosition: Long

    val bufferedPosition: Long

    val duration: Long

    var currentMediaId: String

    var volume: Float

    fun getAudioSessionId(): Int

    fun stop()

    fun play(songInfo: SongInfo, isPlayWhenReady: Boolean)

    fun pause()

    fun seekTo(position: Long)

    fun onFastForward()

    fun onRewind()

    /**
     * 指定语速 refer 是否已当前速度为基数  multiple 倍率
     */
    fun onDerailleur(refer: Boolean, multiple: Float)

    interface Callback {
        fun onCompletion()

        fun onPlaybackStatusChanged(state: Int)

        fun onError(error: String)
    }

    fun setCallback(callback: Callback)
}
