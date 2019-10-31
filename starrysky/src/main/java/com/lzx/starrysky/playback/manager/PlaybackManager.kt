package com.lzx.starrysky.playback.manager

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.ResultReceiver
import android.os.SystemClock
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.lzx.starrysky.BaseMediaInfo
import com.lzx.starrysky.StarrySky
import com.lzx.starrysky.delayaction.Action
import com.lzx.starrysky.delayaction.PlayValidManager
import com.lzx.starrysky.ext.SINGLE_MODE_ONE
import com.lzx.starrysky.notification.INotification
import com.lzx.starrysky.playback.player.ExoPlayback
import com.lzx.starrysky.playback.player.Playback
import com.lzx.starrysky.playback.queue.MediaQueue
import com.lzx.starrysky.provider.MediaQueueProvider
import com.lzx.starrysky.provider.SongInfo
import com.lzx.starrysky.registry.ValidRegistry

class PlaybackManager constructor(
    private val mediaQueue: MediaQueue, private val playback: Playback
) : IPlaybackManager,
    Playback.Callback {

    private var mServiceCallback: IPlaybackManager.PlaybackServiceCallback? = null
    private val mMediaSessionCallback: MediaSessionCallback
    private var notification: INotification? = null
    private var currRepeatMode: Int = 0
    private val shouldPlayNext = true //是否可以播放下一首
    private val shouldPlayPre = true  //是否可以播放上一首
    private var stateBuilder: PlaybackStateCompat.Builder? = null
    private val mHandler = Handler(Looper.getMainLooper())

    init {
        mMediaSessionCallback = MediaSessionCallback()
        playback.setCallback(this)
        currRepeatMode = PlaybackStateCompat.REPEAT_MODE_NONE
    }

    override val mediaSessionCallback: MediaSessionCompat.Callback
        get() = mMediaSessionCallback

    override val isPlaying: Boolean
        get() = playback.isPlaying

    override fun setServiceCallback(serviceCallback: IPlaybackManager.PlaybackServiceCallback) {
        mServiceCallback = serviceCallback
    }

    override fun setMetadataUpdateListener(listener: MediaQueueProvider.MetadataUpdateListener) {
        mediaQueue.setMetadataUpdateListener(listener)
    }

    override fun handlePlayRequest(isPlayWhenReady: Boolean) {
        val validRegistry = StarrySky.get().registry.validRegistry
        if (validRegistry.hasValid()) {
            val validManager = PlayValidManager.get()
            //添加执行action
            validManager.setAction(object : Action {
                override fun call(songInfo: SongInfo?) {
                    val mediaInfo = mediaQueue.songInfoToMediaInfo(songInfo)
                    checkThreadHandPlayRequest(mediaInfo, isPlayWhenReady)
                }
            })
            //添加验证
            for (valid in validRegistry.valids) {
                validManager.addValid(valid ?: ValidRegistry.DefaultValid())
            }
            //执行第一个验证
            validManager.doCall(mediaQueue.currMediaInfo?.mediaId)
        } else {
            checkThreadHandPlayRequest(null, isPlayWhenReady)
        }
    }

    private fun checkThreadHandPlayRequest(mediaInfo: BaseMediaInfo?, isPlayWhenReady: Boolean) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mHandler.post { handPlayRequestImpl(mediaInfo, isPlayWhenReady) }
        } else {
            handPlayRequestImpl(mediaInfo, isPlayWhenReady)
        }
    }

    private fun handPlayRequestImpl(mediaInfo: BaseMediaInfo?, isPlayWhenReady: Boolean) {
        val currentMusic = mediaQueue.getCurrentMusic(mediaInfo)
        if (currentMusic != null) {
            if (isPlayWhenReady) {
                mServiceCallback?.onPlaybackStart()
            }
            playback.play(currentMusic, isPlayWhenReady)
        }
    }

    override fun handlePauseRequest() {
        if (playback.isPlaying) {
            playback.pause()
            mServiceCallback?.onPlaybackStop()
        }
    }

    override fun handleStopRequest(withError: String?) {
        playback.stop(true)
        mServiceCallback?.onPlaybackStop()
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
            stateBuilder = PlaybackStateCompat.Builder()
                .setActions(getAvailableActions())
            //获取播放器播放状态
            var state = playback.state
            //如果错误信息不为 null 的时候，播放状态设为 STATE_ERROR
            if (!error.isNullOrEmpty()) {
                stateBuilder!!.setErrorMessage(error)
                state = PlaybackStateCompat.STATE_ERROR
            }
            //设置播放状态
            stateBuilder!!.setState(state, position, 1.0f, SystemClock.elapsedRealtime())
            //设置当前活动的 songId
            val currentMusic = mediaQueue.getCurrentMusic()
            var currMetadata: MediaMetadataCompat? = null
            if (currentMusic != null) {
                stateBuilder!!.setActiveQueueItemId(currentMusic.getQueueId())
                val musicId = currentMusic.getMediaId()
                currMetadata =
                    StarrySky.get().mediaQueueProvider.getMediaMetadataCompatById(musicId)
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

    override fun registerNotification(notification: INotification) {
        this.notification = notification
    }

    override fun onCompletion() {
        updatePlaybackState(false, null)
        //单曲模式(播放当前就结束)
        if (currRepeatMode == SINGLE_MODE_ONE) {
            playback.currentMediaId = ""
            return
        }
        if (currRepeatMode == PlaybackStateCompat.REPEAT_MODE_NONE) {
            //顺序播放
            if (shouldPlayNext && mediaQueue.skipQueuePosition(1)) {
                handlePlayRequest(true)
                mediaQueue.updateMetadata()
            } else {
                handleStopRequest(null)
            }
        } else if (currRepeatMode == PlaybackStateCompat.REPEAT_MODE_ONE) {
            //单曲循环
            playback.currentMediaId = ""
            handlePlayRequest(true)
        } else if (currRepeatMode == PlaybackStateCompat.REPEAT_MODE_ALL) {
            //列表循环
            if (shouldPlayNext && mediaQueue.skipQueuePosition(1)) {
                handlePlayRequest(true)
                mediaQueue.updateMetadata()
            } else {
                handleStopRequest(null)
            }
        }
    }

    override fun onPlaybackStatusChanged(state: Int) {
        updatePlaybackState(false, null)
    }

    override fun onError(error: String) {
        updatePlaybackState(false, error)
    }

    override fun setCurrentMediaId(mediaId: String) {
        mediaQueue.updateCurrPlayingMedia(mediaId)
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
                mediaQueue.updateCurrPlayingMedia(this)
                handlePlayRequest(false)
            }
        }

        override fun onPlay() {
            super.onPlay()
            if (mediaQueue.getCurrentMusic() == null) {
                return
            }
            handlePlayRequest(true)
        }

        override fun onSkipToQueueItem(id: Long) {
            super.onSkipToQueueItem(id)
            mediaQueue.updateIndexByMediaId(id.toString())
            mediaQueue.updateMetadata()
        }

        override fun onSeekTo(pos: Long) {
            super.onSeekTo(pos)
            playback.seekTo(pos)
        }

        override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
            super.onPlayFromMediaId(mediaId, extras)
            mediaId?.apply {
                mediaQueue.updateCurrPlayingMedia(this)
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
            if (shouldPlayNext) {
                if (mediaQueue.skipQueuePosition(1)) {
                    handlePlayRequest(true)
                    mediaQueue.updateMetadata()
                }
            }
        }

        override fun onSkipToPrevious() {
            super.onSkipToPrevious()
            if (shouldPlayPre) {
                if (mediaQueue.skipQueuePosition(-1)) {
                    handlePlayRequest(true)
                    mediaQueue.updateMetadata()
                }
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

        override fun onSetShuffleMode(shuffleMode: Int) {
            super.onSetShuffleMode(shuffleMode)
            if (shuffleMode == PlaybackStateCompat.SHUFFLE_MODE_NONE) {
                mediaQueue.setNormalMode(playback.currentMediaId)
            } else if (shuffleMode == PlaybackStateCompat.SHUFFLE_MODE_ALL) {
                mediaQueue.setShuffledMode()
            }
            mServiceCallback?.onShuffleModeUpdated(shuffleMode)
        }

        override fun onSetRepeatMode(repeatMode: Int) {
            super.onSetRepeatMode(repeatMode)
            currRepeatMode = repeatMode
            mServiceCallback?.onRepeatModeUpdated(repeatMode)
            updatePlaybackState(true, null)  //更新状态
        }

        override fun onCommand(command: String?, extras: Bundle?, cb: ResultReceiver?) {
            super.onCommand(command, extras, cb)
            if (command == null) {
                return
            }
            if (INotification.ACTION_UPDATE_FAVORITE_UI == command) {
                val isFavorite = extras?.getBoolean("isFavorite")
                isFavorite?.apply { notification?.updateFavoriteUI(this) }
            }
            if (INotification.ACTION_UPDATE_LYRICS_UI == command) {
                val isChecked = extras?.getBoolean("isChecked") ?: false
                notification?.updateLyricsUI(isChecked)
            }
            if (ExoPlayback.ACTION_CHANGE_VOLUME == command) {
                val audioVolume = extras?.getFloat("AudioVolume") ?: 0F
                playback.volume = audioVolume
            }
            if (ExoPlayback.ACTION_DERAILLEUR == command) {
                val refer = extras?.getBoolean("refer") ?: false
                val multiple = extras?.getFloat("multiple") ?: 0F
                handleDerailleur(refer, multiple)
            }
        }
    }
}