package com.lzx.starrysky

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.MutableLiveData
import com.lzx.basecode.FocusInfo
import com.lzx.basecode.KtPreferences
import com.lzx.basecode.Playback
import com.lzx.basecode.isMainProcess
import com.lzx.starrysky.cache.ExoCache
import com.lzx.starrysky.control.PlayerControl
import com.lzx.starrysky.imageloader.ImageLoaderStrategy
import com.lzx.starrysky.playback.PlaybackManager
import com.lzx.starrysky.playback.PlaybackStage
import com.lzx.starrysky.playback.SoundPoolPlayback
import com.lzx.starrysky.service.MusicService
import com.lzx.starrysky.service.ServiceBridge
import com.lzx.starrysky.utils.StarrySkyConstant
import java.util.WeakHashMap


object StarrySky {

    @Volatile
    private var isBindService = false

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
     * 上下文，配置，连接服务监听
     */
    @JvmStatic
    fun init(application: Application, config: StarrySkyConfig = StarrySkyConfig(), connection: ServiceConnection? = null) {
        if (!application.isMainProcess()) {
            return
        }
        this.config = config
        globalContext = application
        this.connection = connection
        KtPreferences.init(globalContext)
        checkAndInitializeStarrySky()
    }

    /**
     * 获取控制播放对象(如果是在主界面调用，可能要等连接成功后再调，不要一打开主界面就马上调用)
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
        return this
    }

    /**
     * 获取播放器（确保连接服务成功后调用）
     */
    @JvmStatic
    fun getPlayer() = bridge?.player


    /**
     * 初始化前检查
     */
    private fun checkAndInitializeStarrySky() {
        try {
            if (isBindService) return
            initializeStarrySky()
        } catch (ex: Exception) {
            ex.printStackTrace()
            isBindService = false
        }
    }

    /**
     * 初始化
     */
    private fun initializeStarrySky() {
        if (config.isUserService) {
            bindService()
        } else {
            bridge = ServiceBridge(globalContext)
            registerComponentsAndStart()
            isBindService = true
        }
    }

    /**
     * 绑定服务
     */
    private fun bindService() {
        try {
            val contextWrapper = ContextWrapper(globalContext)
            val intent = Intent(contextWrapper, MusicService::class.java)
            //ContextCompat.startForegroundService(contextWrapper, intent)
            //contextWrapper.startService(intent)
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

    /**
     * 获取 SoundPool 操作实例
     * 适合短促且对反应速度比较高的音频，建议长度不超过7秒，大小不大于100kb，更多信息请参数 SoundPool
     *
     * SoundPoolCreator 创建 SoundPool 时的参数构建，可通过内部的 buildSoundPool 来构建
     */
    fun soundPool(): SoundPoolPlayback {
        if (bridge?.soundPoolPlayback == null) {
            throw NullPointerException("bridge or soundPoolPlayback is Null")
        }
        return bridge!!.soundPoolPlayback!!
    }

    fun getBridge() = bridge

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            bridge = service as ServiceBridge?
            registerComponentsAndStart()
            isBindService = true
            connection?.onServiceConnected(name, service)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            bindService() // 断开后自动再 bindService
            isBindService = false
            connection?.onServiceDisconnected(name)
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
        StarrySkyConstant.KEY_CACHE_SWITCH = config.isOpenCache
        val cache = if (config.cache == null) {
            ExoCache(globalContext,
                config.cacheDestFileDir,
                config.cacheMaxBytes)
        } else config.cache
        bridge?.register?.cache = cache
        bridge?.register?.isOpenNotification = config.isOpenNotification
        bridge?.register?.notificationConfig = config.notificationConfig
        bridge?.register?.notification = config.notificationFactory
        bridge?.setServiceCallback(object : PlaybackManager.PlaybackServiceCallback {
            override fun onPlaybackStateUpdated(playbackStage: PlaybackStage) {
                playbackState.postValue(playbackStage)
            }

            override fun onFocusStateChange(info: FocusInfo) {
                config.focusChangeListener?.onAudioFocusChange(info)
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
        isBindService = false
        connection = null
    }
}

class ServiceToken(var wrappedContext: ContextWrapper)
