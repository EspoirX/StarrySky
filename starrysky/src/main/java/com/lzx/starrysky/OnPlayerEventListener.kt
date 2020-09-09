package com.lzx.starrysky

/**
 * 播放监听
 */
interface OnPlayerEventListener {
    /**
     * 开始播放
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