package com.lzx.starrysky.utils.imageloader;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

public interface BitmapCallBack {
    void onBitmapLoaded(Bitmap bitmap);

    void onBitmapFailed(Exception e, Drawable errorDrawable);

    class SimperCallback implements BitmapCallBack {

        @Override
        public void onBitmapLoaded(Bitmap bitmap) {

        }

        @Override
        public void onBitmapFailed(Exception e, Drawable errorDrawable) {

        }
    }
}
