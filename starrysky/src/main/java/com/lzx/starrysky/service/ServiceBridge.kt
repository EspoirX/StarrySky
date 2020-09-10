package com.lzx.starrysky.service

import android.os.Binder
import com.lzx.starrysky.control.PlayerControl
import com.lzx.starrysky.control.PlayerControlImpl
import com.lzx.starrysky.imageloader.DefaultImageLoader
import com.lzx.starrysky.imageloader.ImageLoader
import com.lzx.starrysky.intercept.InterceptorService
import com.lzx.starrysky.intercept.StarrySkyInterceptor
import com.lzx.starrysky.notification.INotification
import com.lzx.starrysky.notification.StarrySkyNotificationManager
import com.lzx.starrysky.playback.ExoPlayback
import com.lzx.starrysky.playback.MediaQueueManager
import com.lzx.starrysky.playback.MediaSourceProvider
import com.lzx.starrysky.playback.PlaybackManager
import com.lzx.starrysky.playback.PlaybackStage
import java.lang.ref.WeakReference

class ServiceBridge(private val service: WeakReference<MusicService>) : Binder() {

    val register = StarrySkyRegister()
    private var serviceCallback: PlaybackManager.PlaybackServiceCallback? = null
    private val interceptors: MutableList<StarrySkyInterceptor> = mutableListOf()
    var playerControl: PlayerControl? = null
    var notification: INotification? = null
    var imageLoader: ImageLoader? = null

    fun start() {
        val context = service.get() ?: return
        val sourceProvider = MediaSourceProvider()
        imageLoader = ImageLoader(context)
        if (register.imageLoader == null) {
            imageLoader?.init(DefaultImageLoader())
        } else {
            imageLoader?.init(register.imageLoader!!)
        }
        val mediaQueueManager = MediaQueueManager(sourceProvider, imageLoader)
        val cache = register.cache
        val player = if (register.playback == null) ExoPlayback(context, cache) else register.playback
        val interceptorService = InterceptorService(interceptors)
        val notificationManager = StarrySkyNotificationManager(register.isOpenNotification, register.notificationConfig, register.notification)
        notification = notificationManager.getNotification(context)
        val playbackManager = PlaybackManager(mediaQueueManager, player!!, interceptorService)
        playbackManager.registerNotification(notification)
        playbackManager.setServiceCallback(object : PlaybackManager.PlaybackServiceCallback {
            override fun onPlaybackStateUpdated(playbackStage: PlaybackStage) {
                playerControl?.onPlaybackStateUpdated(playbackStage)
                serviceCallback?.onPlaybackStateUpdated(playbackStage)
            }
        })
        playerControl = PlayerControlImpl(sourceProvider, playbackManager)
    }

    fun addInterceptor(interceptor: StarrySkyInterceptor) = apply {
        interceptors += interceptor
    }

    fun setServiceCallback(serviceCallback: PlaybackManager.PlaybackServiceCallback?) {
        this.serviceCallback = serviceCallback
    }
}