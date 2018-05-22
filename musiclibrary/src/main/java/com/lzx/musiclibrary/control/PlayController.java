package com.lzx.musiclibrary.control;

import android.os.Bundle;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.lzx.musiclibrary.MusicService;
import com.lzx.musiclibrary.aidl.listener.NotifyContract;
import com.lzx.musiclibrary.aidl.model.SongInfo;
import com.lzx.musiclibrary.constans.PlayMode;
import com.lzx.musiclibrary.constans.State;
import com.lzx.musiclibrary.helper.QueueHelper;
import com.lzx.musiclibrary.manager.MediaSessionManager;
import com.lzx.musiclibrary.manager.QueueManager;
import com.lzx.musiclibrary.manager.TimerTaskManager;
import com.lzx.musiclibrary.notification.CustomNotification;
import com.lzx.musiclibrary.notification.IMediaNotification;
import com.lzx.musiclibrary.notification.NotificationCreater;
import com.lzx.musiclibrary.notification.SystemNotification;
import com.lzx.musiclibrary.playback.PlaybackManager;
import com.lzx.musiclibrary.playback.player.Playback;
import com.lzx.musiclibrary.utils.SPUtils;

import java.util.List;

/**
 * 运行在Remote端
 * <p>
 * lzx
 * 2018/2/8
 */

public class PlayController implements QueueManager.MetadataUpdateListener, PlaybackManager.PlaybackServiceCallback {

    private MusicService mMusicService;
    private QueueManager mQueueManager;
    private PlaybackManager mPlaybackManager;
    private MediaSessionManager mMediaSessionManager;
    private TimerTaskManager mTimerTaskManager;
    private PlayMode mPlayMode;
    private NotifyContract.NotifyStatusChanged mNotifyStatusChanged;
    private NotifyContract.NotifyMusicSwitch mNotifyMusicSwitch;
    private NotifyContract.NotifyTimerTask mNotifyTimerTask;
    private Playback mPlayback;

    private IMediaNotification mNotification;

    private PlayController(Builder builder) {
        this.mMusicService = builder.mMusicService;
        this.mPlayback = builder.mPlayback;
        this.mNotifyStatusChanged = builder.mNotifyStatusChanged;
        this.mNotifyMusicSwitch = builder.mNotifyMusicSwitch;
        this.mNotifyTimerTask = builder.notifyTimerTask;

        mPlayMode = new PlayMode();
        mTimerTaskManager = new TimerTaskManager();
        mQueueManager = new QueueManager(mMusicService.getApplicationContext(), this, mPlayMode);
        mPlaybackManager = new PlaybackManager(mPlayback, mQueueManager, mPlayMode, builder.isAutoPlayNext);
        mPlaybackManager.setServiceCallback(this);
        mMediaSessionManager = new MediaSessionManager(this.mMusicService.getApplicationContext(), mPlaybackManager);
        mPlaybackManager.updatePlaybackState(null);

        updateNotificationCreater(builder.notificationCreater);
    }

    void updateNotificationCreater(NotificationCreater creater) {
        if (creater != null) {
            if (creater.isCreateSystemNotification()) {
                mNotification = new SystemNotification(mMusicService, creater, mPlaybackManager);
            } else {
                mNotification = new CustomNotification(mMusicService, creater, mPlaybackManager);
            }
        }
    }

    public static class Builder {
        private MusicService mMusicService;
        private Playback mPlayback;
        private NotifyContract.NotifyStatusChanged mNotifyStatusChanged;
        private NotifyContract.NotifyMusicSwitch mNotifyMusicSwitch;
        private NotifyContract.NotifyTimerTask notifyTimerTask;
        private boolean isAutoPlayNext;
        private NotificationCreater notificationCreater;

        public Builder(MusicService mService) {
            mMusicService = mService;
        }


        Builder setPlayback(Playback playback) {
            mPlayback = playback;
            return this;
        }

        Builder setNotifyStatusChanged(NotifyContract.NotifyStatusChanged notifyStatusChanged) {
            mNotifyStatusChanged = notifyStatusChanged;
            return this;
        }

