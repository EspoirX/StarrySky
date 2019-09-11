package com.lzx.starrysky.playback.offline;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.android.exoplayer2.offline.DownloadManager;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.FileDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.upstream.cache.Cache;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.lzx.starrysky.playback.Utils;

import java.io.File;

public class StarrySkyCacheManager {

    private static final String DOWNLOAD_CONTENT_DIRECTORY = "downloads"; //下载路径子文件夹

    private boolean isOpenCache;
    private String cacheDestFileDir;
    private File downloadDirectory;
    private Context context;
    private StarrySkyCache starrySkyCache;
    private CacheFactory factory;
    private Cache downloadCache;
    private String userAgent;
    private DownloadManager downloadManager;

    public StarrySkyCacheManager(Context context,
                                 boolean isOpenCache,
                                 String cacheDestFileDir,
                                 CacheFactory factory) {
        this.context = context;
        this.isOpenCache = isOpenCache;
        this.cacheDestFileDir = cacheDestFileDir;
        if (factory == null && isOpenCache) {
            this.factory = EXO_CACHE;
        } else {
            this.factory = factory;
        }
        userAgent = Utils.getUserAgent(context,
                context.getApplicationInfo() != null
                        ? context.getApplicationInfo().name
                        : "StarrySky");
    }

    public StarrySkyCache getStarrySkyCache(Context context) {
        if (starrySkyCache == null) {
            synchronized (this) {
                if (starrySkyCache == null && factory != null) {
                    starrySkyCache = factory.build(context, this);
                }
            }
        }
        return starrySkyCache;
    }

    public boolean isOpenCache() {
        return isOpenCache;
    }

    /**
     * DataSourceFactory构造
     */
    public DataSource.Factory buildDataSourceFactory(Context context) {
        DefaultDataSourceFactory upstreamFactory =
                new DefaultDataSourceFactory(context, buildHttpDataSourceFactory());
        return buildReadOnlyCacheDataSource(upstreamFactory, getDownloadCache());
    }

    public HttpDataSource.Factory buildHttpDataSourceFactory() {
        return new DefaultHttpDataSourceFactory(userAgent);
    }

    private static CacheDataSourceFactory buildReadOnlyCacheDataSource(DefaultDataSourceFactory upstreamFactory, Cache cache) {
        return new CacheDataSourceFactory(
                cache,
                upstreamFactory,
                new FileDataSourceFactory(),
                /* cacheWriteDataSinkFactory= */ null,
                CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR,
                /* eventListener= */ null);
    }

    /**
     * 创建缓存文件夹
     */
    public File getDownloadDirectory(Context context) {
        if (!TextUtils.isEmpty(cacheDestFileDir)) {
            downloadDirectory = new File(cacheDestFileDir);
            if (!downloadDirectory.exists()) {
                downloadDirectory.mkdirs();
            }
        }
        if (downloadDirectory == null) {
            downloadDirectory = context.getExternalFilesDir(null);
            if (downloadDirectory == null) {
                downloadDirectory = context.getFilesDir();
            }
        }
        return downloadDirectory;
    }

    /**
     * 获取缓存实例
     */
    public synchronized Cache getDownloadCache() {
        if (downloadCache == null) {
            File downloadContentDirectory = new File(getDownloadDirectory(context), DOWNLOAD_CONTENT_DIRECTORY);
            downloadCache = new SimpleCache(downloadContentDirectory, new NoOpCacheEvictor());
        }
        return downloadCache;
    }

    public interface CacheFactory {
        @NonNull
        StarrySkyCache build(Context context, StarrySkyCacheManager manager);
    }

    public static final CacheFactory EXO_CACHE = ExoCache::new;
}
