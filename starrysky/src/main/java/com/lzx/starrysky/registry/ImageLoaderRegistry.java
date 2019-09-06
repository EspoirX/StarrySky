package com.lzx.starrysky.registry;

import android.support.annotation.NonNull;

import com.lzx.starrysky.StarrySkyActivityLifecycle;
import com.lzx.starrysky.utils.imageloader.ImageLoader;
import com.lzx.starrysky.utils.imageloader.ImageLoaderStrategy;

class ImageLoaderRegistry {
    private ImageLoader mImageLoader;
    private StarrySkyActivityLifecycle lifecycle;

    ImageLoaderRegistry(StarrySkyActivityLifecycle lifecycle) {
        this.lifecycle = lifecycle;
    }

    synchronized void registry(@NonNull ImageLoaderStrategy strategy) {
        initImageLoader();
        mImageLoader.init(strategy);
    }

    private void initImageLoader() {
        if (mImageLoader == null) {
            mImageLoader = new ImageLoader(lifecycle.getActivity());
        }
    }

    synchronized ImageLoader getImageLoader() {
        initImageLoader();
        return mImageLoader;
    }
}