        Builder setNotifyMusicSwitch(NotifyContract.NotifyMusicSwitch notifyMusicSwitch) {
            mNotifyMusicSwitch = notifyMusicSwitch;
            return this;
        }

        Builder setAutoPlayNext(boolean autoPlayNext) {
            isAutoPlayNext = autoPlayNext;
            return this;
        }

        Builder setNotificationCreater(NotificationCreater notificationCreater) {
            this.notificationCreater = notificationCreater;
            return this;
        }

        Builder setNotifyTimerTask(NotifyContract.NotifyTimerTask notifyTimerTask) {
            this.notifyTimerTask = notifyTimerTask;
            return this;
        }

        public PlayController build() {
            return new PlayController(this);
        }
    }

    public void setPlayMode(int mode) {
        mPlayMode.setCurrPlayMode(mMusicService, mode);
        mQueueManager.updatePlayModel(mPlayMode);
    }

    public int getPlayMode() {
        return mPlayMode.getCurrPlayMode(mMusicService);
    }

    void playMusic(List<SongInfo> list, int index, boolean isJustPlay) {
        mQueueManager.setCurrentQueue(list, index);
        setCurrentQueueItem(list.get(index), isJustPlay);
    }

    void playMusicByInfo(SongInfo info, boolean isJustPlay) {
        mQueueManager.addQueueItem(info);
        setCurrentQueueItem(info, isJustPlay);
    }

    void playMusicByIndex(int index, boolean isJustPlay) {
        if (mQueueManager.getPlayingQueue().size() == 0) {
            return;
        }
        if (!QueueHelper.isIndexPlayable(index, mQueueManager.getPlayingQueue())) {
            return;
        }
        SongInfo playInfo = mQueueManager.getPlayingQueue().get(index);
        setCurrentQueueItem(playInfo, isJustPlay);
    }

    int getCurrPlayingIndex() {
        return mQueueManager.getCurrentIndex();
    }

    public void pauseMusic() {
        mPlaybackManager.handlePauseRequest();
    }

    public void resumeMusic() {
        mPlaybackManager.handlePlayRequest();
    }

    public void stopMusic() {
        mPlaybackManager.handleStopRequest(null);
    }

    void setPlayList(List<SongInfo> list) {
        mQueueManager.setCurrentQueue(list);
    }

    void setPlayListWithIndex(List<SongInfo> list, int index) {
        mQueueManager.setCurrentQueue(list, index);
    }

    List<SongInfo> getPlayList() {
        return mQueueManager.getPlayingQueue();
    }

    void deleteMusicInfoOnPlayList(SongInfo info, boolean isNeedToPlayNext) {
        mQueueManager.deleteQueueItem(info, isNeedToPlayNext);
    }

    public int getState() {
        return mPlaybackManager.getPlayback().getState();
    }

    public int getDuration() {
        return mPlaybackManager.getPlayback().getDuration();
    }

    public void playNext() {
        mPlaybackManager.playNextOrPre(1);
    }

    public void playPre() {
        mPlaybackManager.playNextOrPre(-1);
    }

    boolean hasPre() {
        return mPlaybackManager.hasPreSong();
    }

    boolean hasNext() {
        return mPlaybackManager.hasNextSong();
    }

    SongInfo getPreMusic() {
        return mQueueManager.getPreMusicInfo();
    }

    SongInfo getNextMusic() {
        return mQueueManager.getNextMusicInfo();
    }

    SongInfo getCurrPlayingMusic() {
        return mQueueManager.getCurrentMusic();
    }

    void setCurrMusic(int index) {
        mQueueManager.setCurrentMusic(index);
    }

    long getProgress() {
        return mPlaybackManager.getCurrentPosition();
    }

    public long getBufferedPosition() {
        return mPlaybackManager.getBufferedPosition();
    }

    void seekTo(int position) {
        mPlaybackManager.getPlayback().seekTo(position);
    }

