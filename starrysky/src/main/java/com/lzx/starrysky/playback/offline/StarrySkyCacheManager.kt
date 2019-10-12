package com.lzx.starrysky.playback.offline

import android.content.Context
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.upstream.FileDataSourceFactory
import com.google.android.exoplayer2.upstream.HttpDataSource
import com.google.android.exoplayer2.upstream.cache.Cache
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.lzx.starrysky.utils.StarrySkyUtils
import java.io.File

class StarrySkyCacheManager constructor(
    private val context: Context,
    private val isOpenCache: Boolean,
    private val cacheDestFileDir: String?,
    factory: CacheFactory?
) {

    private var factory: CacheFactory? = null
    private val userAgent: String
    private var starrySkyCache: StarrySkyCache? = null
    private var downloadDirectory: File? = null
    private var downloadCache: Cache? = null

    companion object {
        const val DOWNLOAD_CONTENT_DIRECTORY = "downloads" //下载路径子文件夹
    }

    init {
        if (factory == null && isOpenCache) {
            this.factory = object : CacheFactory {
                override fun build(
                    context: Context, manager: StarrySkyCacheManager
                ): StarrySkyCache {
                    return ExoCache(context, manager)
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

    fun getStarrySkyCache(context: Context): StarrySkyCache? {

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
     * DataSourceFactory构造
     */
    fun buildDataSourceFactory(context: Context): DataSource.Factory {
        val upstreamFactory = DefaultDataSourceFactory(context, buildHttpDataSourceFactory())
        return buildReadOnlyCacheDataSource(upstreamFactory, getDownloadCache())
    }

    fun buildHttpDataSourceFactory(): HttpDataSource.Factory {
        return DefaultHttpDataSourceFactory(userAgent)
    }

    private fun buildReadOnlyCacheDataSource(
        upstreamFactory: DefaultDataSourceFactory, cache: Cache
    ): CacheDataSourceFactory {
        return CacheDataSourceFactory(
            cache,
            upstreamFactory,
            FileDataSourceFactory(),
            /* eventListener= */ null,
            CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR, null)/* cacheWriteDataSinkFactory= */
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
    fun getDownloadCache(): Cache {
        if (downloadCache == null) {
            val downloadContentDirectory =
                File(getDownloadDirectory(context), DOWNLOAD_CONTENT_DIRECTORY)
            downloadCache = SimpleCache(downloadContentDirectory, NoOpCacheEvictor())
        }
        return downloadCache!!
    }

    interface CacheFactory {
        fun build(context: Context, manager: StarrySkyCacheManager): StarrySkyCache
    }
}