package com.lzx.starrysky.registry;

import com.lzx.starrysky.provider.MediaResource;
import com.lzx.starrysky.provider.MediaQueueProvider;
import com.lzx.starrysky.control.PlayerControl;
import com.lzx.starrysky.utils.delayaction.Valid;
import com.lzx.starrysky.utils.imageloader.ILoaderStrategy;

public class StarrySkyRegistry {
    private ImageLoaderRegistry mImageLoaderRegistry;
    private PlayerControl mPlayerControl;
    private MediaQueueProvider mMediaQueueProvider;
    private ValidRegistry mValidRegistry;
    private MediaResource mMediaResource;

    public StarrySkyRegistry() {
        mImageLoaderRegistry = new ImageLoaderRegistry();
        mValidRegistry = new ValidRegistry();
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

    public void appendValidRegistry(Valid valid) {
        mValidRegistry.append(valid);
    }

    public ValidRegistry getValidRegistry() {
        return mValidRegistry;
    }

    public MediaResource getMediaResource() {
        return mMediaResource;
    }

    public void registryMediaResource(MediaResource mediaResource) {
        mMediaResource = mediaResource;
    }
}
