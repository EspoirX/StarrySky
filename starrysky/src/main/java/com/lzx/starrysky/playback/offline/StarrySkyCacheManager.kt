package com.lzx.starrysky.playback.offline

import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.upstream.FileDataSourceFactory
import com.google.android.exoplayer2.upstream.HttpDataSource
import com.google.android.exoplayer2.upstream.cache.Cache
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.CacheSpan
import com.google.android.exoplayer2.upstream.cache.CacheUtil
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.lzx.starrysky.playback.player.ExoSourceManager
import com.lzx.starrysky.utils.StarrySkyUtils
import java.io.File
import com.lzx.starrysky.playback.offline.StarrySkyCache as StarrySkyCache1

class StarrySkyCacheManager constructor(
    private val context: Context,
    private val isOpenCache: Boolean,
    private val cacheDestFileDir: String?,
    factory: CacheFactory?
) {

    private var factory: CacheFactory? = null
    private val userAgent: String
    private var starrySkyCache: StarrySkyCache1? = null
    private var downloadDirectory: File? = null
    private var downloadCache: Cache? = null

    init {
        if (factory == null && isOpenCache) {
            this.factory = object : CacheFactory {
                override fun build(
                    context: Context, manager: StarrySkyCacheManager
                ): StarrySkyCache1 {
                    //return ExoCache(context, manager)
                    return DefaultCache()
                }
            }
        } else {
            this.factory = factory
        }
        userAgent = StarrySkyUtils.getUserAgent(context,
            if (context.applicationInfo != null)
                context.applicationInfo.name
            else
                "StarrySky")
    }

    fun getStarrySkyCache(context: Context): StarrySkyCache1? {
        if (starrySkyCache == null) {
            synchronized(this) {
                if (starrySkyCache == null && factory != null) {
                    starrySkyCache = factory!!.build(context, this)
                }
            }
        }
        return starrySkyCache
    }

    fun isOpenCache(): Boolean {
        return isOpenCache
    }

    /**
     * 创建缓存文件夹
     */
    fun getDownloadDirectory(context: Context): File {
        if (!cacheDestFileDir.isNullOrEmpty()) {
            downloadDirectory = File(cacheDestFileDir)
            if (!downloadDirectory!!.exists()) {
                downloadDirectory!!.mkdirs()
            }
        }
        if (downloadDirectory == null) {
            downloadDirectory = context.getExternalFilesDir(null)
            if (downloadDirectory == null) {
                downloadDirectory = context.filesDir
            }
        }
        return downloadDirectory!!
    }

    /**
     * 获取缓存实例
     */
    @Synchronized
    fun getDownloadCache(): Cache? {
        if (downloadCache == null) {
            val path = getDownloadDirectory(context).absolutePath
            val downloadContentDirectory = getDownloadDirectory(context)
            val isLocked = SimpleCache.isCacheFolderLocked(File(path))
            if (!isLocked) {
                downloadCache = SimpleCache(downloadContentDirectory,
                    LeastRecentlyUsedCacheEvictor(ExoSourceManager.DEFAULT_MAX_SIZE.toLong()))
            }
        }
        return downloadCache
    }

    interface CacheFactory {
        fun build(context: Context, manager: StarrySkyCacheManager): StarrySkyCache1
    }
}