package com.lzx.starrysky.service

import android.content.Context
import android.os.Binder
import android.support.v4.media.session.MediaSessionCompat
import com.lzx.starrysky.SongInfo
import com.lzx.starrysky.cache.ExoCache
import com.lzx.starrysky.cache.ICache
import com.lzx.starrysky.manager.PlaybackStage
import com.lzx.starrysky.manager.changePlaybackState
import com.lzx.starrysky.notification.INotification
import com.lzx.starrysky.notification.NotificationConfig
import com.lzx.starrysky.notification.NotificationManager
import com.lzx.starrysky.playback.ExoPlayback
import com.lzx.starrysky.playback.Playback
import com.lzx.starrysky.playback.SoundPoolPlayback


class MusicServiceBinder(private val context: Context) : Binder() {

    var player: Playback? = null
    var soundPool: SoundPoolPlayback? = null
    private var isOpenNotification: Boolean = false
    private var notificationType: Int = INotification.SYSTEM_NOTIFICATION
    private var notificationConfig: NotificationConfig? = null
    private var notificationManager = NotificationManager()
    var notification: INotification? = null
    private var isShowNotification = false
    private var notificationFactory: NotificationManager.NotificationFactory? = null
    private var playerCache: ICache? = null
    private var cacheDestFileDir: String = ""
    private var cacheMaxBytes: Long = 512 * 1024 * 1024
    private var isAutoManagerFocus: Boolean = true

    fun setNotificationConfig(openNotification: Boolean,
                              notificationType: Int,
                              notificationConfig: NotificationConfig?,
                              notificationFactory: NotificationManager.NotificationFactory?
    ) {
        this.isOpenNotification = openNotification
        this.notificationType = notificationType
        this.notificationConfig = notificationConfig
        this.notificationFactory = notificationFactory
        //通知栏配置
        if (isOpenNotification) {
            createNotification()
        }
    }

    fun setIsOpenNotification(open: Boolean) {
        isOpenNotification = open
    }

    private fun createNotification() {
        notification = if (notificationType == INotification.SYSTEM_NOTIFICATION) {
            notificationManager.getSystemNotification(context, notificationConfig)
        } else {
            if (this.notificationFactory != null) {
                this.notificationFactory?.build(context, notificationConfig)
            } else {
                notificationManager.getCustomNotification(context, notificationConfig)
            }
        }
    }

    fun changeNotification(notificationType: Int) {
        if (!isOpenNotification) return
        if (this.notificationType == notificationType) return
        notification?.stopNotification()
        createNotification()
        this.notificationType = notificationType
        player?.let {
            notification?.startNotification(it.getCurrPlayInfo(), it.playbackState().changePlaybackState())
        }
    }

    fun getNotificationType() = notificationType

    fun onChangedNotificationState(songInfo: SongInfo?, playbackState: String,
                                   hasNextSong: Boolean, hasPreSong: Boolean) {
        if (isOpenNotification) {
            notification?.onPlaybackStateChanged(songInfo, playbackState, hasNextSong, hasPreSong)
            isShowNotification = true
        }
    }

    fun openNotification() {
        val info = player?.getCurrPlayInfo()
        val state = player?.playbackState()?.changePlaybackState() ?: PlaybackStage.IDLE
        startNotification(info, state)
    }

    fun startNotification(currPlayInfo: SongInfo?, state: String) {
        if (isOpenNotification) {
            notification?.startNotification(currPlayInfo, state)
            isShowNotification = true
        }

        if(state == PlaybackStage.IDLE){
            // 释放锁
            WifiLockHelper.release()
        }else{
            // 添加锁
            WifiLockHelper.acquire(context)
        }
    }

    fun stopNotification() {
        if (isOpenNotification) {
            notification?.stopNotification()
            isShowNotification = false
        }
    }

    fun setSessionToken(mediaSession: MediaSessionCompat.Token?) {
        if (isOpenNotification) {
            notification?.setSessionToken(mediaSession)
        }
    }

    fun setPlayerCache(cache: ICache?, cacheDestFileDir: String, cacheMaxBytes: Long) {
        playerCache = cache
        this.cacheDestFileDir = cacheDestFileDir
        this.cacheMaxBytes = cacheMaxBytes
    }

    fun setAutoManagerFocus(isAutoManagerFocus: Boolean) {
        this.isAutoManagerFocus = isAutoManagerFocus
    }

    fun initPlaybackManager(playback: Playback?) {
        //播放器
        if (playerCache == null) {
            playerCache = ExoCache(context, cacheDestFileDir, cacheMaxBytes)
        }
        player = playback ?: ExoPlayback(context, playerCache, isAutoManagerFocus)
        soundPool = SoundPoolPlayback(context)
    }

    fun getPlayerCache(): ICache? = playerCache

    fun onStopByTimedOff(time: Long, pause: Boolean, finishCurrSong: Boolean) {
        if (context is MusicService) {
            context.onStopByTimedOffImpl(time, pause, finishCurrSong)
        }
    }
}