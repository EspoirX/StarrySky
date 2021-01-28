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

/**
 * 全局状态监听
 * 有时候需要在状态改变时统一做些什么操作，
 * 比如播放时停止推拉流，停止时恢复，避免写得到处都是，可以用这个监听
 */
interface GlobalPlaybackStageListener {
    fun onPlaybackStageChange(stage: PlaybackStage)
}