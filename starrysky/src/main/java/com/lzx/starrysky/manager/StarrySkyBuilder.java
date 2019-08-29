package com.lzx.starrysky.manager;

import android.content.ComponentName;
import android.content.Context;

import com.lzx.starrysky.MusicService;
import com.lzx.starrysky.utils.imageloader.DefaultImageLoader;
import com.lzx.starrysky.utils.imageloader.ILoaderStrategy;

public class StarrySkyBuilder {

    private MediaSessionConnection mConnection;
    private ILoaderStrategy mImageLoader;
    private PlayerControl mPlayerControl;

    public void setConnection(MediaSessionConnection connection) {
        mConnection = connection;
    }

    public void setImageLoader(ILoaderStrategy imageLoader) {
        mImageLoader = imageLoader;
    }

    public void setPlayerControl(PlayerControl playerControl) {
        mPlayerControl = playerControl;
    }

    StarrySky build(Context context) {
        if (mConnection == null) {
            ComponentName componentName = new ComponentName(context, MusicService.class);
            mConnection = new MediaSessionConnection(context, componentName);
        }
        if (mImageLoader == null) {
            mImageLoader = new DefaultImageLoader();
        }
        if (mPlayerControl == null) {
            mPlayerControl = new StarrySkyPlayerControl(context, mConnection);
        }
        return new StarrySky(
                mConnection,
                mImageLoader,
                mPlayerControl);
    }


}
