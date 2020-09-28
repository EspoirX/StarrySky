package com.lzx.starrysky

import com.lzx.starrysky.cache.ICache
import com.lzx.starrysky.imageloader.ImageLoaderStrategy
import com.lzx.starrysky.intercept.StarrySkyInterceptor
import com.lzx.starrysky.notification.NotificationConfig
import com.lzx.starrysky.notification.StarrySkyNotificationManager
import com.lzx.starrysky.playback.Playback

/**
 * StarrySky 初始化配置类
 */
open class StarrySkyConfig internal constructor(
    builder: Builder
) : Cloneable {

    //通知栏开关
    @get:JvmName("isOpenNotification")
    val isOpenNotification = builder.isOpenNotification

    //通知栏配置
    @get:JvmName("notificationConfig")
    val notificationConfig: NotificationConfig? = builder.notificationConfig

    @get:JvmName("notificationFactory")
    val notificationFactory: StarrySkyNotificationManager.NotificationFactory? =
        builder.notificationFactory

    //缓存开关
    @get:JvmName("isOpenCache")
    val isOpenCache = builder.isOpenCache

    //缓存文件夹
    @get:JvmName("cacheDestFileDir")
    val cacheDestFileDir: String? = builder.cacheDestFileDir

    //缓存实现
    @get:JvmName("cacheManager")
    val cache: ICache? = builder.cache

    //最大缓存size
    var cacheMaxBytes: Long = builder.cacheMaxBytes

    //拦截器
    @get:JvmName("interceptors")
    val interceptors: MutableList<StarrySkyInterceptor> = builder.interceptors

    //图片加载器
    @get:JvmName("imageLoaderStrategy")
    val imageLoader: ImageLoaderStrategy? = builder.imageLoaderStrategy

    //播放器实现
    @get:JvmName("playback")
    val playback: Playback? = builder.playback

    //是否需要后台服务
    @get:JvmName("isUserService")
    val isUserService: Boolean = builder.isUserService

    //是否让播放器自动管理焦点
    @get:JvmName("isAutoManagerFocus")
    val isAutoManagerFocus: Boolean = builder.isAutoManagerFocus

    //设置焦点管理监听
    @get:JvmName("focusChangeListener")
    val focusChangeListener: AudioFocusChangeListener? = builder.focusChangeListener

    //是否需要创建副歌播放器
    @get:JvmName("isCreateRefrainPlayer")
    val isCreateRefrainPlayer: Boolean = builder.isCreateRefrainPlayer

    constructor() : this(Builder())

    open fun newBuilder(): Builder = Builder(this)

    class Builder constructor() {
        internal var isOpenNotification = false
        internal var notificationConfig: NotificationConfig? = null
        internal var notificationFactory: StarrySkyNotificationManager.NotificationFactory? = null
        internal var isOpenCache = false
        internal var cacheDestFileDir: String? = null
        internal var cache: ICache? = null
        internal var cacheMaxBytes: Long = 512 * 1024 * 1024
        internal val interceptors: MutableList<StarrySkyInterceptor> = mutableListOf()
        internal var imageLoaderStrategy: ImageLoaderStrategy? = null
        internal var playback: Playback? = null
        internal var isUserService: Boolean = true
        internal var isAutoManagerFocus: Boolean = true
        internal var focusChangeListener: AudioFocusChangeListener? = null
        internal var isCreateRefrainPlayer: Boolean = false

        internal constructor(config: StarrySkyConfig) : this() {
            this.isOpenNotification = config.isOpenNotification
            this.notificationConfig = config.notificationConfig
            this.notificationFactory = config.notificationFactory
            this.isOpenCache = config.isOpenCache
            this.cacheDestFileDir = config.cacheDestFileDir
            this.cacheMaxBytes = config.cacheMaxBytes
            this.interceptors += config.interceptors
            this.imageLoaderStrategy = config.imageLoader
            this.playback = config.playback
            this.isUserService = config.isUserService
            this.isAutoManagerFocus = config.isAutoManagerFocus
            this.focusChangeListener = config.focusChangeListener
            this.isCreateRefrainPlayer = config.isCreateRefrainPlayer
        }

        fun isOpenNotification(isOpenNotification: Boolean) = apply { this.isOpenNotification = isOpenNotification }

        fun setNotificationConfig(config: NotificationConfig) = apply { this.notificationConfig = config }

        fun setNotificationFactory(factory: StarrySkyNotificationManager.NotificationFactory) = apply { this.notificationFactory = factory }

        fun isOpenCache(isOpenCache: Boolean) = apply { this.isOpenCache = isOpenCache }

        fun setCacheDestFileDir(cacheDestFileDir: String) = apply { this.cacheDestFileDir = cacheDestFileDir }

        fun setCacheMaxBytes(maxBytes: Long) = apply { this.cacheMaxBytes = maxBytes }

        fun setCache(cache: ICache) = apply { this.cache = cache }

        fun addInterceptor(interceptor: StarrySkyInterceptor) = apply { interceptors += interceptor }

        fun setImageLoader(imageLoader: ImageLoaderStrategy) = apply { this.imageLoaderStrategy = imageLoader }

        fun setPlayback(playback: Playback) = apply { this.playback = playback }

        fun isUserService(isUserService: Boolean) = apply { this.isUserService = isUserService }

        fun isAutoManagerFocus(isAutoManagerFocus: Boolean) = apply { this.isAutoManagerFocus = isAutoManagerFocus }

        fun setOnAudioFocusChangeListener(listener: AudioFocusChangeListener) = apply { this.focusChangeListener = listener }

        fun isCreateRefrainPlayer(isCreateRefrainPlayer: Boolean) = apply { this.isCreateRefrainPlayer = isCreateRefrainPlayer }

        fun build(): StarrySkyConfig {
            return StarrySkyConfig(this)
        }
    }
}


interface AudioFocusChangeListener {
    /**
     * state 定义见 FocusAndLockManager 类
     */
    fun onAudioFocusChange(songInfo: SongInfo?, state: Int)
}