package com.lzx.musiclib.imageloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.lzx.musiclib.R;
import com.lzx.starrysky.utils.imageloader.ImageLoaderCallBack;
import com.lzx.starrysky.utils.imageloader.ImageLoaderStrategy;


/**
 * 具体框架实现类
 */
public class GlideLoader implements ImageLoaderStrategy {

    @Override
    public void loadImage(Context context, String url, ImageLoaderCallBack callBack) {
        if (!ActivityUtils.activityIsAlive(context)) {
            return;
        }
        if (TextUtils.isEmpty(url)) {
            return;
        }
        RequestOptions requestOptions = new RequestOptions()
                .override(getTargetWidth(), getTargetHeight())
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE);
        Glide.with(context)
                .applyDefaultRequestOptions(requestOptions)
                .asBitmap()
                .load(url)
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

    @Override
    public int getPlaceholderResId() {
        return R.drawable.default_art;
    }

    @Override
    public int getErrorResId() {
        return R.drawable.default_art;
    }

    @Override
    public int getTargetWidth() {
        return 144;
    }

    @Override
    public int getTargetHeight() {
        return 144;
    }
}
