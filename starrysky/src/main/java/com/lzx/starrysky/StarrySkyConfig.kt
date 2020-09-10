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

    @get:JvmName("cacheManager")
    val cache: ICache? = builder.cache

    @get:JvmName("interceptors")
    val interceptors: MutableList<StarrySkyInterceptor> = builder.interceptors

    @get:JvmName("imageLoaderStrategy")
    val imageLoader: ImageLoaderStrategy? = builder.imageLoaderStrategy

    @get:JvmName("playback")
    val playback: Playback? = builder.playback

    constructor() : this(Builder())

    open fun newBuilder(): Builder = Builder(this)

    class Builder constructor() {
        internal var isOpenNotification = false
        internal var notificationConfig: NotificationConfig? = null
        internal var notificationFactory: StarrySkyNotificationManager.NotificationFactory? = null
        internal var isOpenCache = false
        internal var cacheDestFileDir: String? = null
        internal var cache: ICache? = null
        internal val interceptors: MutableList<StarrySkyInterceptor> = mutableListOf()
        internal var imageLoaderStrategy: ImageLoaderStrategy? = null
        internal var playback: Playback? = null

        internal constructor(config: StarrySkyConfig) : this() {
            this.isOpenNotification = config.isOpenNotification
            this.notificationConfig = config.notificationConfig
            this.notificationFactory = config.notificationFactory
            this.isOpenCache = config.isOpenCache
            this.cacheDestFileDir = config.cacheDestFileDir
            this.interceptors += config.interceptors
            this.imageLoaderStrategy = config.imageLoader
            this.playback = config.playback
        }

        fun isOpenNotification(isOpenNotification: Boolean) = apply {
            this.isOpenNotification = isOpenNotification
        }

        fun setNotificationConfig(notificationConfig: NotificationConfig) = apply {
            this.notificationConfig = notificationConfig
        }

        fun setNotificationFactory(
            notificationFactory: StarrySkyNotificationManager.NotificationFactory
        ) = apply {
            this.notificationFactory = notificationFactory
        }

        fun isOpenCache(isOpenCache: Boolean) = apply {
            this.isOpenCache = isOpenCache
        }

        fun setCacheDestFileDir(cacheDestFileDir: String) = apply {
            this.cacheDestFileDir = cacheDestFileDir
        }

        fun setCache(cache: ICache) = apply {
            this.cache = cache
        }

        fun addInterceptor(interceptor: StarrySkyInterceptor) = apply {
            interceptors += interceptor
        }

        fun setImageLoader(imageLoader: ImageLoaderStrategy) = apply { this.imageLoaderStrategy = imageLoader }

        fun setPlayback(playback: Playback) = apply { this.playback = playback }

        fun build(): StarrySkyConfig {
            return StarrySkyConfig(this)
        }
    }
}