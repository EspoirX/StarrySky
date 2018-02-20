package com.lzx.musiclibrary.control;

import android.app.Notification;
import android.app.PendingIntent;
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
import com.lzx.musiclibrary.notification.NotificationCreater;
import com.lzx.musiclibrary.playback.PlaybackManager;
import com.lzx.musiclibrary.playback.player.Playback;
import com.lzx.musiclibrary.utils.LogUtil;

import java.util.List;

/**
 * 运行在Remote端
 *
 * @author lzx
 * @date 2018/2/8
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
    private Playback mPlayback;


    private PlayController(Builder builder) {
        this.mMusicService = builder.mMusicService;
        this.mPlayMode = builder.mPlayMode;
        this.mPlayback = builder.mPlayback;
        this.mNotifyStatusChanged = builder.mNotifyStatusChanged;
        this.mNotifyMusicSwitch = builder.mNotifyMusicSwitch;

        mTimerTaskManager = new TimerTaskManager();
        mQueueManager = new QueueManager(this, mPlayMode);
        mPlaybackManager = new PlaybackManager(mPlayback, mQueueManager, mPlayMode, builder.isAutoPlayNext);
        mPlaybackManager.setServiceCallback(this);
        mMediaSessionManager = new MediaSessionManager(this.mMusicService.getApplicationContext(), mPlaybackManager);
        mPlaybackManager.updatePlaybackState(null);


    }

    public static class Builder {
        private MusicService mMusicService;
        private PlayMode mPlayMode;
        private Playback mPlayback;
        private NotifyContract.NotifyStatusChanged mNotifyStatusChanged;
        private NotifyContract.NotifyMusicSwitch mNotifyMusicSwitch;
        private boolean isAutoPlayNext;
        private boolean isCreateNotification;

        public Builder(MusicService mService) {
            mMusicService = mService;
        }

        Builder setPlayMode(PlayMode playMode) {
            mPlayMode = playMode;
            return this;
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

        Builder setCreateNotification(boolean createNotification) {
            isCreateNotification = createNotification;
            return this;
        }

        public PlayController build() {
            return new PlayController(this);
        }
    }

    void playMusic(List<SongInfo> list, int index, boolean isJustPlay) {
        //先播放再设置列表
        setCurrentQueueItem(list.get(index), isJustPlay);
        mQueueManager.setCurrentQueue(list, index);
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

    void stopMusic() {
        mPlaybackManager.handleStopRequest("");
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

    public   void playNext() {
        mPlaybackManager.playNextOrPre(1);
    }

    public  void playPre() {
        mPlaybackManager.playNextOrPre(-1);
    }

    boolean hasPre() {
        return mPlaybackManager.hasNextOrPre();
    }

    boolean hasNext() {
        return mPlaybackManager.hasNextOrPre();
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

    void seekTo(int position) {
        mPlaybackManager.getPlayback().seekTo(position);
    }

    void pausePlayInMillis(long time) {
        if (time == -1) {
            mTimerTaskManager.cancelCountDownTask();
        } else {
            mTimerTaskManager.starCountDownTask(time, 1000L, new TimerTaskManager.OnCountDownFinishListener() {
                @Override
                public void onFinish() {
                    mPlaybackManager.handlePauseRequest();
                }
            });
        }
    }

    private void setCurrentQueueItem(SongInfo info, boolean isJustPlay) {
        mQueueManager.setCurrentQueueItem(info.getSongId(), isJustPlay,
                QueueHelper.isNeedToSwitchMusic(mQueueManager, info));
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

    }

    void releaseMediaSession() {
        mMediaSessionManager.release();

    }
}
