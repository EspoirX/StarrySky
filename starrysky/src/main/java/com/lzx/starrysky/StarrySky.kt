package com.lzx.starrysky

import android.util.Log
import com.lzx.starrysky.cache.ICache
import com.lzx.starrysky.control.PlayerControl
import com.lzx.starrysky.intercept.StarrySkyInterceptor
import com.lzx.starrysky.notification.INotification
import com.lzx.starrysky.utils.StarrySkyConstant

// StarrySky -> PlayerControl -> PlaybackManager -> player
object StarrySky {

    //播放控制
    private var playerControl: PlayerControl? = null

    /**
     * 获取全局拦截器集合
     */
    @JvmStatic
    fun interceptors(): MutableList<Pair<StarrySkyInterceptor, String>> = StarrySkyInstall.interceptors

    /**
     * 清除全局拦截器
     */
    @JvmStatic
    fun clearInterceptor() {
        StarrySkyInstall.interceptors.clear()
    }

    internal fun log(msg: String) {
        if (StarrySkyInstall.isDebug) {
            Log.i("StarrySky", msg)
        }
    }

    fun getStackTopActivity() = StarrySkyInstall.appLifecycleCallback.getStackTopActivity()

    fun getActivityStack() = StarrySkyInstall.appLifecycleCallback.activityStack

    /**
     * 获取操作 api
     */
    @JvmStatic
    fun with(): PlayerControl {
        if (playerControl == null) {
            playerControl = PlayerControl(
                StarrySkyInstall.interceptors,
                StarrySkyInstall.globalPlaybackStageListener,
                getBinder()
            )
        }
        return playerControl!!
    }

    /**
     * 获取soundPool
     */
    @JvmStatic
    fun soundPool() = getBinder()?.soundPool

    /**
     * 切换系统和自定义通知栏
     */
    @JvmStatic
    fun changeNotification(notificationType: Int) {
        getBinder()?.changeNotification(notificationType)
    }

    /**
     * 关闭通知栏
     */
    @JvmStatic
    fun closeNotification() {
        getBinder()?.stopNotification()
    }

    /**
     * 打开通知栏
     */
    @JvmStatic
    fun openNotification() {
        getBinder()?.openNotification()
    }

    /**
     * 是否打开通知栏（开关性质的api）
     */
    @JvmStatic
    fun setIsOpenNotification(open: Boolean) {
        getBinder()?.setIsOpenNotification(open)
    }

    /**
     * 获取当前通知栏类型
     */
    @JvmStatic
    fun getNotificationType() = getBinder()?.getNotificationType()
        ?: INotification.SYSTEM_NOTIFICATION

    /**
     * 是否打开了缓存开关
     */
    @JvmStatic
    fun isOpenCache() = StarrySkyConstant.KEY_CACHE_SWITCH

    /**
     * 获取播放缓存类
     */
    @JvmStatic
    fun getPlayerCache(): ICache? = getBinder()?.getPlayerCache()

    /**
     * 音效相关，获取音效操作类
     */
    @JvmStatic
    fun effect() = StarrySkyInstall.voiceEffect

    /**
     * 音效相关，音效开关
     */
    fun effectSwitch(isOpen: Boolean) {
        StarrySkyConstant.keyEffectSwitch = isOpen
        if (isOpen) {
            effect().attachAudioEffect(with().getAudioSessionId())
        }
    }

    /**
     * 获取音效开关
     */
    fun getEffectSwitch() = StarrySkyConstant.keyEffectSwitch

    /**
     * 音效相关，音效配置信息是否要本地存储
     */
    fun saveEffectConfig(save: Boolean) {
        StarrySkyConstant.keySaveEffectConfig = save
    }

    /**
     * 获取图片加载器
     */
    internal fun getImageLoader() = StarrySkyInstall.imageLoader

    private fun getBinder() = StarrySkyInstall.binder

    /**
     * 对象类的全置空
     */
    @JvmStatic
    fun release() {
        StarrySkyInstall.release()
        playerControl?.release()
        playerControl = null
    }
}