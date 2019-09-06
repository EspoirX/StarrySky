package com.lzx.starrysky.registry;

import android.support.annotation.NonNull;

import com.lzx.starrysky.StarrySkyActivityLifecycle;
import com.lzx.starrysky.utils.imageloader.ImageLoader;
import com.lzx.starrysky.utils.imageloader.ImageLoaderStrategy;

public class ImageLoaderRegistry {
    private ImageLoader mImageLoader;
    private StarrySkyActivityLifecycle lifecycle;

    public ImageLoaderRegistry(StarrySkyActivityLifecycle lifecycle) {
        this.lifecycle = lifecycle;
    }

    public synchronized void registry(@NonNull ImageLoaderStrategy strategy) {
        if (mImageLoader == null) {
            mImageLoader = new ImageLoader(lifecycle.getActivity());
        }
        mImageLoader.init(strategy);
    }

    public ImageLoader getImageLoader() {
        return mImageLoader;
    }
}
