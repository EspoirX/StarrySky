package com.lzx.starrysky.registry;

import com.lzx.starrysky.playback.offline.StarrySkyCacheManager;

public class CacheRegistry {
    StarrySkyCacheManager cacheManager;

    public StarrySkyCacheManager getCacheManager() {
        return cacheManager;
    }

    public void setCacheManager(StarrySkyCacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }
}
