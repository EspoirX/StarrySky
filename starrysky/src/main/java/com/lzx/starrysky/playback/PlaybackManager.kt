package com.lzx.starrysky.playback

import android.content.Context
import android.os.Bundle
import com.lzx.basecode.FocusInfo
import com.lzx.basecode.SongInfo
import com.lzx.starrysky.control.RepeatMode
import com.lzx.starrysky.intercept.InterceptorCallback
import com.lzx.starrysky.intercept.InterceptorService
import com.lzx.basecode.isRefrain
import com.lzx.starrysky.notification.INotification
import com.lzx.starrysky.service.MusicService
import com.lzx.basecode.MainLooper
import com.lzx.basecode.Playback
import com.lzx.starrysky.utils.StarrySkyUtils

class PlaybackManager(
    private val context: Context,
    val mediaQueue: MediaQueueManager,
    val playback: Playback,
    private val interceptorService: InterceptorService) : Playback.Callback {

    private var serviceCallback: PlaybackServiceCallback? = null
    private var notification: INotification? = null
    private var refrainPlayback: Playback? = null

    init {
        playback.setCallback(this)
    }

    fun setRefrainPlayback(refrainPlayback: Playback?) {
        this.refrainPlayback = refrainPlayback
        this.refrainPlayback?.setCallback(this)
    }

    fun getRefrainPlayback() = refrainPlayback

    fun setServiceCallback(serviceCallback: PlaybackServiceCallback) {
        this.serviceCallback = serviceCallback
    }

    fun registerNotification(notification: INotification?) {
        this.notification = notification
    }

    fun onPlayRefrain(info: SongInfo?) {
        interceptorService.doInterceptions(info, object : InterceptorCallback {
            override fun onContinue(songInfo: SongInfo?) {
                if (songInfo == null) return
                MainLooper.instance.runOnUiThread(Runnable {
                    refrainPlayback?.currentMediaId = ""
                    refrainPlayback?.play(songInfo, true)
                })
            }

            override fun onInterrupt(exception: Throwable?) {
                MainLooper.instance.runOnUiThread(Runnable {
                    refrainPlayback?.currentMediaId = ""
                })
            }
        })
    }

    fun stopRefrain() {
        refrainPlayback?.stop()
    }

    fun onPlayFromMediaId(songId: String?, extras: Bundle?) {
        songId?.apply {
            if (extras != null && extras.getInt("clearSongId", 0) == 1) {
                playback.currentMediaId = ""
            }
            mediaQueue.updateIndexBySongId(songId)
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

    fun onSkipToNext(): Boolean {
        if (mediaQueue.skipQueuePosition(1)) {
            handlePlayRequest(isPlayWhenReady = true, isActiveTrigger = true)
        }
        return false
    }

    fun onSkipToPrevious() {
        if (mediaQueue.skipQueuePosition(-1)) {
            handlePlayRequest(isPlayWhenReady = true, isActiveTrigger = true)
        }
    }

    private fun deleteAndUpdateInfo(songId: String) {
        val repeatMode = StarrySkyUtils.repeatMode.repeatMode
        val isActiveTrigger = repeatMode != RepeatMode.REPEAT_MODE_SHUFFLE
        val skipSongInfo = mediaQueue.getCurrentSongInfo(isActiveTrigger)
        mediaQueue.provider.deleteSongInfoById(songId)
        mediaQueue.updateIndexByPlayingInfo(skipSongInfo)
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
        if (repeatMode == RepeatMode.REPEAT_MODE_SHUFFLE) {
            mediaQueue.provider.updateShuffleSongList()
        } else {
            mediaQueue.updateIndexByPlayingInfo(playback.currPlayInfo)
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

    fun removeSongInfo(songId: String) {
        val isSameInfo = songId == playback.currPlayInfo?.songId
        val isPlaying = playback.playbackState == Playback.STATE_PLAYING && isSameInfo
        val isPaused = playback.playbackState == Playback.STATE_PAUSED && isSameInfo
        if (isPlaying) {
            if (mediaQueue.skipQueuePosition(1)) {
                deleteAndUpdateInfo(songId)
                handlePlayRequest(isPlayWhenReady = true, isActiveTrigger = true)
            }
        } else if (isPaused) {
            onStop()
            if (mediaQueue.skipQueuePosition(1)) {
                deleteAndUpdateInfo(songId)
                handlePlayRequest(isPlayWhenReady = false, isActiveTrigger = true)
            }
        } else {
            deleteAndUpdateInfo(songId)
        }
    }

    override fun onPlayerStateChanged(songInfo: SongInfo?, playWhenReady: Boolean, playbackState: Int) {
        updatePlaybackState(songInfo, null, playbackState)
        if (songInfo.isRefrain()) {
            return
        }
        if ("playSingle" == songInfo?.headData?.get("SongType")) {
            return
        }
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

    override fun onFocusStateChange(info: FocusInfo) {
        serviceCallback?.onFocusStateChange(info)
    }

    private fun updatePlaybackState(currPlayInfo: SongInfo?, errorMsg: String?, state: Int) {
        val newState = state.changePlaybackState()
        if (!currPlayInfo.isRefrain()) {
            notification?.onPlaybackStateChanged(currPlayInfo, newState)
        }
        when (newState) {
            PlaybackStage.BUFFERING -> {
                if (!currPlayInfo.isRefrain()) {
                    startNotification(currPlayInfo, newState)
                }
            }
            PlaybackStage.PAUSE -> {
                if (!currPlayInfo.isRefrain()) {
                    startNotification(currPlayInfo, newState)
                }
            }
        }
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

    fun stopByTimedOff(time: Long, finishCurrSong: Boolean) {
        if (context is MusicService) {
            context.stopByTimedOff(time, finishCurrSong)
        }
    }

    interface PlaybackServiceCallback {
        fun onPlaybackStateUpdated(playbackStage: PlaybackStage)
        fun onFocusStateChange(info: FocusInfo)
    }

    private fun Int.changePlaybackState(): String {
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


}