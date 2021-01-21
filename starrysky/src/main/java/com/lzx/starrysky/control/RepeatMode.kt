package com.lzx.starrysky.control

import com.lzx.starrysky.utils.StarrySkyConstant
import org.json.JSONObject

data class RepeatMode(val repeatMode: Int, val isLoop: Boolean) {
    companion object {
        const val REPEAT_MODE_NONE = 100     //顺序播放
        const val REPEAT_MODE_ONE = 200      //单曲播放
        const val REPEAT_MODE_SHUFFLE = 300  //随机播放
        const val REPEAT_MODE_REVERSE = 400  //倒序播放

        fun saveRepeatMode(repeatMode: Int, isLoop: Boolean) {
            try {
                val jsonObject = JSONObject()
                jsonObject.put("repeatMode", repeatMode)
                jsonObject.put("isLoop", isLoop)
                StarrySkyConstant.KEY_REPEAT_MODE = jsonObject.toString()
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }

        val with: RepeatMode
            get() {
                val json = StarrySkyConstant.KEY_REPEAT_MODE
                val defaultMode = RepeatMode(REPEAT_MODE_NONE, true)
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
    }
}

fun Int.isModeNone() = this == RepeatMode.REPEAT_MODE_NONE
fun Int.isModeOne() = this == RepeatMode.REPEAT_MODE_ONE
fun Int.isModeShuffle() = this == RepeatMode.REPEAT_MODE_SHUFFLE
fun Int.isModeReverse() = this == RepeatMode.REPEAT_MODE_REVERSE