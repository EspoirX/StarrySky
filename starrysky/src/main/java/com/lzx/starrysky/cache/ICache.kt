package com.lzx.starrysky.cache

import android.content.Context
import java.io.File

interface ICache {

    /**
     * 代理url，如果已经有缓存了，你可以用它来返回缓存地址，如果为空则用正常的 url
     */
    fun getProxyUrl(url: String): String?

    /**
     * 是否打开缓存,默认取 StarrySkyConfig 中的配置，重写可修改
     */
    fun isOpenCache(): Boolean = false

    /**
     * 获取缓存文件夹
     * destFileDir 文件夹路径，可通过 StarrySkyConfig 配置
     */
    fun getCacheDirectory(context: Context, destFileDir: String?): File?

    /**
     * 是否已经有缓存
     */
    fun isCache(url: String): Boolean

    /**
     * 开始缓存，会在播放前调用，一般用在自定义下载，因为边播边存框架一般不会这样
     */
    fun startCache(url: String)
}
