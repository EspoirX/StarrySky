package com.lzx.starrysky.registry;

import android.support.annotation.NonNull;

import com.lzx.starrysky.utils.imageloader.ILoaderStrategy;

public class ImageLoaderRegistry {
    private ILoaderStrategy mStrategies;

    public ImageLoaderRegistry() {
    }

    public synchronized <T> void registry(@NonNull ILoaderStrategy strategy) {
        mStrategies = strategy;
    }
}
