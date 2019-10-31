package com.lzx.starrysky.notification

import com.lzx.starrysky.MusicService
import com.lzx.starrysky.StarrySky

class StarrySkyNotificationManager constructor(
    isOpenNotification: Boolean,
    factory: NotificationFactory?
) {
    private var isOpenNotification: Boolean = false
    private var factory: NotificationFactory? = null
    private var notification: INotification? = null

    init {
        this.isOpenNotification = isOpenNotification
        if (isOpenNotification) {
            this.factory = factory ?: SYSTEM_NOTIFICATION_FACTORY
        }
    }

    fun isOpenNotification(): Boolean {
        return isOpenNotification
    }

    fun getNotification(musicService: MusicService): INotification? {
        return notification ?: synchronized(this) {
            if (notification == null && factory != null) {
                val config = StarrySky.get().registry.notificationConfig
                notification = factory?.build(musicService, config)
            }
            return notification
        }
    }

    interface NotificationFactory {
        fun build(musicService: MusicService, config: NotificationConfig?): INotification
    }

    companion object {
        val SYSTEM_NOTIFICATION_FACTORY: NotificationFactory = object : NotificationFactory {
            override fun build(
                musicService: MusicService, config: NotificationConfig?
            ): INotification {
                return SystemNotification(musicService, config)
            }
        }

        val CUSTOM_NOTIFICATION_FACTORY: NotificationFactory = object : NotificationFactory {
            override fun build(
                musicService: MusicService, config: NotificationConfig?
            ): INotification {
                return CustomNotification(musicService, config)
            }
        }
    }
}