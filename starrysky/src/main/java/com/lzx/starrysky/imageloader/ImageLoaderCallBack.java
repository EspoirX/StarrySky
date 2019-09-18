package com.lzx.starrysky.imageloader;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

public interface ImageLoaderCallBack {
    void onBitmapLoaded(Bitmap bitmap);

    void onBitmapFailed(Drawable errorDrawable);
}
