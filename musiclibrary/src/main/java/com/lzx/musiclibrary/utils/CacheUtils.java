package com.lzx.musiclibrary.utils;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.IOException;

/**
 * 音频缓存工具类
 * Created by xian on 2018/3/31.
 */

public class CacheUtils {

    private static String defaultPath = "/musicLibrary/song-cache/";

    public static File getSongCacheDir() {
        File filePath = new File(getStorageDirectoryPath() + getCachePath());
        if (!filePath.exists()) {
            filePath.mkdirs();
        }
        return filePath;
    }

    public static void cleanSongCacheDir() throws IOException {
        File videoCacheDir = getSongCacheDir();
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
}
