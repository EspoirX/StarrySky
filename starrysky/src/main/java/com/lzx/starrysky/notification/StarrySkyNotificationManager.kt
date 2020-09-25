package com.lzx.starrysky.notification

import android.content.Context

class StarrySkyNotificationManager constructor(
    isOpenNotification: Boolean,
    private val config: NotificationConfig?,
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

    fun getNotification(context: Context): INotification? {
        return notification ?: synchronized(this) {
            if (notification == null && factory != null) {
                notification = factory?.build(context, config)
            }
            return notification
        }
    }

    interface NotificationFactory {
        fun build(context: Context, config: NotificationConfig?): INotification
    }

    companion object {
        val SYSTEM_NOTIFICATION_FACTORY: NotificationFactory = object : NotificationFactory {
            override fun build(
                context: Context, config: NotificationConfig?
            ): INotification {
                return if (config == null) SystemNotification(context) else SystemNotification(context, config)
            }
        }

        val CUSTOM_NOTIFICATION_FACTORY: NotificationFactory = object : NotificationFactory {
            override fun build(
                context: Context, config: NotificationConfig?
            ): INotification {
                return if (config == null) CustomNotification(context) else CustomNotification(context, config)
            }
        }
    }
}