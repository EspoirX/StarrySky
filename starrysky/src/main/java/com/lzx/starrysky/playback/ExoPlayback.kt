package com.lzx.starrysky.playback

import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.DefaultRenderersFactory.ExtensionRendererMode
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.rtmp.RtmpDataSourceFactory
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector.ParametersBuilder
import com.google.android.exoplayer2.trackselection.TrackSelection
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.upstream.FileDataSource
import com.google.android.exoplayer2.upstream.cache.Cache
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.lzx.starrysky.SongInfo
import com.lzx.starrysky.cache.ExoCache
import com.lzx.starrysky.cache.ICache
import com.lzx.starrysky.playback.Playback.Companion.STATE_BUFFERING
import com.lzx.starrysky.playback.Playback.Companion.STATE_IDLE
import com.lzx.starrysky.playback.Playback.Companion.STATE_PAUSED
import com.lzx.starrysky.playback.Playback.Companion.STATE_PLAYING
import com.lzx.starrysky.utils.StarrySkyUtils
import java.util.Locale

class ExoPlayback(val context: Context, val cache: ICache?) : Playback {

    companion object {
        const val TYPE_RTMP = 4
        const val TYPE_FLAC = 5
    }

    private var dataSourceFactory: DataSource.Factory? = null
    private var player: SimpleExoPlayer? = null
    private var mediaSource: MediaSource? = null
    private var trackSelector: DefaultTrackSelector? = null
    private var trackSelectorParameters: DefaultTrackSelector.Parameters? = null

    private var currSongInfo: SongInfo? = null
    private var playOnFocusGain: Boolean = false
    private var exoPlayerNullIsStopped = false
    private var callback: Playback.Callback? = null
    private val mEventListener by lazy { ExoPlayerEventListener() }
    private var sourceTypeErrorInfo: SourceTypeErrorInfo = SourceTypeErrorInfo()

    override val playbackState: Int
        get() {
            return if (player == null) {
                if (exoPlayerNullIsStopped) STATE_PAUSED else STATE_IDLE
            } else {
                when (player?.playbackState) {
                    Player.STATE_IDLE -> STATE_PAUSED
                    Player.STATE_BUFFERING -> STATE_BUFFERING
                    Player.STATE_READY -> {
                        if (player?.playWhenReady == true) STATE_PLAYING else STATE_PAUSED
                    }
                    Player.STATE_ENDED -> STATE_IDLE
                    else -> STATE_IDLE
                }
            }
        }

    override val isConnected: Boolean
        get() = true

    override val isPlaying: Boolean
        get() = playOnFocusGain || player?.playWhenReady == true

    override val currentStreamPosition: Long
        get() = player?.currentPosition ?: 0

    override val bufferedPosition: Long
        get() = player?.bufferedPosition ?: 0

    override val duration: Long
        get() = player?.duration ?: -1

    override var currentMediaId: String = ""

    override var volume: Float
        get() = player?.volume ?: -1f
        set(value) {
            player?.volume = value
        }

    override val currPlayInfo: SongInfo?
        get() = currSongInfo

    override val audioSessionId: Int
        get() = player?.audioSessionId ?: 0

    override fun stop() {
        releaseResources(true)
    }

