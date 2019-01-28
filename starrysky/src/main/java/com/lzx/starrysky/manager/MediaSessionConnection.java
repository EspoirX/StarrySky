package com.lzx.starrysky.manager;

import android.content.ComponentName;
import android.content.Context;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.lzx.starrysky.MusicService;
import com.lzx.starrysky.model.MusicProvider;
import com.lzx.starrysky.model.SongInfo;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * 与服务端连接的管理类
 */
public class MediaSessionConnection {
    private Context mContext;
    private ComponentName serviceComponent;
    private MediaBrowserCompat mediaBrowser;
    private boolean isConnected;
    private String rootMediaId;
    private PlaybackStateCompat playbackState = EMPTY_PLAYBACK_STATE;
    private MediaMetadataCompat nowPlaying = NOTHING_PLAYING;
    private MediaControllerCompat.TransportControls transportControls;
    private MediaControllerCompat mediaController;
    private MediaBrowserConnectionCallback mediaBrowserConnectionCallback;
    private MediaControllerCallback mMediaControllerCallback;

    private static volatile MediaSessionConnection sInstance;

    public static MediaSessionConnection getInstance(Context context) {
        if (sInstance == null) {
            synchronized (MediaSessionConnection.class) {
                if (sInstance == null) {
                    sInstance = new MediaSessionConnection(context, new ComponentName(context, MusicService.class));
                }
            }
        }
        return sInstance;
    }

    private MediaSessionConnection(Context context, ComponentName serviceComponent) {
        mContext = context;
        this.serviceComponent = serviceComponent;
        mediaBrowserConnectionCallback = new MediaBrowserConnectionCallback();
        mMediaControllerCallback = new MediaControllerCallback();
        mediaBrowser = new MediaBrowserCompat(context, serviceComponent, mediaBrowserConnectionCallback, null);
    }

    public void subscribe(String parentId, MediaBrowserCompat.SubscriptionCallback callback) {
        mediaBrowser.subscribe(parentId, callback);
    }

    public void unsubscribe(String parentId, MediaBrowserCompat.SubscriptionCallback callback) {
        mediaBrowser.unsubscribe(parentId, callback);
    }

    /**
     * 是否已连接
     */
    public boolean isConnected() {
        return isConnected;
    }

    /**
     * 获取rootMediaId
     */
    public String getRootMediaId() {
        return rootMediaId;
    }

    /**
     * 获取 MediaBrowserCompat
     */
    public MediaBrowserCompat getMediaBrowser() {
        return mediaBrowser;
    }

    /**
     * 获取当前播放的 PlaybackStateCompat
     */
    public PlaybackStateCompat getPlaybackState() {
        return playbackState;
    }

    /**
     * 获取当前播放的 MediaMetadataCompat
     */
    public MediaMetadataCompat getNowPlaying() {
        return nowPlaying;
    }

    /**
     * 获取播放控制器
     */
    public MediaControllerCompat.TransportControls getTransportControls() {
        return transportControls;
    }

    public MediaControllerCompat getMediaController() {
        return mediaController;
    }

    /**
     * 连接
     */
    public void connect() {
        mediaBrowser.connect();
    }

    /**
     * 断开连接
     */
    public void disconnect() {
        if (mediaController != null) {
            mediaController.unregisterCallback(mMediaControllerCallback);
        }
        mediaBrowser.disconnect();
    }

    /**
     * 连接回调
     */
    private class MediaBrowserConnectionCallback extends MediaBrowserCompat.ConnectionCallback {
        /**
         * 已连接上
         */
        @Override
        public void onConnected() {
            super.onConnected();
            try {
                mediaController = new MediaControllerCompat(mContext, mediaBrowser.getSessionToken());
                mediaController.registerCallback(mMediaControllerCallback);
                transportControls = mediaController.getTransportControls();
                rootMediaId = mediaBrowser.getRoot();
                subscribe(rootMediaId, new MediaBrowserCompat.SubscriptionCallback() {
                    @Override
                    public void onChildrenLoaded(@NonNull String parentId, @NonNull List<MediaBrowserCompat.MediaItem> children) {
                        super.onChildrenLoaded(parentId, children);
                    }
                });
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            isConnected = true;
        }

        @Override
        public void onConnectionSuspended() {
            super.onConnectionSuspended();
            isConnected = false;
        }

        @Override
        public void onConnectionFailed() {
            super.onConnectionFailed();
            isConnected = false;
        }
    }

    private class MediaControllerCallback extends MediaControllerCompat.Callback {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);
            playbackState = state != null ? state : EMPTY_PLAYBACK_STATE;

            //状态监听
            CopyOnWriteArrayList<OnPlayerEventListener> mPlayerEventListeners = MusicManager.getInstance().getPlayerEventListeners();
            if (state != null) {
                for (OnPlayerEventListener listener : mPlayerEventListeners) {
                    switch (state.getState()) {
                        case PlaybackStateCompat.STATE_PLAYING:
                            listener.onPlayerStart();
                            break;
                        case PlaybackStateCompat.STATE_PAUSED:
                            listener.onPlayerPause();
                            break;
                        case PlaybackStateCompat.STATE_STOPPED:
                            listener.onPlayerStop();
                            break;
                        case PlaybackStateCompat.STATE_ERROR:
                            listener.onError(state.getErrorCode(), state.getErrorMessage().toString());
                            break;
                        case PlaybackStateCompat.STATE_NONE:
                            listener.onPlayCompletion();
                            break;
                        case PlaybackStateCompat.STATE_BUFFERING:
                            listener.onBuffering();
                            break;
                        default:
                            break;
                    }
                }
            }

        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            super.onMetadataChanged(metadata);
            nowPlaying = metadata != null ? metadata : NOTHING_PLAYING;

            //状态监听
            CopyOnWriteArrayList<OnPlayerEventListener> mPlayerEventListeners = MusicManager.getInstance().getPlayerEventListeners();
            if (metadata != null) {
                SongInfo songInfo = null;
                for (OnPlayerEventListener listener : mPlayerEventListeners) {
                    List<SongInfo> songInfos = MusicProvider.getInstance().getSongInfos();
                    for (SongInfo info : songInfos) {
                        if (info.getSongId().equals(metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID))) {
                            songInfo = info;
                            break;
                        }
                    }
                    listener.onMusicSwitch(songInfo);
                }
            }

        }

        @Override
        public void onQueueChanged(List<MediaSessionCompat.QueueItem> queue) {
            super.onQueueChanged(queue);
        }

        @Override
        public void onSessionDestroyed() {
            super.onSessionDestroyed();
            mediaBrowserConnectionCallback.onConnectionSuspended();
        }
    }

    private static PlaybackStateCompat EMPTY_PLAYBACK_STATE = new PlaybackStateCompat.Builder()
            .setState(PlaybackStateCompat.STATE_NONE, 0, 0f)
            .build();

    private static MediaMetadataCompat NOTHING_PLAYING = new MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, "")
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, 0)
            .build();
}
