package com.lzx.starrysky;

import android.content.Context;
import android.support.annotation.NonNull;

import com.lzx.starrysky.notification.StarrySkyNotificationManager;
import com.lzx.starrysky.playback.offline.StarrySkyCacheManager;
import com.lzx.starrysky.registry.StarrySkyRegistry;

/**
 * StarrySky 初始化配置类
 */
public abstract class StarrySkyConfig {

    /**
     * 通用配置
     */
    public void applyOptions(@NonNull Context context, @NonNull StarrySkyBuilder builder) {
    }

    /**
     * 添加组件
     */
    public void applyStarrySkyRegistry(@NonNull Context context, StarrySkyRegistry registry) {
    }

    /**
     * 通知栏配置
     */
    public StarrySkyNotificationManager.NotificationFactory getNotificationFactory() {
        return null;
    }

    /**
     * 缓存配置
     */
    public StarrySkyCacheManager.CacheFactory getCacheFactory() {
        return null;
    }
}
