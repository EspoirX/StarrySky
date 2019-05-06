package com.lzx.starrysky.utils.imageloader;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import java.util.concurrent.Executor;

/**
 * 图片加载类
 * 策略或者静态代理模式，开发者只需要关心ImageLoader + LoaderOptions
 */
public class ImageLoader {
    private static ILoaderStrategy sLoader;
    private static volatile ImageLoader sInstance;
    static final Executor sMainThreadExecutor = new MainThreadExecutor();
    private String currSetCookie;

    private ImageLoader() {

    }

    //单例模式
    public static ImageLoader getInstance() {
        if (sInstance == null) {
            synchronized (ImageLoader.class) {
                if (sInstance == null) {
                    sInstance = new ImageLoader();
                }
            }
        }
        return sInstance;
    }

    /**
     * 提供全局替换图片加载框架的接口，若切换其它框架，可以实现一键全局替换
     */
    public void setGlobalImageLoader(ILoaderStrategy loader) {
        sLoader = loader;
    }

    public LoaderOptions load(String url) {
        return new LoaderOptions(url);
    }

    /**
     * 优先使用实时设置的图片loader，其次使用全局设置的图片loader
     */
    void loadOptions(LoaderOptions options) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            setOptions(options);
        } else {
            postOptions(options);
        }
    }

    private void setOptions(LoaderOptions options) {
        checkNotNull();
        sLoader.loadImage(options);
    }

    private void postOptions(LoaderOptions options) {
        sMainThreadExecutor.execute(() -> setOptions(options));
    }

    private void checkNotNull() {
        if (sLoader == null) {
            sLoader = new DefaultImageLoader();
        }
    }

    public String getCurrSetCookie() {
        return currSetCookie;
    }

    public void setCurrSetCookie(String currSetCookie) {
        this.currSetCookie = currSetCookie;
    }

    /**
     * 主线程
     */
    private static class MainThreadExecutor implements Executor {
        final Handler mHandler = new Handler(Looper.getMainLooper());

        MainThreadExecutor() {
        }

        public void execute(@NonNull Runnable command) {
            this.mHandler.post(command);
        }
    }
}
