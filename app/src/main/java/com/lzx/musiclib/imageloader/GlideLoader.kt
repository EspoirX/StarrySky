package com.lzx.musiclib.imageloader

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.lzx.starrysky.imageloader.ImageLoaderCallBack
import com.lzx.starrysky.imageloader.ImageLoaderStrategy

/**
 * 具体框架实现类
 */
class GlideLoader : ImageLoaderStrategy {
    override fun loadImage(
        context: Context, url: String?, callBack: ImageLoaderCallBack
    ) {
        Glide.with(context).asBitmap().load(url)
            .into(object : CustomTarget<Bitmap?>() {
                override fun onLoadCleared(placeholder: Drawable?) {}

                override fun onResourceReady(
                    resource: Bitmap, transition: Transition<in Bitmap?>?
                ) {
                    callBack.onBitmapLoaded(resource)
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    super.onLoadFailed(errorDrawable)
                    callBack.onBitmapFailed(errorDrawable)
                }
            })
    }
}