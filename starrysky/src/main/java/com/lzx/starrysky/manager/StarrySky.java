package com.lzx.starrysky.manager;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;

import com.lzx.starrysky.StarrySkyActivityLifecycle;
import com.lzx.starrysky.playback.download.ExoDownload;
import com.lzx.starrysky.utils.imageloader.ILoaderStrategy;

public class StarrySky {
    private static volatile StarrySky sStarrySky;
    private static volatile boolean isInitializing;
    private volatile static boolean alreadyInit;

    private static Application globalContext;
    private StarrySkyActivityLifecycle mLifecycle;
    private MediaSessionConnection mConnection;
    private ILoaderStrategy mImageLoader;

    public static void init(Application application) {
        if (alreadyInit) {
            return;
        }
        alreadyInit = true;
        globalContext = application;
        get().registerLifecycle(globalContext);
    }

    private void registerLifecycle(Application context) {
        if (null != mLifecycle) {
            context.unregisterActivityLifecycleCallbacks(mLifecycle);
        }
        mLifecycle = new StarrySkyActivityLifecycle();
        context.registerActivityLifecycleCallbacks(mLifecycle);
    }

    public static StarrySky get() {
        if (sStarrySky == null) {
            synchronized (StarrySky.class) {
                if (sStarrySky == null) {
                    checkAndInitializeStarrySky(globalContext);
                }
            }
        }
        return sStarrySky;
    }

    private static void checkAndInitializeStarrySky(@NonNull Context context) {
        if (isInitializing) {
            throw new IllegalStateException("checkAndInitializeStarrySky");
        }
        isInitializing = true;
        initializeStarrySky(context, new StarrySkyBuilder());
        isInitializing = false;
    }

    private static void initializeStarrySky(Context context, StarrySkyBuilder builder) {

        StarrySky starrySky = builder.build(context);

        ExoDownload.initExoDownload(context);

        sStarrySky = starrySky;
    }

    StarrySky(MediaSessionConnection connection, ILoaderStrategy imageLoader) {
        mConnection = connection;
        mImageLoader = imageLoader;
    }
}
