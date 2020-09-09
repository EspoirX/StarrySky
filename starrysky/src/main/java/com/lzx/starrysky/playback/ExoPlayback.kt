package com.lzx.starrysky.playback

import com.lzx.starrysky.SongInfo

class ExoPlayback : Playback {
    override val playbackState: Int
        get() = TODO("Not yet implemented")
    override val isConnected: Boolean
        get() = TODO("Not yet implemented")
    override val isPlaying: Boolean
        get() = TODO("Not yet implemented")
    override val currentStreamPosition: Long
        get() = TODO("Not yet implemented")
    override val bufferedPosition: Long
        get() = TODO("Not yet implemented")
    override val duration: Long
        get() = TODO("Not yet implemented")
    override var currentMediaId: String
        get() = TODO("Not yet implemented")
        set(value) {}
    override var volume: Float
        get() = TODO("Not yet implemented")
        set(value) {}
    override val currPlayInfo: SongInfo?
        get() = TODO("Not yet implemented")
    override val audioSessionId: Int
        get() = TODO("Not yet implemented")

    override fun stop() {
        TODO("Not yet implemented")
    }

    override fun play(songInfo: SongInfo, isPlayWhenReady: Boolean) {
        TODO("Not yet implemented")
    }

    override fun pause() {
        TODO("Not yet implemented")
    }

    override fun seekTo(position: Long) {
        TODO("Not yet implemented")
    }

    override fun onFastForward() {
        TODO("Not yet implemented")
    }

    override fun onRewind() {
        TODO("Not yet implemented")
    }

    override fun onDerailleur(refer: Boolean, multiple: Float) {
        TODO("Not yet implemented")
    }

    override fun getPlaybackSpeed(): Float {
        TODO("Not yet implemented")
    }

    override fun setCallback(callback: Playback.Callback) {
        TODO("Not yet implemented")
    }

}