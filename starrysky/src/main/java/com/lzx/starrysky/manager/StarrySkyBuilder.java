package com.lzx.starrysky.manager;

import android.content.ComponentName;
import android.content.Context;

import com.lzx.starrysky.MusicService;
import com.lzx.starrysky.model.MediaQueueProviderImpl;
import com.lzx.starrysky.utils.imageloader.DefaultImageLoader;
import com.lzx.starrysky.utils.imageloader.ILoaderStrategy;

public class StarrySkyBuilder {

    private MediaSessionConnection mConnection;
    private ILoaderStrategy mImageLoader;
    private PlayerControl mPlayerControl;
    private MediaQueueProvider mMediaQueueProvider;

    public void setConnection(MediaSessionConnection connection) {
        mConnection = connection;
    }

    public void setImageLoader(ILoaderStrategy imageLoader) {
        mImageLoader = imageLoader;
    }

    public void setPlayerControl(PlayerControl playerControl) {
        mPlayerControl = playerControl;
    }

    public void setMediaQueueProvider(MediaQueueProvider mediaQueueProvider) {
        mMediaQueueProvider = mediaQueueProvider;
    }

    StarrySky build(Context context) {
        if (mConnection == null) {
            ComponentName componentName = new ComponentName(context, MusicService.class);
            mConnection = new MediaSessionConnection(context, componentName);
        }
        if (mImageLoader == null) {
            mImageLoader = new DefaultImageLoader();
        }
        if (mMediaQueueProvider == null) {
            mMediaQueueProvider = new MediaQueueProviderImpl();
        }
        if (mPlayerControl == null) {
            mPlayerControl = new StarrySkyPlayerControl(context, mConnection, mMediaQueueProvider);
        }
        return new StarrySky(
                mConnection,
                mImageLoader,
                mPlayerControl,
                mMediaQueueProvider);
    }
}