    override fun play(songInfo: SongInfo, isPlayWhenReady: Boolean) {
        playOnFocusGain = true
        val mediaId = songInfo.songId
        if (mediaId.isEmpty()) {
            return
        }
        currSongInfo = songInfo
        val mediaHasChanged = mediaId != currentMediaId
        if (mediaHasChanged) {
            currentMediaId = mediaId
        }
        StarrySkyUtils.log(
            "title = " + songInfo.songName +
                " \n音频是否有改变 = " + mediaHasChanged +
                " \n是否立即播放 = " + isPlayWhenReady +
                " \nurl = " + songInfo.songUrl)

        //url 处理
        var source = songInfo.songUrl
        if (source.isEmpty()) {
            callback?.onPlaybackError(currPlayInfo, "播放 url 为空")
            return
        }
        source = source.replace(" ".toRegex(), "%20") // Escape spaces for URL
        //代理url
        val proxyUrl = cache?.getProxyUrl(source)
        source = if (proxyUrl.isNullOrEmpty()) source else proxyUrl
        mediaSource = createMediaSource(source)
        if (mediaSource == null) return
        if (mediaHasChanged || player == null) {
            releaseResources(false)  // release everything except the player
            //创建播放器实例
            createExoPlayer()
            player?.prepare(mediaSource!!)
        }
        //当错误发生时，如果还播放同一首歌，
        //这时候需要重新加载一下，并且吧进度 seekTo 到出错的地方
        if (sourceTypeErrorInfo.happenSourceError && !mediaHasChanged) {
            player?.prepare(mediaSource!!)
            if (sourceTypeErrorInfo.currPositionWhenError != 0L) {
                if (sourceTypeErrorInfo.seekToPositionWhenError != 0L) {
                    player?.seekTo(sourceTypeErrorInfo.seekToPositionWhenError)
                } else {
                    player?.seekTo(sourceTypeErrorInfo.currPositionWhenError)
                }
            }
        }
        StarrySkyUtils.log("isPlayWhenReady = $isPlayWhenReady")
        StarrySkyUtils.log("---------------------------------------")
        //如果准备好就播放
        if (isPlayWhenReady) {
            player?.playWhenReady = true
        }
    }

    private fun createMediaSource(source: String): MediaSource {
        val uri = Uri.parse(source)
        val isRtmpSource = source.toLowerCase(Locale.getDefault()).startsWith("rtmp://")
        val isFlacSource = source.toLowerCase(Locale.getDefault()).endsWith(".flac")
        val type = when {
            isRtmpSource -> TYPE_RTMP
            isFlacSource -> TYPE_FLAC
            else -> Util.inferContentType(uri, null)

        }
        dataSourceFactory = buildDataSourceFactory()
        return when (type) {
            C.TYPE_DASH -> DashMediaSource.Factory(dataSourceFactory!!)
                .createMediaSource(uri)
            C.TYPE_SS -> SsMediaSource.Factory(dataSourceFactory!!)
                .createMediaSource(uri)
            C.TYPE_HLS -> HlsMediaSource.Factory(dataSourceFactory!!)
                .createMediaSource(uri)
            C.TYPE_OTHER -> ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(uri)
            TYPE_RTMP -> {
                ProgressiveMediaSource.Factory(RtmpDataSourceFactory())
                    .createMediaSource(uri)
            }
            TYPE_FLAC -> {
                ProgressiveMediaSource.Factory(RtmpDataSourceFactory())
                    .createMediaSource(uri)
            }
            else -> throw IllegalStateException("Unsupported type: $type")
        }
    }

    private fun createExoPlayer() {
        if (player == null) {
            @ExtensionRendererMode val extensionRendererMode = DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER
            val renderersFactory = DefaultRenderersFactory(context).setExtensionRendererMode(extensionRendererMode)

            val builder = ParametersBuilder(context)
            if (Util.SDK_INT >= 21) {
                builder.setTunnelingAudioSessionId(C.generateAudioSessionIdV21(context))
            }
            trackSelectorParameters = builder.build()
            val trackSelectionFactory: TrackSelection.Factory = AdaptiveTrackSelection.Factory()
            trackSelector = DefaultTrackSelector(context, trackSelectionFactory)
            trackSelector?.parameters = trackSelectorParameters as DefaultTrackSelector.Parameters

            player = SimpleExoPlayer.Builder(context, renderersFactory)
                .setTrackSelector(trackSelector!!)
                .build()
            player?.addListener(mEventListener)
            player?.setAudioAttributes(AudioAttributes.DEFAULT, true)
        }
    }

