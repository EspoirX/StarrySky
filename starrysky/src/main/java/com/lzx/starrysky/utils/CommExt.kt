package com.lzx.starrysky.utils

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
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