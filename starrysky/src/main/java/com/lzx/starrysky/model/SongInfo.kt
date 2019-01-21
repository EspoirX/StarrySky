package com.lzx.starrysky.model

import android.graphics.Bitmap

class SongInfo {
    val songId = "" //音乐id
    val songName = "" //音乐标题
    val songCover = "" //音乐封面
    val songHDCover = "" //专辑封面(高清)
    val songSquareCover = "" //专辑封面(正方形)
    val songRectCover = "" //专辑封面(矩形)
    val songRoundCover = "" //专辑封面(圆形)
    val songCoverBitmap: Bitmap? = null
    val songUrl = "" //音乐播放地址
    val genre = "" //类型（流派）
    val type = "" //类型
    val size = "0" //音乐大小
    val duration: Long = 0 //音乐长度
    val artist = "" //音乐艺术家
    val artistId = "" //音乐艺术家id
    val downloadUrl = "" //音乐下载地址
    val site = "" //地点
    val favorites = 0 //喜欢数
    val playCount = 0 //播放数
    val trackNumber: Long = 0 //媒体的曲目号码（序号：1234567……）
    val language = ""//语言
    val country = "" //地区
    val proxyCompany = ""//代理公司
    val publishTime = ""//发布时间
    val description = "" //音乐描述
    val versions = "" //版本

    val albumId = ""    //专辑id
    val albumName = ""  //专辑名称
    val albumCover = "" //专辑封面
    val albumHDCover = "" //专辑封面(高清)
    val albumSquareCover = "" //专辑封面(正方形)
    val albumRectCover = "" //专辑封面(矩形)
    val albumRoundCover = "" //专辑封面(圆形)
    val albumArtist = ""     //专辑艺术家
    val albumSongCount: Long = 0      //专辑音乐数
    val albumPlayCount = 0      //专辑播放数
}
