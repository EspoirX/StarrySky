package com.lzx.starrysky.playback

import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.DefaultRenderersFactory.ExtensionRendererMode
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.MediaSourceFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector.ParametersBuilder
import com.google.android.exoplayer2.trackselection.TrackSelection
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.Cache
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.util.Util
import com.lzx.starrysky.SongInfo
import com.lzx.starrysky.cache.ExoCache
import com.lzx.starrysky.cache.ICache
import com.lzx.starrysky.isRefrain
import com.lzx.starrysky.playback.Playback.Companion.STATE_BUFFERING
import com.lzx.starrysky.playback.Playback.Companion.STATE_IDLE
import com.lzx.starrysky.playback.Playback.Companion.STATE_PAUSED
import com.lzx.starrysky.playback.Playback.Companion.STATE_PLAYING
import com.lzx.starrysky.utils.StarrySkyUtils
import com.lzx.starrysky.utils.isFLAC
import com.lzx.starrysky.utils.isRTMP

/**
 * isAutoManagerFocus 是否让播放器自动管理焦点
 */
class ExoPlayback(val context: Context,
                  private val cache: ICache?,
                  private val isAutoManagerFocus: Boolean)
    : Playback, FocusManager.OnFocusStateChangeListener {

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
    private var exoPlayerNullIsStopped = false
    private var callback: Playback.Callback? = null
    private val mEventListener by lazy { ExoPlayerEventListener() }
    private var sourceTypeErrorInfo: SourceTypeErrorInfo = SourceTypeErrorInfo()
    private var focusManager = FocusManager(context)

    init {
        focusManager.listener = this
    }

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
        get() = player?.playWhenReady == true

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

    private fun getPlayWhenReady() = player?.playWhenReady ?: false

    override fun play(songInfo: SongInfo, isPlayWhenReady: Boolean) {
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
            player?.setMediaSource(mediaSource!!)
            player?.prepare()
            if (!isAutoManagerFocus) {
                focusManager.updateAudioFocus(getPlayWhenReady(), STATE_BUFFERING)
            }
        }
        //当错误发生时，如果还播放同一首歌，
        //这时候需要重新加载一下，并且吧进度 seekTo 到出错的地方
        if (sourceTypeErrorInfo.happenSourceError && !mediaHasChanged) {
            player?.setMediaSource(mediaSource!!)
            player?.prepare()
            if (!isAutoManagerFocus) {
                focusManager.updateAudioFocus(getPlayWhenReady(), STATE_BUFFERING)
            }
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
            if (!isAutoManagerFocus) {
                player?.playbackState?.let { focusManager.updateAudioFocus(getPlayWhenReady(), it) }
            }
        }
        if (songInfo.isRefrain()) {
            StarrySkyUtils.log("播放伴奏 = ${songInfo.songId}")
        }
    }

    @Synchronized
    private fun createMediaSource(source: String): MediaSource {
        val uri = Uri.parse(source)
        val isRtmpSource = source.isRTMP()
        val isFlacSource = source.isFLAC()
        val type = when {
            isRtmpSource -> TYPE_RTMP
            isFlacSource -> TYPE_FLAC
            else -> Util.inferContentType(uri, null)

        }
        dataSourceFactory = buildDataSourceFactory(type)
        val basePath = "com.google.android.exoplayer2.source."
        return when (type) {
            C.TYPE_DASH -> {
                try {
                    val clazz = Class.forName(basePath + "dash.DashMediaSource" + "\$Factory")
                    val constructors = clazz.getConstructor(DataSource.Factory::class.java)
                    constructors.isAccessible = true
                    val factory = constructors.newInstance(dataSourceFactory) as MediaSourceFactory
                    return factory.createMediaSource(MediaItem.fromUri(uri))
                } catch (e: ClassNotFoundException) {
                    throw RuntimeException("Error instantiating ClassNotFoundException DashMediaSource", e)
                } catch (e: Exception) {
                    throw RuntimeException("Error instantiating DASH extension", e)
                }
            }
            C.TYPE_SS -> {
                try {
                    val clazz = Class.forName(basePath + "smoothstreaming.SsMediaSource" + "\$Factory")
                    val constructors = clazz.getConstructor(DataSource.Factory::class.java)
                    constructors.isAccessible = true
                    val factory = constructors.newInstance(dataSourceFactory) as MediaSourceFactory
                    return factory.createMediaSource(MediaItem.fromUri(uri))
                } catch (e: ClassNotFoundException) {
                    throw RuntimeException("Error instantiating ClassNotFoundException SsMediaSource", e)
                } catch (e: Exception) {
                    throw RuntimeException("Error instantiating SS extension", e)
                }
            }
            C.TYPE_HLS -> {
                try {
                    val clazz = Class.forName(basePath + "hls.HlsMediaSource" + "\$Factory")
                    val constructors = clazz.getConstructor(DataSource.Factory::class.java)
                    constructors.isAccessible = true
                    val factory = constructors.newInstance(dataSourceFactory) as MediaSourceFactory
                    return factory.createMediaSource(MediaItem.fromUri(uri))
                } catch (e: ClassNotFoundException) {
                    throw RuntimeException("Error instantiating ClassNotFoundException HlsMediaSource", e)
                } catch (e: Exception) {
                    throw RuntimeException("Error instantiating HLS extension", e)
                }
            }
            C.TYPE_OTHER -> {
                ProgressiveMediaSource.Factory(dataSourceFactory!!).createMediaSource(MediaItem.fromUri(uri))
            }
            TYPE_RTMP -> {
                try {
                    val clazz = Class.forName("com.google.android.exoplayer2.ext.rtmp.RtmpDataSourceFactory")
                    val factory: DataSource.Factory = clazz.newInstance() as DataSource.Factory
                    return ProgressiveMediaSource.Factory(factory).createMediaSource(MediaItem.fromUri(uri))
                } catch (e: ClassNotFoundException) {
                    throw RuntimeException("Error instantiating ClassNotFoundException RtmpDataSourceFactory", e)
                } catch (e: Exception) {
                    throw RuntimeException("Error instantiating RTMP extension", e)
                }
            }
            TYPE_FLAC -> {
                val extractorsFactory = DefaultExtractorsFactory()
                ProgressiveMediaSource.Factory(dataSourceFactory!!, extractorsFactory)
                    .createMediaSource(MediaItem.fromUri(uri))
            }
            else -> throw IllegalStateException("Unsupported type: $type")
        }
    }

    @Synchronized
    private fun createExoPlayer() {
        if (player == null) {
            @ExtensionRendererMode val extensionRendererMode = DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER
            val renderersFactory = DefaultRenderersFactory(context)
                .setExtensionRendererMode(extensionRendererMode)

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
            player?.setAudioAttributes(AudioAttributes.DEFAULT, isAutoManagerFocus)
            if (!isAutoManagerFocus) {
                player?.playbackState?.let { focusManager.updateAudioFocus(getPlayWhenReady(), it) }
            }
        }
    }

    @Synchronized
    private fun buildDataSourceFactory(type: Int): DataSource.Factory? {
        val userAgent = Util.getUserAgent(context, "StarrySky")
        val httpDataSourceFactory = DefaultHttpDataSourceFactory(userAgent,
            DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS,
            DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS,
            true)
        return if (cache?.isOpenCache() == true && cache is ExoCache && !type.isStreamingType()) {
            val upstreamFactory = DefaultDataSourceFactory(context, httpDataSourceFactory)
            buildCacheDataSource(upstreamFactory, cache.getDownloadCache())
        } else {
            DefaultDataSourceFactory(context, httpDataSourceFactory)
        }
    }

    private fun Int.isStreamingType(): Boolean {
        return this == TYPE_RTMP || this == C.TYPE_DASH || this == C.TYPE_SS || this == C.TYPE_HLS
    }

    @Synchronized
    private fun buildCacheDataSource(upstreamFactory: DataSource.Factory?, cache: Cache?): CacheDataSource.Factory? {
        return cache?.let {
            CacheDataSource.Factory()
                .setCache(it)
                .setUpstreamDataSourceFactory(upstreamFactory)
                .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
        }
    }

    private fun releaseResources(releasePlayer: Boolean) {
        if (releasePlayer) {
            player?.release()
            player?.removeListener(mEventListener)
            player = null
            exoPlayerNullIsStopped = true
            if (!isAutoManagerFocus) {
                focusManager.release()
            }
        }
    }

    override fun stop() {
        releaseResources(true)
    }

    override fun pause() {
        player?.playWhenReady = false
        if (!isAutoManagerFocus) {
            player?.playbackState?.let { focusManager.updateAudioFocus(getPlayWhenReady(), it) }
        }
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
            error.printStackTrace()
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

    override fun focusStateChange(info: FocusInfo) {
        if (isAutoManagerFocus) {
            return
        }
        callback?.onFocusStateChange(FocusInfo(currSongInfo, info.audioFocusState, info.playerCommand, info.volume))
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

