package com.lzx.musiclibrary.playback.player;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.Uri;
import android.text.TextUtils;

import com.google.android.exoplayer2.DefaultLoadControl;
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
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.lzx.musiclibrary.MusicService;
import com.lzx.musiclibrary.aidl.model.SongInfo;
import com.lzx.musiclibrary.constans.State;
import com.lzx.musiclibrary.manager.FocusAndLockManager;

import static com.google.android.exoplayer2.C.CONTENT_TYPE_MUSIC;
import static com.google.android.exoplayer2.C.USAGE_MEDIA;
import static com.lzx.musiclibrary.manager.FocusAndLockManager.AUDIO_NO_FOCUS_CAN_DUCK;
import static com.lzx.musiclibrary.manager.FocusAndLockManager.AUDIO_NO_FOCUS_NO_DUCK;
import static com.lzx.musiclibrary.manager.FocusAndLockManager.VOLUME_DUCK;
import static com.lzx.musiclibrary.manager.FocusAndLockManager.VOLUME_NORMAL;

/**
 * Created by xian on 2018/1/20.
 */

public class ExoPlayback implements Playback, FocusAndLockManager.AudioFocusChangeListener {


    private boolean mPlayOnFocusGain;
    private boolean mAudioNoisyReceiverRegistered;
    private String mCurrentMediaId; //当前播放的媒体id

    private SimpleExoPlayer mExoPlayer;
    private final ExoPlayerEventListener mEventListener = new ExoPlayerEventListener();
    private boolean mExoPlayerNullIsStopped = false;


    private FocusAndLockManager mFocusAndLockManager;

    private Callback mCallback;

    private Context mContext;

    public ExoPlayback(Context context) {
        Context applicationContext = context.getApplicationContext();
        this.mContext = applicationContext;
        mFocusAndLockManager = new FocusAndLockManager(applicationContext, this);
    }

