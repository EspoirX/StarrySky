package com.lzx.starrysky

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.MutableLiveData
import com.lzx.starrysky.cache.ExoCache
import com.lzx.starrysky.control.PlayerControl
import com.lzx.starrysky.flac.FlacLibrary
import com.lzx.starrysky.imageloader.ImageLoaderStrategy
import com.lzx.starrysky.playback.Playback
import com.lzx.starrysky.playback.PlaybackManager
import com.lzx.starrysky.playback.PlaybackStage
import com.lzx.starrysky.service.MusicService
import com.lzx.starrysky.service.ServiceBridge
import com.lzx.starrysky.utils.SpUtil
import com.lzx.starrysky.utils.isMainProcess
import java.util.WeakHashMap


class StarrySky {

    companion object {

        @Volatile
        private var sStarrySky: StarrySky? = null

        @Volatile
        private var isInitializing = false

        @Volatile
        private var alreadyInit = false
        private lateinit var config: StarrySkyConfig
        private lateinit var globalContext: Application
        private var connection: ServiceConnection? = null
        private var bridge: ServiceBridge? = null
        private val connectionMap = WeakHashMap<Context, ServiceConnection>()
        private var serviceToken: ServiceToken? = null
        private var playback: Playback? = null
        private var imageLoader: ImageLoaderStrategy? = null
        private val playbackState = MutableLiveData<PlaybackStage>()

        /**
         * 上下文，连接服务监听
         */
        @JvmStatic
        fun init(application: Application, config: StarrySkyConfig = StarrySkyConfig(), connection: ServiceConnection? = null) {
            if (!application.isMainProcess()) {
                return
            }
            if (alreadyInit) {
                return
            }
            FlacLibrary.isAvailable()
            alreadyInit = true
            this.config = config
            globalContext = application
            this.connection = connection
            SpUtil.init(globalContext)
            get()
        }

        /**
         * 获取控制播放对象
         */
        @JvmStatic
        fun with(): PlayerControl {
            if (bridge == null || bridge?.playerControl == null) {
                throw NullPointerException("请确保 with 方法在服务连接成功后调用")
            }
            return bridge?.playerControl!!
        }

        /**
         * 直接获取实例
         */
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

        /**
         * 初始化前检查
         */
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

        /**
         * 初始化
         */
        private fun initializeStarrySky() {
            sStarrySky = StarrySky()
            if (config.isUserService) {
                bindService()
            } else {
                bridge = ServiceBridge(globalContext)
                registerComponentsAndStart()
            }
        }

        /**
         * 绑定服务
         */
        private fun bindService() {
            try {
                val contextWrapper = ContextWrapper(globalContext)
                val intent = Intent(contextWrapper, MusicService::class.java)
                contextWrapper.startService(intent)
                val result = contextWrapper.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
                if (result) {
                    connectionMap[contextWrapper] = serviceConnection
                    serviceToken = ServiceToken(contextWrapper)
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }

        /**
         * 解绑服务
         */
        @JvmStatic
        fun unBindService() {
            try {
                if (serviceToken == null) {
                    return
                }
                val contextWrapper = serviceToken?.wrappedContext
                val binder = connectionMap.getOrDefault(contextWrapper, null)
                binder?.let {
                    contextWrapper?.unbindService(binder)
                    if (connectionMap.isEmpty()) {
                        bridge?.setServiceCallback(null)
                        bridge = null
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }

        /**
         * 获取状态LiveData，如果在MainActivity需要监听进度，建议用这个，因为主界面时可能服务还没连接
         * 所以 with() 方法获取的对象可能为null
         */
        @JvmStatic
        fun playbackState(): MutableLiveData<PlaybackStage> {
            return playbackState
        }

        private val serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                bridge = service as ServiceBridge?
                registerComponentsAndStart()
                connection?.onServiceConnected(name, service)
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                connection?.onServiceDisconnected(name)
                bridge = null
            }
        }

        /**
         * 注册组件并启动
         */
        private fun registerComponentsAndStart() {
            config.interceptors.forEach {
                bridge?.addInterceptor(it)
            }
            bridge?.register?.playback = playback
            if (config.isCreateRefrainPlayer) {
                bridge?.register?.refrainPlayback = playback
            }
            bridge?.register?.imageLoader = imageLoader
            val cache = if (config.cache == null) ExoCache(globalContext, config.isOpenCache, config.cacheDestFileDir) else config.cache
            bridge?.register?.cache = cache
            bridge?.register?.isOpenNotification = config.isOpenNotification
            bridge?.register?.notificationConfig = config.notificationConfig
            bridge?.register?.notification = config.notificationFactory
            bridge?.setServiceCallback(object : PlaybackManager.PlaybackServiceCallback {
                override fun onPlaybackStateUpdated(playbackStage: PlaybackStage) {
                    playbackState.postValue(playbackStage)
                }

                override fun onFocusStateChange(currentAudioFocusState: Int) {
                    config.focusChangeListener?.onAudioFocusChange(currentAudioFocusState)
                }
            })
            bridge?.start(config.isAutoManagerFocus, config.isCreateRefrainPlayer)
        }

        /**
         * 释放资源
         */
        @JvmStatic
        fun release() {
            unBindService()
            isInitializing = false
            alreadyInit = false
            connection = null
            sStarrySky = null
        }
    }
}

class ServiceToken(var wrappedContext: ContextWrapper)