package com.lzx.starrysky

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.lzx.starrysky.cache.ICache
import com.lzx.starrysky.control.PlayerControl
import com.lzx.starrysky.intercept.ISyInterceptor
import com.lzx.starrysky.notification.INotification
import com.lzx.starrysky.notification.NotificationConfig
import com.lzx.starrysky.notification.NotificationManager
import com.lzx.starrysky.notification.imageloader.DefaultImageLoader
import com.lzx.starrysky.notification.imageloader.ImageLoader
import com.lzx.starrysky.notification.imageloader.ImageLoaderStrategy
import com.lzx.starrysky.playback.Playback
import com.lzx.starrysky.service.MusicService
import com.lzx.starrysky.service.ServiceBinder
import com.lzx.starrysky.utils.KtPreferences
import com.lzx.starrysky.utils.StarrySkyConstant
import java.util.WeakHashMap

object StarrySky {

    private var isDebug = true
    private var globalContext: Application? = null
    private var retryLineService = 0

    //服务相关
    private var isConnectionService = true
    private var isStartService = false
    private var onlyStartService = true
    private var connection: ServiceConnection? = null

    @Volatile
    private var isBindService = false
    private val connectionMap = WeakHashMap<Context, ServiceConnection>()
    private var serviceToken: ServiceToken? = null

    //通知栏相关
    private var isOpenNotification: Boolean = false
    private var notificationType: Int = INotification.SYSTEM_NOTIFICATION
    private var notificationConfig: NotificationConfig? = null
    private var notificationFactory: NotificationManager.NotificationFactory? = null

    //图片加载相关
    private var imageStrategy: ImageLoaderStrategy? = null
    private var imageLoader: ImageLoader? = null

    //播放控制
    private var playerControl: PlayerControl? = null

    //全局拦截器
    private val interceptors = mutableListOf<ISyInterceptor>()
    private var binder: ServiceBinder? = null

    //播放器缓存
    private var isOpenCache = false
    private var cacheDestFileDir: String = ""
    private var cacheMaxBytes: Long = 512 * 1024 * 1024
    private var playerCache: ICache? = null

    //是否自动焦点管理
    private var isAutoManagerFocus: Boolean = true

    //播放器
    private var playback: Playback? = null

    //callback
    private var appLifecycleCallback = AppLifecycleCallback()

    @JvmStatic
    fun init(application: Application) = apply {
        globalContext = application
    }

    /**
     * 是否debug，区别就是是否打印一些内部 log
     */
    fun setDebug(debug: Boolean) = apply {
        isDebug = debug
    }

    /**
     * 是否需要后台服务，默认true
     */
    fun connService(isConnectionService: Boolean) = apply {
        this.isConnectionService = isConnectionService
    }

    /**
     * 是否 startService，默认false
     */
    fun isStartService(isStartService: Boolean) = apply {
        this.isStartService = isStartService
    }

    /**
     * 是否只是 startService 而不需要 startForegroundService，默认true
     */
    fun onlyStartService(onlyStartService: Boolean) = apply {
        this.onlyStartService = onlyStartService
    }

    /**
     * 连接服务回调
     */
    fun connServiceListener(connection: ServiceConnection?) = apply {
        this.connection = connection
    }

    /**
     * 拦截器
     */
    fun interceptors(): MutableList<ISyInterceptor> = interceptors

    /**
     * 拦截器
     */
    fun addInterceptor(interceptor: ISyInterceptor) = apply {
        interceptors += interceptor
    }

    /**
     * 拦截器
     */
    fun clearInterceptor() {
        interceptors.clear()
    }

    /**
     * 通知栏开关
     */
    fun setNotificationSwitch(isOpenNotification: Boolean) = apply {
        this.isOpenNotification = isOpenNotification
    }

    /**
     * 通知栏类型
     * INotification.SYSTEM_NOTIFICATION
     * INotification.CUSTOM_NOTIFICATION
     */
    fun setNotificationType(notificationType: Int) = apply {
        this.notificationType = notificationType
    }

    /**
     * 通知栏其他配置
     */
    fun setNotificationConfig(config: NotificationConfig) = apply {
        this.notificationConfig = config
    }

    /**
     * 自定义通知栏
     */
    fun setNotificationFactory(factory: NotificationManager.NotificationFactory) = apply {
        this.notificationFactory = factory
    }

    /**
     * 图片加载
     */
    fun setImageLoader(loader: ImageLoaderStrategy) = apply {
        this.imageStrategy = loader
    }

    /**
     * 是否开启缓存
     */
    fun setOpenCache(open: Boolean) = apply {
        isOpenCache = open
    }

    /**
     * 缓存自定义实现
     */
    fun setCache(cache: ICache) = apply {
        this.playerCache = cache
    }

    /**
     * 是否自动焦点管理
     */
    fun setAutoManagerFocus(isAutoManagerFocus: Boolean) = apply {
        this.isAutoManagerFocus = isAutoManagerFocus
    }

