package com.lzx.starrysky;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;

import com.lzx.starrysky.control.StarrySkyPlayerControl;
import com.lzx.starrysky.playback.manager.IPlaybackManager;
import com.lzx.starrysky.playback.player.Playback;
import com.lzx.starrysky.provider.MediaResource;
import com.lzx.starrysky.common.MediaSessionConnection;
import com.lzx.starrysky.control.PlayerControl;
import com.lzx.starrysky.playback.download.ExoDownload;
import com.lzx.starrysky.provider.MediaQueueProvider;
import com.lzx.starrysky.registry.StarrySkyRegistry;
import com.lzx.starrysky.utils.imageloader.ImageLoaderStrategy;

public class StarrySky {
    private static volatile StarrySky sStarrySky;
    private static volatile boolean isInitializing;
    private volatile static boolean alreadyInit;

    private static Application globalContext;
    private static StarrySkyConfig mStarrySkyConfig;
    private StarrySkyActivityLifecycle mLifecycle;
    private MediaSessionConnection connection;
    private PlayerControl mPlayerControl;
    private StarrySkyRegistry mRegistry;
    private MediaQueueProvider mediaQueueProvider;
    private MediaResource mediaResource;
    private Playback playback;
    private IPlaybackManager playbackManager;
    private ExoDownload exoDownload;

    public static void init(Application application) {
        init(application, null);
    }

    public static void init(Application application, StarrySkyConfig config) {
        if (alreadyInit) {
            return;
        }
        alreadyInit = true;
        globalContext = application;
        mStarrySkyConfig = config;
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
        return StarrySky.get().getPlayerControl();
    }

    public void registerPlayerControl(PlayerControl playerControl) {
        this.mPlayerControl = playerControl;
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
        if (mStarrySkyConfig != null) {
            mStarrySkyConfig.applyOptions(context, builder);
        }

        StarrySky starrySky = builder.build(context);

        if (mStarrySkyConfig != null) {
            mStarrySkyConfig.applyMediaValid(context, starrySky.mRegistry);
        }
        sStarrySky = starrySky;
    }

    StarrySky(
            MediaSessionConnection connection,
            MediaQueueProvider mediaQueueProvider,
            Playback playback,
            IPlaybackManager playbackManager,
            ExoDownload exoDownload) {
        this.connection = connection;

        this.mediaQueueProvider = mediaQueueProvider;
        this.playback = playback;
        this.playbackManager = playbackManager;
        this.exoDownload = exoDownload;
        mediaResource = new MediaResource();

        registerLifecycle(globalContext);

        mRegistry = new StarrySkyRegistry(mLifecycle);

        connection.connect();
    }

    public MediaSessionConnection getConnection() {
        return connection;
    }

    private PlayerControl getPlayerControl() {
        if (mPlayerControl == null) {
            return new StarrySkyPlayerControl(globalContext);
        }
        return mPlayerControl;
    }

    public Playback getPlayback() {
        return playback;
    }

    public StarrySkyRegistry getRegistry() {
        return mRegistry;
    }

    public MediaQueueProvider getMediaQueueProvider() {
        return mediaQueueProvider;
    }

    IPlaybackManager getPlaybackManager() {
        return playbackManager;
    }

    public ExoDownload getExoDownload() {
        return exoDownload;
    }

    public MediaResource getMediaResource() {
        return mediaResource;
    }
}
