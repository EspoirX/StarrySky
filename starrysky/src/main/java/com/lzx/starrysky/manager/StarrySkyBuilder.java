package com.lzx.starrysky.manager;

import android.content.ComponentName;
import android.content.Context;

import com.lzx.starrysky.MusicService;
import com.lzx.starrysky.utils.imageloader.DefaultImageLoader;
import com.lzx.starrysky.utils.imageloader.ILoaderStrategy;

public class StarrySkyBuilder {

    private MediaSessionConnection mConnection;
    private ILoaderStrategy mImageLoader;

    public void setConnection(MediaSessionConnection connection) {
        mConnection = connection;
    }

    public void setImageLoader(ILoaderStrategy imageLoader) {
        mImageLoader = imageLoader;
    }

    StarrySky build(Context context) {
        if (mConnection == null) {
            ComponentName componentName = new ComponentName(context, MusicService.class);
            mConnection = new MediaSessionConnection(componentName);
        }
        if (mImageLoader == null) {
            mImageLoader = new DefaultImageLoader();
        }
        return new StarrySky(
                mConnection,
                mImageLoader);
    }


}
