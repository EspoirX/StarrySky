package com.lzx.musiclibrary.control;

import android.app.Notification;
import android.app.PendingIntent;
import android.os.RemoteCallbackList;
import android.os.RemoteException;

import com.lzx.musiclibrary.MusicService;
import com.lzx.musiclibrary.aidl.listener.IOnPlayerEventListener;
import com.lzx.musiclibrary.aidl.listener.IPlayControl;
import com.lzx.musiclibrary.aidl.listener.NotifyContract;
import com.lzx.musiclibrary.aidl.model.SongInfo;
import com.lzx.musiclibrary.constans.PlayMode;
import com.lzx.musiclibrary.constans.State;
import com.lzx.musiclibrary.helper.QueueHelper;
import com.lzx.musiclibrary.notification.NotificationCreater;
import com.lzx.musiclibrary.playback.player.ExoPlayback;
import com.lzx.musiclibrary.playback.player.MediaPlayback;
import com.lzx.musiclibrary.playback.player.Playback;
import com.lzx.musiclibrary.utils.LogUtil;

import java.util.List;

/**
 * Binder
 * Created by xian on 2018/1/28.
 */

public class PlayControl extends IPlayControl.Stub {

    private MusicService mService;
    private PlayMode mPlayMode;
    private PlayController mController;
    private Playback playback;


    private RemoteCallbackList<IOnPlayerEventListener> mRemoteCallbackList;

    private NotifyContract.NotifyStatusChanged mNotifyStatusChanged;
    private NotifyContract.NotifyMusicSwitch mNotifyMusicSwitch;

    private boolean hasInitNotification = false;
    private boolean isCreateNotification = false;
    private NotificationCreater mNotificationCreater;
    private Notification mNotification;

    private PlayControl(Builder builder) {
        mService = builder.mMusicService;
        isCreateNotification = builder.isCreateNotification;
        mNotifyStatusChanged = new NotifyStatusChange();
        mNotifyMusicSwitch = new NotifyMusicSwitch();
        mRemoteCallbackList = new RemoteCallbackList<>();

        this.isCreateNotification = builder.isCreateNotification;
        if (this.isCreateNotification) {
            mNotificationCreater = NotificationCreater.getInstanse(mService.getApplicationContext());
        }

        mPlayMode = new PlayMode();
        playback = builder.isUseMediaPlayer ? new MediaPlayback(mService.getApplicationContext())
                : new ExoPlayback(mService.getApplicationContext());
        mController = new PlayController.Builder(mService)
                .setAutoPlayNext(builder.isAutoPlayNext)
                .setNotifyMusicSwitch(mNotifyMusicSwitch)
                .setNotifyStatusChanged(mNotifyStatusChanged)
                .setPlayback(playback)
                .setPlayMode(mPlayMode)
                .build();
    }

    public PlayController getController() {
        return mController;
    }

    public static class Builder {
        private MusicService mMusicService;
        private boolean isUseMediaPlayer = false;
        private boolean isAutoPlayNext = true;
        private boolean isCreateNotification = true;

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

        public Builder setCreateNotification(boolean createNotification) {
            isCreateNotification = createNotification;
            return this;
        }

        public PlayControl build() {
            return new PlayControl(this);
        }
    }

    private class NotifyStatusChange implements NotifyContract.NotifyStatusChanged {

