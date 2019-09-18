package com.lzx.starrysky.registry;

import android.content.Context;
import android.support.annotation.NonNull;

import com.lzx.starrysky.imageloader.ImageLoader;
import com.lzx.starrysky.imageloader.ImageLoaderStrategy;

class ImageLoaderRegistry {
    private ImageLoader mImageLoader;
    private Context mContext;

    ImageLoaderRegistry(Context context) {
        this.mContext = context;
    }

    synchronized void registry(@NonNull ImageLoaderStrategy strategy) {
        initImageLoader();
        mImageLoader.init(strategy);
    }

    private void initImageLoader() {
        if (mImageLoader == null) {
            mImageLoader = new ImageLoader(mContext);
        }
    }

    synchronized ImageLoader getImageLoader() {
        initImageLoader();
        return mImageLoader;
    }
}
