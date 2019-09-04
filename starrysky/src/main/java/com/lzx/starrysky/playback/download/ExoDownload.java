package com.lzx.starrysky.playback.download;

import android.content.Context;
import android.text.TextUtils;

import com.google.android.exoplayer2.offline.DownloadManager;
import com.google.android.exoplayer2.offline.DownloaderConstructorHelper;
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

public class ExoDownload {

    private static final String DOWNLOAD_ACTION_FILE = "actions";
    private static final String DOWNLOAD_TRACKER_ACTION_FILE = "tracked_actions";
    private static final String DOWNLOAD_CONTENT_DIRECTORY = "downloads"; //下载路径子文件夹
    private static final int MAX_SIMULTANEOUS_DOWNLOADS = 2;

    private Context mContext;
    private String userAgent;
    private DownloadManager downloadManager;
    private Cache downloadCache;
    private File downloadDirectory;
    private DownloadTracker downloadTracker;
    private String destFileDir;
    private boolean isOpenCache = false;
    public static boolean isShowNotificationWhenDownload = false;


    private ExoDownload(Builder builder) {
        mContext = builder.context;
        destFileDir = builder.destFileDir;
        isOpenCache = builder.isOpenCache;
        isShowNotificationWhenDownload = builder.isShowNotificationWhenDownload;
        userAgent = Utils.getUserAgent(mContext, "ExoPlayback");
    }

    public static class Builder {
        private Context context;
        private String destFileDir;
        private boolean isOpenCache = false;
        private boolean isShowNotificationWhenDownload = false;

        public Builder(Context context) {
            this.context = context;
        }

        /**
         * 配置缓存文件夹
         */
        public Builder setCacheDestFileDir(String destFileDir) {
            this.destFileDir = destFileDir;
            return this;
        }

        /**
         * 配置缓存开关
         */
        public Builder setOpenCache(boolean openCache) {
            isOpenCache = openCache;
            return this;
        }

        /**
         * 配置下载时是否显示通知栏
         */
        public Builder setShowNotificationWhenDownload(boolean showNotificationWhenDownload) {
            isShowNotificationWhenDownload = showNotificationWhenDownload;
            return this;
        }

        public ExoDownload build() {
            return new ExoDownload(this);
        }

    }


    /**
     * 是否打开缓存功能
     */
    public boolean isOpenCache() {
        return isOpenCache;
    }


    /**
     * 下载时是否显示通知栏
     */
    public boolean isShowNotificationWhenDownload() {
        return isShowNotificationWhenDownload;
    }


    /**
     * 获取 DownloadManager
     */
    public DownloadManager getDownloadManager() {
        initDownloadManager(mContext);
        return downloadManager;
    }

    /**
     * 获取 DownloadTracker
     */
    public DownloadTracker getDownloadTracker() {
        initDownloadManager(mContext);
        return downloadTracker;
    }

    /**
     * 初始化 DownloadManager
     */
    private synchronized void initDownloadManager(Context context) {
        if (downloadManager == null) {
            DownloaderConstructorHelper downloaderConstructorHelper =
                    new DownloaderConstructorHelper(getDownloadCache(), new DefaultHttpDataSourceFactory(userAgent));
            downloadManager =
                    new DownloadManager(
                            downloaderConstructorHelper,
                            MAX_SIMULTANEOUS_DOWNLOADS,
                            DownloadManager.DEFAULT_MIN_RETRY_COUNT,
                            new File(getDownloadDirectory(context), DOWNLOAD_ACTION_FILE));
            downloadTracker =
                    new DownloadTracker(
                            /* context= */ mContext,
                            buildDataSourceFactory(mContext),
                            new File(getDownloadDirectory(context), DOWNLOAD_TRACKER_ACTION_FILE));
            downloadManager.addListener(downloadTracker);
        }
    }

    /**
     * 获取缓存实例
     */
    public synchronized Cache getDownloadCache() {
        if (downloadCache == null) {
            File downloadContentDirectory = new File(getDownloadDirectory(mContext), DOWNLOAD_CONTENT_DIRECTORY);
            downloadCache = new SimpleCache(downloadContentDirectory, new NoOpCacheEvictor());
        }
        return downloadCache;
    }

    /**
     * 创建缓存文件夹
     */
    private File getDownloadDirectory(Context context) {
        if (!TextUtils.isEmpty(destFileDir)) {
            downloadDirectory = new File(destFileDir);
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
     * 删除所有缓存文件
     */
    public boolean deleteAllCacheFile() {
        if (downloadDirectory == null) {
            downloadDirectory = getDownloadDirectory(mContext);
        }
        for (File file : downloadDirectory.listFiles()) {
            if (file.isFile()) {
                file.delete(); // 删除所有文件
            } else if (file.isDirectory()) {
                deleteAllCacheFile(); // 递规的方式删除文件夹
            }
        }
        return downloadDirectory.delete();// 删除目录本身
    }

    /**
     * 删除某一首歌的缓存
     */
    public void deleteCacheFileByUrl(String url) {
        getDownloadTracker().deleteCacheFileByUrl(url);
    }

    /**
     * 获取媒体缓存大小
     */
    public long getCachedSize() {
        return getDownloadDirectory(mContext).length();
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
}
