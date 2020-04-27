package com.lzx.starrysky.playback.offline

import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.upstream.cache.Cache
import com.google.android.exoplayer2.upstream.cache.CacheSpan
import com.google.android.exoplayer2.upstream.cache.CacheUtil
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.lzx.starrysky.StarrySky
import com.lzx.starrysky.playback.player.ExoSourceManager
import java.io.File

class ExoCache(private val context: Context) : ICache {

    private var cacheFile: File? = null
    private var exoCache: Cache? = null

    override fun startCache(url: String) {
        //什么都不做
    }

    override fun getProxyUrl(url: String): String? {
        return null
    }

    override fun isOpenCache(): Boolean {
        return true
    }

    override fun getCacheDirectory(context: Context, destFileDir: String?): File? {
        if (cacheFile == null && !destFileDir.isNullOrEmpty()) {
            cacheFile = File(destFileDir)
            if (cacheFile?.exists() == false) {
                cacheFile?.mkdirs()
            }
        }
        if (cacheFile == null) {
            cacheFile = context.getExternalFilesDir(null)
            if (cacheFile == null) {
                cacheFile = context.filesDir
            }
        }
        return cacheFile
    }

    /**
     * 获取缓存实例
     */
    @Synchronized
    fun getDownloadCache(): Cache? {
        if (exoCache == null) {
            val cacheFile = getCacheDirectory(context, StarrySky.get().config().cacheDestFileDir)
            val path = cacheFile?.absolutePath
            val isLocked = SimpleCache.isCacheFolderLocked(File(path))
            if (!isLocked) {
                exoCache = SimpleCache(cacheFile,
                    LeastRecentlyUsedCacheEvictor(ExoSourceManager.DEFAULT_MAX_SIZE.toLong()))
            }
        }
        return exoCache
    }

    override fun isCache(url: String): Boolean {
        var isCache = true
        val cache = getDownloadCache()
        if (url.isNotEmpty()) {
            val key = CacheUtil.generateKey(Uri.parse(url))
            if (!key.isNullOrEmpty()) {
                val cachedSpans = cache?.getCachedSpans(key)
                if (cachedSpans?.size == 0) {
                    isCache = false
                } else {
                    isCache = cache?.let {
                        val contentLength =
                            cache.getContentMetadata(key)["exo_len", C.LENGTH_UNSET.toLong()]
                        var currentLength: Long = 0
                        for (cachedSpan in cachedSpans ?: hashSetOf<CacheSpan>()) {
                            currentLength += cache.getCachedLength(key, cachedSpan.position,
                                cachedSpan.length)
                        }
                        return currentLength >= contentLength
                    } ?: false
                }
            } else {
                isCache = false
            }
        }
        return isCache
    }
}