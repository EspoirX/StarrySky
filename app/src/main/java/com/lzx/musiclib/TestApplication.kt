package com.lzx.musiclib

import android.Manifest
import android.app.Application
import android.content.Context
import android.text.TextUtils
import android.widget.Toast
import com.lzx.musiclib.example.MusicRequest
import com.lzx.musiclib.example.MusicRequest.RequestInfoCallback
import com.lzx.starrysky.StarrySky.Companion.init
import com.lzx.starrysky.StarrySkyConfig
import com.lzx.starrysky.intercept.InterceptorCallback
import com.lzx.starrysky.intercept.StarrySkyInterceptor
import com.lzx.starrysky.provider.SongInfo
import com.lzx.starrysky.utils.MainLooper
import com.lzx.starrysky.utils.StarrySkyUtils
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
        val config = StarrySkyConfig().newBuilder()
            .addInterceptor(RequestSongInfoInterceptor(this))
            .isOpenNotification(true)
            .build()
        init(this, config)
        StarrySkyUtils.isDebug = true
        CrashReport.initCrashReport(applicationContext, "9e447caa98", false)
    }

    class RequestSongInfoInterceptor internal constructor(private val mContext: Context) :
        StarrySkyInterceptor {
        private val mMusicRequest: MusicRequest = MusicRequest()
        override fun process(
            songInfo: SongInfo?, mainLooper: MainLooper, callback: InterceptorCallback
        ) {
            if (songInfo == null) {
                callback.onInterrupt(RuntimeException("SongInfo is null"))
                return
            }
            SoulPermission.getInstance().checkAndRequestPermissions(Permissions.build(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE),
                object : CheckRequestPermissionsListener {
                    override fun onAllPermissionOk(allPermissions: Array<Permission>) {
                        if (TextUtils.isEmpty(songInfo.songUrl)) {
                            mMusicRequest.requestSongUrl(songInfo.songId,
                                object : RequestInfoCallback {
                                    override fun onSuccess(songUrl: String) {
                                        songInfo.songUrl = songUrl //给songInfo设置Url
                                        callback.onContinue(songInfo)
                                    }
                                })
                        } else {
                            callback.onContinue(songInfo)
                        }
                    }

                    override fun onPermissionDenied(refusedPermissions: Array<Permission>) {
                        mainLooper.runOnUiThread(Runnable {
                            Toast.makeText(mContext, "没有权限，播放失败", Toast.LENGTH_SHORT).show()
                        })
                    }
                })
        }
    }
}