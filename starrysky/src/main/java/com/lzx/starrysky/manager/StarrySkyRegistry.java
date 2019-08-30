package com.lzx.starrysky.manager;

import com.lzx.starrysky.utils.imageloader.ILoaderStrategy;

public class StarrySkyRegistry {
    private ImageLoaderRegistry mImageLoaderRegistry;
    private PlayerControl mPlayerControl;
    private MediaQueueProvider mMediaQueueProvider;

    public StarrySkyRegistry() {
        mImageLoaderRegistry = new ImageLoaderRegistry();
    }

    public void registryImageLoader(ILoaderStrategy strategy) {
        if (strategy != null) {
            mImageLoaderRegistry.registry(strategy);
        }
    }

    public void registryPlayerControl(PlayerControl playerControl) {
        if (playerControl != null) {
            mPlayerControl = playerControl;
        }
    }

    public void registryMediaQueueProvider(MediaQueueProvider mediaQueueProvider) {
        if (mediaQueueProvider != null) {
            mMediaQueueProvider = mediaQueueProvider;
        }
    }

    public ImageLoaderRegistry getImageLoaderRegistry() {
        return mImageLoaderRegistry;
    }

    public PlayerControl getPlayerControl() {
        return mPlayerControl;
    }

    public MediaQueueProvider getMediaQueueProvider() {
        return mMediaQueueProvider;
    }
}
