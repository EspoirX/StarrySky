package com.lzx.basecode

import android.graphics.Bitmap
import android.os.Parcelable
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

/**
 * 面向用户的音频信息
 */
@Parcelize
class SongInfo(
    var songId: String = "", //音乐id
    var songUrl: String = "",  //音乐播放地址
    var songName: String = "",  //音乐标题
    var artist: String = "",    //作者
    var songCover: String = "",  //音乐封面
    var coverBitmap: Bitmap? = null,  //音乐封面
    var duration: Long = -1, //音乐长度
    var decode: Boolean = false, //是否需要解码，如果要解码，最好用本地音频
    var headData: HashMap<String, String>? = hashMapOf() //header 信息
) : Parcelable, Cloneable {

    @IgnoredOnParcel
    var objectValue: Any = Any()

    var tag: Any? = null //为某些错误做准备的标签

    override fun clone(): Any {
        var obj: Any? = null
        //调用Object类的clone方法，返回一个Object实例
        try {
            obj = super.clone()
        } catch (e: CloneNotSupportedException) {
            e.printStackTrace()
        }
        return obj!!
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SongInfo

        if (songId != other.songId) return false
        if (songUrl != other.songUrl) return false
        if (songName != other.songName) return false
        if (artist != other.artist) return false
        if (songCover != other.songCover) return false
        if (duration != other.duration) return false
        if (decode != other.decode) return false
        if (headData != other.headData) return false
        if (objectValue != other.objectValue) return false

        return true
    }

    override fun hashCode(): Int {
        var result = songId.hashCode()
        result = 31 * result + songUrl.hashCode()
        result = 31 * result + songName.hashCode()
        result = 31 * result + artist.hashCode()
        result = 31 * result + songCover.hashCode()
        result = 31 * result + duration.hashCode()
        result = 31 * result + decode.hashCode()
        result = 31 * result + (headData?.hashCode() ?: 0)
        result = 31 * result + objectValue.hashCode()
        return result
    }
}

fun SongInfo?.isRefrain(): Boolean {
    return "Refrain" == this?.headData?.get("SongType")
}