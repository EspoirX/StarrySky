package com.lzx.starrysky.utils

import android.os.Build
import android.util.Log
import com.lzx.starrysky.control.RepeatMode
import org.json.JSONObject

object StarrySkyUtils {
    var isDebug = true

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