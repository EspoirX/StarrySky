package com.lzx.starrysky

import com.lzx.starrysky.common.IMediaConnection
import com.lzx.starrysky.control.PlayerControl
import com.lzx.starrysky.imageloader.ImageLoader
import com.lzx.starrysky.intercept.Interceptor
import com.lzx.starrysky.notification.NotificationConfig
import com.lzx.starrysky.notification.StarrySkyNotificationManager
import com.lzx.starrysky.playback.manager.IPlaybackManager
import com.lzx.starrysky.playback.offline.StarrySkyCacheManager
import com.lzx.starrysky.playback.player.Playback
import com.lzx.starrysky.playback.queue.MediaQueue
import com.lzx.starrysky.playback.queue.MediaQueueManager
import com.lzx.starrysky.provider.IMediaSourceProvider
import com.lzx.starrysky.provider.NormalModeProvider

/**
 * StarrySky 初始化配置类
 */
open class StarrySkyConfig internal constructor(
    builder: Builder
) : Cloneable {

    //媒体信息存储管理类
    @get:JvmName("mediaQueueProvider")
    val mediaQueueProvider: IMediaSourceProvider = builder.mediaQueueProvider

    //播放队列管理类
    @get:JvmName("mediaQueue")
    val mediaQueue: MediaQueue = builder.mediaQueue

    //通知栏开关
    @get:JvmName("isOpenNotification")
    val isOpenNotification = builder.isOpenNotification

    //缓存开关
    @get:JvmName("isOpenCache")
    val isOpenCache = builder.isOpenCache

    //缓存文件夹
    @get:JvmName("cacheDestFileDir")
    val cacheDestFileDir: String? = builder.cacheDestFileDir

    //超时时间设置
    @get:JvmName("httpConnectTimeout")
    val httpConnectTimeout: Long = builder.httpConnectTimeout

    @get:JvmName("httpReadTimeout")
    val httpReadTimeout: Long = builder.httpReadTimeout

    //是否跳过https
    @get:JvmName("skipSSLChain")
    val skipSSLChain = builder.skipSSLChain

    @get:JvmName("interceptors")
    val interceptors: MutableList<Interceptor> = builder.interceptors

    @get:JvmName("imageLoader")
    val imageLoader: ImageLoader = builder.imageLoader

    @get:JvmName("notificationFactory")
    val notificationFactory: StarrySkyNotificationManager.NotificationFactory? =
        builder.notificationFactory

    @get:JvmName("notificationManager")
    val notificationManager: StarrySkyNotificationManager? = builder.notificationManager

    @get:JvmName("notificationConfig")
    val notificationConfig: NotificationConfig? = builder.notificationConfig

    @get:JvmName("cacheManager")
    val cacheManager: StarrySkyCacheManager? = builder.cacheManager

    @get:JvmName("mediaConnection")
    val mediaConnection: IMediaConnection? = builder.mediaConnection

    @get:JvmName("playerControl")
    val playerControl: PlayerControl? = builder.playerControl

    @get:JvmName("playback")
    val playback: Playback? = builder.playback

    @get:JvmName("playbackManager")
    val playbackManager: IPlaybackManager? = builder.playbackManager

    constructor() : this(Builder())

    open fun newBuilder(): Builder = Builder(this)

    class Builder constructor() {
        var mediaQueueProvider: IMediaSourceProvider = NormalModeProvider()
        var mediaQueue: MediaQueue = MediaQueueManager(mediaQueueProvider)
        var isOpenNotification = false
        var isOpenCache = false
        var cacheDestFileDir: String? = null
        var httpConnectTimeout: Long = -1
        var httpReadTimeout: Long = -1
        var skipSSLChain = false
        var interceptors: MutableList<Interceptor> = mutableListOf()
        var imageLoader: ImageLoader = ImageLoader()
        var mediaConnection: IMediaConnection? = null
        var notificationFactory: StarrySkyNotificationManager.NotificationFactory? = null
        var notificationManager: StarrySkyNotificationManager? = null
        var notificationConfig: NotificationConfig? = null
        var cacheManager: StarrySkyCacheManager? = null
        var playback: Playback? = null
        var playerControl: PlayerControl? = null
        var playbackManager: IPlaybackManager? = null

        internal constructor(config: StarrySkyConfig) : this() {
            this.mediaQueueProvider = config.mediaQueueProvider
            this.mediaQueue = config.mediaQueue
            this.isOpenNotification = config.isOpenNotification
            this.isOpenCache = config.isOpenCache
            this.cacheDestFileDir = config.cacheDestFileDir
            this.httpConnectTimeout = config.httpConnectTimeout
            this.httpReadTimeout = config.httpReadTimeout
            this.skipSSLChain = config.skipSSLChain
            this.interceptors = config.interceptors
            this.imageLoader = config.imageLoader
            this.notificationFactory = config.notificationFactory
            this.notificationManager = config.notificationManager
            this.notificationConfig = config.notificationConfig
            this.cacheManager = config.cacheManager
            this.playback = config.playback
            this.mediaConnection = config.mediaConnection
            this.playerControl = config.playerControl
            this.playbackManager = config.playbackManager
        }

        fun build(): StarrySkyConfig? {
            return StarrySkyConfig(this)
        }
    }
}