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
import android.os.Environment
import android.os.IBinder
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.lzx.musiclib.viewmodel.MusicViewModel
import com.lzx.starrysky.SongInfo
import com.lzx.starrysky.StarrySky
import com.lzx.starrysky.StarrySkyConfig
import com.lzx.starrysky.imageloader.ImageLoaderCallBack
import com.lzx.starrysky.imageloader.ImageLoaderStrategy
import com.lzx.starrysky.intercept.InterceptorCallback
import com.lzx.starrysky.intercept.StarrySkyInterceptor
import com.lzx.starrysky.notification.INotification
import com.lzx.starrysky.notification.NotificationConfig
import com.lzx.starrysky.notification.StarrySkyNotificationManager
import com.lzx.starrysky.utils.MainLooper
import com.lzx.starrysky.utils.SpUtil
import com.qw.soul.permission.SoulPermission
import com.qw.soul.permission.bean.Permission
import com.qw.soul.permission.bean.Permissions
import com.qw.soul.permission.callbcak.CheckRequestPermissionsListener
import com.tencent.bugly.crashreport.CrashReport


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
            .setCacheDestFileDir(Environment.getExternalStorageDirectory().absolutePath.toString() + "/01010101/")
            .addInterceptor(PermissionInterceptor(this))
            .addInterceptor(RequestSongInfoInterceptor())
            .addInterceptor(RequestSongCoverInterceptor())
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
//            .setNotificationFactory(object : StarrySkyNotificationManager.NotificationFactory {
//                override fun build(context: Context, config: NotificationConfig?): INotification {
//                    //使用自定义通知栏
//                    return StarrySkyNotificationManager.CUSTOM_NOTIFICATION_FACTORY.build(context, config)
//                }
//            })
            .isCreateRefrainPlayer(true)
            .isAutoManagerFocus(false)  //因为开了伴奏播放器，所以要关闭自动焦点管理功能
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
        override fun process(songInfo: SongInfo?, mainLooper: MainLooper, callback: InterceptorCallback) {
            if (songInfo == null) {
                callback.onInterrupt(RuntimeException("SongInfo is null"))
                return
            }
            val hasPermission = SpUtil.instance?.getBoolean("HAS_PERMISSION", false)
            if (hasPermission == true) {
                callback.onContinue(songInfo)
                return
            }
            SoulPermission.getInstance().checkAndRequestPermissions(Permissions.build(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE),
                object : CheckRequestPermissionsListener {
                    override fun onAllPermissionOk(allPermissions: Array<Permission>) {
                        SpUtil.instance?.putBoolean("HAS_PERMISSION", true)
                        callback.onContinue(songInfo)
                    }

                    override fun onPermissionDenied(refusedPermissions: Array<Permission>) {
                        SpUtil.instance?.putBoolean("HAS_PERMISSION", false)
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
            } else if (songInfo.songUrl.isEmpty() && songInfo.headData?.get("source") == "baiduMusic") {
                viewModel.getBaiduMusicUrl(songInfo.songId) {
                    songInfo.songCover = it.songCover
                    songInfo.songUrl = it.songUrl
                    songInfo.duration = it.duration
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