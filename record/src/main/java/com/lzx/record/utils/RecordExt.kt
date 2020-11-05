package com.lzx.record.utils

import android.app.Activity
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.os.Build
import androidx.annotation.RequiresApi
import com.lzx.basecode.orDef


fun Activity.hasPermission(permission: String): Boolean {
    return !isMarshmallow() || isGranted(permission)
}

fun isMarshmallow(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M

@RequiresApi(Build.VERSION_CODES.M)
fun Activity.isGranted(permission: String): Boolean {
    return this.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
}

fun Int.format(): Int {
    return when (this) {
        AudioFormat.ENCODING_PCM_8BIT -> 8
        AudioFormat.ENCODING_PCM_16BIT -> 16
        else -> 0
    }
}

fun Int?.safeQuality(): Int = when {
    this.orDef() < 0 -> 0
    this.orDef() > 9 -> 9
    else -> this.orDef()
}