package com.lzx.musiclibrary.control;

import android.os.Bundle;
import android.os.RemoteCallbackList;
import android.os.RemoteException;

import com.lzx.musiclibrary.MusicService;
import com.lzx.musiclibrary.aidl.listener.NotifyContract;
import com.lzx.musiclibrary.aidl.model.SongInfo;
import com.lzx.musiclibrary.aidl.source.IOnPlayerEventListener;
import com.lzx.musiclibrary.aidl.source.IOnTimerTaskListener;
import com.lzx.musiclibrary.aidl.source.IPlayControl;
import com.lzx.musiclibrary.cache.CacheConfig;
import com.lzx.musiclibrary.constans.State;
import com.lzx.musiclibrary.helper.QueueHelper;
import com.lzx.musiclibrary.notification.NotificationCreater;
import com.lzx.musiclibrary.playback.player.ExoPlayback;
import com.lzx.musiclibrary.playback.player.MediaPlayback;
import com.lzx.musiclibrary.playback.player.Playback;

import java.util.List;

/**
 * Binder
 * Created by xian on 2018/1/28.
 */

public class PlayControl extends IPlayControl.Stub {

    private MusicService mService;

    private PlayControlImpl mController;
    private Playback playback;

    private RemoteCallbackList<IOnPlayerEventListener> mRemoteCallbackList;
    private RemoteCallbackList<IOnTimerTaskListener> mOnTimerTaskListenerList;

    private NotifyContract.NotifyStatusChanged mNotifyStatusChanged;
    private NotifyContract.NotifyMusicSwitch mNotifyMusicSwitch;
    private NotifyContract.NotifyTimerTask mNotifyTimerTask;
    private boolean isAsyncLoading = false;

    private PlayControl(Builder builder) {
        mService = builder.mMusicService;

        mNotifyStatusChanged = new NotifyStatusChange();
        mNotifyMusicSwitch = new NotifyMusicSwitch();
        mNotifyTimerTask = new NotifyTimerTask();
        mRemoteCallbackList = new RemoteCallbackList<>();
        mOnTimerTaskListenerList = new RemoteCallbackList<>();


        playback = builder.isUseMediaPlayer
                ? new MediaPlayback(mService.getApplicationContext(), builder.cacheConfig, builder.isGiveUpAudioFocusManager)
                : new ExoPlayback(mService.getApplicationContext(), builder.cacheConfig, builder.isGiveUpAudioFocusManager);

        mController = new PlayControlImpl.Builder(mService)
                .setAutoPlayNext(builder.isAutoPlayNext)
                .setNotifyMusicSwitch(mNotifyMusicSwitch)
                .setNotifyStatusChanged(mNotifyStatusChanged)
                .setNotifyTimerTask(mNotifyTimerTask)
                .setPlayback(playback)
                .setNotificationCreater(builder.notificationCreater)
                .build();
    }

    public PlayControlImpl getController() {
        return mController;
    }

    public static class Builder {
        private MusicService mMusicService;
        private boolean isUseMediaPlayer = false;
        private boolean isAutoPlayNext = true;
        private boolean isGiveUpAudioFocusManager = false;
        private NotificationCreater notificationCreater;
        private CacheConfig cacheConfig;

        public Builder(MusicService mService) {
            mMusicService = mService;
        }

        public Builder setUseMediaPlayer(boolean useMediaPlayer) {
            isUseMediaPlayer = useMediaPlayer;
            return this;
        }

        public Builder setAutoPlayNext(boolean autoPlayNext) {
            isAutoPlayNext = autoPlayNext;
            return this;
        }

        public Builder setNotificationCreater(NotificationCreater notificationCreater) {
            this.notificationCreater = notificationCreater;
            return this;
        }

        public Builder setCacheConfig(CacheConfig cacheConfig) {
            this.cacheConfig = cacheConfig;
            return this;
        }

        public Builder setGiveUpAudioFocusManager(boolean giveUpAudioFocusManager) {
            isGiveUpAudioFocusManager = giveUpAudioFocusManager;
            return this;
        }

