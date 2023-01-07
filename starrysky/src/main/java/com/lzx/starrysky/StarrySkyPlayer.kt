package com.lzx.starrysky

import android.app.Activity
import com.lzx.starrysky.cache.ICache
import com.lzx.starrysky.control.PlayerControl
import com.lzx.starrysky.notification.INotification
import com.lzx.starrysky.notification.NotificationConfig
import com.lzx.starrysky.notification.NotificationManager
import com.lzx.starrysky.playback.ExoPlayback
import com.lzx.starrysky.playback.Playback
import com.lzx.starrysky.service.MusicServiceBinder

class StarrySkyPlayer(private var userGlobalConfig: Boolean = true) {


    companion object {
        @JvmOverloads
        @JvmStatic
        fun create(userGlobalConfig: Boolean = true): StarrySkyPlayer {
            return StarrySkyPlayer(userGlobalConfig)
        }
    }

    //通知栏相关
    private var isOpenNotification: Boolean = false
    private var notificationType: Int = INotification.SYSTEM_NOTIFICATION
    private var notificationConfig: NotificationConfig? = null
    private var notificationFactory: NotificationManager.NotificationFactory? = null

    //播放器缓存
    private var isOpenCache = false
    private var cacheDestFileDir: String = ""
    private var cacheMaxBytes: Long = 512 * 1024 * 1024
    private var playerCache: ICache? = null

    //是否自动焦点管理
    private var isAutoManagerFocus: Boolean = true

    //播放器
    private var playback: Playback? =
        StarrySkyInstall.globalContext?.let { ExoPlayback(it, playerCache, isAutoManagerFocus) }

    //播放控制
    private var playerControl: PlayerControl? = null

    /**
     * 通知栏开关，打开则显示通知栏，关闭则不显示
     */
    fun setNotificationSwitch(isOpenNotification: Boolean) = apply {
        if (userGlobalConfig) {
            this.isOpenNotification = StarrySkyInstall.isOpenNotification
            return@apply
        }
        this.isOpenNotification = isOpenNotification
    }

    /**
     * 通知栏类型
     * INotification.SYSTEM_NOTIFICATION
     * INotification.CUSTOM_NOTIFICATION
     * 默认系统通知栏
     */
    fun setNotificationType(notificationType: Int) = apply {
        if (userGlobalConfig) {
            this.notificationType = StarrySkyInstall.notificationType
            return@apply
        }
        this.notificationType = notificationType
    }

    /**
     * 通知栏其他配置
     */
    fun setNotificationConfig(config: NotificationConfig) = apply {
        if (userGlobalConfig) {
            this.notificationConfig = StarrySkyInstall.notificationConfig
            return@apply
        }
        this.notificationConfig = config
    }

    /**
     * 自定义通知栏，可参考 NotificationManager 内部的两个默认实现
     */
    fun setNotificationFactory(factory: NotificationManager.NotificationFactory) = apply {
        if (userGlobalConfig) {
            this.notificationFactory = StarrySkyInstall.notificationFactory
            return@apply
        }
        this.notificationFactory = factory
    }

    /**
     * 是否开启缓存功能
     */
    fun setOpenCache(open: Boolean) = apply {
        if (userGlobalConfig) {
            this.isOpenCache = StarrySkyInstall.isOpenCache
            return@apply
        }
        this.isOpenCache = open
    }

    /**
     * 自定义缓存实现
     */
    fun setCache(cache: ICache) = apply {
        if (userGlobalConfig) {
            this.playerCache = StarrySkyInstall.playerCache
            return@apply
        }
        this.playerCache = cache
    }

    /**
     * 设置缓存路径
     */
    fun setCacheDestFileDir(cacheDestFileDir: String) = apply {
        if (userGlobalConfig) {
            this.cacheDestFileDir = StarrySkyInstall.cacheDestFileDir
            return@apply
        }
        this.cacheDestFileDir = cacheDestFileDir
    }

    /**
     * 设置最大缓存大小
     */
    fun setCacheMaxBytes(cacheMaxBytes: Long) = apply {
        if (userGlobalConfig) {
            this.cacheMaxBytes = StarrySkyInstall.cacheMaxBytes
            return@apply
        }
        this.cacheMaxBytes = cacheMaxBytes
    }

    /**
     * 是否自动焦点管理
     */
    fun setAutoManagerFocus(isAutoManagerFocus: Boolean) = apply {
        if (userGlobalConfig) {
            this.isAutoManagerFocus = StarrySkyInstall.isAutoManagerFocus
            return@apply
        }
        this.isAutoManagerFocus = isAutoManagerFocus
    }

    /**
     * 自定义播放器实现
     */
    fun setPlayback(playback: Playback) = apply {
        this.playback = playback
    }

    private var binder: MusicServiceBinder? = null

    fun with(): PlayerControl {
        if (playerControl == null) {
            binder = MusicServiceBinder(StarrySkyInstall.globalContext!!)
            binder?.setPlayerCache(
                playerCache,
                cacheDestFileDir,
                cacheMaxBytes
            )
            binder?.setAutoManagerFocus(isAutoManagerFocus)
            binder?.initPlaybackManager(playback)
            playerControl = PlayerControl(
                StarrySkyInstall.interceptors,
                StarrySkyInstall.globalPlaybackStageListener,
                binder
            )
        }
        return playerControl!!
    }

    fun release(activity: Activity?) {
        with().removeProgressListener(activity.toString())
        with().resetVariable(activity)
    }
}