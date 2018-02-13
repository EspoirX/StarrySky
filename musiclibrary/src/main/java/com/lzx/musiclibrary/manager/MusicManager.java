package com.lzx.musiclibrary.manager;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;

import com.lzx.musiclibrary.MusicService;
import com.lzx.musiclibrary.aidl.listener.IOnPlayerEventListener;
import com.lzx.musiclibrary.aidl.listener.IPlayControl;
import com.lzx.musiclibrary.aidl.listener.OnPlayerEventListener;
import com.lzx.musiclibrary.aidl.model.SongInfo;
import com.lzx.musiclibrary.constans.State;
import com.lzx.musiclibrary.playback.PlayStateObservable;
import com.lzx.musiclibrary.utils.LogUtil;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Observer;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * @author lzx
 * @date 2018/1/22
 */

public class MusicManager implements IPlayControl {

    public static final int MSG_MUSIC_CHANGE = 0;
    public static final int MSG_PLAYER_START = 1;
    public static final int MSG_PLAYER_PAUSE = 2;
    public static final int MSG_PLAY_COMPLETION = 3;
    public static final int MSG_PLAYER_ERROR = 4;
    public static final int MSG_BUFFERING = 5;

    private Context mContext;
    private boolean isUseMediaPlayer;
    private boolean isAutoPlayNext = true;
    private Notification mNotification;
    private IPlayControl control;
    private ClientHandler mClientHandler;
    private PlayStateObservable mStateObservable;
    private CopyOnWriteArrayList<OnPlayerEventListener> mPlayerEventListeners = new CopyOnWriteArrayList<>();

    public static MusicManager get() {
        return SingletonHolder.sInstance;
    }

    private static class SingletonHolder {
        @SuppressLint("StaticFieldLeak")
        private static final MusicManager sInstance = new MusicManager();
    }

    MusicManager() {
        mClientHandler = new ClientHandler(this);
        mStateObservable = new PlayStateObservable();
    }

    public MusicManager setContext(Context context) {
        mContext = context;
        return this;
    }

    public MusicManager setUseMediaPlayer(boolean isUseMediaPlayer) {
        this.isUseMediaPlayer = isUseMediaPlayer;
        return this;
    }

    public MusicManager setAutoPlayNext(boolean autoPlayNext) {
        isAutoPlayNext = autoPlayNext;
        return this;
    }

    public MusicManager setNotification(Notification notification) {
        mNotification = notification;
        return this;
    }

    public void bindService() {
        Intent intent = new Intent(mContext, MusicService.class);
        intent.putExtra("isUseMediaPlayer", isUseMediaPlayer);
        intent.putExtra("isAutoPlayNext", isAutoPlayNext);
        intent.putExtra("notification", mNotification);
        mContext.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            control = IPlayControl.Stub.asInterface(iBinder);
            try {
                control.registerPlayerEventListener(mOnPlayerEventListener);
                LogUtil.i("--onServiceConnected--");
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            LogUtil.i("--onServiceDisconnected--");
        }
    };

