package com.lzx.musiclibrary.control;

import android.os.Bundle;
import android.support.v4.media.session.PlaybackStateCompat;

import com.lzx.musiclibrary.MusicService;
import com.lzx.musiclibrary.aidl.listener.NotifyContract;
import com.lzx.musiclibrary.aidl.model.SongInfo;
import com.lzx.musiclibrary.aidl.source.IOnPlayerEventListener;
import com.lzx.musiclibrary.aidl.source.IOnTimerTaskListener;
import com.lzx.musiclibrary.aidl.source.IPlayControl;
import com.lzx.musiclibrary.bus.Bus;
import com.lzx.musiclibrary.constans.PlayMode;
import com.lzx.musiclibrary.constans.State;
import com.lzx.musiclibrary.helper.QueueHelper;
import com.lzx.musiclibrary.manager.MediaSessionManager;
import com.lzx.musiclibrary.manager.TimerTaskManager;
import com.lzx.musiclibrary.notification.CustomNotification;
import com.lzx.musiclibrary.notification.IMediaNotification;
import com.lzx.musiclibrary.notification.NotificationCreater;
import com.lzx.musiclibrary.notification.SystemNotification;
import com.lzx.musiclibrary.playback.PlaybackManager;
import com.lzx.musiclibrary.playback.player.Playback;
import com.lzx.musiclibrary.queue.PlayQueueManager;
import com.lzx.musiclibrary.utils.SPUtils;

import java.util.List;

import static com.lzx.musiclibrary.constans.Constans.play_back_pitch;
import static com.lzx.musiclibrary.constans.Constans.play_back_speed;

/**
 * 运行在Remote端
 * <p>
 * lzx
 * 2018/2/8
 */

public class BasePlayControl extends IPlayControl.Stub implements PlaybackManager.PlaybackServiceCallback {

    MusicService mMusicService;
    NotifyContract.NotifyStatusChanged mNotifyStatusChanged;
    NotifyContract.NotifyMusicSwitch mNotifyMusicSwitch;
    NotifyContract.NotifyTimerTask mNotifyTimerTask;
    Playback mPlayback;
    boolean isAutoPlayNext;
    NotificationCreater notificationCreater;

    PlayQueueManager mPlayQueueManager;
    PlaybackManager mPlaybackManager;
    MediaSessionManager mMediaSessionManager;
    private TimerTaskManager mTimerTaskManager;
    private IMediaNotification mNotification;

    BasePlayControl() {
        Bus.getInstance().register(this);
    }

    void init() {
        mTimerTaskManager = new TimerTaskManager();
        mPlayQueueManager = new PlayQueueManager(mMusicService.getApplicationContext());
        mPlaybackManager = new PlaybackManager(mPlayback, mPlayQueueManager, isAutoPlayNext);
        mPlaybackManager.setServiceCallback(this);
        mMediaSessionManager = new MediaSessionManager(this.mMusicService.getApplicationContext(), mPlaybackManager);
        mPlaybackManager.updatePlaybackState(null, false);
        updateNotificationCreater(notificationCreater);
    }

    public void unregisterBus() {
        Bus.getInstance().unregister(this);
    }

    /**
     * 设置当前播放音频并通知播放
     */
    private void setCurrentQueueItem(SongInfo info, boolean isJustPlay) {
        if (info != null) {
            mPlayQueueManager.setCurrentQueueItem(info.getSongId(), isJustPlay, QueueHelper.isNeedToSwitchMusic(mPlaybackManager, info));
        }
    }

    @Override
    public void playMusic(List<SongInfo> list, int index, boolean isJustPlay) {
        if (!QueueHelper.isIndexPlayable(index, list)) {
            return;
        }
        mPlayQueueManager.setSongInfos(list, index);
        setCurrentQueueItem(list.get(index), isJustPlay);
    }

    @Override
    public void playMusicByInfo(SongInfo info, boolean isJustPlay) {
        mPlayQueueManager.addSongInfo(info);
        setCurrentQueueItem(info, isJustPlay);
    }

    @Override
    public void playMusicByIndex(int index, boolean isJustPlay) {
        if (mPlayQueueManager.getSongInfos().size() == 0) {
            return;
        }
        if (!QueueHelper.isIndexPlayable(index, mPlayQueueManager.getSongInfos())) {
            return;
        }
        SongInfo playInfo = mPlayQueueManager.getSongInfos().get(index);
        setCurrentQueueItem(playInfo, isJustPlay);
    }