    /**
     * 缓存路径
     */
    fun setCacheDestFileDir(cacheDestFileDir: String) = apply {
        this.cacheDestFileDir = cacheDestFileDir
    }

    /**
     * 最大缓存大小
     */
    fun setCacheMaxBytes(cacheMaxBytes: Long) = apply {
        this.cacheMaxBytes = cacheMaxBytes
    }

    /**
     * 播放器
     */
    fun setPlayback(playback: Playback) = apply {
        this.playback = playback
    }

    /**
     * 初始化
     */
    fun apply() {
        if (globalContext == null) {
            throw NullPointerException("context is null")
        }
        globalContext!!.registerActivityLifecycleCallbacks(appLifecycleCallback)
        KtPreferences.init(globalContext)
        StarrySkyConstant.KEY_CACHE_SWITCH = isOpenCache
        //图片加载
        playerControl = PlayerControl(interceptors)
        imageLoader = ImageLoader(globalContext)
        if (imageStrategy == null) {
            imageLoader?.init(DefaultImageLoader())
        } else {
            imageLoader?.init(imageStrategy!!)
        }
        if (isConnectionService) {
            bindService()
        } else {
            binder = ServiceBinder(globalContext!!)
            binder?.setPlayerCache(playerCache, cacheDestFileDir, cacheMaxBytes)
            binder?.setAutoManagerFocus(isAutoManagerFocus)
            binder?.initPlaybackManager(playback)
            playerControl?.attachPlayerCallback()
        }
    }

    internal fun log(msg: String?) {
        if (isDebug) {
            Log.i("StarrySky", msg)
        }
    }

    /**
     * 获取操作 api
     */
    @JvmStatic
    fun with() = playerControl!!

    /**
     * 获取soundPool
     */
    @JvmStatic
    fun soundPool() = binder?.soundPool

    /**
     * 切换系统和自定义通知栏
     */
    @JvmStatic
    fun changeNotification(notificationType: Int) {
        getBinder()?.changeNotification(notificationType)
    }


    @JvmStatic
    fun isOpenCache() = StarrySkyConstant.KEY_CACHE_SWITCH

    /**
     * 获取图片加载器
     */
    internal fun getImageLoader() = imageLoader

    internal fun getBinder() = binder

    internal fun context() = globalContext

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            retryLineService = 0
            binder = service as ServiceBinder?
            binder?.setNotificationConfig(isOpenNotification, notificationType, notificationConfig, notificationFactory)
            binder?.setPlayerCache(playerCache, cacheDestFileDir, cacheMaxBytes)
            binder?.setAutoManagerFocus(isAutoManagerFocus)
            binder?.initPlaybackManager(playback)
            playerControl?.attachPlayerCallback()
            isBindService = true
            connection?.onServiceConnected(name, service)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBindService = false
            connection?.onServiceDisconnected(name)
            if (retryLineService < 3) {
                retryLineService++
                bindService() // 断开后自动再 bindService
            }
        }
    }

    /**
     * 绑定服务
     */
    @JvmStatic
    fun bindService() {
        try {
            if (isBindService || globalContext == null) return
            val contextWrapper = ContextWrapper(globalContext)
            val intent = Intent(contextWrapper, MusicService::class.java)
            if (isStartService) {
                if (globalContext!!.applicationInfo.targetSdkVersion >= 26 && Build.VERSION.SDK_INT >= 26) {
                    try {
                        contextWrapper.startService(intent)
                    } catch (ex: Exception) {
                        if (!onlyStartService) {
                            intent.putExtra("flag_must_to_show_notification", true)
                            contextWrapper.startForegroundService(intent)
                        }
                        ex.printStackTrace()
                    }
                } else {
                    contextWrapper.startService(intent)
                }
            }
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
     * 解邦服务
     */
    @JvmStatic
    fun unBindService() {
        try {
            if (serviceToken == null || !isBindService) {
                return
            }
            val contextWrapper = serviceToken?.wrappedContext
            val conn = connectionMap.getOrDefault(contextWrapper, null)
            conn?.let {
                contextWrapper?.unbindService(conn)
                if (isStartService) {
                    val intent = Intent(contextWrapper, MusicService::class.java)
                    contextWrapper?.stopService(intent)
                }
                isBindService = false
                if (connectionMap.isEmpty()) {
                    binder = null
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    /**
     * 对象类的全置空
     */
    @JvmStatic
    fun release() {
        globalContext!!.unregisterActivityLifecycleCallbacks(appLifecycleCallback)
        unBindService()
        notificationConfig = null
        notificationFactory = null
        imageStrategy = null
        imageLoader = null
        playerControl = null
        playerCache = null
        playback = null
        connection = null
        serviceToken = null
        binder = null
        globalContext = null
        interceptors.clear()
        connectionMap.clear()
    }

    class ServiceToken(var wrappedContext: ContextWrapper)
}