    public void unbindService() {
        try {
            if (control != null && control.asBinder().isBinderAlive()) {
                control.unregisterPlayerEventListener(mOnPlayerEventListener);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        mContext.unbindService(mServiceConnection);
    }

    public void addStateObservable(Observer o) {
        if (mStateObservable != null) {
            mStateObservable.addObserver(o);
        }
    }

    public void deleteStateObservable(Observer o) {
        if (mStateObservable != null) {
            mStateObservable.deleteObserver(o);
        }
    }

    public void clearStateObservable() {
        if (mStateObservable != null) {
            mStateObservable.deleteObservers();
        }
    }

    private IOnPlayerEventListener mOnPlayerEventListener = new IOnPlayerEventListener.Stub() {
        @Override
        public void onMusicSwitch(SongInfo music) {
            mClientHandler.obtainMessage(MSG_MUSIC_CHANGE, music).sendToTarget();
        }

        @Override
        public void onPlayerStart() {
            mClientHandler.obtainMessage(MSG_PLAYER_START).sendToTarget();
        }

        @Override
        public void onPlayerPause() {
            mClientHandler.obtainMessage(MSG_PLAYER_PAUSE).sendToTarget();
        }

        @Override
        public void onPlayCompletion() {
            mClientHandler.obtainMessage(MSG_PLAY_COMPLETION).sendToTarget();
        }

        @Override
        public void onError(String errorMsg) {
            mClientHandler.obtainMessage(MSG_PLAYER_ERROR, errorMsg).sendToTarget();
        }

        @Override
        public void onBuffering(boolean isFinishBuffer) {
            mClientHandler.obtainMessage(MSG_BUFFERING, isFinishBuffer).sendToTarget();
        }
    };

    private static class ClientHandler extends Handler {

        private final WeakReference<MusicManager> mWeakReference;

        ClientHandler(MusicManager manager) {
            super(Looper.getMainLooper());
            mWeakReference = new WeakReference<>(manager);
        }

        @Override
        public void handleMessage(Message msg) {
            MusicManager manager = mWeakReference.get();
            switch (msg.what) {
                case MSG_MUSIC_CHANGE:
                    SongInfo musicInfo = (SongInfo) msg.obj;

                    manager.notifyPlayerEventChange(MSG_MUSIC_CHANGE, musicInfo, "", false);
                    manager.mStateObservable.stateChangeNotifyObservers(MSG_MUSIC_CHANGE);
                    break;
                case MSG_PLAYER_START:
                    manager.notifyPlayerEventChange(MSG_PLAYER_START, null, "", false);
                    manager.mStateObservable.stateChangeNotifyObservers(MSG_PLAYER_START);
                    break;
                case MSG_PLAYER_PAUSE:
                    manager.notifyPlayerEventChange(MSG_PLAYER_PAUSE, null, "", false);
                    manager.mStateObservable.stateChangeNotifyObservers(MSG_PLAYER_START);
                    break;
                case MSG_PLAY_COMPLETION:
                    manager.notifyPlayerEventChange(MSG_PLAY_COMPLETION, null, "", false);
                    manager.mStateObservable.stateChangeNotifyObservers(MSG_PLAYER_START);
                    break;
                case MSG_PLAYER_ERROR:
                    String errMsg = (String) msg.obj;
                    manager.notifyPlayerEventChange(MSG_PLAYER_ERROR, null, errMsg, false);
                    manager.mStateObservable.stateChangeNotifyObservers(MSG_PLAYER_START);
                    break;
                case MSG_BUFFERING:
                    boolean isFinishBuffer = (boolean) msg.obj;
                    manager.notifyPlayerEventChange(MSG_BUFFERING, null, "", isFinishBuffer);
                    manager.mStateObservable.stateChangeNotifyObservers(MSG_PLAYER_START);
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    }

    public void addPlayerEventListener(OnPlayerEventListener listener) {
        if (listener != null) {
            if (!mPlayerEventListeners.contains(listener)) {
                mPlayerEventListeners.add(listener);
            }
        }
    }

    public void removePlayerEventListener(OnPlayerEventListener listener) {
        if (listener != null) {
            if (mPlayerEventListeners.contains(listener)) {
                mPlayerEventListeners.remove(listener);
            }
        }
    }

    public void clearPlayerEventListener() {
        mPlayerEventListeners.clear();
    }

    private void notifyPlayerEventChange(int msg, SongInfo info, String errorMsg, boolean isFinishBuffer) {
        for (OnPlayerEventListener listener : mPlayerEventListeners) {
            switch (msg) {
                case MSG_MUSIC_CHANGE:
                    listener.onMusicSwitch(info);
                    break;
                case MSG_PLAYER_START:
                    listener.onPlayerStart();
                    break;
                case MSG_PLAYER_PAUSE:
                    listener.onPlayerPause();
                    break;
                case MSG_PLAY_COMPLETION:
                    listener.onPlayCompletion();
                    break;
                case MSG_PLAYER_ERROR:
                    listener.onError(errorMsg);
                    break;
                case MSG_BUFFERING:
                    listener.onBuffering(isFinishBuffer);
                    break;
            }
        }
    }

    @Override
    public void playMusic(List<SongInfo> list, int index, boolean isJustPlay) {
        if (control != null) {
            try {
                control.playMusic(list, index, isJustPlay);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void playMusic(List<SongInfo> list, int index) {
        playMusic(list, index, false);
    }

    @Override
    public void playMusicByInfo(SongInfo info, boolean isJustPlay) {
        if (control != null) {
            try {
                control.playMusicByInfo(info, isJustPlay);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void playMusicByInfo(SongInfo info) {
        playMusicByInfo(info, false);
    }

    @Override
    public void playMusicByIndex(int index, boolean isJustPlay) {
        if (control != null) {
            try {
                control.playMusicByIndex(index, isJustPlay);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void playMusicByIndex(int index) {
        playMusicByIndex(index, false);
    }

    @Override
    public void playMusicAutoStopWhen(List<SongInfo> list, int index, int time) {
        if (control != null) {
            try {
                control.playMusicAutoStopWhen(list, index, time);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void playMusicByInfoAutoStopWhen(SongInfo info, int time) {
        if (control != null) {
            try {
                control.playMusicByInfoAutoStopWhen(info, time);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void playMusicByIndexAutoStopWhen(int index, int time) {
        if (control != null) {
            try {
                control.playMusicByIndexAutoStopWhen(index, time);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setAutoStopTime(int time) {
        if (control != null) {
            try {
                control.setAutoStopTime(time);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int getCurrPlayingIndex() {
        if (control != null) {
            try {
                return control.getCurrPlayingIndex();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    @Override
    public void pauseMusic() {
        if (control != null) {
            try {
                control.pauseMusic();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void resumeMusic() {
        if (control != null) {
            try {
                control.resumeMusic();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void stopMusic() {
        if (control != null) {
            try {
                control.stopMusic();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setPlayList(List<SongInfo> list) {
        if (control != null) {
            try {
                control.setPlayList(list);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setPlayListWithIndex(List<SongInfo> list, int index) {
        if (control != null) {
            try {
                control.setPlayListWithIndex(list, index);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public List<SongInfo> getPlayList() {
        if (control != null) {
            try {
                return control.getPlayList();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public void deleteSongInfoOnPlayList(SongInfo info, boolean isNeedToPlayNext) throws RemoteException {
        if (control != null) {
            try {
                control.deleteSongInfoOnPlayList(info, isNeedToPlayNext);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int getStatus() {
        if (control != null) {
            try {
                return control.getStatus();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    @Override
    public void playNext() {
        if (control != null) {
            try {
                control.playNext();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void playPre() {
        if (control != null) {
            try {
                control.playPre();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean hasPre() {
        if (control != null) {
            try {
                return control.hasPre();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public boolean hasNext() {
        if (control != null) {
            try {
                return control.hasNext();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public SongInfo getPreMusic() {
        if (control != null) {
            try {
                return control.getPreMusic();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public SongInfo getNextMusic() {
        if (control != null) {
            try {
                return control.getNextMusic();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public SongInfo getCurrPlayingMusic() {
        if (control != null) {
            try {
                return control.getCurrPlayingMusic();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public void setCurrMusic(int index) {
        if (control != null) {
            try {
                control.setCurrMusic(index);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setPlayMode(int mode) {
        if (control != null) {
            try {
                control.setPlayMode(mode);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int getPlayMode() {
        if (control != null) {
            try {
                return control.getPlayMode();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    @Override
    public long getProgress() {
        if (control != null) {
            try {
                return control.getProgress();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    @Override
    public void seekTo(int position) {
        if (control != null) {
            try {
                control.seekTo(position);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void reset() {
        if (control != null) {
            try {
                control.reset();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 判断当前的音乐是不是正在播放的音乐
     */
    public static boolean isCurrMusicIsPlayingMusic(SongInfo currMusic) {
        SongInfo playingMusic = MusicManager.get().getCurrPlayingMusic();
        boolean result;
        if (playingMusic == null) {
            result = false;
        } else {
            result = currMusic.getSongId().equals(playingMusic.getSongId());
        }
        return result;
    }

    /**
     * 是否在暂停
     */
    public static boolean isPaused() {
        return MusicManager.get().getStatus() == State.STATE_PAUSED;
    }

    /**
     * 是否正在播放
     */
    public static boolean isPlaying() {
        return MusicManager.get().getStatus() == State.STATE_PLAYING;
    }

    public static boolean isIdea() {
        return MusicManager.get().getStatus() == State.STATE_NONE ||
                MusicManager.get().getStatus() == State.STATE_IDLE;
    }

    /**
     * 当前的音乐是否在播放
     */
    public static boolean isCurrMusicIsPlaying(SongInfo currMusic) {
        return isCurrMusicIsPlayingMusic(currMusic) && isPlaying();
    }

    /**
     * 当前音乐是否在暂停
     */
    public static boolean isCurrMusicIsPaused(SongInfo currMusic) {
        return isCurrMusicIsPlayingMusic(currMusic) && isPaused();
    }

    @Override
    public void registerPlayerEventListener(IOnPlayerEventListener listener) {

    }

    @Override
    public void unregisterPlayerEventListener(IOnPlayerEventListener listener) {

    }


    @Override
    public IBinder asBinder() {
        return null;
    }

}
