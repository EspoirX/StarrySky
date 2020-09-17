package com.lzx.starrysky.playback

import android.os.Bundle
import com.lzx.starrysky.SongInfo
import com.lzx.starrysky.control.RepeatMode
import com.lzx.starrysky.intercept.InterceptorCallback
import com.lzx.starrysky.intercept.InterceptorService
import com.lzx.starrysky.notification.INotification
import com.lzx.starrysky.utils.MainLooper
import com.lzx.starrysky.utils.StarrySkyUtils

class PlaybackManager(
    val mediaQueue: MediaQueueManager,
    val playback: Playback,
    private val interceptorService: InterceptorService) : Playback.Callback {

    private var serviceCallback: PlaybackServiceCallback? = null
    private var notification: INotification? = null

    init {
        playback.setCallback(this)
    }

    fun setServiceCallback(serviceCallback: PlaybackServiceCallback) {
        this.serviceCallback = serviceCallback
    }

    fun registerNotification(notification: INotification?) {
        this.notification = notification
    }

    fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
        mediaId?.apply {
            if (extras != null && extras.getInt("clearSongId", 0) == 1) {
                playback.currentMediaId = ""
            }
            mediaQueue.updateIndexBySongId(this)
            handlePlayRequest(isPlayWhenReady = true, isActiveTrigger = true)
        }
    }

    private fun handlePlayRequest(isPlayWhenReady: Boolean, isActiveTrigger: Boolean) {
        val playSongInfo = mediaQueue.getCurrentSongInfo(isActiveTrigger) //要播放的歌曲信息
        interceptorService.doInterceptions(playSongInfo, object : InterceptorCallback {
            override fun onContinue(songInfo: SongInfo?) {
                MainLooper.instance.runOnUiThread(Runnable {
                    mediaQueue.updateMusicArt(playSongInfo)  //更新媒体封面信息
                    handPlayRequestImpl(songInfo, isPlayWhenReady)
                })
            }

            override fun onInterrupt(exception: Throwable?) {
                MainLooper.instance.runOnUiThread(Runnable {
                    updatePlaybackState(playSongInfo, exception?.message, Playback.STATE_ERROR)
                    playback.currentMediaId = ""
                })
            }
        })
    }

    private fun handPlayRequestImpl(songInfo: SongInfo?, playWhenReady: Boolean) {
        songInfo?.let {
            playback.play(it, playWhenReady)
        }
    }

    fun onPause() {
        if (playback.isPlaying) {
            playback.pause()
        }
    }

    fun onPlay() {
        playback.currPlayInfo?.let {
            handlePlayRequest(isPlayWhenReady = true, isActiveTrigger = true)
        }
    }

    fun onStop() {
        handleStopRequest(null)
    }

    private fun handleStopRequest(withError: String?) {
        playback.stop()
        val stage = if (withError.isNullOrEmpty()) Playback.STATE_STOPPED else Playback.STATE_ERROR
        updatePlaybackState(playback.currPlayInfo, withError, stage)
    }

    fun onPrepare() {
        handlePlayRequest(isPlayWhenReady = false, isActiveTrigger = true)
    }

    fun onPrepareFromSongId(songId: String?) {
        songId?.apply {
            mediaQueue.updateIndexBySongId(this)
            handlePlayRequest(isPlayWhenReady = false, isActiveTrigger = true)
        }
    }

    fun onSkipToNext() {
        if (mediaQueue.skipQueuePosition(1)) {
            handlePlayRequest(isPlayWhenReady = true, isActiveTrigger = true)
        }
    }

    fun onSkipToPrevious() {
        if (mediaQueue.skipQueuePosition(-1)) {
            handlePlayRequest(isPlayWhenReady = true, isActiveTrigger = true)
        }
    }

    fun onFastForward() {
        playback.onFastForward()
    }

    fun onRewind() {
        playback.onRewind()
    }

    fun onDerailleur(refer: Boolean, multiple: Float) {
        playback.onDerailleur(refer, multiple)
    }

    fun seekTo(pos: Long) {
        playback.seekTo(pos)
        if (playback.playbackState == Playback.STATE_PAUSED) {
            onPlay()
        }
    }

    fun setRepeatMode(repeatMode: Int, loop: Boolean) {
        if (StarrySkyUtils.repeatMode.repeatMode == RepeatMode.REPEAT_MODE_SHUFFLE) {
            mediaQueue.provider.updateShuffleSongList()
        } else {
            mediaQueue.updateIndexBySongId(playback.currentMediaId)
        }
    }

    fun isSkipToNextEnabled(): Boolean {
        val repeatMode = StarrySkyUtils.repeatMode
        if (repeatMode.repeatMode == RepeatMode.REPEAT_MODE_NONE ||
            repeatMode.repeatMode == RepeatMode.REPEAT_MODE_ONE ||
            repeatMode.repeatMode == RepeatMode.REPEAT_MODE_REVERSE) {
            return if (repeatMode.isLoop) true else !mediaQueue.currSongIsLastSong()
        }
        return true
    }

    fun isSkipToPreviousEnabled(): Boolean {
        val repeatMode = StarrySkyUtils.repeatMode
        if (repeatMode.repeatMode == RepeatMode.REPEAT_MODE_NONE ||
            repeatMode.repeatMode == RepeatMode.REPEAT_MODE_ONE ||
            repeatMode.repeatMode == RepeatMode.REPEAT_MODE_REVERSE) {
            return if (repeatMode.isLoop) true else !mediaQueue.currSongIsFirstSong()
        }
        return true
    }

    override fun onPlayerStateChanged(songInfo: SongInfo?, playWhenReady: Boolean, playbackState: Int) {
        updatePlaybackState(songInfo, null, playbackState)
        if (playbackState == Playback.STATE_IDLE) {
            onPlaybackCompletion()
        }
    }

    private fun onPlaybackCompletion() {
        val repeatMode = StarrySkyUtils.repeatMode
        when (repeatMode.repeatMode) {
            //顺序播放
            RepeatMode.REPEAT_MODE_NONE -> {
                if (repeatMode.isLoop) {
                    playback.currentMediaId = ""
                    onSkipToNext()
                } else if (!mediaQueue.currSongIsLastSong()) {
                    onSkipToNext()
                }
            }
            //单曲播放
            RepeatMode.REPEAT_MODE_ONE -> {
                playback.currentMediaId = ""
                if (repeatMode.isLoop) {
                    handlePlayRequest(isPlayWhenReady = true, isActiveTrigger = false)
                }
            }
            //随机播放
            RepeatMode.REPEAT_MODE_SHUFFLE -> {
                playback.currentMediaId = ""
                if (mediaQueue.skipQueuePosition(1)) {
                    handlePlayRequest(isPlayWhenReady = true, isActiveTrigger = false)
                }
            }
            //倒序播放
            RepeatMode.REPEAT_MODE_REVERSE -> {
                if (repeatMode.isLoop) {
                    playback.currentMediaId = ""
                    onSkipToPrevious()
                } else if (!mediaQueue.currSongIsFirstSong()) {
                    onSkipToPrevious()
                }
            }
        }
    }

    override fun onPlaybackError(songInfo: SongInfo?, error: String) {
        updatePlaybackState(songInfo, error, Playback.STATE_ERROR)
    }

    override fun onFocusStateChange(currentAudioFocusState: Int) {
        serviceCallback?.onFocusStateChange(currentAudioFocusState)
    }

    private fun updatePlaybackState(currPlayInfo: SongInfo?, errorMsg: String?, state: Int) {
        var newState = PlaybackStage.IDEA
        when (state) {
            Playback.STATE_IDLE -> {
                newState = PlaybackStage.IDEA
            }
            Playback.STATE_BUFFERING -> {
                newState = PlaybackStage.BUFFERING
            }
            Playback.STATE_PLAYING -> {
                newState = PlaybackStage.PLAYING
                startNotification(currPlayInfo, newState)
            }
            Playback.STATE_PAUSED -> {
                newState = PlaybackStage.PAUSE
                startNotification(currPlayInfo, newState)
            }
            Playback.STATE_STOPPED -> {
                newState = PlaybackStage.STOP
            }
            Playback.STATE_ERROR -> {
                newState = PlaybackStage.ERROR
            }
        }
        notification?.onPlaybackStateChanged(currPlayInfo, newState)
        StarrySkyUtils.log("PlaybackStage = $newState")
        val playbackStage = PlaybackStage()
        playbackStage.errorMsg = errorMsg
        playbackStage.songInfo = currPlayInfo
        playbackStage.stage = newState
        serviceCallback?.onPlaybackStateUpdated(playbackStage)
    }

    private fun startNotification(currPlayInfo: SongInfo?, state: String) {
        notification?.startNotification(currPlayInfo, state)
    }

    interface PlaybackServiceCallback {
        fun onPlaybackStateUpdated(playbackStage: PlaybackStage)
        fun onFocusStateChange(currentAudioFocusState: Int)
    }
}