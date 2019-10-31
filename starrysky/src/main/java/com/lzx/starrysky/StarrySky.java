package com.lzx.starrysky;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;

import com.lzx.starrysky.common.IMediaConnection;
import com.lzx.starrysky.control.PlayerControl;
import com.lzx.starrysky.control.StarrySkyPlayerControl;
import com.lzx.starrysky.notification.StarrySkyNotificationManager;
import com.lzx.starrysky.playback.manager.IPlaybackManager;
import com.lzx.starrysky.playback.manager.PlaybackManager;
import com.lzx.starrysky.playback.offline.StarrySkyCacheManager;
import com.lzx.starrysky.playback.player.ExoPlayback;
import com.lzx.starrysky.playback.player.Playback;
import com.lzx.starrysky.playback.queue.MediaQueue;
import com.lzx.starrysky.provider.MediaQueueProvider;
import com.lzx.starrysky.provider.MediaResource;
import com.lzx.starrysky.registry.StarrySkyRegistry;

public class StarrySky {
    private static volatile StarrySky sStarrySky;
    private static volatile boolean isInitializing;
    private volatile static boolean alreadyInit;

    private static Application globalContext;
    private static StarrySkyConfig mStarrySkyConfig;
    private StarrySkyActivityLifecycle mLifecycle;
    private IMediaConnection connection;
    private PlayerControl mPlayerControl;
    private StarrySkyRegistry mRegistry;
    private MediaQueueProvider mediaQueueProvider;
    private MediaResource mediaResource;
    private Playback playback;
    private IPlaybackManager playbackManager;
    private MediaQueue mediaQueue;
    private static IMediaConnection.OnConnectListener mOnConnectListener;

    public static void init(Application application) {
        init(application, null, null);
    }

    public static void init(Application application, StarrySkyConfig config) {
        init(application, config, null);
    }

    public static void init(Application application, StarrySkyConfig config,
                            IMediaConnection.OnConnectListener listener) {
        if (alreadyInit) {
            return;
        }
        alreadyInit = true;
        globalContext = application;
        mStarrySkyConfig = config;
        mOnConnectListener = listener;
        get();
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

    public static void release() {
        if (StarrySky.get().mLifecycle != null) {
            globalContext.unregisterActivityLifecycleCallbacks(StarrySky.get().mLifecycle);
        }
        isInitializing = false;
        alreadyInit = false;
        globalContext = null;
        mStarrySkyConfig = null;
        mOnConnectListener = null;
        sStarrySky = null;
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
        sStarrySky = starrySky;

        if (mStarrySkyConfig != null) {
            mStarrySkyConfig.applyStarrySkyRegistry(context, starrySky.mRegistry);
        }

        //注册通知栏
        StarrySkyNotificationManager.NotificationFactory factory =
                mStarrySkyConfig != null
                        ? mStarrySkyConfig.getNotificationFactory()
                        : null;
        StarrySkyNotificationManager notificationManager =
                new StarrySkyNotificationManager(builder.isOpenNotification, factory);
        starrySky.mRegistry.registryNotificationManager(notificationManager);

        //注册缓存
        StarrySkyCacheManager.CacheFactory cacheFactory =
                mStarrySkyConfig != null
                        ? mStarrySkyConfig.getCacheFactory()
                        : null;
        StarrySkyCacheManager cacheManager = new StarrySkyCacheManager(
                context,
                builder.isOpenCache,
                builder.cacheDestFileDir,
                cacheFactory);
        starrySky.mRegistry.registryStarryCache(cacheManager);

        //播放器
        starrySky.playback = starrySky.mRegistry.getPlayback();
        if (starrySky.playback == null) {
            starrySky.playback = new ExoPlayback(context, cacheManager);
        }
        if (starrySky.playbackManager == null) {
            starrySky.playbackManager =
                    new PlaybackManager(starrySky.mediaQueue, starrySky.playback);
        }
    }

    StarrySky(
            IMediaConnection connection,
            MediaQueueProvider mediaQueueProvider,
            MediaQueue mediaQueue) {
        this.connection = connection;

        this.mediaQueueProvider = mediaQueueProvider;
        this.mediaQueue = mediaQueue;
        mediaResource = new MediaResource();

        registerLifecycle(globalContext);

        mRegistry = new StarrySkyRegistry(globalContext);

        //链接服务
        connection.connect();
        connection.setOnConnectListener(mOnConnectListener);
    }

    public IMediaConnection getConnection() {
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

    public MediaResource getMediaResource() {
        return mediaResource;
    }
}
