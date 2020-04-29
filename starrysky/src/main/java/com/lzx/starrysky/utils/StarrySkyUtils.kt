package com.lzx.starrysky.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import android.support.v4.media.MediaMetadataCompat
import android.text.TextUtils
import android.util.Log
import com.google.android.exoplayer2.ExoPlayerLibraryInfo
import com.lzx.starrysky.control.RepeatMode
import com.lzx.starrysky.ext.album
import com.lzx.starrysky.ext.albumArtUri
import com.lzx.starrysky.ext.artist
import com.lzx.starrysky.ext.duration
import com.lzx.starrysky.ext.id
import com.lzx.starrysky.ext.mediaUri
import com.lzx.starrysky.ext.title
import com.lzx.starrysky.provider.SongInfo
import com.lzx.starrysky.utils.SpUtil.Companion.instance
import org.json.JSONObject

object StarrySkyUtils {
    var isDebug = true

    /**
     * 判断Activity 是否可用
     *
     * @param activity 目标Activity
     * @return true of false
     */
    fun isActivityAvailable(activity: Activity?): Boolean {
        if (null == activity) {
            return false
        }
        if (activity.isFinishing) {
            return false
        }
        return !(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && activity.isDestroyed)
    }

    fun isPatchProcess(context: Context): Boolean {
        val am =
            context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningApp =
            am.runningAppProcesses
        return if (runningApp == null) {
            false
        } else {
            val var3: Iterator<*> = runningApp.iterator()
            var info: RunningAppProcessInfo
            do {
                if (!var3.hasNext()) {
                    return false
                }
                info = var3.next() as RunningAppProcessInfo
            } while (info.pid != Process.myPid())
            info.processName.endsWith("patch")
        }
    }

    fun getUserAgent(
        context: Context, applicationName: String
    ): String {
        val versionName: String
        versionName = try {
            val packageName = context.packageName
            val info =
                context.packageManager.getPackageInfo(packageName, 0)
            info.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            "?"
        }
        return (applicationName + "/" + versionName + " (Linux;Android " + Build.VERSION.RELEASE
            + ") " + ExoPlayerLibraryInfo.VERSION_SLASHY)
    }

    /**
     * 反射一下主线程获取一下上下文
     */
    val contextReflex: Application?
        get() = try {
            @SuppressLint("PrivateApi") val activityThreadClass =
                Class.forName("android.app.ActivityThread")

            @SuppressLint("DiscouragedPrivateApi")
            val currentApplicationMethod =
                activityThreadClass.getDeclaredMethod("currentApplication")
            currentApplicationMethod.isAccessible = true
            currentApplicationMethod.invoke(null) as Application
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }

    fun saveRepeatMode(repeatMode: Int, isLoop: Boolean) {
        try {
            val jsonObject = JSONObject()
            jsonObject.put("repeatMode", repeatMode)
            jsonObject.put("isLoop", isLoop)
            instance.putString(
                RepeatMode.KEY_REPEAT_MODE, jsonObject.toString())
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    val repeatMode: RepeatMode
        get() {
            val json = instance.getString(
                RepeatMode.KEY_REPEAT_MODE)
            val defaultMode =
                RepeatMode(
                    RepeatMode.REPEAT_MODE_NONE, true)
            return if (TextUtils.isEmpty(json)) {
                defaultMode
            } else {
                try {
                    val jsonObject = JSONObject(json)
                    RepeatMode(jsonObject.getInt("repeatMode"),
                        jsonObject.getBoolean("isLoop"))
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    defaultMode
                }
            }
        }

    fun formatStackTrace(stackTrace: Array<StackTraceElement>): String {
        val sb = StringBuilder()
        for (element in stackTrace) {
            sb.append("    at ").append(element.toString())
            sb.append("\n")
        }
        return sb.toString()
    }

    /**
     * SongInfo 转 MediaMetadataCompat
     */
    @Synchronized
    fun toMediaMetadata(info: SongInfo): MediaMetadataCompat {
        val albumTitle = if (info.songName.isNotEmpty()) info.songName else ""
        val songCover = if (info.songCover.isNotEmpty()) info.songCover else ""
        val builder = MediaMetadataCompat.Builder()
        builder.id = info.songId
        builder.mediaUri = info.songUrl
        if (albumTitle.isNotEmpty()) {
            builder.album = albumTitle
        }
        if (info.duration != -1L) {
            builder.duration = info.duration
        }
        if (songCover.isNotEmpty()) {
            builder.albumArtUri = songCover
        }
        if (info.songName.isNotEmpty()) {
            builder.title = info.songName
        }
        if (info.artist.isNotEmpty()) {
            builder.artist = info.artist
        }
        return builder.build()
    }

    fun log(msg: String?) {
        if (isDebug) {
            Log.i("StarrySky", msg)
        }
    }
}