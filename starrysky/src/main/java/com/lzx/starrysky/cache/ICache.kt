package com.lzx.starrysky.cache

import android.content.Context
import com.lzx.starrysky.SongInfo
import java.io.File

interface ICache {

    /**
     * 代理url，如果已经有缓存了，你可以用它来返回缓存地址，如果为空则用正常的 url
     */
    fun getProxyUrl(url: String, songInfo: SongInfo): String?

    /**
     * 是否打开缓存
     */
    fun isOpenCache(): Boolean = false

    /**
     * 获取缓存文件夹
     * destFileDir 文件夹路径
     */
    fun getCacheDirectory(context: Context, destFileDir: String?): File?

    /**
     * 是否已经有缓存
     */
    fun isCache(url: String): Boolean
}
