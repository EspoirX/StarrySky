package com.lzx.starrysky.imageloader;

import android.content.Context;

public interface ImageLoaderStrategy {
    void loadImage(Context context, String url, ImageLoaderCallBack callBack);
}
