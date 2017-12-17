package com.example.musiclib.service;

import android.os.Build;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.example.musiclib.manager.MusicManager;
import com.example.musiclib.model.MusicInfo;
import com.example.musiclib.utils.CoverLoader;


/**
 * 实现媒体控制和线控等功能 5.0 以上
 *
 * @author lzx
 * @date 2017/12/14
 */

public class MediaSessionManager {
    private static final String TAG = "MediaSessionManager";
    private static final long MEDIA_SESSION_ACTIONS = PlaybackStateCompat.ACTION_PLAY
            | PlaybackStateCompat.ACTION_PAUSE
            | PlaybackStateCompat.ACTION_PLAY_PAUSE
            | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
            | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
            | PlaybackStateCompat.ACTION_STOP
            | PlaybackStateCompat.ACTION_SEEK_TO;

    private MusicPlayService mPlayService;
    private MediaSessionCompat mMediaSession;

    public MediaSessionManager(MusicPlayService playService) {
        mPlayService = playService;
        setupMediaSession();
    }

    private void setupMediaSession() {
        mMediaSession = new MediaSessionCompat(mPlayService, TAG);
        mMediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS | MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS);
        mMediaSession.setCallback(callback);
        mMediaSession.setActive(true);
    }

    public void updatePlaybackState() {
        int state = (mPlayService.isPlaying() || mPlayService.isPreparing()) ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED;
        mMediaSession.setPlaybackState(
                new PlaybackStateCompat.Builder()
                        .setActions(MEDIA_SESSION_ACTIONS)
                        .setState(state, mPlayService.getCurrentPosition(), 1)
                        .build());
    }

    public void updateMetaData(MusicInfo music) {
        if (music == null) {
            mMediaSession.setMetadata(null);
            return;
        }

        MediaMetadataCompat.Builder metaData = new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, music.getAlbumTitle())
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, music.getAlbumNickname())
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, music.getAlbumTitle())
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ARTIST, music.getAlbumNickname())
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, Long.parseLong(music.getMusicTime()))
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, CoverLoader.getInstance().getDefaultCover());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            metaData.putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, MusicManager.getMusicList().size());
        }

        mMediaSession.setMetadata(metaData.build());
    }

    public void release() {
        mMediaSession.setCallback(null);
        mMediaSession.setActive(false);
        mMediaSession.release();
    }

    private MediaSessionCompat.Callback callback = new MediaSessionCompat.Callback() {
        @Override
        public void onPlay() {
            mPlayService.playPause();
        }

        @Override
        public void onPause() {
            mPlayService.playPause();
        }

        @Override
        public void onSkipToNext() {
            mPlayService.next();
        }

        @Override
        public void onSkipToPrevious() {
            mPlayService.prev();
        }

        @Override
        public void onStop() {
            mPlayService.stop();
        }

        @Override
        public void onSeekTo(long pos) {
            mPlayService.seekTo((int) pos);
        }
    };
}
