package com.lzx.musiclib

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
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
            .addInterceptor(RequestSongInfoInterceptor())
            .addInterceptor(RequestSongCoverInterceptor())
            .isOpenNotification(true)
            .build()
        StarrySky.init(this, config, object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                this@TestApplication.showToast("连接成功")
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                this@TestApplication.showToast("连接失败")
            }
        })
    }

    /**
     * 请求播放url拦截器
     */
    class RequestSongInfoInterceptor : StarrySkyInterceptor {
        private val viewModel = MusicViewModel()
        override fun process(
            songInfo: SongInfo?, mainLooper: MainLooper, callback: InterceptorCallback
        ) {
            if (songInfo == null) {
                callback.onInterrupt(RuntimeException("SongInfo is null"))
                return
            }
            if (songInfo.songUrl.isEmpty() && songInfo.headData?.get("source") == "qqMusic") {
                viewModel.getQQMusicUrl(songInfo.songId) {
                    songInfo.songUrl = it
                    callback.onContinue(songInfo)
                }
            } else {
                callback.onContinue(songInfo)
            }
        }
    }

    /**
     * 请求封面url拦截器
     */
    class RequestSongCoverInterceptor : StarrySkyInterceptor {
        private val viewModel = MusicViewModel()
        override fun process(
            songInfo: SongInfo?, mainLooper: MainLooper, callback: InterceptorCallback
        ) {
            if (songInfo == null) {
                callback.onInterrupt(RuntimeException("SongInfo is null"))
                return
            }
            if (songInfo.songCover.isEmpty() && songInfo.headData?.get("source") == "qqMusic") {
                viewModel.getQQMusicSongCover(songInfo.songId) {
                    songInfo.songCover = it
                    callback.onContinue(songInfo)
                }
            } else {
                callback.onContinue(songInfo)
            }
        }
    }
}