package com.lzx.starrysky.playback.manager

import android.os.Bundle
import android.os.ResultReceiver
import android.os.SystemClock
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.lzx.starrysky.StarrySky
import com.lzx.starrysky.control.RepeatMode
import com.lzx.starrysky.intercept.InterceptorCallback
import com.lzx.starrysky.intercept.InterceptorService
import com.lzx.starrysky.notification.INotification
import com.lzx.starrysky.playback.player.ExoPlayback
import com.lzx.starrysky.playback.player.Playback
import com.lzx.starrysky.playback.queue.MediaQueue
import com.lzx.starrysky.provider.IMediaSourceProvider
import com.lzx.starrysky.provider.SongInfo
import com.lzx.starrysky.utils.MainLooper
import com.lzx.starrysky.utils.StarrySkyUtils

class PlaybackManager constructor(
    private val mediaQueue: MediaQueue, private val playback: Playback
) : IPlaybackManager, Playback.Callback {

    private var mServiceCallback: IPlaybackManager.PlaybackServiceCallback? = null
    private val mMediaSessionCallback: MediaSessionCallback
    private var notification: INotification? = null
    private var stateBuilder: PlaybackStateCompat.Builder? = null
    private val interceptorService: InterceptorService = InterceptorService()

    init {
        mMediaSessionCallback = MediaSessionCallback()
        playback.setCallback(this)
    }

    override val mediaSessionCallback: MediaSessionCompat.Callback
        get() = mMediaSessionCallback

    override val isPlaying: Boolean
        get() = playback.isPlaying

    override fun setServiceCallback(serviceCallback: IPlaybackManager.PlaybackServiceCallback) {
        mServiceCallback = serviceCallback
    }

    override fun setMetadataUpdateListener(listener: IMediaSourceProvider.MetadataUpdateListener) {
        mediaQueue.setMetadataUpdateListener(listener)
    }

    /**
     * 主动触发的都从正常的队列中取
     * 非主动触发的根据播放模式在不同的队列中取
     */
    override fun handlePlayRequest(isPlayWhenReady: Boolean, isActiveTrigger: Boolean) {
        val playSongInfo = mediaQueue.getCurrentSongInfo(isActiveTrigger) //要播放的歌曲信息
        interceptorService.doInterceptions(playSongInfo, object : InterceptorCallback {
            override fun onContinue(songInfo: SongInfo?) {
                MainLooper.instance.runOnUiThread(Runnable {
                    mediaQueue.updateMediaMetadata(playSongInfo) //更新媒体封面信息
                    handPlayRequestImpl(songInfo, isPlayWhenReady)
                })
            }

            override fun onInterrupt(exception: Throwable?) {
                MainLooper.instance.runOnUiThread(Runnable {
                    updatePlaybackState(playSongInfo, isOnlyUpdateActions = false, isError = true,
                        error = exception?.message)
                    playback.currentMediaId = ""
                })
            }
        })
    }

    private fun handPlayRequestImpl(songInfo: SongInfo?, isPlayWhenReady: Boolean) {
        songInfo?.let {
            if (isPlayWhenReady) {
                mServiceCallback?.onPlaybackStart()
            }
            playback.play(it, isPlayWhenReady)
        }
    }

    override fun handlePauseRequest() {
        if (playback.isPlaying) {
            playback.pause()
            mServiceCallback?.onPlaybackStop(false)
        }
    }

    override fun handleStopRequest(withError: String?) {
        playback.stop()
        mServiceCallback?.onPlaybackStop(true)
        val hasError = withError?.isNotEmpty() ?: false
        updatePlaybackState(playback.currPlayInfo, false, hasError, withError)
    }

    override fun handleFastForward() {
        playback.onFastForward()
    }

    override fun handleRewind() {
        playback.onRewind()
    }

    override fun handleDerailleur(refer: Boolean, multiple: Float) {
        playback.onDerailleur(refer, multiple)
    }

    override fun updatePlaybackState(
        currPlayInfo: SongInfo?,
        isOnlyUpdateActions: Boolean,
        isError: Boolean,
        error: String?
    ) {
        if (isOnlyUpdateActions) {
            //单独更新 Actions
            stateBuilder?.setActions(getAvailableActions())
            mServiceCallback?.onPlaybackStateUpdated(stateBuilder?.build(), null)
        } else {
            //当前播放进度
            var currentStreamPosition = PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN
            if (playback.isConnected) {
                currentStreamPosition = playback.currentStreamPosition
            }
            //构建一个播放状态对象
            stateBuilder = PlaybackStateCompat.Builder().setActions(getAvailableActions())
            //获取播放器播放状态
            var state = playback.playbackState
            //发生错误的时候，播放状态设为 STATE_ERROR
            if (isError) {
                stateBuilder?.setErrorMessage(if (!error.isNullOrEmpty()) error else "错误信息为 null")
                state = Playback.STATE_ERROR
            }
            //设置播放状态
            stateBuilder?.setState(state, currentStreamPosition, 1.0f,
                SystemClock.elapsedRealtime())

            //获取当前的 MediaMetadataCompat
            var currMetadata: MediaMetadataCompat? = null
            currPlayInfo?.let {
                stateBuilder?.setActiveQueueItemId(-1L)
                currMetadata =
                    StarrySky.get().mediaQueueProvider().getMediaMetadataById(it.songId)
            }
            //把状态回调出去
            val playbackState = stateBuilder?.build()
            mServiceCallback?.onPlaybackStateUpdated(playbackState, currMetadata)
            //如果是播放或者暂停的状态，更新一下通知栏
            if (state == Playback.STATE_PLAYING || state == Playback.STATE_PAUSED) {
                notification?.startNotification(currPlayInfo, playbackState)
            }
        }
    }

    /**
     * 获取状态
     */
    private fun getAvailableActions(): Long {
        var actions = PlaybackStateCompat.ACTION_PLAY_PAUSE or
            PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID or
            PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH or
            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
            PlaybackStateCompat.ACTION_SKIP_TO_NEXT
        actions = if (playback.isPlaying) {
            actions or PlaybackStateCompat.ACTION_PAUSE //添加 ACTION_PAUSE
        } else {
            actions or PlaybackStateCompat.ACTION_PLAY //添加 ACTION_PLAY
        }
        var shouldPlayNext = true
        var shouldPlayPre = true
        val repeatMode = StarrySkyUtils.repeatMode
        if (repeatMode.repeatMode != RepeatMode.REPEAT_MODE_SHUFFLE) {
            //如果没开启循环并且当前歌曲是最后一首，则不能下一首
            shouldPlayNext = !(!repeatMode.isLoop && mediaQueue.currSongIsLastSong())
            shouldPlayPre = !(!repeatMode.isLoop && mediaQueue.currSongIsFirstSong())
        }
        if (!shouldPlayNext) {
            //在不能播放下一首的情况下，判断actions是否包含ACTION_SKIP_TO_NEXT，如果包含则清除
            if (actions and PlaybackStateCompat.ACTION_SKIP_TO_NEXT != 0L) {
                actions = actions and PlaybackStateCompat.ACTION_SKIP_TO_NEXT.inv()
            }
        } else {
            //判断 actions 是否包含 ACTION_SKIP_TO_NEXT，如果不包含，则添加
            if (actions and PlaybackStateCompat.ACTION_SKIP_TO_NEXT == 0L) {
                actions = actions or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
            }
        }
        //同理
        if (!shouldPlayPre) {
            if (actions and PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS != 0L) {
                actions = actions and PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS.inv()
            }
        } else {
            if (actions and PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS == 0L) {
                actions = actions or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
            }
        }
        return actions
    }

    override fun registerNotification(notification: INotification?) {
        this.notification = notification
    }

    /**
     * 播放器回调播放完成时会回调这里
     */
    override fun onPlaybackCompletion() {
        updatePlaybackState(playback.currPlayInfo, isOnlyUpdateActions = false, isError = false,
            error = null)
        val repeatMode = StarrySkyUtils.repeatMode
        when (repeatMode.repeatMode) {
            //顺序播放
            RepeatMode.REPEAT_MODE_NONE -> {
                if (repeatMode.isLoop) {
                    skipToNextSongImpl(1)
                } else {
                    if (!mediaQueue.currSongIsLastSong()) {
                        skipToNextSongImpl(1)
                    }
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
                skipToNextSongImpl(1)
            }
            //倒序播放
            RepeatMode.REPEAT_MODE_REVERSE -> {
                if (repeatMode.isLoop) {
                    skipToNextSongImpl(-1)
                } else {
                    if (!mediaQueue.currSongIsFirstSong()) {
                        skipToNextSongImpl(-1)
                    }
                }
            }
        }
    }

    /**
     * 播放器回调状态改变时回调这里
     */
    override fun onPlaybackStatusChanged(songInfo: SongInfo?, state: Int) {
        updatePlaybackState(songInfo, isOnlyUpdateActions = false, isError = false, error = null)
    }

    /**
     * 播放器回调错误时回调这里
     */
    override fun onPlaybackError(songInfo: SongInfo?, error: String) {
        updatePlaybackState(songInfo, isOnlyUpdateActions = false, isError = true, error = error)
    }

    private fun skipToNextSongImpl(amount: Int) {
        if (mediaQueue.skipQueuePosition(amount)) {
            handlePlayRequest(isPlayWhenReady = true, isActiveTrigger = false)
        }
    }

    /**
     * MusicManager API 方法的具体实现
     */
    inner class MediaSessionCallback : MediaSessionCompat.Callback() {

        /**
         * 缓冲不播放
         */
        override fun onPrepare() {
            super.onPrepare()
            handlePlayRequest(isPlayWhenReady = false, isActiveTrigger = true)
        }

        /**
         * 根据 id 缓冲
         */
        override fun onPrepareFromMediaId(mediaId: String?, extras: Bundle?) {
            super.onPrepareFromMediaId(mediaId, extras)
            mediaId?.apply {
                mediaQueue.updateIndexBySongId(this)
                handlePlayRequest(isPlayWhenReady = false, isActiveTrigger = true)
            }
        }

        /**
         * 根据 id 播放
         */
        override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
            super.onPlayFromMediaId(mediaId, extras)
            mediaId?.apply {
                mediaQueue.updateIndexBySongId(this)
                handlePlayRequest(isPlayWhenReady = true, isActiveTrigger = true)
            }
        }

        /**
         * 暂停后播放
         */
        override fun onPlay() {
            super.onPlay()
            playback.currPlayInfo?.let {
                handlePlayRequest(isPlayWhenReady = true, isActiveTrigger = true)
            }
        }

        /**
         * 暂停
         */
        override fun onPause() {
            super.onPause()
            handlePauseRequest()
        }

        /**
         * 停止
         */
        override fun onStop() {
            super.onStop()
            handleStopRequest(null)
        }

        /**
         * 拖进度条
         */
        override fun onSeekTo(pos: Long) {
            super.onSeekTo(pos)
            playback.seekTo(pos)
            if (playback.playbackState == Playback.STATE_PAUSED) {
                onPlay()
            }
        }

        /**
         * 下一首
         */
        override fun onSkipToNext() {
            super.onSkipToNext()
            if (mediaQueue.skipQueuePosition(1)) {
                handlePlayRequest(isPlayWhenReady = true, isActiveTrigger = true)
            }
        }

        /**
         * 上一首
         */
        override fun onSkipToPrevious() {
            super.onSkipToPrevious()
            if (mediaQueue.skipQueuePosition(-1)) {
                handlePlayRequest(isPlayWhenReady = true, isActiveTrigger = true)
            }
        }

        /**
         * 快进
         */
        override fun onFastForward() {
            super.onFastForward()
            handleFastForward()
        }

        /**
         * 快退
         */
        override fun onRewind() {
            super.onRewind()
            handleRewind()
        }

        /**
         * 自定义命令
         */
        override fun onCommand(command: String?, extras: Bundle?, cb: ResultReceiver?) {
            super.onCommand(command, extras, cb)
            if (command == null) {
                return
            }
            when (command) {
                ExoPlayback.ACTION_CHANGE_VOLUME -> {
                    val audioVolume = extras?.getFloat("AudioVolume") ?: 0F
                    playback.volume = audioVolume
                }
                ExoPlayback.ACTION_DERAILLEUR -> {
                    val refer = extras?.getBoolean("refer") ?: false
                    val multiple = extras?.getFloat("multiple") ?: 0F
                    handleDerailleur(refer, multiple)
                }
                //播放模式
                RepeatMode.KEY_REPEAT_MODE -> {
                    val repeatMode = StarrySkyUtils.repeatMode.repeatMode
                    if (repeatMode == RepeatMode.REPEAT_MODE_SHUFFLE) {
                        StarrySky.get().mediaQueueProvider().updateShuffleSongList()
                    } else {
                        mediaQueue.updateIndexBySongId(playback.currentMediaId)
                    }
                    updatePlaybackState(null, isOnlyUpdateActions = true, isError = false, error = null)
                }
                else -> {
                    //通知栏相关
                    notification?.onCommand(command, extras)
                }
            }
        }
    }
}