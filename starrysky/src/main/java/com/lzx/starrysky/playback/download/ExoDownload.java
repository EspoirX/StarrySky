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

    private static Context sContext;
    private String userAgent;
    private DownloadManager downloadManager;
    private Cache downloadCache;
    private File downloadDirectory;
    private DownloadTracker downloadTracker;
    private String destFileDir;
    private boolean isOpenCache = false;

    public static ExoDownload getInstance() {
        return SingletonHolder.sInstance;
    }

    private static class SingletonHolder {
        private static final ExoDownload sInstance = new ExoDownload();
    }

    public static void initExoDownload(Context context) {
        sContext = context;
    }

    private ExoDownload() {
        userAgent = Utils.getUserAgent(sContext, "ExoPlayback");
    }

    public void setCacheDestFileDir(String destFileDir) {
        this.destFileDir = destFileDir;
    }

    public boolean isOpenCache() {
        return isOpenCache;
    }

    public void setOpenCache(boolean openCache) {
        isOpenCache = openCache;
    }

    public DownloadManager getDownloadManager() {
        initDownloadManager(sContext);
        return downloadManager;
    }

    public DownloadTracker getDownloadTracker() {
        initDownloadManager(sContext);
        return downloadTracker;
    }

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
                            /* context= */ sContext,
                            buildDataSourceFactory(sContext),
                            new File(getDownloadDirectory(context), DOWNLOAD_TRACKER_ACTION_FILE));
            downloadManager.addListener(downloadTracker);
        }
    }

    public synchronized Cache getDownloadCache() {
        if (downloadCache == null) {
            File downloadContentDirectory = new File(getDownloadDirectory(sContext), DOWNLOAD_CONTENT_DIRECTORY);
            downloadCache = new SimpleCache(downloadContentDirectory, new NoOpCacheEvictor());
        }
        return downloadCache;
    }

    /**
     * 设置缓存文件夹
     */
    private File getDownloadDirectory(Context context) {
        //String destFileDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/cacheDir";
        if (!TextUtils.isEmpty(destFileDir)) {
            downloadDirectory = new File(destFileDir);
        }
        if (downloadDirectory == null) {
            downloadDirectory = context.getExternalFilesDir(null);
            if (downloadDirectory == null) {
                downloadDirectory = context.getFilesDir();
            }
        }
        return downloadDirectory;
    }

    public DataSource.Factory buildDataSourceFactory(Context context) {
        DefaultDataSourceFactory upstreamFactory =
                new DefaultDataSourceFactory(context, buildHttpDataSourceFactory());
        return buildReadOnlyCacheDataSource(upstreamFactory, getDownloadCache());
    }

    public HttpDataSource.Factory buildHttpDataSourceFactory() {
        return new DefaultHttpDataSourceFactory(userAgent);
    }

    private static CacheDataSourceFactory buildReadOnlyCacheDataSource(
            DefaultDataSourceFactory upstreamFactory, Cache cache) {
        return new CacheDataSourceFactory(
                cache,
                upstreamFactory,
                new FileDataSourceFactory(),
                /* cacheWriteDataSinkFactory= */ null,
                CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR,
                /* eventListener= */ null);
    }
}
