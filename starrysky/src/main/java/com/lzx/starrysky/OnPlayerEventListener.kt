package com.lzx.starrysky

import com.lzx.starrysky.manager.PlaybackStage

/**
 * 播放监听
 */
interface OnPlayerEventListener {
    fun onPlaybackStageChange(stage: PlaybackStage)
}

/**
 * 进度监听
 */
interface OnPlayProgressListener {
    fun onPlayProgress(currPos: Long, duration: Long)
}