    public int getAudioSessionId() {
        return mPlaybackManager.getAudioSessionId();
    }

    void pausePlayInMillis(long time) {
        mTimerTaskManager.cancelCountDownTask();
        if (time != -1) {
            mTimerTaskManager.starCountDownTask(time, new TimerTaskManager.OnCountDownFinishListener() {
                @Override
                public void onFinish() {
                    if (mPlaybackManager.getPlayback().getState() == State.STATE_PLAYING) {
                        mPlaybackManager.handlePauseRequest();
                        mNotifyTimerTask.notifyTimerTasFinish();
                    }
                }

                @Override
                public void onTick(long millisUntilFinished) {

                }
            });
        }
    }

    private void setCurrentQueueItem(SongInfo info, boolean isJustPlay) {
        mQueueManager.setCurrentQueueItem(info.getSongId(), isJustPlay,
                QueueHelper.isNeedToSwitchMusic(mPlaybackManager, info));
    }

    public void openCacheWhenPlaying(boolean isOpen) {
        mPlayback.openCacheWhenPlaying(isOpen);
    }

    void setPlaybackParameters(float speed, float pitch) {
        mPlayback.setPlaybackParameters(speed, pitch);
    }

    public void setVolume(float audioVolume) {
        mPlayback.setVolume(audioVolume);
    }

    @Override
    public void onMetadataChanged(SongInfo songInfo) {
        mMediaSessionManager.updateMetaData(QueueHelper.fetchInfoWithMediaMetadata(songInfo));
    }

    @Override
    public void onMetadataRetrieveError() {
        mPlaybackManager.updatePlaybackState("Unable to retrieve metadata.");
    }

    @Override
    public void onCurrentQueueIndexUpdated(int queueIndex, boolean isJustPlay, boolean isSwitchMusic) {
        //播放
        mPlaybackManager.handlePlayPauseRequest(isJustPlay, isSwitchMusic);
    }

    @Override
    public void onQueueUpdated(List<MediaSessionCompat.QueueItem> newQueue, List<SongInfo> playingQueue) {
        mMediaSessionManager.setQueue(newQueue);
    }

    @Override
    public void onPlaybackSwitch(SongInfo info) {
        mNotifyMusicSwitch.notify(info);
        if (mNotification != null) {
            mNotification.startNotification(info);
        }
    }

    @Override
    public void onPlaybackError(String errorMsg) {
        mNotifyStatusChanged.notify(mQueueManager.getCurrentMusic(), mQueueManager.getCurrentIndex(), State.STATE_ERROR, errorMsg);
    }

    @Override
    public void onPlaybackCompletion() {
        mNotifyStatusChanged.notify(mQueueManager.getCurrentMusic(), mQueueManager.getCurrentIndex(), State.STATE_ENDED, null);
    }

    @Override
    public void onNotificationRequired() {

    }

    @Override
    public void onPlaybackStateUpdated(int state, PlaybackStateCompat newState) {
        //状态改变
        mNotifyStatusChanged.notify(mQueueManager.getCurrentMusic(), mQueueManager.getCurrentIndex(), state, null);
        mMediaSessionManager.setPlaybackState(newState);
        if (mNotification != null) {
            if (state == State.STATE_PLAYING) {
                mNotification.updateViewStateAtStart();
            } else if (state == State.STATE_PAUSED) {
                mNotification.updateViewStateAtPause();
            }
        }
    }

    void updateFavorite(boolean isFavorite) {
        if (mNotification != null) {
            mNotification.updateFavorite(isFavorite);
        }
    }

    void updateLyrics(boolean isChecked) {
        if (mNotification != null) {
            mNotification.updateLyrics(isChecked);
        }
    }

    void updateContentIntent(Bundle bundle, String targetClass) {
        if (mNotification != null) {
            mNotification.updateContentIntent(bundle, targetClass);
        }
    }

    public void releaseMediaSession() {
        mMediaSessionManager.release();
    }

    public void stopNotification() {
        if (mNotification != null) {
            mNotification.stopNotification();
        }
    }
}
