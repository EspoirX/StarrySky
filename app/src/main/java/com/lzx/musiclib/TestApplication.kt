package com.lzx.musiclib

import android.app.Application
import android.content.Context
import com.lzx.starrysky.SongInfo
import com.lzx.starrysky.StarrySky
import com.lzx.starrysky.StarrySkyConfig
import com.lzx.starrysky.intercept.InterceptorCallback
import com.lzx.starrysky.intercept.StarrySkyInterceptor
import com.lzx.starrysky.utils.MainLooper

/**
 * create by lzx
 * time:2018/11/9
 */
open class TestApplication : Application() {

    companion object {
        var context: Context? = null
    }

    override fun onCreate() {
        super.onCreate()
        context = this
        val config = StarrySkyConfig().newBuilder()
            .build()
        StarrySky.init(this, config)
    }

    /**
     * 请求播放url拦截器
     */
    class RequestSongInfoInterceptor : StarrySkyInterceptor {
        override fun process(songInfo: SongInfo?, mainLooper: MainLooper, callback: InterceptorCallback) {

        }
    }
}