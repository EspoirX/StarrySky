package com.lzx.starrysky.manager

import com.lzx.starrysky.SongInfo
import com.lzx.starrysky.playback.Playback

class PlaybackStage {
    companion object {
        const val IDEA = "IDEA"            //初始状态，播完完成，停止播放都会回调该状态
        const val PLAYING = "PLAYING"      //开始播放，播放中
        const val SWITCH = "SWITCH"        //切歌
        const val PAUSE = "PAUSE"          //暂停
        const val BUFFERING = "BUFFERING"  //缓冲
        const val ERROR = "ERROR"          //出错
    }

    var lastSongInfo: SongInfo? = null  //上一个音频信息（切歌回调时有用）
    var songInfo: SongInfo? = null      //当前音频信息
    var isStop: Boolean = false    //由于播完完成和停止播放都会回调IDEA，如果要做区分的话可以用这个字段，停止就是true，播放完成就是false
    var errorMsg: String? = null
    var stage: String = IDEA
}

fun Int.changePlaybackState(): String {
    return when (this) {
        Playback.STATE_IDLE -> PlaybackStage.IDEA
        Playback.STATE_BUFFERING -> PlaybackStage.BUFFERING
        Playback.STATE_PLAYING -> PlaybackStage.PLAYING
        Playback.STATE_PAUSED -> PlaybackStage.PAUSE
        Playback.STATE_ERROR -> PlaybackStage.ERROR
        else -> PlaybackStage.IDEA
    }
}
