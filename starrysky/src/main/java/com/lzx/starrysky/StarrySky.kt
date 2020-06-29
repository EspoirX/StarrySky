package com.lzx.starrysky

import android.app.Application
import android.content.ComponentName
import android.content.Context
import com.lzx.starrysky.common.IMediaConnection
import com.lzx.starrysky.common.IMediaConnection.OnConnectListener
import com.lzx.starrysky.common.MediaSessionConnection
import com.lzx.starrysky.control.PlayerControl
import com.lzx.starrysky.control.StarrySkyPlayerControl
import com.lzx.starrysky.imageloader.ImageLoader
import com.lzx.starrysky.intercept.StarrySkyInterceptor
import com.lzx.starrysky.notification.NotificationConfig
import com.lzx.starrysky.notification.StarrySkyNotificationManager
import com.lzx.starrysky.playback.manager.IPlaybackManager
import com.lzx.starrysky.playback.manager.PlaybackManager
import com.lzx.starrysky.playback.offline.ExoCache
import com.lzx.starrysky.playback.offline.ICache
import com.lzx.starrysky.playback.player.ExoPlayback
import com.lzx.starrysky.playback.player.Playback
import com.lzx.starrysky.playback.queue.MediaQueueManager
import com.lzx.starrysky.provider.IMediaSourceProvider
import com.lzx.starrysky.provider.MediaSourceProvider
import com.lzx.starrysky.utils.SpUtil
import com.lzx.starrysky.utils.StarrySkyUtils

class StarrySky {

    var mLifecycle: StarrySkyActivityLifecycle? = null

    init {
        SpUtil.init(globalContext)
        registerLifecycle(globalContext)
    }

    private fun registerLifecycle(context: Application) {
        mLifecycle?.let {
            context.unregisterActivityLifecycleCallbacks(it)
        }
        mLifecycle = StarrySkyActivityLifecycle()
        context.registerActivityLifecycleCallbacks(mLifecycle)
    }

    fun getContext(): Context? {
        return globalContext
    }

    fun mediaQueueProvider(): IMediaSourceProvider {
        return mediaQueueProvider
    }

    fun setMediaSourceProvider(queueProvider: IMediaSourceProvider) {
        mediaQueueProvider = queueProvider
    }

    fun mediaConnection(): IMediaConnection {
        return mediaConnection
    }

    fun playBack(): Playback {
        return playback
    }

    fun imageLoader(): ImageLoader {
        return imageLoader
    }

    fun interceptors(): MutableList<StarrySkyInterceptor> {
        return mStarrySkyConfig.interceptors
    }

    fun interceptorTimeOut(): Long {
        return mStarrySkyConfig.interceptorTimeOut
    }

    fun notificationConfig(): NotificationConfig? {
        return mStarrySkyConfig.notificationConfig
    }

    fun notificationManager(): StarrySkyNotificationManager {
        return notificationManager
    }

    fun config(): StarrySkyConfig {
        return mStarrySkyConfig
    }

    fun playbackManager(): IPlaybackManager {
        return playbackManager
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
        private lateinit var cache: ICache
        private lateinit var playback: Playback
        private lateinit var playerControl: PlayerControl
        private lateinit var mediaQueueProvider: IMediaSourceProvider
        private lateinit var playbackManager: PlaybackManager
        private lateinit var imageLoader: ImageLoader

        @JvmStatic
        fun init(
                application: Application,
                config: StarrySkyConfig = StarrySkyConfig(),
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

        @JvmStatic
        fun with(): PlayerControl {
            return playerControl
        }

        @JvmStatic
        fun release() {
            get().mLifecycle?.let {
                globalContext.unregisterActivityLifecycleCallbacks(it)
            }
            isInitializing = false
            alreadyInit = false
            mOnConnectListener = null
            sStarrySky = null
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
                StarrySkyUtils.contextReflex?.let { globalContext = it }
            }

            requireNotNull(globalContext) { "StarrySky 初始化失败，上下文为 null" }

            notificationManager = StarrySkyNotificationManager(mStarrySkyConfig.isOpenNotification,
                    mStarrySkyConfig.notificationFactory)

            cache = if (mStarrySkyConfig.cache == null) {
                ExoCache(globalContext)
            } else {
                mStarrySkyConfig.cache
            }!!
            playback = if (mStarrySkyConfig.playback == null) {
                ExoPlayback(globalContext, cache)
            } else {
                mStarrySkyConfig.playback
            }!!

            sStarrySky = StarrySky()

            imageLoader = ImageLoader()
            mStarrySkyConfig.imageLoader?.let {
                imageLoader.init(it)
            }

            //因为有层级调用关系，所以顺序不能乱
            mediaConnection = (if (mStarrySkyConfig.mediaConnection == null) {
                val componentName = ComponentName(globalContext, MusicService::class.java)
                MediaSessionConnection(globalContext, componentName)
            } else {
                mStarrySkyConfig.mediaConnection
            })!!

            mediaQueueProvider = if (mStarrySkyConfig.mediaQueueProvider == null) {
                MediaSourceProvider()
            } else {
                mStarrySkyConfig.mediaQueueProvider!!
            }

            playerControl = if (mStarrySkyConfig.playerControl == null) {
                StarrySkyPlayerControl(globalContext)
            } else {
                mStarrySkyConfig.playerControl
            }!!

            playbackManager = PlaybackManager(MediaQueueManager(), playback)

            //链接服务
            mediaConnection.connect()
            mediaConnection.setOnConnectListener(mOnConnectListener)
        }
    }
}