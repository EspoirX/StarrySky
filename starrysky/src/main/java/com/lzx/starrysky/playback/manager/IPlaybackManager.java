package com.lzx.starrysky.playback.manager;

import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.lzx.starrysky.notification.INotification;
import com.lzx.starrysky.provider.MediaQueueProvider;

public interface IPlaybackManager {

    void setServiceCallback(PlaybackServiceCallback serviceCallback);

    void setMetadataUpdateListener(MediaQueueProvider.MetadataUpdateListener listener);

    MediaSessionCompat.Callback getMediaSessionCallback();

    /**
     * 播放
     */
    void handlePlayRequest(boolean isPlayWhenReady);

    /**
     * 暂停
     */
    void handlePauseRequest();

    /**
     * 停止
     */
    void handleStopRequest(String withError);

    /**
     * 快进
     */
    void handleFastForward();

    /**
     * 倒带
     */
    void handleRewind();

    /**
     * 指定语速 refer 是否已当前速度为基数  multiple 倍率
     */
    void handleDerailleur(boolean refer, float multiple);

    /**
     * 更新播放状态
     */
    void updatePlaybackState(boolean isOnlyUpdateActions, String error);

    /**
     * 是否在播放
     */
    boolean isPlaying();

    void registerNotification(INotification notification);

    interface PlaybackServiceCallback {
        void onPlaybackStart();

        void onNotificationRequired();

        void onPlaybackStop();

        void onPlaybackStateUpdated(PlaybackStateCompat newState, MediaMetadataCompat currMetadata);

        void onShuffleModeUpdated(int shuffleMode);

        void onRepeatModeUpdated(int repeatMode);
    }
}
