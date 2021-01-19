package com.lzx.starrysky.notification.imageloader

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable

/**
 * 图片加载类
 * 策略或者静态代理模式，开发者只需要关心ImageLoader + ImageLoaderStrategy
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

interface ImageLoaderStrategy {
    fun loadImage(context: Context, url: String?, callBack: ImageLoaderCallBack)
}

interface ImageLoaderCallBack {
    fun onBitmapLoaded(bitmap: Bitmap?)

    fun onBitmapFailed(errorDrawable: Drawable?)
}
