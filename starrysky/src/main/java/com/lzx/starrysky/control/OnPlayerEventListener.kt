package com.lzx.starrysky.control

import com.lzx.starrysky.provider.SongInfo

/**
 * 播放监听
 */
interface OnPlayerEventListener {
    /**
     * 切歌回调
     */
    fun onMusicSwitch(songInfo: SongInfo)

    /**
     * 开始播放,与 onMusicSwitch 的关系是先回调 onMusicSwitch，再回调 onPlayerStart
     */
    fun onPlayerStart()

    /**
     * 暂停播放
     */
    fun onPlayerPause()

    /**
     * 停止播放
     */
    fun onPlayerStop()

    /**
     * 播放完成
     */
    fun onPlayCompletion(songInfo: SongInfo)

    /**
     * 正在缓冲
     */
    fun onBuffering()

    /**
     * 发生错误
     */
    fun onError(errorCode: Int, errorMsg: String)
}
