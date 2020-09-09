package com.lzx.starrysky.playback

import android.os.Bundle
import com.lzx.starrysky.MediaQueueManager

class PlaybackManager(private val mediaQueue: MediaQueueManager, val playback: Playback) {

    fun onPlayFromMediaId(mediaId: String, extras: Bundle?) {
        TODO("Not yet implemented")
    }

    fun onPause() {
        TODO("Not yet implemented")
    }

    fun onPlay() {
        TODO("Not yet implemented")
    }

    fun onStop() {
        TODO("Not yet implemented")
    }

    fun onPrepare() {
        TODO("Not yet implemented")
    }

    fun onPrepareFromSongId(songId: String) {
        TODO("Not yet implemented")
    }

    fun onSkipToNext() {
        TODO("Not yet implemented")
    }

    fun onSkipToPrevious() {
        TODO("Not yet implemented")
    }

    fun onFastForward() {
        TODO("Not yet implemented")
    }

    fun onRewind() {
        TODO("Not yet implemented")
    }

    fun onDerailleur(bundle: Bundle) {
        TODO("Not yet implemented")
    }

    fun seekTo(pos: Long) {
        TODO("Not yet implemented")
    }

    fun setRepeatMode(repeatMode: Int, loop: Boolean) {
        TODO("Not yet implemented")
    }

    fun isSkipToNextEnabled(): Boolean {
        TODO("Not yet implemented")
    }

    fun isSkipToPreviousEnabled(): Boolean {
        TODO("Not yet implemented")
    }

    fun getPlaybackState(): PlaybackStage? {
        TODO("Not yet implemented")
    }

    fun getErrorMessage(): CharSequence {
        TODO("Not yet implemented")
    }

    fun getErrorCode(): Int {
        TODO("Not yet implemented")
    }

    interface PlaybackServiceCallback {
        fun onPlaybackStateUpdated(playbackStage: PlaybackStage)
    }
}