        public PlayControl build() {
            return new PlayControl(this);
        }
    }

    private class NotifyStatusChange implements NotifyContract.NotifyStatusChanged {

        @Override
        public void notify(SongInfo info, int index, int status, String errorMsg) {
            synchronized (PlayControl.class) {
                final int N = mRemoteCallbackList.beginBroadcast();
                for (int i = 0; i < N; i++) {
                    IOnPlayerEventListener listener = mRemoteCallbackList.getBroadcastItem(i);
                    if (listener != null) {
                        try {
                            switch (status) {
                                case State.STATE_IDLE:
                                    listener.onPlayCompletion();
                                    break;
                                case State.STATE_ASYNC_LOADING:
                                    isAsyncLoading = true;
                                    listener.onAsyncLoading(false);
                                    break;
                                case State.STATE_PLAYING:
                                    isAsyncLoading = false;
                                    listener.onAsyncLoading(true);
                                    listener.onPlayerStart();
                                    break;
                                case State.STATE_PAUSED:
                                    if (isAsyncLoading) {
                                        listener.onAsyncLoading(true);
                                        isAsyncLoading = false;
                                    }
                                    listener.onPlayerPause();
                                    break;
                                case State.STATE_ENDED:
                                    listener.onPlayCompletion();
                                    break;
                                case State.STATE_STOP:
                                    listener.onPlayerStop();
                                    break;
                                case State.STATE_ERROR:
                                    listener.onError(errorMsg);
                                    break;
                            }
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                }
                mRemoteCallbackList.finishBroadcast();
            }
        }
    }

    private class NotifyMusicSwitch implements NotifyContract.NotifyMusicSwitch {

        @Override
        public void notify(SongInfo info) {
            synchronized (PlayControl.class) {
                final int N = mRemoteCallbackList.beginBroadcast();
                for (int i = 0; i < N; i++) {
                    IOnPlayerEventListener listener = mRemoteCallbackList.getBroadcastItem(i);
                    if (listener != null) {
                        try {
                            listener.onMusicSwitch(info);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                }
                mRemoteCallbackList.finishBroadcast();
            }
        }
    }

    private class NotifyTimerTask implements NotifyContract.NotifyTimerTask {

        @Override
        public void notifyTimerTasFinish() {
            synchronized (PlayControl.class) {
                final int N = mOnTimerTaskListenerList.beginBroadcast();
                for (int i = 0; i < N; i++) {
                    IOnTimerTaskListener listener = mOnTimerTaskListenerList.getBroadcastItem(i);
                    if (listener != null) {
                        try {
                            listener.onTimerFinish();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                }
                mOnTimerTaskListenerList.finishBroadcast();
            }
        }

        @Override
        public void onTimerTick(long millisUntilFinished, long totalTime) {
            synchronized (PlayControl.class) {
                final int N = mOnTimerTaskListenerList.beginBroadcast();
                for (int i = 0; i < N; i++) {
                    IOnTimerTaskListener listener = mOnTimerTaskListenerList.getBroadcastItem(i);
                    if (listener != null) {
                        try {
                            listener.onTimerTick(millisUntilFinished, totalTime);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                }
                mOnTimerTaskListenerList.finishBroadcast();
            }
        }
    }

    public Playback getPlayback() {
        return playback;
    }

    public void releaseMediaSession() {
        mController.releaseMediaSession();
    }

    @Override
    public void playMusic(List<SongInfo> list, int index, boolean isJustPlay) {
        if (!QueueHelper.isIndexPlayable(index, list)) {
            return;
        }
        mController.playMusic(list, index, isJustPlay);
    }

    @Override
    public void playMusicByInfo(SongInfo info, boolean isJustPlay) {
        if (info == null) {
            return;
        }
        mController.playMusicByInfo(info, isJustPlay);
    }

    @Override
    public void playMusicByIndex(int index, boolean isJustPlay) {
        mController.playMusicByIndex(index, isJustPlay);
    }

    @Override
    public void pausePlayInMillis(long time) {
        mController.pausePlayInMillis(time);
    }

    @Override
    public int getCurrPlayingIndex() {
        return mController.getCurrPlayingIndex();
    }

    @Override
    public void pauseMusic() {
        mController.pauseMusic();
    }

    @Override
    public void resumeMusic() {
        mController.resumeMusic();
    }

    @Override
    public void stopMusic() {
        mController.stopMusic();
    }

    @Override
    public void setPlayList(List<SongInfo> list) {
        mController.setPlayList(list);
    }

    @Override
    public void setPlayListWithIndex(List<SongInfo> list, int index) {
        mController.setPlayListWithIndex(list, index);
    }

    @Override
    public List<SongInfo> getPlayList() {
        return mController.getPlayList();
    }

    @Override
    public void deleteSongInfoOnPlayList(SongInfo info, boolean isNeedToPlayNext) {
        mController.deleteMusicInfoOnPlayList(info, isNeedToPlayNext);
    }

    @Override
    public int getStatus() {
        return mController.getState();
    }

    @Override
    public int getDuration() {
        return mController.getDuration();
    }

    @Override
    public void playNext() {
        mController.playNext();
    }

    @Override
    public void playPre() {
        mController.playPre();
    }

    @Override
    public boolean hasPre() {
        return mController.hasPre();
    }

    @Override
    public boolean hasNext() {
        return mController.hasNext();
    }

    @Override
    public SongInfo getPreMusic() {
        return mController.getPreMusic();
    }

    @Override
    public SongInfo getNextMusic() {
        return mController.getNextMusic();
    }

    @Override
    public SongInfo getCurrPlayingMusic() {
        return mController.getCurrPlayingMusic();
    }

    @Override
    public void setCurrMusic(int index) {
        mController.setCurrMusic(index);
    }

    @Override
    public void setPlayMode(int mode) {
        mController.setPlayMode(mode);
    }

    @Override
    public int getPlayMode() {
        return mController.getPlayMode();
    }

    @Override
    public long getProgress() {
        return mController.getProgress();
    }

    @Override
    public void seekTo(int position) {
        mController.seekTo(position);
    }

    @Override
    public void reset() {
        mController.stopMusic();
        mController.stopNotification();
    }

    @Override
    public void openCacheWhenPlaying(boolean isOpen) {
        mController.openCacheWhenPlaying(isOpen);
    }

    @Override
    public void stopNotification() {
        mController.stopNotification();
    }

    @Override
    public void setPlaybackParameters(float speed, float pitch) {
        mController.setPlaybackParameters(speed, pitch);
    }

    @Override
    public long getBufferedPosition() {
        return mController.getBufferedPosition();
    }

    @Override
    public void setVolume(float audioVolume) {
        mController.setVolume(audioVolume);
    }

    @Override
    public void updateNotificationCreater(NotificationCreater creater) {
        mController.updateNotificationCreater(creater);
    }

    @Override
    public void updateNotificationFavorite(boolean isFavorite) {
        mController.updateFavorite(isFavorite);
    }

    @Override
    public void updateNotificationLyrics(boolean isChecked) {
        mController.updateLyrics(isChecked);
    }

    @Override
    public void updateNotificationContentIntent(Bundle bundle, String targetClass) {
        mController.updateContentIntent(bundle, targetClass);
    }

    @Override
    public void registerPlayerEventListener(IOnPlayerEventListener listener) {
        mRemoteCallbackList.register(listener);
    }

    @Override
    public void unregisterPlayerEventListener(IOnPlayerEventListener listener) {
        mRemoteCallbackList.unregister(listener);
    }

    @Override
    public void registerTimerTaskListener(IOnTimerTaskListener listener) {
        mOnTimerTaskListenerList.register(listener);
    }

    @Override
    public void unregisterTimerTaskListener(IOnTimerTaskListener listener) {
        mOnTimerTaskListenerList.unregister(listener);
    }

    @Override
    public int getAudioSessionId() {
        return mController.getAudioSessionId();
    }

    @Override
    public float getPlaybackSpeed() {
        return mController.getPlaybackSpeed();
    }

    @Override
    public float getPlaybackPitch() {
        return mController.getPlaybackPitch();
    }
}
