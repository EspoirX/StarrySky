package com.lzx.musiclib.imageloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.lzx.starrysky.imageloader.ImageLoaderCallBack;
import com.lzx.starrysky.imageloader.ImageLoaderStrategy;


/**
 * 具体框架实现类
 */
public class GlideLoader implements ImageLoaderStrategy {

    @Override
    public void loadImage(Context context, String url, ImageLoaderCallBack callBack) {
        Glide.with(context).asBitmap().load(url)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        callBack.onBitmapLoaded(resource);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        callBack.onBitmapFailed(errorDrawable);
                    }
                });
    }
}
