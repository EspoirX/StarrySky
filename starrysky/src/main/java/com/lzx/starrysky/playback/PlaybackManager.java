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
import android.os.Bundle;
import android.os.ResultReceiver;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.lzx.starrysky.notification.factory.INotification;
import com.lzx.starrysky.notification.factory.NotificationFactory;



/**
 * 播放管理类
 */
public class PlaybackManager implements Playback.Callback {

    private static final String TAG = "PlaybackManager";
    private static final String CUSTOM_ACTION_THUMBS_UP = "com.lzx.starrysky.THUMBS_UP";

    private Context mContext;
    private QueueManager mQueueManager;
    private Playback mPlayback;
    private PlaybackServiceCallback mServiceCallback;
    private MediaSessionCallback mMediaSessionCallback;
    private NotificationFactory mNotificationFactory;

    public PlaybackManager(Context context, PlaybackServiceCallback serviceCallback, QueueManager queueManager,
                           Playback playback) {
        mContext = context;

        mServiceCallback = serviceCallback;
        mQueueManager = queueManager;
        mMediaSessionCallback = new MediaSessionCallback();
        mPlayback = playback;
        mPlayback.setCallback(this);
    }

    public void setNotificationFactory(NotificationFactory notificationFactory) {
        mNotificationFactory = notificationFactory;
    }

    public Playback getPlayback() {
        return mPlayback;
    }

    public MediaSessionCompat.Callback getMediaSessionCallback() {
        return mMediaSessionCallback;
    }

    /**
     * 播放
     */
    public void handlePlayRequest() {
        MediaSessionCompat.QueueItem currentMusic = mQueueManager.getCurrentMusic();
        if (currentMusic != null) {
            mServiceCallback.onPlaybackStart();
            mPlayback.play(currentMusic);
        }
    }

    /**
     * 暂停
     */
    public void handlePauseRequest() {
        if (mPlayback.isPlaying()) {
            mPlayback.pause();
            mServiceCallback.onPlaybackStop();
        }
    }

    /**
     * 停止
     */
    public void handleStopRequest(String withError) {
        mPlayback.stop(true);
        mServiceCallback.onPlaybackStop();
        updatePlaybackState(withError);
    }

    /**
     * 快进
     */
    public void handleFastForward() {
        mPlayback.onFastForward();
    }

    /**
     * 倒带
     */
    public void handleRewind() {
        mPlayback.onRewind();
    }

    /**
     * 更新播放状态
     */
    public void updatePlaybackState(String error) {
        long position = PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN;
        if (mPlayback != null && mPlayback.isConnected()) {
            position = mPlayback.getCurrentStreamPosition();
        }
        //构建一个播放状态对象
        PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder()
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
        MediaSessionCompat.QueueItem currentMusic = mQueueManager.getCurrentMusic();
        if (currentMusic != null) {
            stateBuilder.setActiveQueueItemId(currentMusic.getQueueId());
        }
        //把状态回调出去
        mServiceCallback.onPlaybackStateUpdated(stateBuilder.build());
        //如果是播放或者暂停的状态，更新一下通知栏
        if (state == PlaybackStateCompat.STATE_PLAYING || state == PlaybackStateCompat.STATE_PAUSED) {
            mServiceCallback.onNotificationRequired();
        }
    }

    private long getAvailableActions() {
        long actions =
                PlaybackStateCompat.ACTION_PLAY_PAUSE |
                        PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID |
                        PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH |
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT;
        if (mPlayback.isPlaying()) {
            actions |= PlaybackStateCompat.ACTION_PAUSE;
        } else {
            actions |= PlaybackStateCompat.ACTION_PLAY;
        }
        return actions;
    }

    /**
     * 播放器播放完成回调
     */
    @Override
    public void onCompletion() {
        if (mQueueManager.skipQueuePosition(1)) {
            handlePlayRequest();
            mQueueManager.updateMetadata();
        } else {
            handleStopRequest(null);
        }
    }

    /**
     * 播放器播放状态改变回调
     */
    @Override
    public void onPlaybackStatusChanged(int state) {
        updatePlaybackState(null);
    }

    /**
     * 播放器发送错误回调
     */
    @Override
    public void onError(String error) {
        updatePlaybackState(error);
    }

    /**
     * 设置当前播放 id
     */
    @Override
    public void setCurrentMediaId(String mediaId) {
        mQueueManager.setQueueFromMusic(mediaId);
    }

    /**
     * MusicManager API 方法的具体实现
     */
    private class MediaSessionCallback extends MediaSessionCompat.Callback {
        @Override
        public void onPlay() {
            if (mQueueManager.getCurrentMusic() == null) {
                mQueueManager.setRandomQueue();
            }
            handlePlayRequest();
        }

        @Override
        public void onSkipToQueueItem(long queueId) {
            mQueueManager.setCurrentQueueItem(String.valueOf(queueId));
            mQueueManager.updateMetadata();
        }

        @Override
        public void onSeekTo(long position) {
            mPlayback.seekTo((int) position);
        }

        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            mQueueManager.setQueueFromMusic(mediaId);
            handlePlayRequest();
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
            if (mQueueManager.skipQueuePosition(1)) {
                handlePlayRequest();
            } else {
                handleStopRequest("Cannot skip");
            }
            mQueueManager.updateMetadata();
        }

        @Override
        public void onSkipToPrevious() {
            if (mQueueManager.skipQueuePosition(-1)) {
                handlePlayRequest();
            } else {
                handleStopRequest("Cannot skip");
            }
            mQueueManager.updateMetadata();
        }

        @Override
        public void onCustomAction(@NonNull String action, Bundle extras) {
            if (CUSTOM_ACTION_THUMBS_UP.equals(action)) {
                updatePlaybackState(null);
            }
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
                if (mNotificationFactory != null) {
                    mNotificationFactory.updateFavoriteUI(isFavorite);
                }
            }
            if (INotification.ACTION_UPDATE_LYRICS_UI.equals(command)) {
                boolean isChecked = extras.getBoolean("isChecked");
                if (mNotificationFactory != null) {
                    mNotificationFactory.updateLyricsUI(isChecked);
                }
            }
            if (ExoPlayback.ACTION_CHANGE_VOLUME.equals(command)){}{
                float audioVolume = extras.getFloat("AudioVolume");
                mPlayback.setVolume(audioVolume);
            }
        }
    }

    public interface PlaybackServiceCallback {
        void onPlaybackStart();

        void onNotificationRequired();

        void onPlaybackStop();

        void onPlaybackStateUpdated(PlaybackStateCompat newState);
    }
}
