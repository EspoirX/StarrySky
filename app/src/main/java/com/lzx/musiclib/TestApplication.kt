package com.lzx.musiclib

import android.Manifest
import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.IBinder
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.danikula.videocache.HttpProxyCacheServer
import com.lzx.record.StarrySkyRecord
import com.lzx.starrysky.SongInfo
import com.lzx.starrysky.StarrySky
import com.lzx.starrysky.StarrySkyConfig
import com.lzx.starrysky.cache.ICache
import com.lzx.starrysky.imageloader.ImageLoaderCallBack
import com.lzx.starrysky.imageloader.ImageLoaderStrategy
import com.lzx.starrysky.intercept.InterceptorCallback
import com.lzx.starrysky.intercept.StarrySkyInterceptor
import com.lzx.starrysky.notification.NotificationConfig
import com.lzx.basecode.MainLooper
import com.qw.soul.permission.SoulPermission
import com.qw.soul.permission.bean.Permission
import com.qw.soul.permission.bean.Permissions
import com.qw.soul.permission.callbcak.CheckRequestPermissionsListener
import com.tencent.bugly.crashreport.CrashReport
import java.io.File


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
        //bugly
        CrashReport.initCrashReport(applicationContext, "9e447caa98", false)

        val notificationConfig = NotificationConfig.create {
            targetClass { "com.lzx.musiclib.MainActivity" }
            targetClassBundle {
                val bundle = Bundle()
                bundle.putString("notifyKey", "我是点击通知栏转跳带的参数")
                return@targetClassBundle bundle
            }
        }
        val config = StarrySkyConfig().newBuilder()
            .isOpenCache(true)
            .setCacheDestFileDir("000StarrySkyCache/".toSdcardPath())
//            .setCacheMaxBytes(1024 * 1024 * 1024)  //设置缓存上限，默认 512 * 1024 * 1024
//            .setCache(AndroidVideoCache(this))
            .addInterceptor(PermissionInterceptor(this))
//            .addInterceptor(RequestSongInfoInterceptor())
//            .addInterceptor(RequestSongCoverInterceptor())
            .setImageLoader(object : ImageLoaderStrategy {
                //使用自定义图片加载器
                override fun loadImage(context: Context, url: String?, callBack: ImageLoaderCallBack) {
                    Glide.with(context).asBitmap().load(url).into(object : CustomTarget<Bitmap?>() {
                        override fun onLoadCleared(placeholder: Drawable?) {}

                        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap?>?) {
                            callBack.onBitmapLoaded(resource)
                        }

                        override fun onLoadFailed(errorDrawable: Drawable?) {
                            super.onLoadFailed(errorDrawable)
                            callBack.onBitmapFailed(errorDrawable)
                        }
                    })
                }
            })
            .isOpenNotification(true)
            .setNotificationConfig(notificationConfig)
            .isCreateRefrainPlayer(true)
            .isAutoManagerFocus(false)  //因为开了伴奏播放器，所以要关闭自动焦点管理功能
            //自己管理焦点的示例代码
//            .setOnAudioFocusChangeListener(object : AudioFocusChangeListener {
//                override fun onAudioFocusChange(focusInfo: FocusInfo) {
//                    Log.i("TestApplication", "焦点state=" + focusInfo.audioFocusState + " 播放=" + focusInfo.playerCommand + " 音量=" + focusInfo.volume)
//                    StarrySky.with().setVolume(focusInfo.volume)
//                    if (focusInfo.playerCommand == FocusManager.DO_NOT_PLAY || focusInfo.playerCommand == FocusManager.WAIT_FOR_CALLBACK) {
//                        StarrySky.with().pauseMusic()
//                    } else if (focusInfo.playerCommand == FocusManager.PLAY_WHEN_READY) {
//                        StarrySky.with().restoreMusic()
//                    }
//                }
//            })
            .build()
        StarrySky.init(this, config, object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                this@TestApplication.showToast("连接成功")
                val localBroadcastManager = LocalBroadcastManager.getInstance(this@TestApplication)
                localBroadcastManager.sendBroadcast(Intent("onServiceConnectedSuccessAction"))
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                this@TestApplication.showToast("连接失败")
            }
        })
    }

    /**
     * 权限申请拦截器
     */
    class PermissionInterceptor internal constructor(private val mContext: Context) : StarrySkyInterceptor {
        override fun process(songInfo: SongInfo?, mainLooper: com.lzx.basecode.MainLooper, callback: InterceptorCallback) {
            if (songInfo == null) {
                callback.onInterrupt(RuntimeException("SongInfo is null"))
                return
            }
            val hasPermission = SpConstant.HAS_PERMISSION
            if (hasPermission) {
                callback.onContinue(songInfo)
                return
            }
            SoulPermission.getInstance().checkAndRequestPermissions(Permissions.build(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE),
                object : CheckRequestPermissionsListener {
                    override fun onAllPermissionOk(allPermissions: Array<Permission>) {
                        SpConstant.HAS_PERMISSION = true
                        callback.onContinue(songInfo)
                    }

                    override fun onPermissionDenied(refusedPermissions: Array<Permission>) {
                        SpConstant.HAS_PERMISSION = false
                        callback.onInterrupt(RuntimeException("没有权限，播放失败"))
                        mainLooper.runOnUiThread(Runnable {
                            mContext.showToast("没有权限，播放失败")
                        })
                    }
                })
        }
    }

    /**
     * 请求播放url拦截器
     */
