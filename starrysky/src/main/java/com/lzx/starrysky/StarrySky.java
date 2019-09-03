package com.lzx.starrysky;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;

import com.lzx.starrysky.playback.manager.IPlaybackManager;
import com.lzx.starrysky.playback.player.Playback;
import com.lzx.starrysky.playback.queue.MediaQueue;
import com.lzx.starrysky.provider.MediaResource;
import com.lzx.starrysky.common.MediaSessionConnection;
import com.lzx.starrysky.control.PlayerControl;
import com.lzx.starrysky.playback.download.ExoDownload;
import com.lzx.starrysky.provider.MediaQueueProvider;
import com.lzx.starrysky.registry.StarrySkyRegistry;
import com.lzx.starrysky.utils.imageloader.ILoaderStrategy;

public class StarrySky {
    private static volatile StarrySky sStarrySky;
    private static volatile boolean isInitializing;
    private volatile static boolean alreadyInit;

    private static Application globalContext;
    private StarrySkyActivityLifecycle mLifecycle;
    private MediaSessionConnection mConnection;
    private ILoaderStrategy mImageLoader;
    private PlayerControl mPlayerControl;
    private StarrySkyRegistry mRegistry;

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

    public static PlayerControl with() {
        return get().getPlayerControl();
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

    StarrySky(
            MediaSessionConnection connection,
            ILoaderStrategy imageLoader,
            PlayerControl playerControl,
            MediaQueueProvider mediaQueueProvider,
            MediaQueue mediaQueue,
            Playback playback,
            IPlaybackManager playbackManager) {

        mConnection = connection;
        mImageLoader = imageLoader;
        mPlayerControl = playerControl;

        mRegistry = new StarrySkyRegistry();

        mRegistry
                .append(ILoaderStrategy.class, mImageLoader)
                .append(PlayerControl.class, mPlayerControl)
                .append(MediaQueueProvider.class, mediaQueueProvider)
                .append(MediaResource.class, new MediaResource())
                .append(MediaQueue.class, mediaQueue)
                .append(Playback.class, playback)
                .append(IPlaybackManager.class, playbackManager);

        mConnection.connect();
    }

    public MediaSessionConnection getConnection() {
        return mConnection;
    }

    public ILoaderStrategy getImageLoader() {
        return mImageLoader;
    }

    public PlayerControl getPlayerControl() {
        return mPlayerControl;
    }

    public StarrySkyRegistry getRegistry() {
        return mRegistry;
    }
}
