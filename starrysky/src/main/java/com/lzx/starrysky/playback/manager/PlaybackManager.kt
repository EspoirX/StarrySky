package com.lzx.starrysky.playback.manager

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.ResultReceiver
import android.os.SystemClock
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.lzx.starrysky.StarrySky
import com.lzx.starrysky.control.PlayerControl
import com.lzx.starrysky.intercept.InterceptorCallback
import com.lzx.starrysky.intercept.InterceptorService
import com.lzx.starrysky.notification.INotification
import com.lzx.starrysky.playback.player.ExoPlayback
import com.lzx.starrysky.playback.player.Playback
import com.lzx.starrysky.playback.queue.MediaQueue
import com.lzx.starrysky.provider.IMediaSourceProvider
import com.lzx.starrysky.provider.NormalModeProvider
import com.lzx.starrysky.provider.ShuffleModeProvider
import com.lzx.starrysky.provider.SongInfo
import com.lzx.starrysky.utils.StarrySkyUtils

class PlaybackManager constructor(
        private val mediaQueue: MediaQueue, private val playback: Playback
) : IPlaybackManager, Playback.Callback {

    private var mServiceCallback: IPlaybackManager.PlaybackServiceCallback? = null
    private val mMediaSessionCallback: MediaSessionCallback
    private var notification: INotification? = null
    private var stateBuilder: PlaybackStateCompat.Builder? = null
    private val interceptorService: InterceptorService = InterceptorService()
    private val mHandler = Handler(Looper.getMainLooper())

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

    override fun handlePlayRequest(isPlayWhenReady: Boolean) {
        interceptorService.doInterceptions(mediaQueue.currentSongInfo, object : InterceptorCallback {
            override fun onContinue(songInfo: SongInfo?) {
                checkThreadHandPlayRequest(songInfo, isPlayWhenReady)
            }

            override fun onInterrupt(exception: Throwable?) {
                mHandler.post { updatePlaybackState(false, exception?.message) }
            }
        })
    }

    private fun checkThreadHandPlayRequest(songInfo: SongInfo?, isPlayWhenReady: Boolean) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mHandler.post { handPlayRequestImpl(songInfo, isPlayWhenReady) }
        } else {
            handPlayRequestImpl(songInfo, isPlayWhenReady)
        }
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
        updatePlaybackState(false, withError)
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

    override fun updatePlaybackState(isOnlyUpdateActions: Boolean, error: String?) {
        if (isOnlyUpdateActions && stateBuilder != null) {
            //单独更新 Actions
            stateBuilder!!.setActions(getAvailableActions())
            mServiceCallback?.onPlaybackStateUpdated(stateBuilder!!.build(), null)
        } else {
            var position = PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN
            if (playback.isConnected) {
                position = playback.currentStreamPosition
            }
            //构建一个播放状态对象
            stateBuilder = PlaybackStateCompat.Builder().setActions(getAvailableActions())
            //获取播放器播放状态
            var state = playback.playbackState
            //如果错误信息不为 null 的时候，播放状态设为 STATE_ERROR
            if (!error.isNullOrEmpty()) {
                stateBuilder!!.setErrorMessage(error)
                state = PlaybackStateCompat.STATE_ERROR
            }
            //设置播放状态
            stateBuilder!!.setState(state, position, 1.0f, SystemClock.elapsedRealtime())
            //设置当前活动的 songId
            val currentMusic = mediaQueue.currentSongInfo
            var currMetadata: MediaMetadataCompat? = null
            if (currentMusic != null) {
                stateBuilder!!.setActiveQueueItemId(-1L)
                val musicId = currentMusic.songId
                currMetadata =
                        StarrySky.get().mediaQueueProvider().getMediaMetadataById(musicId)
            }
            //把状态回调出去
            mServiceCallback?.onPlaybackStateUpdated(stateBuilder!!.build(), currMetadata)
            //如果是播放或者暂停的状态，更新一下通知栏
            if (state == PlaybackStateCompat.STATE_PLAYING || state == PlaybackStateCompat.STATE_PAUSED) {
                mServiceCallback?.onNotificationRequired()
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
//        if (!shouldPlayNext) {
//            //在不能播放下一首的情况下，判断actions是否包含ACTION_SKIP_TO_NEXT，如果包含则清除
//            if (actions and PlaybackStateCompat.ACTION_SKIP_TO_NEXT != 0L) {
//                actions = actions and PlaybackStateCompat.ACTION_SKIP_TO_NEXT.inv()
//            }
//        } else {
        //判断 actions 是否包含 ACTION_SKIP_TO_NEXT，如果不包含，则添加
        if (actions and PlaybackStateCompat.ACTION_SKIP_TO_NEXT == 0L) {
            actions = actions or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
        }
//        }
        //同理
//        if (!shouldPlayPre) {
//            if (actions and PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS != 0L) {
//                actions = actions and PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS.inv()
//            }
//        } else {
        if (actions and PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS == 0L) {
            actions = actions or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
        }
//        }
        return actions
    }

    override fun registerNotification(notification: INotification) {
        this.notification = notification
    }

    override fun onCompletion() {
        updatePlaybackState(false, null)
        val repeatMode = StarrySkyUtils.getRepeatMode()
        when (repeatMode.repeatMode) {
            //顺序播放
            PlayerControl.REPEAT_MODE_NONE -> {
                if (repeatMode.isLoop) {
                    skipToNextSongImpl(1)
                } else {
                    if (!mediaQueue.currSongIsLastSong()) {
                        skipToNextSongImpl(1)
                    }
                }
            }
            //单曲播放
            PlayerControl.REPEAT_MODE_ONE -> {
                playback.currentMediaId = ""
                if (repeatMode.isLoop) {
                    handlePlayRequest(true)
                }
            }
            //随机播放
            PlayerControl.REPEAT_MODE_SHUFFLE -> {
                if (repeatMode.isLoop) {
                    skipToNextSongImpl(1)
                } else {
                    if (!mediaQueue.currSongIsLastSong()) {
                        skipToNextSongImpl(1)
                    }
                }
            }
            //倒序播放
            PlayerControl.REPEAT_MODE_REVERSE -> {
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

    private fun skipToNextSongImpl(amount: Int) {
        if (mediaQueue.skipQueuePosition(amount)) {
            handlePlayRequest(true)
            mediaQueue.updateMediaMetadata()
        }
    }

    override fun onPlaybackStatusChanged(state: Int) {
        updatePlaybackState(false, null)
    }

    override fun onError(error: String) {
        updatePlaybackState(false, error)
    }

    /**
     * MusicManager API 方法的具体实现
     */
    inner class MediaSessionCallback : MediaSessionCompat.Callback() {
        override fun onPrepare() {
            super.onPrepare()
            handlePlayRequest(false)
        }

        override fun onPrepareFromMediaId(mediaId: String?, extras: Bundle?) {
            super.onPrepareFromMediaId(mediaId, extras)
            mediaId?.apply {
                mediaQueue.updateCurrPlayingSongInfo(this)
                handlePlayRequest(false)
            }
        }

        override fun onPlay() {
            super.onPlay()
            mediaQueue.currentSongInfo?.let {
                handlePlayRequest(true)
            }
        }

        override fun onSkipToQueueItem(id: Long) {
            super.onSkipToQueueItem(id)
            mediaQueue.updateIndexBySongId(id.toString())
            mediaQueue.updateMediaMetadata()
        }

        override fun onSeekTo(pos: Long) {
            super.onSeekTo(pos)
            playback.seekTo(pos)
            if (playback.playbackState == Playback.PLAYBACK_STATE_PAUSED) {
                onPlay()
            }
        }

        override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
            super.onPlayFromMediaId(mediaId, extras)
            mediaId?.apply {
                mediaQueue.updateCurrPlayingSongInfo(this)
                handlePlayRequest(true)
            }
        }

        override fun onPause() {
            super.onPause()
            handlePauseRequest()
        }

        override fun onStop() {
            super.onStop()
            handleStopRequest(null)
        }

        override fun onSkipToNext() {
            super.onSkipToNext()
            if (mediaQueue.skipQueuePosition(1)) {
                handlePlayRequest(true)
                mediaQueue.updateMediaMetadata()
            }
        }

        override fun onSkipToPrevious() {
            super.onSkipToPrevious()
            if (mediaQueue.skipQueuePosition(-1)) {
                handlePlayRequest(true)
                mediaQueue.updateMediaMetadata()
            }
        }

        override fun onFastForward() {
            super.onFastForward()
            handleFastForward()
        }

        override fun onRewind() {
            super.onRewind()
            handleRewind()
        }

        override fun onCommand(command: String?, extras: Bundle?, cb: ResultReceiver?) {
            super.onCommand(command, extras, cb)
            if (command == null) {
                return
            }
            when (command) {
                INotification.ACTION_UPDATE_FAVORITE_UI -> {
                    val isFavorite = extras?.getBoolean("isFavorite")
                    isFavorite?.apply { notification?.updateFavoriteUI(this) }
                }
                INotification.ACTION_UPDATE_LYRICS_UI -> {
                    val isChecked = extras?.getBoolean("isChecked") ?: false
                    notification?.updateLyricsUI(isChecked)
                }
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
                PlayerControl.KEY_REPEAT_MODE -> {
                    val repeatMode = StarrySkyUtils.getRepeatMode().repeatMode
                    if (repeatMode == PlayerControl.REPEAT_MODE_SHUFFLE) {
                        if (StarrySky.get().mediaQueueProvider() is NormalModeProvider) {
                            StarrySky.get().setMediaSourceProvider(ShuffleModeProvider())
                        }
                    } else {
                        if (StarrySky.get().mediaQueueProvider() is ShuffleModeProvider) {
                            StarrySky.get().setMediaSourceProvider(NormalModeProvider())
                        }
                    }
                }
            }
        }
    }
}