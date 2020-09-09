package com.lzx.starrysky.playback

import com.lzx.starrysky.SongInfo

class PlaybackStage {
    companion object {
        const val IDEA = "IDEA"
        const val PLAYING = "PLAYING"
        const val PAUSE = "PAUSE"
        const val STOP = "STOP"
        const val BUFFERING = "BUFFERING"
        const val ERROR = "ERROR"
    }

    var songInfo: SongInfo? = null
    var errorMsg: String? = null
    var stage: String = IDEA
}

