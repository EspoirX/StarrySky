package com.lzx.starrysky.provider

import android.graphics.Bitmap
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * 面向用户的音频信息
 */

@Parcelize
class SongInfo(
    var songId: String = "", //音乐id
    var songName: String = "",  //音乐标题
    var songCover: String = "",  //音乐封面
    var songHDCover: String = "",  //专辑封面(高清)
    var songSquareCover: String = "",  //专辑封面(正方形)
    var songRectCover: String = "",  //专辑封面(矩形)
    var songRoundCover: String = "",  //专辑封面(圆形)
    var songNameKey: String = "",
    var songCoverBitmap: Bitmap? = null,
    var songUrl: String = "",  //音乐播放地址
    var genre: String = "",  //类型（流派）
    var type: String = "",  //类型
    var size: String = "", //音乐大小
    var duration: Long = -1, //音乐长度
    var artist: String = "",  //音乐艺术家
    var artistKey: String = "",
    var artistId: String = "",  //音乐艺术家id
    var downloadUrl: String = "",  //音乐下载地址
    var site: String = "",  //地点
    var favorites: Int = 0, //喜欢数
    var playCount: Int = 0, //播放数
    var trackNumber: Int = 0, //媒体的曲目号码（序号：1234567……）
    var language: String = "", //语言
    var country: String = "",  //地区
    var proxyCompany: String = "", //代理公司
    var publishTime: String = "", //发布时间
    var year: String = "",  //录制音频文件的年份
    var modifiedTime: String = "",  //最后修改时间
    var description: String = "",  //音乐描述
    var versions: String = "",  //版本
    var mimeType: String = "",
    var albumId: String = "",     //专辑id
    var albumName: String = "",   //专辑名称
    var albumNameKey: String = "",
    var albumCover: String = "",  //专辑封面
    var albumHDCover: String = "",  //专辑封面(高清)
    var albumSquareCover: String = "",  //专辑封面(正方形)
    var albumRectCover: String = "",  //专辑封面(矩形)
    var albumRoundCover: String = "",  //专辑封面(圆形)
    var albumArtist: String = "",      //专辑艺术家
    var albumSongCount: Int = 0,     //专辑音乐数
    var albumPlayCount: Int = 0    //专辑播放数
) : Parcelable {


    override fun equals(obj: Any?): Boolean {
        if (this === obj) {
            return true
        }
        if (obj == null || javaClass != obj.javaClass) {
            return false
        }
        val songInfo = obj as SongInfo?
        return songId == songInfo!!.songId && songUrl == songInfo.songUrl
    }

    override fun hashCode(): Int {
        var result = songId.hashCode()
        result = 31 * result + songName.hashCode()
        result = 31 * result + songCover.hashCode()
        result = 31 * result + songHDCover.hashCode()
        result = 31 * result + songSquareCover.hashCode()
        result = 31 * result + songRectCover.hashCode()
        result = 31 * result + songRoundCover.hashCode()
        result = 31 * result + songNameKey.hashCode()
        result = 31 * result + (songCoverBitmap?.hashCode() ?: 0)
        result = 31 * result + songUrl.hashCode()
        result = 31 * result + genre.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + size.hashCode()
        result = 31 * result + duration.hashCode()
        result = 31 * result + artist.hashCode()
        result = 31 * result + artistKey.hashCode()
        result = 31 * result + artistId.hashCode()
        result = 31 * result + downloadUrl.hashCode()
        result = 31 * result + site.hashCode()
        result = 31 * result + favorites
        result = 31 * result + playCount
        result = 31 * result + trackNumber
        result = 31 * result + language.hashCode()
        result = 31 * result + country.hashCode()
        result = 31 * result + proxyCompany.hashCode()
        result = 31 * result + publishTime.hashCode()
        result = 31 * result + year.hashCode()
        result = 31 * result + modifiedTime.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + versions.hashCode()
        result = 31 * result + mimeType.hashCode()
        result = 31 * result + albumId.hashCode()
        result = 31 * result + albumName.hashCode()
        result = 31 * result + albumNameKey.hashCode()
        result = 31 * result + albumCover.hashCode()
        result = 31 * result + albumHDCover.hashCode()
        result = 31 * result + albumSquareCover.hashCode()
        result = 31 * result + albumRectCover.hashCode()
        result = 31 * result + albumRoundCover.hashCode()
        result = 31 * result + albumArtist.hashCode()
        result = 31 * result + albumSongCount
        result = 31 * result + albumPlayCount
        return result
    }
}
