package com.lzx.starrysky.imageloader

import com.lzx.starrysky.StarrySky

/**
 * 图片加载类
 * 策略或者静态代理模式，开发者只需要关心ImageLoader + ImageLoaderOptions
 */
class ImageLoader {
    private var mLoader: ImageLoaderStrategy? = null

    fun init(loader: ImageLoaderStrategy) {
        this.mLoader = loader
    }

    fun load(url: String, callBack: ImageLoaderCallBack) {
        if (mLoader == null) {
            mLoader = DefaultImageLoader()
        }
        StarrySky.get().mLifecycle?.activity?.let { mLoader?.loadImage(it, url, callBack) }
    }
}
