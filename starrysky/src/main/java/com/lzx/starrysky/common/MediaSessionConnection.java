package com.lzx.starrysky.common;

import android.arch.lifecycle.MutableLiveData;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.lzx.starrysky.MusicManager;
import com.lzx.starrysky.StarrySky;
import com.lzx.starrysky.control.OnPlayerEventListener;
import com.lzx.starrysky.provider.SongInfo;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * 与服务端连接的管理类
 */
public class MediaSessionConnection {
    private Context mContext;

    private MediaBrowserCompat mediaBrowser;
    private String rootMediaId;
    private MutableLiveData<Boolean> isConnected = new MutableLiveData<>();
    private MutableLiveData<PlaybackStage> playbackState = new MutableLiveData<>();
    private MutableLiveData<MediaMetadataCompat> nowPlaying = new MutableLiveData<>();
    private MutableLiveData<PlaybackStateCompat> playbackStateCompat = new MutableLiveData<>();

    private MediaControllerCompat.TransportControls transportControls;
    private MediaControllerCompat mediaController;
    private MediaBrowserConnectionCallback mediaBrowserConnectionCallback;
    private MediaControllerCallback mMediaControllerCallback;
    private OnConnectListener mConnectListener;


    public MediaSessionConnection(Context context, ComponentName serviceComponent) {
        this.mContext = context;
        mediaBrowserConnectionCallback = new MediaBrowserConnectionCallback();
        mMediaControllerCallback = new MediaControllerCallback();
        mediaBrowser = new MediaBrowserCompat(mContext, serviceComponent, mediaBrowserConnectionCallback, null);

        isConnected.postValue(false);
        playbackState.postValue(PlaybackStage.buildNone());
        nowPlaying.postValue(NOTHING_PLAYING);
        playbackStateCompat.postValue(EMPTY_PLAYBACK_STATE);
    }

    public void subscribe(String parentId, MediaBrowserCompat.SubscriptionCallback callback) {
        mediaBrowser.subscribe(parentId, callback);
    }

    public void unsubscribe(String parentId, MediaBrowserCompat.SubscriptionCallback callback) {
        mediaBrowser.unsubscribe(parentId, callback);
    }

    /**
     * 给服务发消息
     */
    public void sendCommand(String command, Bundle parameters) {
        if (mediaBrowser.isConnected()) {
            mediaController.sendCommand(command, parameters, new ResultReceiver(new Handler()) {

            });
        }
    }

    public void setConnectListener(OnConnectListener connectListener) {
        mConnectListener = connectListener;
    }

    /**
     * 是否已连接
     */
    public MutableLiveData<Boolean> isConnected() {
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


    public int getShuffleMode() {
        return mediaController.getShuffleMode();
    }

    public int getRepeatMode() {
        return mediaController.getRepeatMode();
    }

    /**
     * 获取当前播放的 MediaMetadataCompat
     */
    public MediaMetadataCompat getNowPlaying() {
        return nowPlaying.getValue();
    }

    public PlaybackStateCompat getPlaybackStateCompat() {
        return playbackStateCompat.getValue();
    }

    public MutableLiveData<PlaybackStage> getPlaybackState() {
        return playbackState;
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
        if (isConnected.getValue() == null || !isConnected.getValue()) {
            //进程被异常杀死时，App 被外部链接唤起时，connect 状态为 CONNECT_STATE_CONNECTING，
            //导致崩溃，所以要先执行 disconnect
            disconnectImpl();
            mediaBrowser.connect();
        }
    }

    /**
     * 断开连接
     */
    public void disconnect() {
        if (isConnected.getValue() != null && isConnected.getValue()) {
            disconnectImpl();
            isConnected.postValue(false);
        }
    }

    private void disconnectImpl() {
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
                isConnected.postValue(true);
                if (mConnectListener != null) {
                    mConnectListener.onConnected();
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onConnectionSuspended() {
            super.onConnectionSuspended();
            isConnected.postValue(false);
            disconnect();
        }

        @Override
        public void onConnectionFailed() {
            super.onConnectionFailed();
            isConnected.postValue(false);
        }
    }

    private class MediaControllerCallback extends MediaControllerCompat.Callback {

        PlaybackStage playbackStage = PlaybackStage.buildNone();


        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);
            if (state == null) {
                playbackState.postValue(playbackStage);
                playbackStateCompat.postValue(EMPTY_PLAYBACK_STATE);
                return;
            }
            playbackStateCompat.postValue(state);
            String songId = getNowPlaying().getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
            //状态监听
            CopyOnWriteArrayList<OnPlayerEventListener> mPlayerEventListeners = MusicManager.getInstance().getPlayerEventListeners();
            switch (state.getState()) {
                case PlaybackStateCompat.STATE_PLAYING:
                    for (OnPlayerEventListener listener : mPlayerEventListeners) {
                        listener.onPlayerStart();
                    }
                    playbackState.postValue(playbackStage.buildStart(songId));
                    break;
                case PlaybackStateCompat.STATE_PAUSED:
                    for (OnPlayerEventListener listener : mPlayerEventListeners) {
                        listener.onPlayerPause();
                    }
                    playbackState.postValue(playbackStage.buildPause(songId));
                    break;
                case PlaybackStateCompat.STATE_STOPPED:
                    for (OnPlayerEventListener listener : mPlayerEventListeners) {
                        listener.onPlayerStop();
                    }
                    playbackState.postValue(playbackStage.buildStop(songId));
                    break;
                case PlaybackStateCompat.STATE_ERROR:
                    for (OnPlayerEventListener listener : mPlayerEventListeners) {
                        listener.onError(state.getErrorCode(), state.getErrorMessage().toString());
                    }
                    playbackState.postValue(playbackStage.buildError(songId, state.getErrorCode(), state.getErrorMessage().toString()));
                    break;
                case PlaybackStateCompat.STATE_NONE:
                    for (OnPlayerEventListener listener : mPlayerEventListeners) {
                        SongInfo songInfo = StarrySky.get().getMediaQueueProvider().getSongInfo(songId);
                        listener.onPlayCompletion(songInfo);
                    }
                    playbackState.postValue(playbackStage.buildCompletion(songId));
                    break;
                case PlaybackStateCompat.STATE_BUFFERING:
                    for (OnPlayerEventListener listener : mPlayerEventListeners) {
                        listener.onBuffering();
                    }
                    playbackState.postValue(playbackStage.buildBuffering(songId));
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            super.onMetadataChanged(metadata);
            nowPlaying.postValue(metadata != null ? metadata : NOTHING_PLAYING);
            if (metadata == null) {
                return;
            }
            String songId = metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
            SongInfo songInfo = StarrySky.get().getMediaQueueProvider().getSongInfo(songId);
            playbackState.postValue(playbackStage.buildSwitch(songId));
            //状态监听
            CopyOnWriteArrayList<OnPlayerEventListener> mPlayerEventListeners = StarrySky.with().getPlayerEventListeners();
            for (OnPlayerEventListener listener : mPlayerEventListeners) {
                listener.onMusicSwitch(songInfo);
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

    public interface OnConnectListener {
        void onConnected();
    }
}
