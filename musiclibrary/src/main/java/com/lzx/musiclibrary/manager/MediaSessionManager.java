package com.lzx.musiclibrary.manager;

import android.content.ComponentName;
import android.content.Context;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.lzx.musiclibrary.playback.PlaybackManager;
import com.lzx.musiclibrary.receiver.RemoteControlReceiver;

import java.util.List;

/**
 * @author lzx
 * @date 2018/2/11
 */

public class MediaSessionManager {
    private static final String TAG = "MusicService";
    private MediaSessionCompat mMediaSession;
    private Context mContext;

    public MediaSessionManager(Context context, PlaybackManager playbackManager) {
        mContext = context;
        ComponentName mediaButtonReceiver = new ComponentName(mContext, RemoteControlReceiver.class);
        mMediaSession = new MediaSessionCompat(mContext, TAG, mediaButtonReceiver, null);
        mMediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS | MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS);
        mMediaSession.setCallback(playbackManager.getMediaSessionCallback());
        mMediaSession.setActive(true);
    }

    public void updateMetaData(MediaMetadataCompat metadataCompat) {

        mMediaSession.setMetadata(metadataCompat);
    }

    public void setQueue(List<MediaSessionCompat.QueueItem> newQueue) {
        mMediaSession.setQueue(newQueue);
    }

    public void setPlaybackState(PlaybackStateCompat state){
        mMediaSession.setPlaybackState(state);
    }

    public void release(){
        mMediaSession.setCallback(null);
        mMediaSession.setActive(false);
        mMediaSession.release();
    }
}
