package com.lzx.starrysky;

import android.content.Context;
import android.support.annotation.NonNull;

import com.lzx.starrysky.notification.StarrySkyNotificationManager;
import com.lzx.starrysky.playback.offline.StarrySkyCacheManager;
import com.lzx.starrysky.registry.StarrySkyRegistry;

public abstract class StarrySkyConfig {

    /**
     * 通用配置
     */
    public void applyOptions(@NonNull Context context, @NonNull StarrySkyBuilder builder) {
        // Default empty impl.
    }

    /**
     * 添加播放前规则
     */
    public void applyMediaValid(@NonNull Context context, StarrySkyRegistry registry) {

    }

    /**
     * 通知栏配置
     */
    public StarrySkyNotificationManager.NotificationFactory getNotificationFactory() {
        return null;
    }

    public StarrySkyCacheManager.CacheFactory getCacheFactory() {
        return null;
    }


}
