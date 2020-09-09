package com.lzx.starrysky

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.lzx.starrysky.control.PlayerControl
import com.lzx.starrysky.playback.Playback
import java.util.WeakHashMap


class StarrySky {

    companion object {

        @Volatile
        private var sStarrySky: StarrySky? = null

        @Volatile
        private var isInitializing = false

        @Volatile
        private var alreadyInit = false
        private lateinit var globalContext: Application
        private var connection: ServiceConnection? = null
        private var bridge: ServiceBridge? = null
        private val connectionMap = WeakHashMap<Context, ServiceConnection>()
        private var serviceToken: ServiceToken? = null
        private var playback: Playback? = null


        /**
         * 上下文，连接服务监听
         */
        @JvmStatic
        fun init(application: Application, connection: ServiceConnection? = null) {
            if (alreadyInit) {
                return
            }
            alreadyInit = true
            globalContext = application
            this.connection = connection
            get()
        }

        /**
         * 获取控制播放对象
         */
        @JvmStatic
        fun with(): PlayerControl {
            if (bridge == null) {
                throw NullPointerException("bridge is null，can you init StarrySky？")
            }
            return bridge!!.playerControl
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
            sStarrySky = StarrySky()
            bindService()
        }

        /**
         * 绑定服务
         */
        private fun bindService() {
            try {
                val contextWrapper = ContextWrapper(globalContext)
                val intent = Intent(contextWrapper, MusicService::class.java)
                contextWrapper.startService(intent)
                val result = contextWrapper.bindService(intent, serviceConnection, 0)
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
                        bridge = null
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }

        private val serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                bridge = service as ServiceBridge?
                bridge?.register?.playback = playback
                connection?.onServiceConnected(name, service)
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                connection?.onServiceDisconnected(name)
                bridge = null
            }
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