    private fun buildDataSourceFactory(): DataSource.Factory? {
        val userAgent = Util.getUserAgent(context, "StarrySky")
        val upstreamFactory = DefaultDataSourceFactory(context, DefaultHttpDataSourceFactory(userAgent))
        return if (cache?.isOpenCache() == true && cache is ExoCache) {
            buildReadOnlyCacheDataSource(upstreamFactory, cache.getDownloadCache())
        } else {
            DefaultDataSourceFactory(context, DefaultHttpDataSourceFactory(userAgent))
        }
    }

    private fun buildReadOnlyCacheDataSource(
        upstreamFactory: DataSource.Factory?, cache: Cache?): CacheDataSourceFactory? {
        return CacheDataSourceFactory(
            cache,
            upstreamFactory,
            FileDataSource.Factory(),  /* cacheWriteDataSinkFactory= */
            null,
            CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR,  /* eventListener= */
            null)
    }

    private fun releaseResources(releasePlayer: Boolean) {
        if (releasePlayer) {
            player?.release()
            player?.removeListener(mEventListener)
            player = null
            exoPlayerNullIsStopped = true
            playOnFocusGain = false
        }
    }

    override fun pause() {
        player?.playWhenReady = false
        releaseResources(false)
    }

    override fun seekTo(position: Long) {
        player?.seekTo(position)
        sourceTypeErrorInfo.seekToPosition = position
        if (sourceTypeErrorInfo.happenSourceError) {
            sourceTypeErrorInfo.seekToPositionWhenError = position
        }
    }

    override fun onFastForward() {
        player?.let {
            val currSpeed = it.playbackParameters.speed
            val currPitch = it.playbackParameters.pitch
            val newSpeed = currSpeed + 0.5f
            it.setPlaybackParameters(PlaybackParameters(newSpeed, currPitch))
        }
    }

    override fun onRewind() {
        player?.let {
            val currSpeed = it.playbackParameters.speed
            val currPitch = it.playbackParameters.pitch
            var newSpeed = currSpeed - 0.5f
            if (newSpeed <= 0) {
                newSpeed = 0f
            }
            it.setPlaybackParameters(PlaybackParameters(newSpeed, currPitch))
        }
    }

    override fun onDerailleur(refer: Boolean, multiple: Float) {
        player?.let {
            val currSpeed = it.playbackParameters.speed
            val currPitch = it.playbackParameters.pitch
            val newSpeed = if (refer) currSpeed * multiple else multiple
            if (newSpeed > 0) {
                it.setPlaybackParameters(PlaybackParameters(newSpeed, currPitch))
            }
        }
    }

    override fun getPlaybackSpeed(): Float {
        return player?.playbackParameters?.speed ?: 1.0f
    }

    override fun setCallback(callback: Playback.Callback) {
        this.callback = callback
    }

    private inner class ExoPlayerEventListener : Player.EventListener {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            var newState = STATE_IDLE
            when (playbackState) {
                Player.STATE_IDLE -> newState = STATE_IDLE
                Player.STATE_READY -> {
                    newState = if (player?.playWhenReady == true) STATE_PLAYING else STATE_PAUSED
                }
                Player.STATE_ENDED -> newState = STATE_IDLE
                Player.STATE_BUFFERING -> newState = STATE_BUFFERING
            }
            callback?.onPlayerStateChanged(currSongInfo, playWhenReady, newState)
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
            if (error.type == ExoPlaybackException.TYPE_SOURCE) {
                sourceTypeErrorInfo.happenSourceError = true
                sourceTypeErrorInfo.seekToPositionWhenError = sourceTypeErrorInfo.seekToPosition
                sourceTypeErrorInfo.currPositionWhenError = currentStreamPosition
            }
            callback?.onPlaybackError(currSongInfo, "ExoPlayer error $what")
        }
    }
}

/**
 * 发生错误时保存的信息
 */
class SourceTypeErrorInfo {
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

