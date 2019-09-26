package com.lzx.starrysky.playback.offline

interface StarrySkyCache {

    /**
     * 根据url判断是否已经缓存
     */
    fun isCache(url: String): Boolean

    /**
     * 开始缓存
     */
    fun startCache(mediaId: String, url: String, extension: String)

    /**
     * 根据url删除指定缓存文件
     */
    fun deleteCacheFileByUrl(url: String)

    /**
     * 删除所有缓存文件
     */
    fun deleteAllCacheFile(): Boolean
}
