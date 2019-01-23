/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lzx.starrysky.playback;

import android.content.Context;
import android.net.Uri;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.lzx.starrysky.model.MusicProvider;

import static android.support.v4.media.session.MediaSessionCompat.QueueItem;
import static com.google.android.exoplayer2.C.CONTENT_TYPE_MUSIC;
import static com.google.android.exoplayer2.C.USAGE_MEDIA;

public final class LocalPlayback implements Playback {

    private static final String TAG = "LocalPlayback";

    private final Context mContext;
    private boolean mPlayOnFocusGain;
    private Callback mCallback;
    private final MusicProvider mMusicProvider;
    private String mCurrentMediaId;

    private SimpleExoPlayer mExoPlayer;
    private final ExoPlayerEventListener mEventListener = new ExoPlayerEventListener();

    // Whether to return STATE_NONE or STATE_STOPPED when mExoPlayer is null;
    private boolean mExoPlayerNullIsStopped = false;

    public LocalPlayback(Context context, MusicProvider musicProvider) {
        this.mContext = context.getApplicationContext();
        this.mMusicProvider = musicProvider;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop(boolean notifyListeners) {
        releaseResources(true);
    }

    @Override
    public void setState(int state) {

    }

    @Override
    public int getState() {
        if (mExoPlayer == null) {
            return mExoPlayerNullIsStopped
                    ? PlaybackStateCompat.STATE_STOPPED
                    : PlaybackStateCompat.STATE_NONE;
        }
        switch (mExoPlayer.getPlaybackState()) {
            case Player.STATE_IDLE:
                return PlaybackStateCompat.STATE_PAUSED;
            case Player.STATE_BUFFERING:
                return PlaybackStateCompat.STATE_BUFFERING;
            case Player.STATE_READY:
                return mExoPlayer.getPlayWhenReady()
                        ? PlaybackStateCompat.STATE_PLAYING
                        : PlaybackStateCompat.STATE_PAUSED;
            case Player.STATE_ENDED:
                return PlaybackStateCompat.STATE_PAUSED;
            default:
                return PlaybackStateCompat.STATE_NONE;
        }
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public boolean isPlaying() {
        return mPlayOnFocusGain || (mExoPlayer != null && mExoPlayer.getPlayWhenReady());
    }

    @Override
    public long getCurrentStreamPosition() {
        return mExoPlayer != null ? mExoPlayer.getCurrentPosition() : 0;
    }

    @Override
    public void updateLastKnownStreamPosition() {
        // Nothing to do. Position maintained by ExoPlayer.
    }

    @Override
    public void play(QueueItem item) {
        mPlayOnFocusGain = true;
//        tryToGetAudioFocus();
//        registerAudioNoisyReceiver();
        String mediaId = item.getDescription().getMediaId();
        boolean mediaHasChanged = !TextUtils.equals(mediaId, mCurrentMediaId);
        if (mediaHasChanged) {
            mCurrentMediaId = mediaId;
        }

        if (mediaHasChanged || mExoPlayer == null) {
            releaseResources(false); // release everything except the player
            MediaMetadataCompat track = mMusicProvider.getMusic(item.getDescription().getMediaId());

            String source = track.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI);
            if (source != null) {
                source = source.replaceAll(" ", "%20"); // Escape spaces for URLs
            }

            if (mExoPlayer == null) {
                mExoPlayer = ExoPlayerFactory.newSimpleInstance(mContext);
                mExoPlayer.addListener(mEventListener);
            }

            final AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(CONTENT_TYPE_MUSIC)
                    .setUsage(USAGE_MEDIA)
                    .build();
            mExoPlayer.setAudioAttributes(audioAttributes, true);


            DataSource.Factory dataSourceFactory =
                    new DefaultDataSourceFactory(
                            mContext, Util.getUserAgent(mContext, "starrysky"), null);

            ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
            // The MediaSource represents the media to be played.
            ExtractorMediaSource.Factory extractorMediaFactory =
                    new ExtractorMediaSource.Factory(dataSourceFactory);
            extractorMediaFactory.setExtractorsFactory(extractorsFactory);
            MediaSource mediaSource =
                    extractorMediaFactory.createMediaSource(Uri.parse(source));

            mExoPlayer.prepare(mediaSource);
        }
        mExoPlayer.setPlayWhenReady(true);
    }

    @Override
    public void pause() {
        if (mExoPlayer != null) {
            mExoPlayer.setPlayWhenReady(false);
        }
        releaseResources(false);
    }

    @Override
    public void seekTo(long position) {
        if (mExoPlayer != null) {
            mExoPlayer.seekTo(position);
        }
    }

    @Override
    public void setCallback(Callback callback) {
        this.mCallback = callback;
    }

    @Override
    public void setCurrentMediaId(String mediaId) {
        this.mCurrentMediaId = mediaId;
    }

    @Override
    public String getCurrentMediaId() {
        return mCurrentMediaId;
    }

    /**
     * 快进
     */
    @Override
    public void onFastForward() {
        if (mExoPlayer != null) {
            float currSpeed = mExoPlayer.getPlaybackParameters().speed;
            float currPitch = mExoPlayer.getPlaybackParameters().pitch;
            float newSpeed = currSpeed + 0.5f;
            mExoPlayer.setPlaybackParameters(new PlaybackParameters(newSpeed, currPitch));
        }
    }

    /**
     * 倒带
     */
    @Override
    public void onRewind() {
        if (mExoPlayer != null) {
            float currSpeed = mExoPlayer.getPlaybackParameters().speed;
            float currPitch = mExoPlayer.getPlaybackParameters().pitch;
            float newSpeed = currSpeed - 0.5f;
            if (newSpeed <= 0) {
                newSpeed = 0;
            }
            mExoPlayer.setPlaybackParameters(new PlaybackParameters(newSpeed, currPitch));
        }
    }

    /**
     * 设置音量
     * @param audioVolume
     */
    @Override
    public void setVolume(float audioVolume){
        if (mExoPlayer!=null){
            mExoPlayer.setVolume(audioVolume);
        }
    }

    /**
     * Releases resources used by the service for playback, which is mostly just the WiFi lock for
     * local playback. If requested, the ExoPlayer instance is also released.
     *
     * @param releasePlayer Indicates whether the player should also be released
     */
    private void releaseResources(boolean releasePlayer) {
        if (releasePlayer && mExoPlayer != null) {
            mExoPlayer.release();
            mExoPlayer.removeListener(mEventListener);
            mExoPlayer = null;
            mExoPlayerNullIsStopped = true;
            mPlayOnFocusGain = false;
        }
    }

    private final class ExoPlayerEventListener implements Player.EventListener {
        @Override
        public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {
            // Nothing to do.
        }

        @Override
        public void onTracksChanged(
                TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
            // Nothing to do.
        }

        @Override
        public void onLoadingChanged(boolean isLoading) {
            // Nothing to do.
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            switch (playbackState) {
                case Player.STATE_IDLE:
                case Player.STATE_BUFFERING:
                case Player.STATE_READY:
                    if (mCallback != null) {
                        mCallback.onPlaybackStatusChanged(getState());
                    }
                    break;
                case Player.STATE_ENDED:
                    // The media player finished playing the current song.
                    if (mCallback != null) {
                        mCallback.onCompletion();
                    }
                    break;
            }
        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            final String what;
            switch (error.type) {
                case ExoPlaybackException.TYPE_SOURCE:
                    what = error.getSourceException().getMessage();
                    break;
                case ExoPlaybackException.TYPE_RENDERER:
                    what = error.getRendererException().getMessage();
                    break;
                case ExoPlaybackException.TYPE_UNEXPECTED:
                    what = error.getUnexpectedException().getMessage();
                    break;
                default:
                    what = "Unknown: " + error;
            }

//            LogHelper.e(TAG, "ExoPlayer error: what=" + what);
            if (mCallback != null) {
                mCallback.onError("ExoPlayer error " + what);
            }
        }

        @Override
        public void onPositionDiscontinuity(int reason) {
            // Nothing to do.
        }

        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
            // Nothing to do.
        }

        @Override
        public void onSeekProcessed() {
            // Nothing to do.
        }

        @Override
        public void onRepeatModeChanged(int repeatMode) {
            // Nothing to do.
        }

        @Override
        public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
            // Nothing to do.
        }
    }
}
