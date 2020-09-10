package com.lzx.starrysky

import com.lzx.starrysky.playback.PlaybackStage

/**
 * 播放监听
 */
interface OnPlayerEventListener {
    fun onPlaybackStageChange(stage: PlaybackStage)
}