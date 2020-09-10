package com.lzx.starrysky.service

import android.content.Context
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

class ServiceBridge(private val context: Context) : Binder() {

    val register = StarrySkyRegister()
    private var serviceCallback: PlaybackManager.PlaybackServiceCallback? = null
    private val interceptors: MutableList<StarrySkyInterceptor> = mutableListOf()
    var playerControl: PlayerControl? = null
    var notification: INotification? = null
    var imageLoader: ImageLoader? = null

    fun start() {
        //数据存储
        val sourceProvider = MediaSourceProvider()
        //图片加载
        imageLoader = ImageLoader(context)
        if (register.imageLoader == null) {
            imageLoader?.init(DefaultImageLoader())
        } else {
            imageLoader?.init(register.imageLoader!!)
        }
        //队列管理
        val mediaQueueManager = MediaQueueManager(sourceProvider, imageLoader)
        //缓存
        val cache = register.cache
        //播放器
        val player = if (register.playback == null) ExoPlayback(context, cache) else register.playback
        //拦截器
        val interceptorService = InterceptorService(interceptors)
        if (context is MusicService) {
            //通知栏
            val notificationManager = StarrySkyNotificationManager(register.isOpenNotification, register.notificationConfig, register.notification)
            notification = notificationManager.getNotification(context)
        }
        //播放管理
        val playbackManager = PlaybackManager(mediaQueueManager, player!!, interceptorService)
        playbackManager.registerNotification(notification)
        playbackManager.setServiceCallback(object : PlaybackManager.PlaybackServiceCallback {
            override fun onPlaybackStateUpdated(playbackStage: PlaybackStage) {
                playerControl?.onPlaybackStateUpdated(playbackStage)
                serviceCallback?.onPlaybackStateUpdated(playbackStage)
            }
        })
        //播放控制
        playerControl = PlayerControlImpl(sourceProvider, playbackManager)
    }

    /**
     * 添加拦截器
     */
    fun addInterceptor(interceptor: StarrySkyInterceptor) = apply {
        interceptors += interceptor
    }

    /**
     * 状态监听
     */
    fun setServiceCallback(serviceCallback: PlaybackManager.PlaybackServiceCallback?) {
        this.serviceCallback = serviceCallback
    }
}