//    class RequestSongInfoInterceptor : StarrySkyInterceptor {
//        private val viewModel = MusicViewModel()
//        override fun process(
//            songInfo: SongInfo?, mainLooper: MainLooper, callback: InterceptorCallback
//        ) {
//            if (songInfo == null) {
//                callback.onInterrupt(RuntimeException("SongInfo is null"))
//                return
//            }
//            if (songInfo.songUrl.isEmpty() && songInfo.headData?.get("source") == "qqMusic") {
//                viewModel.getQQMusicUrl(songInfo.songId) {
//                    songInfo.songUrl = it
//                    callback.onContinue(songInfo)
//                }
//            } else if (songInfo.songUrl.isEmpty() && songInfo.headData?.get("source") == "baiduMusic") {
//                viewModel.getBaiduMusicUrl(songInfo.songId) {
//                    songInfo.songCover = it.songCover
//                    songInfo.songUrl = it.songUrl
//                    songInfo.duration = it.duration
//                    callback.onContinue(songInfo)
//                }
//            } else {
//                callback.onContinue(songInfo)
//            }
//        }
//    }

    /**
     * 请求封面url拦截器
     */
//    class RequestSongCoverInterceptor : StarrySkyInterceptor {
//        private val viewModel = MusicViewModel()
//        override fun process(
//            songInfo: SongInfo?, mainLooper: MainLooper, callback: InterceptorCallback
//        ) {
//            if (songInfo == null) {
//                callback.onInterrupt(RuntimeException("SongInfo is null"))
//                return
//            }
//            if (songInfo.songCover.isEmpty() && songInfo.headData?.get("source") == "qqMusic") {
//                viewModel.getQQMusicSongCover(songInfo.songId) {
//                    songInfo.songCover = it
//                    callback.onContinue(songInfo)
//                }
//            } else {
//                callback.onContinue(songInfo)
//            }
//        }
//    }

    /**
     * 使用 AndroidVideoCache 这个第三方库做缓存的例子
     */
    class AndroidVideoCache(private val context: Context) : ICache {

        private var proxy: HttpProxyCacheServer? = null
        private var cacheFile: File? = null

        override fun startCache(url: String) {
            //什么都不做
        }

        private fun getProxy(): HttpProxyCacheServer? {
            return if (proxy == null) newProxy().also { proxy = it } else proxy
        }

        private fun newProxy(): HttpProxyCacheServer? {
            return HttpProxyCacheServer.Builder(context)
                .maxCacheSize(1024 * 1024 * 1024)       // 1 Gb for cache
                .cacheDirectory(getCacheDirectory(context, ""))
                .build()
        }

        override fun getProxyUrl(url: String): String? {
            return getProxy()?.getProxyUrl(url)
        }

        override fun isOpenCache(): Boolean {
            return super.isOpenCache()
        }

        override fun getCacheDirectory(context: Context, destFileDir: String?): File? {
            var fileDir = destFileDir
            if (fileDir.isNullOrEmpty()) {
                fileDir = "StarrySkyCache/".toSdcardPath()
            }
            if (cacheFile == null && fileDir.isNotEmpty()) {
                cacheFile = File(fileDir)
                if (cacheFile?.exists() == false) {
                    cacheFile?.mkdirs()
                }
            }
            if (cacheFile == null) {
                cacheFile = context.getExternalFilesDir(null)
                if (cacheFile == null) {
                    cacheFile = context.filesDir
                }
            }
            return cacheFile
        }

        override fun isCache(url: String): Boolean {
            return getProxy()?.isCached(url) ?: false
        }
    }
}