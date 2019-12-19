package com.lzx.starrysky.playback.player

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ads.AdsMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.Cache
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.CacheSpan
import com.google.android.exoplayer2.upstream.cache.CacheUtil
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.exoplayer2.util.Util
import com.lzx.starrysky.utils.StarrySkyUtils
import java.io.File

class ExoSourceManager constructor(private val context: Context) {

    private var dataSource: String = ""
    private var sHttpConnectTimeout = -1
    private var sHttpReadTimeout = -1
    private var sSkipSSLChain = false
    private val mMapHeadData: Map<String, String>? = hashMapOf()
    private var cache: Cache? = null
    private var isCached = false

    companion object {
        const val TYPE_RTMP = 4
        const val TYPE_FLAC = 5
        const val DEFAULT_MAX_SIZE = 512 * 1024 * 1024
    }

    @SuppressLint("DefaultLocale")
    fun buildMediaSource(
        dataSource: String,
        overrideExtension: String?,
        cacheEnable: Boolean,
        cache: Cache?
    ): MediaSource {
        this.dataSource = dataSource
        this.cache = cache
        val dashClassName = "com.google.android.exoplayer2.source.dash.DashMediaSource"
        val ssClassName = "com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource"
        val hlsClassName = "com.google.android.exoplayer2.source.hls.HlsMediaSource"
        val rtmpClassName = "com.google.android.exoplayer2.ext.rtmp.RtmpDataSourceFactory"

        val contentUri = Uri.parse(dataSource)
        val contentType: Int = inferContentType(dataSource, overrideExtension)

        val dataSourceFactory = getDataSourceFactoryCache(cacheEnable)

        when (contentType) {
            C.TYPE_DASH -> {
                checkClassExist(dashClassName,
                    "类 DashMediaSource 不存在，请导入 exoplayer:exoplayer-dash 包")
                val factory: AdsMediaSource.MediaSourceFactory =
                    newClassInstance(dashClassName, dataSourceFactory)
                return factory.createMediaSource(contentUri)
            }
            C.TYPE_SS -> {
                checkClassExist(ssClassName,
                    "类 SsMediaSource 不存在，请导入 exoplayer:exoplayer-smoothstreaming 包")
                val factory: AdsMediaSource.MediaSourceFactory =
                    newClassInstance(ssClassName, dataSourceFactory)
                return factory.createMediaSource(contentUri)
            }
            C.TYPE_HLS -> {
                checkClassExist(hlsClassName, "类 HlsMediaSource 不存在，请导入 exoplayer:exoplayer-hls 包")
                val factory: AdsMediaSource.MediaSourceFactory =
                    newClassInstance(hlsClassName, dataSourceFactory)
                return factory.createMediaSource(contentUri)
            }
            C.TYPE_OTHER -> {
                val factory: DataSource.Factory = dataSourceFactory
                return ExtractorMediaSource.Factory(factory)
                    .setExtractorsFactory(DefaultExtractorsFactory())
                    .createMediaSource(contentUri)
            }
            TYPE_RTMP -> {
                val clazz = Class.forName(rtmpClassName)
                checkClassExist(rtmpClassName,
                    "类 RtmpDataSourceFactory 不存在，请导入 exoplayer:extension-rtmp 包")
                val factory: DataSource.Factory = clazz.newInstance() as DataSource.Factory
                return ExtractorMediaSource.Factory(factory).createMediaSource(contentUri)
            }
            TYPE_FLAC -> {
                val extractorsFactory = DefaultExtractorsFactory()
                return ExtractorMediaSource(contentUri, dataSourceFactory, extractorsFactory, null,
                    null)
            }
            else -> {
                throw IllegalStateException("Unsupported type: $contentType")
            }
        }
    }

    @SuppressLint("WrongConstant", "DefaultLocale")
    @C.ContentType
    fun inferContentType(dataSource: String, overrideExtension: String?): Int {
        val isRtmpSource = dataSource.toLowerCase().startsWith("rtmp://")
        val isFlacSource = dataSource.toLowerCase().endsWith(".flac")
        return when {
            isRtmpSource -> {
                TYPE_RTMP
            }
            isFlacSource -> {
                TYPE_FLAC
            }
            else -> {
                inferContentType(Uri.parse(dataSource), overrideExtension)
            }
        }
    }

