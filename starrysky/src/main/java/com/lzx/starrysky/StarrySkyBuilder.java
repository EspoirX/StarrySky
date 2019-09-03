package com.lzx.starrysky;

import android.content.ComponentName;
import android.content.Context;

import com.lzx.starrysky.common.MediaSessionConnection;
import com.lzx.starrysky.control.PlayerControl;
import com.lzx.starrysky.control.StarrySkyPlayerControl;
import com.lzx.starrysky.playback.manager.IPlaybackManager;
import com.lzx.starrysky.playback.manager.PlaybackManager;
import com.lzx.starrysky.playback.player.ExoPlayback;
import com.lzx.starrysky.playback.queue.MediaQueue;
import com.lzx.starrysky.playback.queue.MediaQueueManager;
import com.lzx.starrysky.playback.player.Playback;
import com.lzx.starrysky.provider.MediaQueueProvider;
import com.lzx.starrysky.provider.MediaQueueProviderImpl;
import com.lzx.starrysky.provider.MediaQueueProviderSurface;
import com.lzx.starrysky.utils.imageloader.DefaultImageLoader;
import com.lzx.starrysky.utils.imageloader.ILoaderStrategy;

public class StarrySkyBuilder {

    private MediaSessionConnection mConnection;
    private ILoaderStrategy mImageLoader;
    private PlayerControl mPlayerControl;
    private MediaQueueProvider mMediaQueueProvider;
    private MediaQueue mMediaQueue;
    private Playback mPlayback;
    private IPlaybackManager mIPlaybackManager;

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

    public void setMediaQueue(MediaQueue mediaQueue) {
        mMediaQueue = mediaQueue;
    }

    public void setPlayback(Playback playback) {
        mPlayback = playback;
    }

    public void setIPlaybackManager(IPlaybackManager IPlaybackManager) {
        mIPlaybackManager = IPlaybackManager;
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
        MediaQueueProviderSurface surface = new MediaQueueProviderSurface(mMediaQueueProvider);
        if (mPlayerControl == null) {
            mPlayerControl = new StarrySkyPlayerControl(context, mConnection, surface);
        }
        if (mMediaQueue == null) {
            mMediaQueue = new MediaQueueManager(surface, context);
        }
        if (mPlayback == null) {
            mPlayback = new ExoPlayback(context, surface);
        }
        mPlayerControl.setPlayBack(mPlayback);
        if (mIPlaybackManager == null) {
            mIPlaybackManager = new PlaybackManager(context, mMediaQueue, mPlayback);
        }
        return new StarrySky(
                mConnection,
                mImageLoader,
                mPlayerControl,
                surface,
                mMediaQueue,
                mPlayback,
                mIPlaybackManager);
    }
}
