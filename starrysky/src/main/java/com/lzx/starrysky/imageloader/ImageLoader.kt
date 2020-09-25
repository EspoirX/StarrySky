package com.lzx.starrysky.imageloader

import android.content.Context

/**
 * 图片加载类
 * 策略或者静态代理模式，开发者只需要关心ImageLoader + ImageLoaderOptions
 */
open class ImageLoader(private val context: Context?) {
    private var mLoader: ImageLoaderStrategy? = null

    fun init(loader: ImageLoaderStrategy) {
        this.mLoader = loader
    }

    fun load(url: String, callBack: ImageLoaderCallBack) {
        if (mLoader == null) {
            mLoader = DefaultImageLoader()
        }
        context?.let { mLoader?.loadImage(it, url, callBack) }
    }
}
