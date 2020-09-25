package com.lzx.starrysky.utils

import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.app.PendingIntent
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.content.Intent
import android.os.Process
import android.widget.Toast
import com.lzx.starrysky.notification.INotification

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

fun Context.getResourceId(name: String, className: String): Int {
    val packageName = applicationContext.packageName
    val res = applicationContext.resources
    return res.getIdentifier(name, className, packageName)
}

fun Context.getPendingIntent(action: String): PendingIntent {
    val packageName = applicationContext.packageName
    val intent = Intent(action)
    intent.setPackage(packageName)
    return PendingIntent.getBroadcast(this, INotification.REQUEST_CODE, intent, PendingIntent.FLAG_CANCEL_CURRENT)
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

fun String.md5(): String {
    return MD5.hexdigest(this)
}