    private final IntentFilter mAudioNoisyIntentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);

    private final BroadcastReceiver mAudioNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                if (isPlaying()) {
                    Intent i = new Intent(context, MusicService.class);
                    mContext.startService(i);
                }
            }
        }
    };

    /**
     * 释放服务使用的资源进行播放，这主要只是WiFi锁本地播放。 如果请求，ExoPlayer实例也被释放。
     *
     * @param releasePlayer 指示播放器是否也应该被释放
     */
    private void releaseResources(boolean releasePlayer) {
        // Stops and releases player (if requested and available).
        if (releasePlayer && mExoPlayer != null) {
            mExoPlayer.release();
            mExoPlayer.removeListener(mEventListener);
            mExoPlayer = null;
            mExoPlayerNullIsStopped = true;
            mPlayOnFocusGain = false;
        }

        mFocusAndLockManager.releaseWifiLock();
    }

    @Override
    public void start() {

    }

    @Override
    public void stop(boolean notifyListeners) {
        mFocusAndLockManager.giveUpAudioFocus();
        unregisterAudioNoisyReceiver();
        releaseResources(true);

    }

    @Override
    public void setState(int state) {

    }

    @Override
    public int getState() {
        //STATE_IDLE      没有任何媒体播放。
        //STATE_BUFFERING 无法立即从当前位置进行播放
        //STATE_READY     可以从当前位置立即进行播放。 如果  {@link #getPlayWhenReady（）}为true，立即播放，否则暂停。
        //STATE_ENDED     已经完成播放媒体。
        int state = State.STATE_NONE;
        if (mExoPlayer == null) {
            state = /*mExoPlayerNullIsStopped ?   : */State.STATE_NONE;
        } else {
            switch (mExoPlayer.getPlaybackState()) {
                case Player.STATE_IDLE:
                    state = State.STATE_IDLE;
                    break;
                case Player.STATE_BUFFERING:
                    state = State.STATE_BUFFERING;
                    break;
                case Player.STATE_READY:
                    state = mExoPlayer.getPlayWhenReady() ? State.STATE_PLAYING : State.STATE_PAUSED;
                    break;
                case Player.STATE_ENDED:
                    state = State.STATE_ENDED;
                    break;
            }
        }
        return state;
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

    }

    @Override
    public void play(SongInfo info) {
        mPlayOnFocusGain = true;
        mFocusAndLockManager.tryToGetAudioFocus();
        registerAudioNoisyReceiver();
        String mediaId = info.getSongId();
        boolean mediaHasChanged = !TextUtils.equals(mediaId, mCurrentMediaId);
        if (mediaHasChanged) {
            mCurrentMediaId = mediaId;
        }

        if (mediaHasChanged || mExoPlayer == null) {
            releaseResources(false); // release everything except the player

            String source = info.getSongUrl();
            if (source != null) {
                source = source.replaceAll(" ", "%20"); // Escape spaces for URLs
            }

            if (mExoPlayer == null) {
                mExoPlayer = ExoPlayerFactory.newSimpleInstance(mContext, new DefaultTrackSelector(), new DefaultLoadControl());
                mExoPlayer.addListener(mEventListener);
            }

            final AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(CONTENT_TYPE_MUSIC)
                    .setUsage(USAGE_MEDIA)
                    .build();
            mExoPlayer.setAudioAttributes(audioAttributes);

            DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(mContext, Util.getUserAgent(mContext, "musiclibrary"), null);

            ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();

            MediaSource mediaSource = new ExtractorMediaSource(Uri.parse(source), dataSourceFactory, extractorsFactory, null, null);


            mExoPlayer.prepare(mediaSource);

            mFocusAndLockManager.acquireWifiLock();
        }

        configurePlayerState();
    }

    @Override
    public void pause() {
        // Pause player and cancel the 'foreground service' state.
        if (mExoPlayer != null) {
            mExoPlayer.setPlayWhenReady(false);
        }
        // While paused, retain the player instance, but give up audio focus.
        releaseResources(false);
        unregisterAudioNoisyReceiver();
    }

    @Override
    public void seekTo(long position) {
        if (mExoPlayer != null) {
            registerAudioNoisyReceiver();
            mExoPlayer.seekTo(position);
        }
    }

    @Override
    public void setCurrentMediaId(String mediaId) {
        this.mCurrentMediaId = mediaId;
    }

    @Override
    public String getCurrentMediaId() {
        return mCurrentMediaId;
    }

    @Override
    public void setCallback(Callback callback) {
        this.mCallback = callback;
    }

    /**
     *       *根据音频焦点设置重新配置播放器并启动/重新启动。 这种方法
     *       *启动/重新启动ExoPlayer的实例尊重当前的音频焦点状态。 所以，如果我们
     *       *有焦点，会正常播放; 如果我们没有重点，它会离开玩家
     *       *暂停或将其设置为低音量，具体取决于当前焦点允许的内容
     *       *设置。
     *      
     */
    private void configurePlayerState() {
        if (mFocusAndLockManager.getCurrentAudioFocusState() == AUDIO_NO_FOCUS_NO_DUCK) {
            // We don't have audio focus and can't duck, so we have to pause
            pause();

        } else {
            registerAudioNoisyReceiver();

            if (mFocusAndLockManager.getCurrentAudioFocusState() == AUDIO_NO_FOCUS_CAN_DUCK) {
                // We're permitted to play, but only if we 'duck', ie: play softly
                mExoPlayer.setVolume(VOLUME_DUCK);
            } else {
                mExoPlayer.setVolume(VOLUME_NORMAL);
            }

            // If we were playing when we lost focus, we need to resume playing.
            if (mPlayOnFocusGain) {
                mExoPlayer.setPlayWhenReady(true);
                mPlayOnFocusGain = false;
            }
        }
    }

    private void registerAudioNoisyReceiver() {
        if (!mAudioNoisyReceiverRegistered) {
            mContext.registerReceiver(mAudioNoisyReceiver, mAudioNoisyIntentFilter);
            mAudioNoisyReceiverRegistered = true;
        }
    }

    private void unregisterAudioNoisyReceiver() {
        if (mAudioNoisyReceiverRegistered) {
            mContext.unregisterReceiver(mAudioNoisyReceiver);
            mAudioNoisyReceiverRegistered = false;
        }
    }

    @Override
    public void onAudioFocusLossTransient() {
        mPlayOnFocusGain = mExoPlayer != null && mExoPlayer.getPlayWhenReady();
    }

    @Override
    public void onAudioFocusChange() {
        if (mExoPlayer != null) {
            // Update the player state based on the change
            configurePlayerState();
        }
    }

    /**
     * ExoPlayer事件监听器
     */
    private final class ExoPlayerEventListener implements Player.EventListener {

        @Override
        public void onTimelineChanged(Timeline timeline, Object manifest) {

        }

        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

        }

        @Override
        public void onLoadingChanged(boolean isLoading) {

        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            if (mCallback != null) {
                switch (playbackState) {
                    case Player.STATE_IDLE:
                        mCallback.onPlaybackStatusChanged(State.STATE_IDLE);
                        break;
                    case Player.STATE_BUFFERING:
                        mCallback.onPlaybackStatusChanged(State.STATE_BUFFERING);
                        break;
                    case Player.STATE_READY:
                        mCallback.onPlaybackStatusChanged(playWhenReady ? State.STATE_PLAYING : State.STATE_PAUSED);
                        break;
                    case Player.STATE_ENDED:
                        mCallback.onPlayCompletion();
                        mCallback.onPlaybackStatusChanged(State.STATE_ENDED);
                        break;
                }
            }
        }

        @Override
        public void onRepeatModeChanged(int repeatMode) {

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
            if (mCallback != null) {
                mCallback.onError("ExoPlayer error " + what);
            }
        }

        @Override
        public void onPositionDiscontinuity() {

        }

        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

        }
    }


}
