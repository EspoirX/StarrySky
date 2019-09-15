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

package com.lzx.starrysky.playback.manager;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.lzx.starrysky.BaseMediaInfo;
import com.lzx.starrysky.StarrySky;
import com.lzx.starrysky.ext.PlaybackStateCompatExt;
import com.lzx.starrysky.notification.INotification;
import com.lzx.starrysky.playback.player.ExoPlayback;
import com.lzx.starrysky.playback.player.Playback;
import com.lzx.starrysky.playback.queue.MediaQueue;
import com.lzx.starrysky.provider.MediaQueueProvider;
import com.lzx.starrysky.provider.MediaResource;
import com.lzx.starrysky.registry.ValidRegistry;
import com.lzx.starrysky.utils.delayaction.PlayValidManager;
import com.lzx.starrysky.utils.delayaction.Valid;


/**
 * 播放管理类
 */
public class PlaybackManager implements IPlaybackManager, Playback.Callback {

    private static final String TAG = "PlaybackManager";

    private MediaQueue mMediaQueue;
    private Playback mPlayback;
    private PlaybackServiceCallback mServiceCallback;
    private MediaSessionCallback mMediaSessionCallback;
    private INotification notification;
    private int currRepeatMode;
    private boolean shouldPlayNext = true; //是否可以播放下一首
    private boolean shouldPlayPre = true;  //是否可以播放上一首
    private PlaybackStateCompat.Builder stateBuilder;
    private Handler mHandler = new Handler(Looper.getMainLooper());


    public PlaybackManager(MediaQueue mediaQueue, Playback playback) {
        mMediaQueue = mediaQueue;
        mMediaSessionCallback = new MediaSessionCallback();
        mPlayback = playback;
        mPlayback.setCallback(this);
        currRepeatMode = PlaybackStateCompat.REPEAT_MODE_NONE;
    }

    @Override
    public void setServiceCallback(PlaybackServiceCallback serviceCallback) {
        mServiceCallback = serviceCallback;
    }

    @Override
    public void setMetadataUpdateListener(MediaQueueProvider.MetadataUpdateListener listener) {
        mMediaQueue.setMetadataUpdateListener(listener);
    }

    @Override
    public void registerNotification(INotification notification) {
        this.notification = notification;
    }

    public Playback getPlayback() {
        return mPlayback;
    }

    @Override
    public MediaSessionCompat.Callback getMediaSessionCallback() {
        return mMediaSessionCallback;
    }

    @Override
    public boolean isPlaying() {
        return mPlayback.isPlaying();
    }

    /**
     * 播放
     */
    @Override
    public void handlePlayRequest(boolean isPlayWhenReady) {
        ValidRegistry validRegistry = StarrySky.get().getRegistry().getValidRegistry();
        if (validRegistry.hasValid()) {
            PlayValidManager validManager = PlayValidManager.get();
            validManager.setAction(songInfo -> {
                BaseMediaInfo mediaInfo = mMediaQueue.songInfoToMediaInfo(songInfo);
                PlaybackManager.this.checkThreadHandPlayRequest(mediaInfo, isPlayWhenReady);
            });
            for (Valid valid : validRegistry.getValids()) {
                validManager.addValid(valid != null ? valid : new ValidRegistry.DefaultValid());
            }
            validManager.doCall(mMediaQueue.getCurrMediaInfo().getMediaId());
        } else {
            checkThreadHandPlayRequest(null, isPlayWhenReady);
        }
    }