        @Override
        public void notify(SongInfo info, int index, int status, String errorMsg) {
            synchronized (NotifyStatusChange.class) {

                if (isCreateNotification && mNotification != null) {
                    if (status == State.STATE_PLAYING) {
                        mNotificationCreater.updateViewStateAtStart(mNotification);
                    } else if (status == State.STATE_PAUSED) {
                        mNotificationCreater.updateViewStateAtPause(mNotification);
                    }
                }

                final int N = mRemoteCallbackList.beginBroadcast();
                for (int i = 0; i < N; i++) {

                    IOnPlayerEventListener listener = mRemoteCallbackList.getBroadcastItem(i);
                    if (listener != null) {
                        try {
                            switch (status) {
                                case State.STATE_IDLE:
                                    listener.onPlayCompletion();
                                    break;
                                case State.STATE_BUFFERING:
                                    listener.onBuffering(true);
                                    break;
                                case State.STATE_PLAYING:
                                    listener.onBuffering(false);
                                    listener.onPlayerStart();
                                    break;
                                case State.STATE_PAUSED:
                                    listener.onPlayerPause();
                                    break;
                                case State.STATE_ENDED:
                                    listener.onPlayCompletion();
                                    break;
                                case State.STATE_ERROR:
                                    listener.onError("");
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
            synchronized (NotifyMusicSwitch.class) {

                if (isCreateNotification && mNotification != null) {
                    mNotificationCreater.updateModelDetail(info, mNotification);
                }

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

    public Playback getPlayback() {
        return playback;
    }

    public void releaseMediaSession() {
        mController.releaseMediaSession();
    }

    @Override
    public void playMusic(List<SongInfo> list, int index, boolean isJustPlay) throws RemoteException {
        if (!QueueHelper.isIndexPlayable(index, list)) {
            return;
        }
        mController.playMusic(list, index, isJustPlay);
    }

    @Override
    public void playMusicByInfo(SongInfo info, boolean isJustPlay) throws RemoteException {
        if (info == null) {
            return;
        }
        mController.playMusicByInfo(info, isJustPlay);
    }

    @Override
    public void playMusicByIndex(int index, boolean isJustPlay) throws RemoteException {
        mController.playMusicByIndex(index, isJustPlay);
    }

    @Override
    public void pausePlayInMillis(long time) throws RemoteException {
        mController.pausePlayInMillis(time);
    }

    @Override
    public int getCurrPlayingIndex() throws RemoteException {
        return mController.getCurrPlayingIndex();
    }

    @Override
    public void pauseMusic() throws RemoteException {
        mController.pauseMusic();
    }

    @Override
    public void resumeMusic() throws RemoteException {
        mController.resumeMusic();
    }

    @Override
    public void stopMusic() throws RemoteException {
        mController.stopMusic();
    }

    @Override
    public void setPlayList(List<SongInfo> list) throws RemoteException {
        mController.setPlayList(list);
    }

    @Override
    public void setPlayListWithIndex(List<SongInfo> list, int index) throws RemoteException {
        mController.setPlayListWithIndex(list, index);
    }

    @Override
    public List<SongInfo> getPlayList() throws RemoteException {
        return mController.getPlayList();
    }

    @Override
    public void deleteSongInfoOnPlayList(SongInfo info, boolean isNeedToPlayNext) throws RemoteException {
        mController.deleteMusicInfoOnPlayList(info, isNeedToPlayNext);
    }

    @Override
    public int getStatus() throws RemoteException {
        return mController.getState();
    }

    @Override
    public void playNext() throws RemoteException {
        mController.playNext();
    }

    @Override
    public void playPre() throws RemoteException {
        mController.playPre();
    }

    @Override
    public boolean hasPre() throws RemoteException {
        return mController.hasPre();
    }

    @Override
    public boolean hasNext() throws RemoteException {
        return mController.hasNext();
    }

    @Override
    public SongInfo getPreMusic() throws RemoteException {
        return mController.getPreMusic();
    }

    @Override
    public SongInfo getNextMusic() throws RemoteException {
        return mController.getNextMusic();
    }

    @Override
    public SongInfo getCurrPlayingMusic() throws RemoteException {
        return mController.getCurrPlayingMusic();
    }

    @Override
    public void setCurrMusic(int index) throws RemoteException {
        mController.setCurrMusic(index);
    }

    @Override
    public void setPlayMode(int mode, boolean isSaveLocal) throws RemoteException {
        if (isSaveLocal) {
            mPlayMode.setCurrPlayMode(mService, mode);
        } else {
            mPlayMode.setCurrPlayMode(mode);
        }
    }

    @Override
    public int getPlayMode(boolean isGetLocal) throws RemoteException {
        return mPlayMode.getCurrPlayMode();
    }

    @Override
    public long getProgress() throws RemoteException {
        return mController.getProgress();
    }

    @Override
    public void seekTo(int position) throws RemoteException {
        mController.seekTo(position);
    }

    @Override
    public void reset() throws RemoteException {

    }

    @Override
    public void setStartOrPauseIntent(PendingIntent startOrPauseIntent) throws RemoteException {
        if (isCreateNotification && startOrPauseIntent != null) {
            mNotificationCreater.setStartOrPausePendingIntent(startOrPauseIntent);
        }
    }

    @Override
    public void setNextIntent(PendingIntent nextIntent) throws RemoteException {
        if (isCreateNotification && nextIntent != null) {
            mNotificationCreater.setNextPendingIntent(nextIntent);
        }
    }

    @Override
    public void setPreIntent(PendingIntent preIntent) throws RemoteException {
        if (isCreateNotification && preIntent != null) {
            mNotificationCreater.setPrePendingIntent(preIntent);
        }
    }

    @Override
    public void setCloseIntent(PendingIntent closeIntent) throws RemoteException {
        if (isCreateNotification && closeIntent != null) {
            mNotificationCreater.setClosePendingIntent(closeIntent);
        }
    }

    @Override
    public void setNotification(Notification notification) throws RemoteException {
        if (!hasInitNotification) {
            hasInitNotification = true;
            mNotification = notification;
            try {
                if (mNotification != null) {
                    mService.startForeground(NotificationCreater.NOTIFICATION_ID, mNotification);
                }
            } catch (Exception e) {
                e.printStackTrace();
                LogUtil.i(e.getMessage());
            }
        }
    }

    @Override
    public void registerPlayerEventListener(IOnPlayerEventListener listener) throws RemoteException {
        mRemoteCallbackList.register(listener);
    }

    @Override
    public void unregisterPlayerEventListener(IOnPlayerEventListener listener) throws RemoteException {
        mRemoteCallbackList.unregister(listener);
    }


}
