package com.lzx.starrysky.imageloader

import android.graphics.Bitmap
import android.graphics.drawable.Drawable

interface ImageLoaderCallBack {
    fun onBitmapLoaded(bitmap: Bitmap?)

    fun onBitmapFailed(errorDrawable: Drawable?)
}
