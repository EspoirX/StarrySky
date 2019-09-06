package com.lzx.starrysky.registry;

import android.support.annotation.NonNull;

import com.lzx.starrysky.StarrySkyActivityLifecycle;
import com.lzx.starrysky.utils.imageloader.DefaultImageLoader;
import com.lzx.starrysky.utils.imageloader.ImageLoader;
import com.lzx.starrysky.utils.imageloader.ImageLoaderStrategy;

public class ImageLoaderRegistry {
    private ImageLoader mImageLoader;

    public ImageLoaderRegistry(StarrySkyActivityLifecycle lifecycle) {
        mImageLoader = new ImageLoader(lifecycle.getActivity());
        mImageLoader.init(new DefaultImageLoader());
    }

    public synchronized void registry(@NonNull ImageLoaderStrategy strategy) {
        mImageLoader.init(strategy);
    }

    public ImageLoader getImageLoader() {
        return mImageLoader;
    }
}
