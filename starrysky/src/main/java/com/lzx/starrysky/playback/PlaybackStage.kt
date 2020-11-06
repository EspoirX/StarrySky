package com.lzx.starrysky.playback

import com.lzx.basecode.Playback
import com.lzx.basecode.SongInfo

class PlaybackStage {
    companion object {
        const val IDEA = "IDEA"            //初始状态，播完完成
        const val PLAYING = "PLAYING"      //播放中
        const val PAUSE = "PAUSE"          //暂停
        const val STOP = "STOP"            //停止
        const val BUFFERING = "BUFFERING"  //缓冲
        const val ERROR = "ERROR"          //出错
    }

    var songInfo: SongInfo? = null
    var errorMsg: String? = null
    var stage: String = IDEA
}

fun Int.changePlaybackState(): String {
    return when (this) {
        Playback.STATE_IDLE -> PlaybackStage.IDEA
        Playback.STATE_BUFFERING -> PlaybackStage.BUFFERING
        Playback.STATE_PLAYING -> PlaybackStage.PLAYING
        Playback.STATE_PAUSED -> PlaybackStage.PAUSE
        Playback.STATE_STOPPED -> PlaybackStage.STOP
        Playback.STATE_ERROR -> PlaybackStage.ERROR
        else -> PlaybackStage.IDEA
    }
}
