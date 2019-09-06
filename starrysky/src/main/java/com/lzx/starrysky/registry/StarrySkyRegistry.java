package com.lzx.starrysky.registry;

import com.lzx.starrysky.StarrySkyActivityLifecycle;
import com.lzx.starrysky.utils.delayaction.Valid;
import com.lzx.starrysky.utils.imageloader.ImageLoader;
import com.lzx.starrysky.utils.imageloader.ImageLoaderStrategy;

public class StarrySkyRegistry {
    //图片加载
    //通知栏
    //播放前验证
    private ValidRegistry mValidRegistry;
    private ImageLoaderRegistry mImageLoaderRegistry;
    private StarrySkyActivityLifecycle mLifecycle;

    public StarrySkyRegistry(StarrySkyActivityLifecycle lifecycle) {
        mLifecycle = lifecycle;
        mValidRegistry = new ValidRegistry();
        mImageLoaderRegistry = new ImageLoaderRegistry(mLifecycle);
    }

    public void appendValidRegistry(Valid valid) {
        mValidRegistry.append(valid);
    }

    public ValidRegistry getValidRegistry() {
        return mValidRegistry;
    }

    public void registryImageLoader(ImageLoaderStrategy strategy) {
        mImageLoaderRegistry.registry(strategy);
    }

    public ImageLoader getImageLoader() {
        return mImageLoaderRegistry.getImageLoader();
    }

}
