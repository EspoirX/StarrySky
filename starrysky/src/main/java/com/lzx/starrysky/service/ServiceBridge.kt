package com.lzx.starrysky.service

import android.os.Binder
import com.lzx.starrysky.control.PlayerControl
import com.lzx.starrysky.control.PlayerControlImpl
import com.lzx.starrysky.imageloader.DefaultImageLoader
import com.lzx.starrysky.imageloader.ImageLoader
import com.lzx.starrysky.intercept.InterceptorService
import com.lzx.starrysky.intercept.StarrySkyInterceptor
import com.lzx.starrysky.playback.ExoPlayback
import com.lzx.starrysky.playback.MediaQueueManager
import com.lzx.starrysky.playback.MediaSourceProvider
import com.lzx.starrysky.playback.PlaybackManager
import com.lzx.starrysky.playback.PlaybackStage
import java.lang.ref.WeakReference

class ServiceBridge(private val service: WeakReference<MusicService>) : Binder() {

    val register = StarrySkyRegister()
    private val interceptors: MutableList<StarrySkyInterceptor> = mutableListOf()
    var playerControl: PlayerControl? = null

    fun start() {
        val context = service.get()?.applicationContext ?: return
        val sourceProvider = MediaSourceProvider()
        val imageLoader = ImageLoader(context)
        if (register.imageLoader == null) {
            imageLoader.init(DefaultImageLoader())
        } else {
            imageLoader.init(register.imageLoader!!)
        }
        val mediaQueueManager = MediaQueueManager(sourceProvider, imageLoader)
        val cache = register.cache
        val player = if (register.playback == null) ExoPlayback(context, cache) else register.playback
        val interceptorService = InterceptorService(interceptors)
        val playbackManager = PlaybackManager(mediaQueueManager, player!!, interceptorService)
        playbackManager.setServiceCallback(object : PlaybackManager.PlaybackServiceCallback {
            override fun onPlaybackStateUpdated(playbackStage: PlaybackStage) {
                playerControl?.onPlaybackStateUpdated(playbackStage)
            }
        })
        playerControl = PlayerControlImpl(sourceProvider, playbackManager)
    }

    fun addInterceptor(interceptor: StarrySkyInterceptor) = apply {
        interceptors += interceptor
    }
}