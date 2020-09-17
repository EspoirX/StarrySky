package com.lzx.starrysky.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Process
import android.util.Log
import com.lzx.starrysky.control.RepeatMode
import org.json.JSONObject

object StarrySkyUtils {
    var isDebug = true

    /**
     * 判断Activity 是否可用
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
            var info: ActivityManager.RunningAppProcessInfo
            do {
                if (!var3.hasNext()) {
                    return false
                }
                info = var3.next() as ActivityManager.RunningAppProcessInfo
            } while (info.pid != Process.myPid())
            info.processName.endsWith("patch")
        }
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
            SpUtil.instance?.putString(
                RepeatMode.KEY_REPEAT_MODE, jsonObject.toString())
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    val repeatMode: RepeatMode
        get() {
            val json = SpUtil.instance?.getString(RepeatMode.KEY_REPEAT_MODE)
            val defaultMode = RepeatMode(RepeatMode.REPEAT_MODE_NONE, true)
            return if (json.isNullOrEmpty()) {
                defaultMode
            } else {
                try {
                    val jsonObject = JSONObject(json)
                    RepeatMode(jsonObject.getInt("repeatMode"), jsonObject.getBoolean("isLoop"))
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    defaultMode
                }
            }
        }

    //判断是否是android 5.0
    fun isLollipop(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
    }

    fun log(msg: String?) {
        if (isDebug) {
            Log.i("StarrySky", msg)
        }
    }
}