package com.lzx.starrysky.service

import com.lzx.starrysky.cache.ICache
import com.lzx.starrysky.imageloader.ImageLoaderStrategy
import com.lzx.starrysky.notification.NotificationConfig
import com.lzx.starrysky.notification.StarrySkyNotificationManager
import com.lzx.starrysky.playback.Playback

class StarrySkyRegister {
    var playback: Playback? = null
    var imageLoader: ImageLoaderStrategy? = null
    var cache: ICache? = null
    var isOpenNotification: Boolean = false
    var notificationConfig: NotificationConfig? = null
    var notification: StarrySkyNotificationManager.NotificationFactory? = null
}