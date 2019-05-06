package com.lzx.musiclib.imageloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.lzx.musiclib.R;
import com.lzx.starrysky.utils.imageloader.ILoaderStrategy;
import com.lzx.starrysky.utils.imageloader.LoaderOptions;


/**
 * 具体框架实现类
 */
public class GlideLoader implements ILoaderStrategy {

    @Override
    public void loadImage(LoaderOptions options) {
        Context context = null;
        if (options.mContext != null) {
            context = options.mContext;
        }
        if (!ActivityUtils.activityIsAlive(context)) {
            return;
        }
        if (TextUtils.isEmpty(options.url)) {
            return;
        }

        RequestOptions requestOptions = new RequestOptions()
                .fallback(options.placeholderResId != 0
                        ? options.placeholderResId
                        : R.drawable.default_art)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE);

        Glide.with(context)
                .applyDefaultRequestOptions(requestOptions)
                .asBitmap()
                .load(options.url)
                .into(new SimpleTarget<Bitmap>(options.targetWidth, options.targetHeight) {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        if (options.bitmapCallBack != null) {
                            options.bitmapCallBack.onBitmapLoaded(resource);
                        }
                    }
                });
    }
}