    @C.ContentType
    fun inferContentType(uri: Uri?, overrideExtension: String?): Int {
        return Util.inferContentType(uri, overrideExtension)
    }

    private fun getDataSourceFactoryCache(
        cacheEnable: Boolean
    ): DataSource.Factory {
        return if (cacheEnable) {
            if (cache != null) {
                isCached = resolveCacheState(cache, dataSource)
            }
            CacheDataSourceFactory(cache, getDataSourceFactory(),
                CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
        } else {
            getDataSourceFactory()
        }
    }

    /**
     * 根据缓存块判断是否缓存成功
     *
     * @param cache
     */
    private fun resolveCacheState(
        cache: Cache?, url: String?
    ): Boolean {
        var isCache = true
        if (!url.isNullOrEmpty()) {
            val key = CacheUtil.generateKey(Uri.parse(url))
            if (!key.isNullOrEmpty()) {
                val cachedSpans = cache?.getCachedSpans(key)
                if (cachedSpans?.size == 0) {
                    isCache = false
                } else {
                    cache?.let {
                        val contentLength = cache.getContentMetadata(
                            key)["exo_len", C.LENGTH_UNSET.toLong()]
                        var currentLength: Long = 0
                        for (cachedSpan in cachedSpans ?: hashSetOf<CacheSpan>()) {
                            currentLength += cache.getCachedLength(key, cachedSpan.position,
                                cachedSpan.length)
                        }
                        isCache = currentLength >= contentLength
                    }
                }
            } else {
                isCache = false
            }
        }
        return isCache
    }

    private fun getDataSourceFactory(): DataSource.Factory {
        return DefaultDataSourceFactory(context, buildHttpDataSourceFactory())
    }

    private fun buildHttpDataSourceFactory(): DataSource.Factory? {
        var connectTimeout = 8000
        var readTimeout = 8000
        if (sHttpConnectTimeout > 0) {
            connectTimeout = sHttpConnectTimeout
        }
        if (sHttpReadTimeout > 0) {
            readTimeout = sHttpReadTimeout
        }
        var allowCrossProtocolRedirects = false
        if (mMapHeadData != null && mMapHeadData.size > 0) {
            allowCrossProtocolRedirects = "true" == mMapHeadData["allowCrossProtocolRedirects"]
        }
        val userAgent = StarrySkyUtils.getUserAgent(context,
            if (context.applicationInfo != null) context.applicationInfo.name else "StarrySky")
        if (sSkipSSLChain) {
            val dataSourceFactory =
                SkipSSLHttpDataSourceFactory(
                    userAgent,
                    DefaultBandwidthMeter.Builder(context).build(),
                    connectTimeout,
                    readTimeout,
                    allowCrossProtocolRedirects)
            if (mMapHeadData != null && mMapHeadData.size > 0) {
                for ((key, value) in mMapHeadData.entries) {
                    dataSourceFactory.getDefaultRequestProperties().set(key, value)
                }
            }
            return dataSourceFactory
        }
        val dataSourceFactory = DefaultHttpDataSourceFactory(
            userAgent,
            DefaultBandwidthMeter.Builder(context).build(),
            connectTimeout,
            readTimeout,
            allowCrossProtocolRedirects)
        if (mMapHeadData != null && mMapHeadData.size > 0) {
            for ((key, value) in mMapHeadData.entries) {
                dataSourceFactory.defaultRequestProperties[key] = value
            }
        }
        return dataSourceFactory
    }

    private fun checkClassExist(className: String, errorStr: String) {
        try {
            javaClass.classLoader?.loadClass(className)
        } catch (ex: ClassNotFoundException) {
            throw IllegalArgumentException(errorStr)
        }
    }

    private fun newClassInstance(
        className: String, dataSourceFactory: DataSource.Factory
    ): AdsMediaSource.MediaSourceFactory {
        val clazz = Class.forName(className)
        val innerClazz = clazz.declaredClasses
        for (cls in innerClazz) {
            if (cls.name == clazz.name + "\$Factory") {
                val constructors = cls.getConstructor(DataSource.Factory::class.java)
                constructors.isAccessible = true
                return constructors.newInstance(
                    dataSourceFactory) as AdsMediaSource.MediaSourceFactory
            }
        }

        throw IllegalArgumentException("获取 " + clazz.name + "\$Factory 实例失败")
    }
}