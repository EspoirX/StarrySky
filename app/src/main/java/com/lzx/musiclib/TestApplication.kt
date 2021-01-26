package com.lzx.musiclib

//import androidx.localbroadcastmanager.content.LocalBroadcastManager
import android.Manifest
import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.danikula.videocache.HttpProxyCacheServer
import com.danikula.videocache.file.Md5FileNameGenerator
import com.lzx.musiclib.viewmodel.MusicViewModel
import com.lzx.starrysky.SongInfo
import com.lzx.starrysky.StarrySky
import com.lzx.starrysky.cache.ICache
import com.lzx.starrysky.intercept.AsyncInterceptor
import com.lzx.starrysky.intercept.InterceptorCallback
import com.lzx.starrysky.intercept.SyncInterceptor
import com.lzx.starrysky.notification.NotificationConfig
import com.lzx.starrysky.notification.imageloader.ImageLoaderCallBack
import com.lzx.starrysky.notification.imageloader.ImageLoaderStrategy
import com.lzx.starrysky.utils.StarrySkyConstant
import com.lzx.starrysky.utils.toSdcardPath
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
        StarrySky.init(this)
            .setOpenCache(false)
            .setCacheDestFileDir("000StarrySkyCache/".toSdcardPath())
            .setCacheMaxBytes(1024 * 1024 * 1024)  //设置缓存上限，默认 512 * 1024 * 1024
            .setCache(AndroidVideoCache(this))
            .addInterceptor(PermissionInterceptor(this))
            .addInterceptor(RequestSongInfoInterceptor())
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
            .setNotificationSwitch(true)
            //.setNotificationConfig(notificationConfig)
            .apply()
    }

    /**
     * 权限申请拦截器
     */
    class PermissionInterceptor internal constructor(private val mContext: Context) : AsyncInterceptor() {
        override fun process(songInfo: SongInfo?, callback: InterceptorCallback) {
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
                        mContext.showToast("没有权限，播放失败")
                    }
                })
        }

        override fun getTag(): String = "PermissionInterceptor"
    }

    /**
     * 模拟请求封面，url拦截器
     */
    class RequestSongInfoInterceptor : SyncInterceptor {

        val z = "https://n.sinaimg.cn/sinacn10115/439/w641h598/20200214/4a9b-ipmxpvz8164848.jpg"
        val a = "https://www.yxwoo.com/uploads/images/xiaz/2020/0629/1593419103514.jpg"
        val b = "https://tupian.qqw21.com/article/UploadPic/2020-6/202061122583635797.jpg"
        val c = "https://img.xintp.com/2020/06/05/41xmurd45ei.jpg"
        val d = "https://www.yxwoo.com/uploads/images/xiaz/2020/0629/1593419103842.jpg"
        val e = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRWIl7bedDSfIb9VLSrQWKIS85S0p-Be-qhLw&usqp=CAU"
        val f = "https://i.pinimg.com/736x/05/f9/a2/05f9a2605cd47a742bad0466f632fa72.jpg"
        val g = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQFkIzh8fgjP8MCnV8ziC6nkEkUfMEcbjSssA&usqp=CAU"

        val map = hashMapOf<String, String>()

        init {
            map["z"] = z
            map["a"] = a
            map["b"] = b
            map["c"] = c
            map["d"] = d
            map["e"] = e
            map["f"] = f
            map["g"] = g
        }

        override fun process(songInfo: SongInfo?): SongInfo? {
            if (songInfo?.songCover.isNullOrEmpty()) {
                songInfo?.songCover = map[songInfo?.songName].orEmpty()
            }
            if (songInfo?.songUrl.isNullOrEmpty()) {
                val name = songInfo?.songName + "(" + songInfo?.songId + ").m4a"
                songInfo?.songUrl = MusicViewModel.baseUrl + name
            }
            return songInfo
        }

        override fun getTag(): String = "RequestSongCoverInterceptor"
    }

    /**
     * 使用 AndroidVideoCache 这个第三方库做缓存的例子
     */
    class AndroidVideoCache(private val context: Context) : ICache {

        private var proxy: HttpProxyCacheServer? = null
        private var cacheFile: File? = null

        private fun getProxy(songInfo: SongInfo?): HttpProxyCacheServer? {
            return if (proxy == null) newProxy(songInfo).also { proxy = it } else proxy
        }

        private fun newProxy(songInfo: SongInfo?): HttpProxyCacheServer? {
            val builder = HttpProxyCacheServer.Builder(context)
                .maxCacheSize(1024 * 1024 * 1024)       // 1 Gb for cache
                .cacheDirectory(getCacheDirectory(context, ""))
            if (songInfo == null) {
                builder.fileNameGenerator(Md5FileNameGenerator())
            } else {
                builder.fileNameGenerator { url ->
                    val extension = getExtension(url)
                    val name = songInfo.songId
                    if (extension.isEmpty()) name else "$name.$extension"
                }
            }
            return builder.build()
        }

        private fun getExtension(url: String?): String {
            if (url.isNullOrEmpty()) return ""
            val dotIndex = url.lastIndexOf('.')
            val slashIndex = url.lastIndexOf('/')
            return if (dotIndex != -1 && dotIndex > slashIndex && dotIndex + 2 + 4 > url.length) url.substring(dotIndex + 1, url.length) else ""
        }

        override fun getProxyUrl(url: String, songInfo: SongInfo): String? {
            return if (isOpenCache()) getProxy(songInfo)?.getProxyUrl(url) else url
        }

        override fun isOpenCache(): Boolean {
            return StarrySkyConstant.KEY_CACHE_SWITCH
        }

        override fun getCacheDirectory(context: Context, destFileDir: String?): File? {
            var fileDir = destFileDir
            if (fileDir.isNullOrEmpty()) {
                fileDir = "00StarrySkyCache/".toSdcardPath()
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
            return true
            // return getProxy(null)?.isCached(url) ?: false
        }
    }
}