package com.lzx.starrysky

import android.app.Application
import android.content.ComponentName
import com.lzx.starrysky.common.IMediaConnection
import com.lzx.starrysky.common.IMediaConnection.OnConnectListener
import com.lzx.starrysky.common.MediaSessionConnection
import com.lzx.starrysky.control.PlayerControl
import com.lzx.starrysky.control.StarrySkyPlayerControl
import com.lzx.starrysky.imageloader.ImageLoader
import com.lzx.starrysky.notification.NotificationConfig
import com.lzx.starrysky.notification.StarrySkyNotificationManager
import com.lzx.starrysky.playback.manager.IPlaybackManager
import com.lzx.starrysky.playback.offline.StarrySkyCacheManager
import com.lzx.starrysky.playback.player.ExoPlayback
import com.lzx.starrysky.playback.player.Playback
import com.lzx.starrysky.provider.IMediaSourceProvider
import com.lzx.starrysky.utils.StarrySkyUtils

class StarrySky {

    var mLifecycle: StarrySkyActivityLifecycle? = null

    init {
        registerLifecycle(globalContext)
        //链接服务
        mediaConnection.connect()
        mediaConnection.setOnConnectListener(mOnConnectListener)
    }

    private fun registerLifecycle(context: Application) {
        mLifecycle?.let {
            context.unregisterActivityLifecycleCallbacks(it)
        }
        mLifecycle = StarrySkyActivityLifecycle()
        context.registerActivityLifecycleCallbacks(mLifecycle)
    }

    fun mediaQueueProvider(): IMediaSourceProvider {
        return mStarrySkyConfig.mediaQueueProvider
    }

    fun mediaConnection(): IMediaConnection {
        return mediaConnection
    }

    fun playBack(): Playback {
        return playback
    }

    fun imageLoader(): ImageLoader {
        return mStarrySkyConfig.imageLoader
    }

    fun notificationConfig(): NotificationConfig? {
        return mStarrySkyConfig.notificationConfig
    }

    fun playbackManager(): IPlaybackManager? {
        return mStarrySkyConfig.playbackManager
    }

    companion object {
        @Volatile
        private var sStarrySky: StarrySky? = null

        @Volatile
        private var isInitializing = false

        @Volatile
        private var alreadyInit = false
        private lateinit var globalContext: Application
        private lateinit var mStarrySkyConfig: StarrySkyConfig
        private var mOnConnectListener: OnConnectListener? = null
        private lateinit var mediaConnection: IMediaConnection
        private lateinit var notificationManager: StarrySkyNotificationManager
        private lateinit var cacheManager: StarrySkyCacheManager
        private lateinit var playback: Playback
        private lateinit var playerControl: PlayerControl

        @JvmOverloads
        fun init(
            application: Application,
            config: StarrySkyConfig,
            listener: OnConnectListener? = null
        ) {
            if (alreadyInit) {
                return
            }
            alreadyInit = true
            globalContext = application
            mStarrySkyConfig = config
            mOnConnectListener = listener
            get()
        }

        @JvmStatic
        fun get(): StarrySky {
            if (sStarrySky == null) {
                synchronized(StarrySky::class.java) {
                    if (sStarrySky == null) {
                        checkAndInitializeStarrySky()
                    }
                }
            }
            return sStarrySky!!
        }

        fun release() {
            get().mLifecycle?.let {
                globalContext.unregisterActivityLifecycleCallbacks(it)
            }
            isInitializing = false
            alreadyInit = false
            mOnConnectListener = null
            sStarrySky = null
        }

        @JvmStatic
        fun with(): PlayerControl {
            return playerControl
        }

        private fun checkAndInitializeStarrySky() {
            check(!isInitializing) { "checkAndInitializeStarrySky" }
            isInitializing = true
            try {
                initializeStarrySky()
            } catch (ex: Exception) {
                ex.printStackTrace()
            } finally {
                isInitializing = false
            }
        }

        private fun initializeStarrySky() {
            if (globalContext == null) {
                globalContext = StarrySkyUtils.getContextReflex()
            }
            requireNotNull(globalContext) { "StarrySky 初始化失败，上下文为 null" }

            mediaConnection = (if (mStarrySkyConfig.mediaConnection == null) {
                val componentName = ComponentName(globalContext, MusicService::class.java)
                MediaSessionConnection(globalContext, componentName)
            } else {
                mStarrySkyConfig.mediaConnection
            })!!
            notificationManager = if (mStarrySkyConfig.notificationManager == null) {
                StarrySkyNotificationManager(mStarrySkyConfig.isOpenNotification,
                    mStarrySkyConfig.notificationFactory)
            } else {
                mStarrySkyConfig.notificationManager
            }!!
            cacheManager = if (mStarrySkyConfig.cacheManager == null) {
                StarrySkyCacheManager(
                    globalContext, mStarrySkyConfig.isOpenCache, mStarrySkyConfig.cacheDestFileDir)
            } else {
                mStarrySkyConfig.cacheManager
            }!!
            playback = if (mStarrySkyConfig.playback == null) {
                ExoPlayback(globalContext, cacheManager)
            } else {
                mStarrySkyConfig.playback
            }!!
            playerControl = if (mStarrySkyConfig.playerControl == null) {
                StarrySkyPlayerControl(globalContext)
            } else {
                mStarrySkyConfig.playerControl
            }!!
            sStarrySky = StarrySky()
        }
    }
}