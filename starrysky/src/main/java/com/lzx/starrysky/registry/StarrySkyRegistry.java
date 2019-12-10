package com.lzx.starrysky.registry;

import android.content.Context;
import android.util.Log;

import com.lzx.starrysky.notification.NotificationConfig;
import com.lzx.starrysky.notification.StarrySkyNotificationManager;
import com.lzx.starrysky.playback.offline.StarrySkyCacheManager;
import com.lzx.starrysky.playback.player.Playback;
import com.lzx.starrysky.delayaction.Valid;
import com.lzx.starrysky.imageloader.ImageLoader;
import com.lzx.starrysky.imageloader.ImageLoaderStrategy;

public class StarrySkyRegistry {

    private ValidRegistry mValidRegistry;
    private ImageLoaderRegistry mImageLoaderRegistry;
    private NotificationRegistry mNotificationRegistry;
    private CacheRegistry mCacheRegistry;
    private Playback mPlayback;

    public StarrySkyRegistry(Context context) {
        mValidRegistry = new ValidRegistry();
        mNotificationRegistry = new NotificationRegistry();
        mCacheRegistry = new CacheRegistry();
        mImageLoaderRegistry = new ImageLoaderRegistry(context);
    }

    /**
     * 添加播放前验证
     */
    public void appendValidRegistry(Valid valid) {
        mValidRegistry.append(valid);
    }

    public ValidRegistry getValidRegistry() {
        return mValidRegistry;
    }

    /**
     * 注册图片加载引擎
     */
    public void registryImageLoader(ImageLoaderStrategy strategy) {
        mImageLoaderRegistry.registry(strategy);
    }

    public ImageLoader getImageLoader() {
        return mImageLoaderRegistry.getImageLoader();
    }

    /**
     * 注册通知栏配置信息
     */
    public void registryNotificationConfig(NotificationConfig config) {
        mNotificationRegistry.setConfig(config);
    }

    /**
     * 该方法在内部使用
     */
    public void registryNotificationManager(StarrySkyNotificationManager manager) {
        mNotificationRegistry.setNotificationManager(manager);
    }

    public NotificationConfig getNotificationConfig() {
        return mNotificationRegistry.getConfig();
    }

    public StarrySkyNotificationManager getNotificationManager() {
        return mNotificationRegistry.getNotificationManager();
    }

    /**
     * 该方法在内部使用
     */
    public void registryStarryCache(StarrySkyCacheManager starrySkyCacheManager) {
        mCacheRegistry.setCacheManager(starrySkyCacheManager);
    }

    /**
     * 如果在自定义playback时，直接拿这个会是 null 的，这时可以直接 new StarrySkyCacheManager()，
     * 然后传入即可
     */
    public StarrySkyCacheManager getStarrySkyCacheManager() {
        return mCacheRegistry.getCacheManager();
    }

    /**
     * 注册播放器
     */
    public void registryPlayback(Playback playback) {
        this.mPlayback = playback;
    }

    public Playback getPlayback() {
        return mPlayback;
    }
}
