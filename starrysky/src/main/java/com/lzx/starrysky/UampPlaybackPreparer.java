package com.lzx.starrysky;

import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.lzx.starrysky.model.MusicProvider;

import java.util.List;

public class UampPlaybackPreparer implements MediaSessionConnector.PlaybackPreparer {
    private ExoPlayer mExoPlayer;
    private DataSource.Factory dataSourceFactory;

    public UampPlaybackPreparer(ExoPlayer exoPlayer, DataSource.Factory dataSourceFactory) {
        mExoPlayer = exoPlayer;
        this.dataSourceFactory = dataSourceFactory;
    }

    @Override
    public long getSupportedPrepareActions() {
        return PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID |
                PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID |
                PlaybackStateCompat.ACTION_PREPARE_FROM_SEARCH |
                PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH;
    }

    @Override
    public void onPrepare() {

    }

    @Override
    public void onPrepareFromMediaId(String mediaId, Bundle extras) {
        List<MediaMetadataCompat> list = MusicProvider.getInstance().getMetadatas();
        MediaMetadataCompat itemToPlay = null;
        for (MediaMetadataCompat metadata : list) {
            if (mediaId.equals(metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID))) {
                itemToPlay = metadata;
                break;
            }
        }
        if (itemToPlay != null) {
            ConcatenatingMediaSource mediaSource = MusicProvider.getInstance().toMediaSource(dataSourceFactory);
            int initialWindowIndex = list.indexOf(itemToPlay);
            mExoPlayer.prepare(mediaSource);
            mExoPlayer.seekTo(initialWindowIndex, 0);
        }
    }

    @Override
    public void onPrepareFromSearch(String query, Bundle extras) {

    }

    @Override
    public void onPrepareFromUri(Uri uri, Bundle extras) {

    }

    @Override
    public String[] getCommands() {
        return new String[0];
    }

    @Override
    public void onCommand(Player player, String command, Bundle extras, ResultReceiver cb) {

    }
}
