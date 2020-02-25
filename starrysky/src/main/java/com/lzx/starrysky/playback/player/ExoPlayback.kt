package com.lzx.starrysky.playback.player

import android.content.Context
import android.support.v4.media.session.PlaybackStateCompat
import com.google.android.exoplayer2.C.CONTENT_TYPE_MUSIC
import com.google.android.exoplayer2.C.USAGE_MEDIA
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.Timeline
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.drm.DefaultDrmSessionManager
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.util.EventLogger
import com.lzx.starrysky.playback.offline.StarrySkyCacheManager
import com.lzx.starrysky.provider.MediaResource
import com.lzx.starrysky.utils.StarrySkyUtils

open class ExoPlayback internal constructor(
    var context: Context, private var cacheManager: StarrySkyCacheManager
) : Playback {

    private val trackSelectorParameters: DefaultTrackSelector.Parameters by lazy {
        DefaultTrackSelector.ParametersBuilder().build()
    }
    private val mEventListener by lazy {
        ExoPlayerEventListener()
    }
    private val sourceManager: ExoSourceManager by lazy {
        ExoSourceManager(context, cacheManager)
    }

    private var mPlayOnFocusGain: Boolean = false
    private var mCallback: Playback.Callback? = null
    private var mExoPlayerNullIsStopped = false
    private var mExoPlayer: SimpleExoPlayer? = null

    private var sourceTypeErrorInfo: SourceTypeErrorInfo = SourceTypeErrorInfo()

    /**
     * 发生错误时保存的信息
     */
    inner class SourceTypeErrorInfo {
        var seekToPosition = 0L
        var happenSourceError = false //是否发生资源问题的错误
        var seekToPositionWhenError = 0L
        var currPositionWhenError = 0L //发生错误时的进度

        fun clear() {
            happenSourceError = false //是否发生资源问题的错误
            seekToPosition = 0L
            seekToPositionWhenError = 0L
            currPositionWhenError = 0L //发生错误时的进度
        }
    }

    companion object {
        const val ACTION_CHANGE_VOLUME = "ACTION_CHANGE_VOLUME"
        const val ACTION_DERAILLEUR = "ACTION_DERAILLEUR"
    }

    override var state: Int
        get() = if (mExoPlayer == null) {
            if (mExoPlayerNullIsStopped)
                PlaybackStateCompat.STATE_STOPPED
            else
                PlaybackStateCompat.STATE_NONE
        } else {
            when (mExoPlayer!!.playbackState) {
                Player.STATE_IDLE -> PlaybackStateCompat.STATE_PAUSED
                Player.STATE_BUFFERING -> PlaybackStateCompat.STATE_BUFFERING
                Player.STATE_READY -> {
                    if (mExoPlayer!!.playWhenReady)
                        PlaybackStateCompat.STATE_PLAYING
                    else
                        PlaybackStateCompat.STATE_PAUSED
                }
                Player.STATE_ENDED -> PlaybackStateCompat.STATE_NONE
                else -> PlaybackStateCompat.STATE_NONE
            }
        }
        set(value) {}

    override val isConnected: Boolean
        get() = true

    override val isPlaying: Boolean
        get() = mPlayOnFocusGain || (mExoPlayer != null && mExoPlayer!!.playWhenReady)

    override val currentStreamPosition: Long
        get() = mExoPlayer?.currentPosition ?: 0

    override val bufferedPosition: Long
        get() = mExoPlayer?.bufferedPosition ?: 0

    override val duration: Long
        get() = mExoPlayer?.duration ?: -1

    override var currentMediaId: String = ""

    override var volume: Float
        get() = mExoPlayer?.volume ?: -1f
        set(value) {
            mExoPlayer?.volume = value
        }

    override fun getAudioSessionId(): Int {
        return mExoPlayer?.audioSessionId ?: 0
    }

    override fun start() {
        // Nothing to do.
    }

    override fun stop(notifyListeners: Boolean) {
        releaseResources(true)
    }

    override fun updateLastKnownStreamPosition() {
        // Nothing to do. Position maintained by ExoPlayer.
    }

    override fun play(mediaResource: MediaResource, isPlayWhenReady: Boolean) {
        mPlayOnFocusGain = true
        val mediaId = mediaResource.getMediaId()
        if (mediaId.isNullOrEmpty()) {
            return
        }
        val mediaHasChanged = mediaId != currentMediaId
        if (mediaHasChanged) {
            currentMediaId = mediaId
        }
        StarrySkyUtils.log(
            "Playback# resource is empty = " + mediaResource.getMediaUrl().isNullOrEmpty() +
                " mediaHasChanged = " + mediaHasChanged +
                " isPlayWhenReady = " + isPlayWhenReady)
        StarrySkyUtils.log("---------------------------------------")

        //创建 mediaSource
        var source = mediaResource.getMediaUrl()
        if (source.isNullOrEmpty()) {
            mCallback?.onError("播放 url 为空")
            return
        }
        source = source.replace(" ".toRegex(), "%20") // Escape spaces for URL
        val mediaSource = sourceManager.buildMediaSource(
            source,
            null,
            mediaResource.getMapHeadData(),
            cacheManager.isOpenCache(),
            cacheManager.getDownloadCache())

        //如果资源改变了或者播放器为空则重新加载
        if (mediaHasChanged || mExoPlayer == null) {
            releaseResources(false)  // release everything except the player

            //创建播放器实例
            createExoPlayer()

            mExoPlayer!!.prepare(mediaSource)
        }
        //当错误发生时，如果还播放同一首歌，
        //这时候需要重新加载一下，并且吧进度 seekTo 到出错的地方
        if (sourceTypeErrorInfo.happenSourceError && !mediaHasChanged) {
            mExoPlayer!!.prepare(mediaSource)
            if (sourceTypeErrorInfo.currPositionWhenError != 0L) {
                if (sourceTypeErrorInfo.seekToPositionWhenError != 0L) {
                    mExoPlayer!!.seekTo(sourceTypeErrorInfo.seekToPositionWhenError)
                } else {
                    mExoPlayer!!.seekTo(sourceTypeErrorInfo.currPositionWhenError)
                }
            }
        }
        //如果准备好就播放
        if (isPlayWhenReady) {
            mExoPlayer!!.playWhenReady = true
        }
    }

    /**
     * 创建播放器实例
     */
    private fun createExoPlayer() {
        if (mExoPlayer == null) {
            //轨道选择
            val trackSelectionFactory = AdaptiveTrackSelection.Factory()

            //使用扩展渲染器的模式
            @DefaultRenderersFactory.ExtensionRendererMode
            val extensionRendererMode = DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER
            val renderersFactory = DefaultRenderersFactory(context, extensionRendererMode)

            //轨道选择
            val trackSelector = DefaultTrackSelector(trackSelectionFactory)
            trackSelector.parameters = trackSelectorParameters

            val drmSessionManager: DefaultDrmSessionManager<FrameworkMediaCrypto>? = null

            mExoPlayer = ExoPlayerFactory.newSimpleInstance(context, renderersFactory,
                trackSelector, drmSessionManager)

            mExoPlayer!!.addListener(mEventListener)
            mExoPlayer!!.addAnalyticsListener(EventLogger(trackSelector))

            val audioAttributes = AudioAttributes.Builder()
                .setContentType(CONTENT_TYPE_MUSIC)
                .setUsage(USAGE_MEDIA)
                .build()
            mExoPlayer!!.setAudioAttributes(audioAttributes, true) //第二个参数能使ExoPlayer自动管理焦点
        }
    }

    override fun pause() {
        mExoPlayer?.playWhenReady = false
        releaseResources(false)
    }

    override fun seekTo(position: Long) {
        mExoPlayer?.seekTo(position)
        sourceTypeErrorInfo.seekToPosition = position
        if (sourceTypeErrorInfo.happenSourceError) {
            sourceTypeErrorInfo.seekToPositionWhenError = position
        }
    }

    override fun onFastForward() {
        if (mExoPlayer != null) {
            val currSpeed = mExoPlayer!!.playbackParameters.speed
            val currPitch = mExoPlayer!!.playbackParameters.pitch
            val newSpeed = currSpeed + 0.5f
            mExoPlayer!!.playbackParameters = PlaybackParameters(newSpeed, currPitch)
        }
    }

    override fun onRewind() {
        if (mExoPlayer != null) {
            val currSpeed = mExoPlayer!!.playbackParameters.speed
            val currPitch = mExoPlayer!!.playbackParameters.pitch
            var newSpeed = currSpeed - 0.5f
            if (newSpeed <= 0) {
                newSpeed = 0f
            }
            mExoPlayer!!.playbackParameters = PlaybackParameters(newSpeed, currPitch)
        }
    }

    override fun onDerailleur(refer: Boolean, multiple: Float) {
        if (mExoPlayer != null) {
            val currSpeed = mExoPlayer!!.playbackParameters.speed
            val currPitch = mExoPlayer!!.playbackParameters.pitch
            val newSpeed = if (refer) currSpeed * multiple else multiple
            if (newSpeed > 0) {
                mExoPlayer!!.playbackParameters = PlaybackParameters(newSpeed, currPitch)
            }
        }
    }

    override fun setCallback(callback: Playback.Callback) {
        this.mCallback = callback
    }

    private fun releaseResources(releasePlayer: Boolean) {
        if (releasePlayer) {
            mExoPlayer?.release()
            mExoPlayer?.removeListener(mEventListener)
            mExoPlayer = null
            mExoPlayerNullIsStopped = true
            mPlayOnFocusGain = false
        }
    }

    private inner class ExoPlayerEventListener : Player.EventListener {
        override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) {
            // Nothing to do.
        }

        override fun onTracksChanged(
            trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?
        ) {
            // Nothing to do.
        }

        override fun onLoadingChanged(isLoading: Boolean) {
            // Nothing to do.
        }

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            when (playbackState) {
                Player.STATE_IDLE, Player.STATE_BUFFERING, Player.STATE_READY ->
                    mCallback?.onPlaybackStatusChanged(state)
                Player.STATE_ENDED -> mCallback?.onCompletion()
            }
            if (playbackState == Player.STATE_READY) {
                sourceTypeErrorInfo.clear()
            }
        }

        override fun onPlayerError(error: ExoPlaybackException) {
            val what: String = when (error.type) {
                ExoPlaybackException.TYPE_SOURCE -> error.sourceException.message.toString()
                ExoPlaybackException.TYPE_RENDERER -> error.rendererException.message.toString()
                ExoPlaybackException.TYPE_UNEXPECTED -> error.unexpectedException.message.toString()
                else -> "Unknown: $error"
            }
            mCallback?.onError("ExoPlayer error $what")
            if (error.type == ExoPlaybackException.TYPE_SOURCE) {
                sourceTypeErrorInfo.happenSourceError = true
                sourceTypeErrorInfo.seekToPositionWhenError = sourceTypeErrorInfo.seekToPosition
                sourceTypeErrorInfo.currPositionWhenError = currentStreamPosition
            }
        }

        override fun onPositionDiscontinuity(reason: Int) {
            // Nothing to do.
        }

        override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {
            // Nothing to do.
        }

        override fun onSeekProcessed() {
            // Nothing to do.
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            // Nothing to do.
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            // Nothing to do.
        }
    }
}