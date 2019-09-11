package com.lzx.starrysky.playback.offline;

public interface StarrySkyCache {

    /**
     * 根据url判断是否已经缓存
     */
    boolean isCache(String url);

    /**
     * 开始缓存
     */
    void startCache(String mediaId, String url, String extension);

    /**
     * 根据url删除指定缓存文件
     */
    void deleteCacheFileByUrl(String url);

    /**
     * 删除所有缓存文件
     */
    boolean deleteAllCacheFile();
}