    private void checkThreadHandPlayRequest(BaseMediaInfo mediaInfo, boolean isPlayWhenReady) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mHandler.post(() -> handPlayRequestImpl(mediaInfo, isPlayWhenReady));
        } else {
            handPlayRequestImpl(mediaInfo, isPlayWhenReady);
        }
    }

    private void handPlayRequestImpl(BaseMediaInfo mediaInfo, boolean isPlayWhenReady) {
        MediaResource currentMusic = mMediaQueue.getCurrentMusic(mediaInfo);
        if (currentMusic != null) {
            if (isPlayWhenReady) {
                mServiceCallback.onPlaybackStart();
            }
            mPlayback.play(currentMusic, isPlayWhenReady);
        }
    }

    /**
     * 暂停
     */
    @Override
    public void handlePauseRequest() {
        if (mPlayback.isPlaying()) {
            mPlayback.pause();
            mServiceCallback.onPlaybackStop();
        }
    }

    /**
     * 停止
     */
    @Override
    public void handleStopRequest(String withError) {
        mPlayback.stop(true);
        mServiceCallback.onPlaybackStop();
        updatePlaybackState(false, withError);
    }

    /**
     * 快进
     */
    @Override
    public void handleFastForward() {
        mPlayback.onFastForward();
    }

    /**
     * 倒带
     */
    @Override
    public void handleRewind() {
        mPlayback.onRewind();
    }

    /**
     * 指定语速 refer 是否已当前速度为基数  multiple 倍率
     */
    @Override
    public void handleDerailleur(boolean refer, float multiple) {
        mPlayback.onDerailleur(refer, multiple);
    }

    /**
     * 更新播放状态
     */
    @Override
    public void updatePlaybackState(boolean isOnlyUpdateActions, String error) {
        if (isOnlyUpdateActions && stateBuilder != null) {
            //单独更新 Actions
            stateBuilder.setActions(getAvailableActions());
            mServiceCallback.onPlaybackStateUpdated(stateBuilder.build(), null);
        } else {
            long position = PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN;
            if (mPlayback != null && mPlayback.isConnected()) {
                position = mPlayback.getCurrentStreamPosition();
            }
            //构建一个播放状态对象
            stateBuilder = new PlaybackStateCompat.Builder()
                    .setActions(getAvailableActions());
            //获取播放器播放状态
            int state = mPlayback.getState();
            //如果错误信息不为 null 的时候，播放状态设为 STATE_ERROR
            if (error != null) {
                stateBuilder.setErrorMessage(error);
                state = PlaybackStateCompat.STATE_ERROR;
            }
            //设置播放状态
            stateBuilder.setState(state, position, 1.0f, SystemClock.elapsedRealtime());
            //设置当前活动的 songId
            MediaResource currentMusic = mMediaQueue.getCurrentMusic();
            MediaMetadataCompat currMetadata = null;
            if (currentMusic != null) {
                stateBuilder.setActiveQueueItemId(currentMusic.getQueueId());
                final String musicId = currentMusic.getMediaId();
                currMetadata = StarrySky.get().getMediaQueueProvider().getMediaMetadataCompatById(musicId);
            }
            //把状态回调出去
            mServiceCallback.onPlaybackStateUpdated(stateBuilder.build(), currMetadata);
            //如果是播放或者暂停的状态，更新一下通知栏
            if (state == PlaybackStateCompat.STATE_PLAYING || state == PlaybackStateCompat.STATE_PAUSED) {
                mServiceCallback.onNotificationRequired();
            }
        }
    }

    /**
     * 获取状态
     */
    private long getAvailableActions() {
        long actions = PlaybackStateCompat.ACTION_PLAY_PAUSE |
                PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID |
                PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH |
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT;
        if (mPlayback.isPlaying()) {
            actions |= PlaybackStateCompat.ACTION_PAUSE; //添加 ACTION_PAUSE
        } else {
            actions |= PlaybackStateCompat.ACTION_PLAY; //添加 ACTION_PLAY
        }
        if (!shouldPlayNext) {
            //在不能播放下一首的情况下，判断actions是否包含ACTION_SKIP_TO_NEXT，如果包含则清除
            if ((actions & PlaybackStateCompat.ACTION_SKIP_TO_NEXT) != 0) {
                actions &= ~PlaybackStateCompat.ACTION_SKIP_TO_NEXT;
            }
        } else {
            //判断 actions 是否包含 ACTION_SKIP_TO_NEXT，如果不包含，则添加
            if ((actions & PlaybackStateCompat.ACTION_SKIP_TO_NEXT) == 0) {
                actions |= PlaybackStateCompat.ACTION_SKIP_TO_NEXT;
            }
        }
        //同理
        if (!shouldPlayPre) {
            if ((actions & PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS) != 0) {
                actions &= ~PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS;
            }
        } else {
            if ((actions & PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS) == 0) {
                actions |= PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS;
            }
        }
        return actions;
    }

    /**
     * 播放器播放完成回调
     */
    @Override
    public void onCompletion() {
        updatePlaybackState(false, null);
        //单曲模式(播放当前就结束)
        if (currRepeatMode == PlaybackStateCompatExt.SINGLE_MODE_ONE) {
            return;
        }
        if (currRepeatMode == PlaybackStateCompat.REPEAT_MODE_NONE) {
            //顺序播放
            if (shouldPlayNext && mMediaQueue.skipQueuePosition(1)) {
                handlePlayRequest(true);
                mMediaQueue.updateMetadata();
            } else {
                handleStopRequest(null);
            }
        } else if (currRepeatMode == PlaybackStateCompat.REPEAT_MODE_ONE) {
            //单曲循环
            mPlayback.setCurrentMediaId("");
            handlePlayRequest(true);
        } else if (currRepeatMode == PlaybackStateCompat.REPEAT_MODE_ALL) {
            //列表循环
            if (shouldPlayNext && mMediaQueue.skipQueuePosition(1)) {
                handlePlayRequest(true);
                mMediaQueue.updateMetadata();
            } else {
                handleStopRequest(null);
            }
        }
    }

    /**
     * 播放器播放状态改变回调
     */
    @Override
    public void onPlaybackStatusChanged(int state) {
        updatePlaybackState(false, null);
    }

    /**
     * 播放器发送错误回调
     */
    @Override
    public void onError(String error) {
        updatePlaybackState(false, error);
    }

    /**
     * 设置当前播放 id
     */
    @Override
    public void setCurrentMediaId(String mediaId) {
        mMediaQueue.updateCurrPlayingMedia(mediaId);
    }

    /**
     * MusicManager API 方法的具体实现
     */
    private class MediaSessionCallback extends MediaSessionCompat.Callback {

        @Override
        public void onPrepare() {
            super.onPrepare();
            handlePlayRequest(false);
        }

        @Override
        public void onPrepareFromMediaId(String mediaId, Bundle extras) {
            super.onPrepareFromMediaId(mediaId, extras);
            mMediaQueue.updateCurrPlayingMedia(mediaId);
            handlePlayRequest(false);
        }

        @Override
        public void onPlay() {
            if (mMediaQueue.getCurrentMusic() == null) {
                return;
            }
            handlePlayRequest(true);
        }

        @Override
        public void onSkipToQueueItem(long queueId) {
            mMediaQueue.updateIndexByMediaId(String.valueOf(queueId));
            mMediaQueue.updateMetadata();
        }

        @Override
        public void onSeekTo(long position) {
            mPlayback.seekTo((int) position);
        }

        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            mMediaQueue.updateCurrPlayingMedia(mediaId);
            handlePlayRequest(true);
        }

        @Override
        public void onPause() {
            handlePauseRequest();
        }

        @Override
        public void onStop() {
            handleStopRequest(null);
        }

        @Override
        public void onSkipToNext() {
            if (shouldPlayNext) {
                if (mMediaQueue.skipQueuePosition(1)) {
                    handlePlayRequest(true);
                    mMediaQueue.updateMetadata();
                }
            }
        }

        @Override
        public void onSkipToPrevious() {
            if (shouldPlayPre) {
                if (mMediaQueue.skipQueuePosition(-1)) {
                    handlePlayRequest(true);
                    mMediaQueue.updateMetadata();
                }
            }
        }

        @Override
        public void onCustomAction(@NonNull String action, Bundle extras) {
            //updatePlaybackState(null);
        }

        @Override
        public void onFastForward() {
            super.onFastForward();
            handleFastForward();
        }

        @Override
        public void onRewind() {
            super.onRewind();
            handleRewind();
        }

        @Override
        public void onSetShuffleMode(int shuffleMode) {
            super.onSetShuffleMode(shuffleMode);
            if (shuffleMode == PlaybackStateCompat.SHUFFLE_MODE_NONE) {
                if (mPlayback != null) {
                    mMediaQueue.setNormalMode(mPlayback.getCurrentMediaId());
                }
            } else if (shuffleMode == PlaybackStateCompat.SHUFFLE_MODE_ALL) {
                mMediaQueue.setShuffledMode();
            }
            mServiceCallback.onShuffleModeUpdated(shuffleMode);
        }

        @Override
        public void onSetRepeatMode(int repeatMode) {
            super.onSetRepeatMode(repeatMode);
            currRepeatMode = repeatMode;
            mServiceCallback.onRepeatModeUpdated(repeatMode);
            updatePlaybackState(true, null);  //更新状态
        }

        /**
         * 自定义方法
         */
        @Override
        public void onCommand(String command, Bundle extras, ResultReceiver cb) {
            super.onCommand(command, extras, cb);
            if (command == null) {
                return;
            }
            if (INotification.ACTION_UPDATE_FAVORITE_UI.equals(command)) {
                boolean isFavorite = extras.getBoolean("isFavorite");
                if (notification != null) {
                    notification.updateFavoriteUI(isFavorite);
                }
            }
            if (INotification.ACTION_UPDATE_LYRICS_UI.equals(command)) {
                boolean isChecked = extras.getBoolean("isChecked");
                if (notification != null) {
                    notification.updateLyricsUI(isChecked);
                }
            }
            if (ExoPlayback.ACTION_CHANGE_VOLUME.equals(command)) {
                float audioVolume = extras.getFloat("AudioVolume");
                mPlayback.setVolume(audioVolume);
            }
            if (ExoPlayback.ACTION_DERAILLEUR.equals(command)) {
                boolean refer = extras.getBoolean("refer");
                float multiple = extras.getFloat("multiple");
                handleDerailleur(refer, multiple);
            }
        }
    }


}
