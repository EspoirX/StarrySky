package com.lzx.musiclibrary.playback.player;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.os.Build;
import android.text.TextUtils;

import com.danikula.videocache.HttpProxyCacheServer;
import com.lzx.musiclibrary.MusicService;
import com.lzx.musiclibrary.aidl.model.SongInfo;
import com.lzx.musiclibrary.cache.CacheConfig;
import com.lzx.musiclibrary.cache.CacheUtils;
import com.lzx.musiclibrary.constans.State;
import com.lzx.musiclibrary.manager.FocusAndLockManager;
import com.lzx.musiclibrary.utils.BaseUtil;
import com.lzx.musiclibrary.utils.LogUtil;
import com.lzx.musiclibrary.utils.SPUtils;

import java.io.IOException;

import static com.lzx.musiclibrary.constans.Constans.play_back_pitch;
import static com.lzx.musiclibrary.constans.Constans.play_back_speed;
import static com.lzx.musiclibrary.manager.FocusAndLockManager.AUDIO_NO_FOCUS_CAN_DUCK;
import static com.lzx.musiclibrary.manager.FocusAndLockManager.AUDIO_NO_FOCUS_NO_DUCK;
import static com.lzx.musiclibrary.manager.FocusAndLockManager.VOLUME_DUCK;
import static com.lzx.musiclibrary.manager.FocusAndLockManager.VOLUME_NORMAL;

/**
 * Created by xian on 2018/1/20.
 */

