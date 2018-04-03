package com.lzx.musiclibrary.cache;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import com.danikula.videocache.HttpProxyCacheServer;

import java.io.File;
import java.io.IOException;

/**
 * 音频缓存工具类
 * Created by xian on 2018/3/31.
 */

public class CacheUtils {

    private static String defaultPath = "/musicLibrary/song-cache/";

    public static File getDefaultSongCacheDir() {
        return getSongCacheDir(getStorageDirectoryPath() + getCachePath());
    }

    public static File getSongCacheDir(String path) {
        File filePath = new File(path);
        if (!filePath.exists()) {
            filePath.mkdirs();
        }
        return filePath;
    }

    public static void cleanSongCacheDir() throws IOException {
        File videoCacheDir = getDefaultSongCacheDir();
        cleanDirectory(videoCacheDir);
    }

    public static String getStorageDirectoryPath() {
        return Environment.getExternalStorageDirectory().getPath();
    }

    public static String getCachePath() {
        return defaultPath;
    }

    public static void setCachePath(String path) {
        defaultPath = path;
    }

    private static void cleanDirectory(File file) throws IOException {
        if (!file.exists()) {
            return;
        }
        File[] contentFiles = file.listFiles();
        if (contentFiles != null) {
            for (File contentFile : contentFiles) {
                delete(contentFile);
            }
        }
    }

    private static void delete(File file) throws IOException {
        if (file.isFile() && file.exists()) {
            deleteOrThrow(file);
        } else {
            cleanDirectory(file);
            deleteOrThrow(file);
        }
    }

    private static void deleteOrThrow(File file) throws IOException {
        if (file.exists()) {
            boolean isDeleted = file.delete();
            if (!isDeleted) {
                throw new IOException(String.format("File %s can't be deleted", file.getAbsolutePath()));
            }
        }
    }


    public static HttpProxyCacheServer.Builder createHttpProxyCacheServerBuilder(Context context, CacheConfig cacheConfig) {
        HttpProxyCacheServer.Builder builder = new HttpProxyCacheServer.Builder(context);
        if (cacheConfig == null) {
            builder.cacheDirectory(CacheUtils.getDefaultSongCacheDir())
                    .maxCacheSize(1024 * 1024 * 1024) //1G
                    .fileNameGenerator(new MusicMd5Generator());
        } else {
            String configCachePath = cacheConfig.getCachePath();
            int configMaxSize = cacheConfig.getMaxCacheSize();
            int configMaxFileCount = cacheConfig.getMaxCacheFilesCount();
            builder.cacheDirectory(
                    !TextUtils.isEmpty(configCachePath)
                            ? CacheUtils.getSongCacheDir(configCachePath)
                            : CacheUtils.getDefaultSongCacheDir());
            builder.maxCacheSize(configMaxSize != 0 ? configMaxSize : 1024 * 1024 * 1024);
            if (configMaxFileCount != 0) {
                builder.maxCacheFilesCount(configMaxFileCount);
            }
            builder.fileNameGenerator(new MusicMd5Generator());
        }
        return builder;
    }
}
