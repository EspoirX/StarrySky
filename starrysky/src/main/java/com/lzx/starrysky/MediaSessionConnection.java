package com.lzx.starrysky;

import android.content.ComponentName;
import android.content.Context;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import java.util.List;

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

    public MediaSessionConnection(Context context, ComponentName serviceComponent) {
        mContext = context;
        this.serviceComponent = serviceComponent;
        mediaBrowserConnectionCallback = new MediaBrowserConnectionCallback();
        mediaBrowser = new MediaBrowserCompat(context, serviceComponent, mediaBrowserConnectionCallback, null);
        mediaBrowser.connect();



    }

    public void subscribe(String parentId, MediaBrowserCompat.SubscriptionCallback callback) {
        mediaBrowser.subscribe(parentId, callback);
    }

    public void unsubscribe(String parentId, MediaBrowserCompat.SubscriptionCallback callback) {
        mediaBrowser.unsubscribe(parentId, callback);
    }

    public boolean isConnected() {
        return isConnected;
    }

    public String getRootMediaId() {
        return rootMediaId;
    }

    public PlaybackStateCompat getPlaybackState() {
        return playbackState;
    }

    public MediaMetadataCompat getNowPlaying() {
        return nowPlaying;
    }

    public MediaControllerCompat.TransportControls getTransportControls() {
        return transportControls;
    }

    private class MediaBrowserConnectionCallback extends MediaBrowserCompat.ConnectionCallback {
        @Override
        public void onConnected() {
            super.onConnected();
            try {
                mediaController = new MediaControllerCompat(mContext, mediaBrowser.getSessionToken());
                mediaController.registerCallback(new MediaControllerCallback());
                transportControls = mediaController.getTransportControls();
                rootMediaId = mediaBrowser.getRoot();
                Log.i("xian","rootMediaId="+rootMediaId);
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
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            super.onMetadataChanged(metadata);
            nowPlaying = metadata != null ? metadata : NOTHING_PLAYING;
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