    @Override
    public void pausePlayInMillis(final long time) {
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
                    mNotifyTimerTask.onTimerTick(millisUntilFinished, time);
                }
            });
        }
    }

    @Override
    public int getCurrPlayingIndex() {
        return mPlayQueueManager.getCurrentIndex();
    }

    @Override
    public void pauseMusic() {
        mPlaybackManager.handlePauseRequest();
    }

    @Override
    public void resumeMusic() {
        mPlaybackManager.handlePlayRequest();
    }

    @Override
    public void stopMusic() {
        mPlaybackManager.handleStopRequest(null, false);
    }

    @Override
    public void setPlayList(List<SongInfo> list) {
        mPlayQueueManager.setSongInfos(list);
    }

    @Override
    public void setPlayListWithIndex(List<SongInfo> list, int index) {
        mPlayQueueManager.setSongInfos(list, index);
    }

    @Override
    public List<SongInfo> getPlayList() {
        return mPlayQueueManager.getSongInfos();
    }

    @Override
    public void deleteSongInfoOnPlayList(SongInfo info, boolean isNeedToPlayNext) {
        mPlayQueueManager.deleteSongInfo(info, isNeedToPlayNext);
    }

    @Override
    public int getStatus() {
        return mPlaybackManager.getPlayback().getState();
    }

    @Override
    public int getDuration() {
        return mPlaybackManager.getPlayback().getDuration();
    }

    @Override
    public void playNext() {
        SongInfo nextSongInfo = mPlayQueueManager.getNextMusicInfo();
        setCurrentQueueItem(nextSongInfo, true);
    }

    @Override
    public void playPre() {
        SongInfo preSongInfo = mPlayQueueManager.getPreMusicInfo();
        setCurrentQueueItem(preSongInfo, true);
    }

    @Override
    public boolean hasPre() {
        return mPlayQueueManager.hasPreSong();
    }

    @Override
    public boolean hasNext() {
        return mPlayQueueManager.hasNextSong();
    }

    @Override
    public SongInfo getPreMusic() {
        return mPlayQueueManager.getPreMusicInfo();
    }

    @Override
    public SongInfo getNextMusic() {
        return mPlayQueueManager.getNextMusicInfo();
    }

    @Override
    public SongInfo getCurrPlayingMusic() {
        return mPlayQueueManager.getCurrentSongInfo();
    }

    @Override
    public void setCurrMusic(int index) {
        mPlayQueueManager.setCurrentSong(index);
    }

    @Override
    public void setPlayMode(int mode) {
        PlayMode.getInstance().setCurrPlayMode(mMusicService, mode);
    }

    @Override
    public int getPlayMode() {
        return PlayMode.getInstance().getCurrPlayMode(mMusicService);
    }

    @Override
    public long getProgress() {
        return mPlaybackManager.getCurrentPosition();
    }

    @Override
    public void seekTo(int position) {
        mPlaybackManager.getPlayback().seekTo(position);
    }

    @Override
    public void reset() {
        mPlaybackManager.handleStopRequest(null, true);
        stopNotification();
    }

    @Override
    public void openCacheWhenPlaying(boolean isOpen) {
        mPlayback.openCacheWhenPlaying(isOpen);
    }

    @Override
    public void stopNotification() {
        if (mNotification != null) {
            mNotification.stopNotification();
        }
    }

    @Override
    public void setPlaybackParameters(float speed, float pitch) {
        SPUtils.put(mMusicService.getApplicationContext(), play_back_speed, speed);
        SPUtils.put(mMusicService.getApplicationContext(), play_back_pitch, pitch);
        mPlayback.setPlaybackParameters(speed, pitch);
    }

    @Override
    public long getBufferedPosition() {
        return mPlaybackManager.getBufferedPosition();
    }

    @Override
    public void setVolume(float audioVolume) {
        mPlayback.setVolume(audioVolume);
    }

    @Override
    public void updateNotificationCreater(NotificationCreater creater) {
        if (creater != null) {
            if (creater.isCreateSystemNotification()) {
                mNotification = new SystemNotification(mMusicService, creater, mPlaybackManager);
            } else {
                mNotification = new CustomNotification(mMusicService, creater, mPlaybackManager);
            }
        }
    }

    @Override
    public void updateNotificationFavorite(boolean isFavorite) {
        if (mNotification != null) {
            mNotification.updateFavorite(isFavorite);
        }
    }

    @Override
    public void updateNotificationLyrics(boolean isChecked) {
        if (mNotification != null) {
            mNotification.updateLyrics(isChecked);
        }
    }

    @Override
    public void updateNotificationContentIntent(Bundle bundle, String targetClass) {
        if (mNotification != null) {
            mNotification.updateContentIntent(bundle, targetClass);
        }
    }

    @Override
    public void registerPlayerEventListener(IOnPlayerEventListener listener) {

    }

    @Override
    public void unregisterPlayerEventListener(IOnPlayerEventListener listener) {

    }

    @Override
    public void registerTimerTaskListener(IOnTimerTaskListener listener) {

    }

    @Override
    public void unregisterTimerTaskListener(IOnTimerTaskListener listener) {

    }

    @Override
    public int getAudioSessionId() {
        return mPlaybackManager.getAudioSessionId();
    }

    @Override
    public float getPlaybackSpeed() {
        return mPlayback.getPlaybackSpeed();
    }

    @Override
    public float getPlaybackPitch() {
        return mPlayback.getPlaybackPitch();
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
        mNotifyStatusChanged.notify(mPlayQueueManager.getCurrentSongInfo(), mPlayQueueManager.getCurrentIndex(), State.STATE_ERROR, errorMsg);
    }

    @Override
    public void onPlaybackCompletion(SongInfo songInfo) {
        mNotifyStatusChanged.notify(songInfo, mPlayQueueManager.getCurrentIndex(), State.STATE_IDLE, null);
    }

    @Override
    public void onNotificationRequired() {

    }

    @Override
    public void onPlaybackStateUpdated(int state, PlaybackStateCompat newState) {
        //状态改变
        mNotifyStatusChanged.notify(mPlayQueueManager.getCurrentSongInfo(), mPlayQueueManager.getCurrentIndex(), state, null);
        mMediaSessionManager.setPlaybackState(newState);
        if (mNotification != null) {
            if (state == State.STATE_PLAYING) {
                mNotification.updateViewStateAtStart();
            } else {
                mNotification.updateViewStateAtPause();
            }
        }
    }
}
