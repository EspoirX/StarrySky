package com.lzx.starrysky.utils.imageloader;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

public interface ImageLoaderCallBack {
    void onBitmapLoaded(Bitmap bitmap);

    void onBitmapFailed(Drawable errorDrawable);

    class SimperCallback implements ImageLoaderCallBack {

        @Override
        public void onBitmapLoaded(Bitmap bitmap) {

        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {

        }
    }
}
