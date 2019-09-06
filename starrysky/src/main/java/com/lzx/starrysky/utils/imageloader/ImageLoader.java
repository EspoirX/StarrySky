package com.lzx.starrysky.utils.imageloader;

import android.content.Context;
import android.support.annotation.NonNull;

/**
 * 图片加载类
 * 策略或者静态代理模式，开发者只需要关心ImageLoader + ImageLoaderOptions
 */
public class ImageLoader {
    private ImageLoaderStrategy mLoader;
    private Context context;

    public ImageLoader(Context context) {
        this.context = context;
    }

    public void init(@NonNull ImageLoaderStrategy loader) {
        this.mLoader = loader;
    }

    public void load(  String url, ImageLoaderCallBack callBack) {
        if (ensureLoader()) {
            throw new IllegalStateException("init method should be called first");
        }
        mLoader.loadImage(context, url, callBack);
    }

    private boolean ensureLoader() {
        return mLoader == null;
    }
}