public class MediaPlayback implements Playback,
        FocusAndLockManager.AudioFocusChangeListener,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnSeekCompleteListener {

    private boolean isOpenCacheWhenPlaying = false;
    private boolean mPlayOnFocusGain;
    private boolean mAudioNoisyReceiverRegistered;
    private boolean mExoPlayerNullIsStopped = false;
    private String mCurrentMediaId; //当前播放的媒体id
    private long currbufferedPosition = 0;
    private SongInfo mCurrentMediaSongInfo;
    private boolean isGiveUpAudioFocusManager = false;
    private MediaPlayer mMediaPlayer;
    private Callback mCallback;
    private Context mContext;
    private FocusAndLockManager mFocusAndLockManager;

    private int mPlayState = State.STATE_IDLE;

    private HttpProxyCacheServer mProxyCacheServer;
    private HttpProxyCacheServer.Builder builder;


    public MediaPlayback(Context context, CacheConfig cacheConfig, boolean isGiveUpAudioFocusManager) {
        Context applicationContext = context.getApplicationContext();
        this.mContext = applicationContext;
        mFocusAndLockManager = new FocusAndLockManager(applicationContext, this);
        this.isGiveUpAudioFocusManager = isGiveUpAudioFocusManager;
        builder = CacheUtils.createHttpProxyCacheServerBuilder(mContext, cacheConfig);
        if (cacheConfig != null && cacheConfig.isOpenCacheWhenPlaying()) {
            isOpenCacheWhenPlaying = true;
        }
        mProxyCacheServer = builder.build();
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

    private void releaseResources(boolean releasePlayer) {
        // Stops and releases player (if requested and available).
        if (releasePlayer && mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
            mExoPlayerNullIsStopped = true;
            mPlayOnFocusGain = false;
        }
        mFocusAndLockManager.releaseWifiLock();
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

    private void configurePlayerState() {
        if (mFocusAndLockManager.getCurrentAudioFocusState() == AUDIO_NO_FOCUS_NO_DUCK) {
            if (!isGiveUpAudioFocusManager) {
                pause();
            }
        } else {
            registerAudioNoisyReceiver();
            if (mFocusAndLockManager.getCurrentAudioFocusState() == AUDIO_NO_FOCUS_CAN_DUCK) {
                mMediaPlayer.setVolume(VOLUME_DUCK, VOLUME_DUCK);
            } else {
                mMediaPlayer.setVolume(VOLUME_NORMAL, VOLUME_NORMAL);
            }
            if (mPlayOnFocusGain) {
                mMediaPlayer.start();
                mPlayState = State.STATE_PLAYING;
                mPlayOnFocusGain = false;
                if (mCallback != null) {
                    mCallback.onPlaybackStatusChanged(mPlayState);
                }
            }
        }
    }

    @Override
    public void start() {

    }

    @Override
    public void stop(boolean notifyListeners) {
        mFocusAndLockManager.giveUpAudioFocus();
        unregisterAudioNoisyReceiver();
        releaseResources(true);
        mPlayState = State.STATE_STOP;
    }

    @Override
    public void setState(int state) {

    }

    @Override
    public int getState() {
        int state;
        if (mMediaPlayer == null) {
            state = State.STATE_IDLE;
        } else {
            state = mPlayState;
        }
        return state;
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public boolean isPlaying() {
        return mPlayOnFocusGain || (mMediaPlayer != null && mPlayState == State.STATE_PLAYING);
    }

    @Override
    public long getCurrentStreamPosition() {
        return mMediaPlayer != null ? mMediaPlayer.getCurrentPosition() : 0;
    }

    @Override
    public long getBufferedPosition() {
        return currbufferedPosition;
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
            mCurrentMediaSongInfo = info;
        }

        if (mediaHasChanged || mMediaPlayer == null) {
            releaseResources(false); // release everything except the player

            String source = info.getSongUrl();
            if (source != null && BaseUtil.isOnLineSource(source)) {
                source = source.replaceAll(" ", "%20"); // Escape spaces for URLs
            }

            String playUrl;
            if (BaseUtil.isOnLineSource(source)) {
                if (isOpenCacheWhenPlaying) {
                    playUrl = mProxyCacheServer.getProxyUrl(source);
                } else {
                    playUrl = source;
                }
            } else {
                playUrl = source;
            }
            if (TextUtils.isEmpty(playUrl)) {
                if (mCallback != null) {
                    mCallback.onError("song url is null");
                }
                return;
            }

            //LogUtil.i("isOpenCacheWhenPlaying = " + isOpenCacheWhenPlaying + " playUri = " + playUrl);

            if (mMediaPlayer == null) {
                mMediaPlayer = new MediaPlayer();
                //当装载流媒体完毕的时候回调
                mMediaPlayer.setOnPreparedListener(this);
                //当流媒体播放完毕的时候回调
                mMediaPlayer.setOnCompletionListener(this);
                //当播放中发生错误的时候回调
                mMediaPlayer.setOnErrorListener(this);
                mMediaPlayer.setOnBufferingUpdateListener(this);
                mMediaPlayer.setOnSeekCompleteListener(this);

                changePlaybackParameters();
            }

            try {
                mMediaPlayer.reset();
                mMediaPlayer.setDataSource(playUrl);
                mMediaPlayer.prepareAsync();
                mPlayState = State.STATE_ASYNC_LOADING;
                if (mCallback != null) {
                    mCallback.onPlaybackStatusChanged(mPlayState);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            mFocusAndLockManager.acquireWifiLock();
        } else {
            configurePlayerState();
        }
    }

    private void changePlaybackParameters() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            float spSpeed = (float) SPUtils.get(mContext, play_back_speed, 1f);
            float spPitch = (float) SPUtils.get(mContext, play_back_pitch, 1f);
            float currSpeed = mMediaPlayer.getPlaybackParams().getSpeed();
            float currPitch = mMediaPlayer.getPlaybackParams().getPitch();
            if (spSpeed != currSpeed || spPitch != currPitch) {
                setPlaybackParameters(spSpeed, spPitch);
            }
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        configurePlayerState();
    }

    @Override
    public void pause() {
        if (mMediaPlayer != null) {
            mMediaPlayer.pause();
            mPlayState = State.STATE_PAUSED;
            if (mCallback != null) {
                mCallback.onPlaybackStatusChanged(mPlayState);
            }
        }
        // While paused, retain the player instance, but give up audio focus.
        releaseResources(false);
        unregisterAudioNoisyReceiver();
    }

    @Override
    public void seekTo(long position) {
        if (mMediaPlayer != null) {
            registerAudioNoisyReceiver();
            mMediaPlayer.seekTo((int) position);
        }
    }

    @Override
    public void setCurrentMediaId(String mediaId) {
        mCurrentMediaId = mediaId;
    }

    @Override
    public String getCurrentMediaId() {
        return mCurrentMediaId;
    }

    @Override
    public int getDuration() {
        return mMediaPlayer != null ? mMediaPlayer.getDuration() : 0;
    }

    @Override
    public SongInfo getCurrentMediaSongInfo() {
        return mCurrentMediaSongInfo;
    }

    @Override
    public void openCacheWhenPlaying(boolean isOpen) {
        isOpenCacheWhenPlaying = isOpen;
    }

    @Override
    public void setPlaybackParameters(float speed, float pitch) {
        if (mMediaPlayer != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PlaybackParams params = new PlaybackParams();
            params.setSpeed(speed);
            params.setPitch(pitch);
            mMediaPlayer.setPlaybackParams(params);
        }
    }

    @Override
    public void setVolume(float audioVolume) {
        if (mMediaPlayer != null) {
            mMediaPlayer.setVolume(audioVolume, audioVolume);
        }
    }

    @Override
    public int getAudioSessionId() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.getAudioSessionId();
        }
        return 0;
    }

    @Override
    public float getPlaybackSpeed() {
        if (mMediaPlayer != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return mMediaPlayer.getPlaybackParams().getSpeed();
        } else {
            return (float) SPUtils.get(mContext, play_back_speed, 1f);
        }
    }

    @Override
    public float getPlaybackPitch() {
        if (mMediaPlayer != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return mMediaPlayer.getPlaybackParams().getPitch();
        } else {
            return (float) SPUtils.get(mContext, play_back_pitch, 1f);
        }
    }


    @Override
    public void setCallback(Callback callback) {
        this.mCallback = callback;
    }

    @Override
    public void onAudioFocusLossTransient() {
        mPlayOnFocusGain = mMediaPlayer != null && mPlayState == State.STATE_PLAYING;
    }

    @Override
    public void onAudioFocusChange() {
        if (mMediaPlayer != null) {
            // Update the player state based on the change
            configurePlayerState();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (mCallback != null) {
            mCallback.onPlayCompletion();
            //mCallback.onPlaybackStatusChanged(State.STATE_ENDED);
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        if (mCallback != null) {
            mCallback.onError("MediaPlayer error " + what);
        }
        return false;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        if (isOpenCacheWhenPlaying && mCurrentMediaSongInfo != null) {
            boolean fullyCached = mProxyCacheServer.isCached(mCurrentMediaSongInfo.getSongUrl());
            currbufferedPosition = fullyCached ? getDuration() : percent * getDuration();
        } else {
            currbufferedPosition = percent * getDuration();
        }
    }

    @Override
    public void onSeekComplete(MediaPlayer mediaPlayer) {
       //TODO
        LogUtil.i("-------------------onSeekComplete------------------");
    }
}
