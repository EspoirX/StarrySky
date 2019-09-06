package com.lzx.starrysky.registry;

import com.lzx.starrysky.StarrySkyActivityLifecycle;
import com.lzx.starrysky.notification.NotificationConfig;
import com.lzx.starrysky.notification.StarrySkyNotificationManager;
import com.lzx.starrysky.utils.delayaction.Valid;
import com.lzx.starrysky.utils.imageloader.ImageLoader;
import com.lzx.starrysky.utils.imageloader.ImageLoaderStrategy;

public class StarrySkyRegistry {

    private ValidRegistry mValidRegistry;
    private ImageLoaderRegistry mImageLoaderRegistry;
    private NotificationRegistry mNotificationRegistry;

    public StarrySkyRegistry() {
        mValidRegistry = new ValidRegistry();
        mNotificationRegistry = new NotificationRegistry();
    }

    public void initImageLoaderRegistry(StarrySkyActivityLifecycle lifecycle) {
        mImageLoaderRegistry = new ImageLoaderRegistry(lifecycle);
    }

    public void appendValidRegistry(Valid valid) {
        mValidRegistry.append(valid);
    }

    public ValidRegistry getValidRegistry() {
        return mValidRegistry;
    }

    public void registryImageLoader(ImageLoaderStrategy strategy) {
        mImageLoaderRegistry.registry(strategy);
    }

    public ImageLoader getImageLoader() {
        return mImageLoaderRegistry.getImageLoader();
    }

    public void registryNotificationConfig(NotificationConfig config) {
        mNotificationRegistry.setConfig(config);
    }

    public void registryNotificationManager(StarrySkyNotificationManager manager) {
        mNotificationRegistry.setNotificationManager(manager);
    }

    public NotificationConfig getNotificationConfig() {
        return mNotificationRegistry.getConfig();
    }

    public StarrySkyNotificationManager getNotificationManager() {
        return mNotificationRegistry.getNotificationManager();
    }

}
