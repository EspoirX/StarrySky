package com.lzx.starrysky.playback

import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.DefaultRenderersFactory.ExtensionRendererMode
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.rtmp.RtmpDataSourceFactory
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.source.rtsp.RtspMediaSource
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector.ParametersBuilder
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.Cache
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.util.Util
import com.lzx.starrysky.SongInfo
import com.lzx.starrysky.StarrySky
import com.lzx.starrysky.cache.ExoCache
import com.lzx.starrysky.cache.ICache
import com.lzx.starrysky.playback.Playback.Companion.STATE_BUFFERING
import com.lzx.starrysky.playback.Playback.Companion.STATE_ERROR
import com.lzx.starrysky.playback.Playback.Companion.STATE_IDLE
import com.lzx.starrysky.playback.Playback.Companion.STATE_PAUSED
import com.lzx.starrysky.playback.Playback.Companion.STATE_PLAYING
import com.lzx.starrysky.utils.isFLAC
import com.lzx.starrysky.utils.isRTMP
import com.lzx.starrysky.utils.orDef


/**
 * isAutoManagerFocus 是否让播放器自动管理焦点
 */
class ExoPlayback(
    val context: Context,
    private val cache: ICache?,
    private val isAutoManagerFocus: Boolean
) : Playback, FocusManager.OnFocusStateChangeListener {

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
    private var callback: Playback.Callback? = null
    private val eventListener by lazy { ExoPlayerEventListener() }
    private var sourceTypeErrorInfo: SourceTypeErrorInfo = SourceTypeErrorInfo()
    private var focusManager = FocusManager(context)
    private var hasError = false

    init {
        focusManager.listener = this
    }

    override fun playbackState(): Int {
        return if (player == null) {
            STATE_IDLE
        } else {
            when (player?.playbackState) {
                Player.STATE_IDLE -> STATE_IDLE //error或stop
                Player.STATE_BUFFERING -> STATE_BUFFERING
                Player.STATE_READY -> {
                    if (player?.playWhenReady == true) STATE_PLAYING else STATE_PAUSED
                }
                Player.STATE_ENDED -> STATE_IDLE
                else -> STATE_IDLE
            }
        }
    }

    override fun isPlaying(): Boolean = player?.playWhenReady == true

    override fun currentStreamPosition(): Long = player?.currentPosition.orDef()

    override fun bufferedPosition(): Long = player?.bufferedPosition.orDef()

    override fun duration(): Long = if (player?.duration.orDef() > 0) player?.duration.orDef() else 0

    override var currentMediaId: String = ""

    override fun setVolume(audioVolume: Float) {
        var volume = audioVolume
        if (volume < 0) {
            volume = 0f
        }
        if (volume > 1) {
            volume = 1f
        }
        player?.volume = volume
    }

    override fun getVolume(): Float = player?.volume ?: -1f

    override fun getCurrPlayInfo(): SongInfo? = currSongInfo

    override fun getAudioSessionId(): Int = player?.audioSessionId ?: 0

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
        StarrySky.log(
            "title = " + songInfo.songName +
                    " \n音频是否有改变 = " + mediaHasChanged +
                    " \n是否立即播放 = " + isPlayWhenReady +
                    " \nurl = " + songInfo.songUrl
        )

        //url 处理
        var source = songInfo.songUrl
        if (source.isEmpty()) {
            callback?.onPlaybackError(currSongInfo, "播放 url 为空")
            return
        }
        source = source.replace(" ".toRegex(), "%20") // Escape spaces for URL
        //代理url
        val proxyUrl = cache?.getProxyUrl(source, songInfo)
        source = if (proxyUrl.isNullOrEmpty()) source else proxyUrl
        mediaSource = createMediaSource(source)
        if (mediaSource == null) return
        if (mediaHasChanged || player == null) {
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
        StarrySky.log("isPlayWhenReady = $isPlayWhenReady")
        StarrySky.log("---------------------------------------")
        //如果准备好就播放
        if (isPlayWhenReady) {
            player?.playWhenReady = true
            hasError = false
            if (!isAutoManagerFocus) {
                player?.playbackState?.let { focusManager.updateAudioFocus(getPlayWhenReady(), it) }
            }
        }
    }

    private fun String.hasMediaSource(): Boolean =
        runCatching {
            Class.forName("com.google.android.exoplayer2.$this")
            return@runCatching true
        }.onFailure {
            it.printStackTrace()
        }.getOrElse { false }

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
        return when (type) {
            C.TYPE_DASH -> {
                if ("source.dash.DashMediaSource".hasMediaSource()) {
                    return DashMediaSource.Factory(dataSourceFactory!!).createMediaSource(MediaItem.fromUri(uri))
                } else {
                    throw IllegalStateException("has not DashMediaSource")
                }
            }
            C.TYPE_SS -> {
                if ("source.smoothstreaming.SsMediaSource".hasMediaSource()) {
                    return SsMediaSource.Factory(dataSourceFactory!!).createMediaSource(MediaItem.fromUri(uri))
                } else {
                    throw IllegalStateException("has not SsMediaSource")
                }
            }
            C.TYPE_HLS -> {
                if ("source.hls.HlsMediaSource".hasMediaSource()) {
                    return HlsMediaSource.Factory(dataSourceFactory!!).createMediaSource(MediaItem.fromUri(uri))
                } else {
                    throw IllegalStateException("has not HlsMediaSource")
                }
            }
            C.TYPE_RTSP -> {
                if ("source.rtsp.RtspMediaSource".hasMediaSource()) {
                    return RtspMediaSource.Factory().createMediaSource(MediaItem.fromUri(uri))
                } else {
                    throw IllegalStateException("has not RtspMediaSource")
                }
            }
            C.TYPE_OTHER -> {
                ProgressiveMediaSource.Factory(dataSourceFactory!!).createMediaSource(MediaItem.fromUri(uri))
            }
            TYPE_RTMP -> {
                if ("ext.rtmp.RtmpDataSourceFactory".hasMediaSource()) {
                    val factory = RtmpDataSourceFactory()
                    return ProgressiveMediaSource.Factory(factory).createMediaSource(MediaItem.fromUri(uri))
                } else {
                    throw IllegalStateException("has not RtmpDataSourceFactory")
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

            trackSelectorParameters = ParametersBuilder(context).build()
            trackSelector = DefaultTrackSelector(context)
            trackSelector?.parameters = trackSelectorParameters as DefaultTrackSelector.Parameters

            player = SimpleExoPlayer.Builder(context, renderersFactory)
                .setTrackSelector(trackSelector!!)
                .build()

            player?.addListener(eventListener)
            player?.setAudioAttributes(AudioAttributes.DEFAULT, isAutoManagerFocus)
            if (!isAutoManagerFocus) {
                player?.playbackState?.let { focusManager.updateAudioFocus(getPlayWhenReady(), it) }
            }
        }
    }

    @Synchronized
    private fun buildDataSourceFactory(type: Int): DataSource.Factory? {
        val userAgent = Util.getUserAgent(context, "StarrySky")
        val httpDataSourceFactory = DefaultHttpDataSourceFactory(
            userAgent,
            DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS,
            DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS,
            true
        )
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

    override fun stop() {
        player?.stop(true)
        player?.release()
        player?.removeListener(eventListener)
//        player?.removeAnalyticsListener(analyticsListener)
        player = null
        if (!isAutoManagerFocus) {
            focusManager.release()
        }
    }

    override fun pause() {
        player?.playWhenReady = false
        if (!isAutoManagerFocus) {
            player?.playbackState?.let { focusManager.updateAudioFocus(getPlayWhenReady(), it) }
        }
    }

    override fun seekTo(position: Long) {
        player?.seekTo(position)
        sourceTypeErrorInfo.seekToPosition = position
        if (sourceTypeErrorInfo.happenSourceError) {
            sourceTypeErrorInfo.seekToPositionWhenError = position
        }
    }

    override fun onFastForward(speed: Float) {
        player?.let {
            val currSpeed = it.playbackParameters.speed
            val currPitch = it.playbackParameters.pitch
            val newSpeed = currSpeed + speed
            it.setPlaybackParameters(PlaybackParameters(newSpeed, currPitch))
        }
    }

    override fun onRewind(speed: Float) {
        player?.let {
            val currSpeed = it.playbackParameters.speed
            val currPitch = it.playbackParameters.pitch
            var newSpeed = currSpeed - speed
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

    override fun skipToNext() {
        callback?.skipToNext()
    }

    override fun skipToPrevious() {
        callback?.skipToPrevious()
    }

    override fun setCallback(callback: Playback.Callback?) {
        this.callback = callback
    }

    private inner class ExoPlayerEventListener : Player.EventListener {

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            var newState = STATE_IDLE
            when (playbackState) {
                Player.STATE_IDLE -> {
                    //error和stop的时候会是这个状态，这里过滤掉error，避免重复回调
                    newState = if (hasError) STATE_ERROR else STATE_IDLE
                }
                Player.STATE_READY -> {
                    newState = if (player?.playWhenReady == true) STATE_PLAYING else STATE_PAUSED
                }
                Player.STATE_ENDED -> newState = STATE_IDLE
                Player.STATE_BUFFERING -> newState = STATE_BUFFERING
            }
            if (!hasError) {
                callback?.onPlayerStateChanged(currSongInfo, playWhenReady, newState)
            }
            if (playbackState == Player.STATE_READY) {
                sourceTypeErrorInfo.clear()
            }
            if (playbackState == Player.STATE_IDLE) {
                currentMediaId = ""
            }
        }

        override fun onPlayerError(error: ExoPlaybackException) {
            error.printStackTrace()
            hasError = true
            val what: String = when (error.type) {
                ExoPlaybackException.TYPE_SOURCE -> error.sourceException.message.toString()
                ExoPlaybackException.TYPE_RENDERER -> error.rendererException.message.toString()
                ExoPlaybackException.TYPE_UNEXPECTED -> error.unexpectedException.message.toString()
                else -> "Unknown: $error"
            }
            if (error.type == ExoPlaybackException.TYPE_SOURCE) {
                sourceTypeErrorInfo.happenSourceError = true
                sourceTypeErrorInfo.seekToPositionWhenError = sourceTypeErrorInfo.seekToPosition
                sourceTypeErrorInfo.currPositionWhenError = currentStreamPosition()
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

