package com.lzx.basecode

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.content.Intent
import android.os.Build
import android.os.Process
import android.widget.Toast
import java.util.Locale

fun Context.getResourceId(name: String, className: String): Int {
    val packageName = applicationContext.packageName
    val res = applicationContext.resources
    return res.getIdentifier(name, className, packageName)
}

fun Context.getPendingIntent(requestCode: Int, action: String): PendingIntent {
    val packageName = applicationContext.packageName
    val intent = Intent(action)
    intent.setPackage(packageName)
    return PendingIntent.getBroadcast(this, requestCode, intent, PendingIntent.FLAG_CANCEL_CURRENT)
}

fun Context.showToast(msg: String?) {
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}

fun Context.isMainProcess(): Boolean {
    val am = this.getSystemService(ACTIVITY_SERVICE) as ActivityManager
    val runningApp = am.runningAppProcesses
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
        this.packageName == info.processName
    }
}

fun Context.isPatchProcess(): Boolean {
    val am = this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val runningApp = am.runningAppProcesses
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

/**
 * 判断Activity 是否可用
 */
fun Context?.isActivityAvailable(): Boolean {
    if (null == this) return false
    if (this !is Activity) return false
    if (this.isFinishing) return false
    return !(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && this.isDestroyed)
}


/**
 * 反射一下主线程获取一下上下文
 */
val contextReflex: Application?
    get() = try {
        @SuppressLint("PrivateApi")
        val activityThreadClass = Class.forName("android.app.ActivityThread")

        @SuppressLint("DiscouragedPrivateApi")
        val currentApplicationMethod = activityThreadClass.getDeclaredMethod("currentApplication")
        currentApplicationMethod.isAccessible = true
        currentApplicationMethod.invoke(null) as Application
    } catch (ex: Exception) {
        ex.printStackTrace()
        null
    }

fun <T> Int.isIndexPlayable(queue: List<T>?): Boolean {
    return queue != null && this >= 0 && this < queue.size
}

/**
 * 得到目标界面 Class
 */
fun String?.getTargetClass(): Class<*>? {
    var clazz: Class<*>? = null
    try {
        if (!this.isNullOrEmpty()) {
            clazz = Class.forName(this)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return clazz
}

fun String.md5(): String {
    return MD5.hexdigest(this)
}

fun String.isRTMP(): Boolean {
    return this.toLowerCase(Locale.getDefault()).startsWith("rtmp://")
}

fun String.isFLAC(): Boolean {
    return this.toLowerCase(Locale.getDefault()).endsWith(".flac")
}