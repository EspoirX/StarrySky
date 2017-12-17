package com.example.musiclib.manager;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;

import com.example.musiclib.model.MusicInfo;
import com.example.musiclib.model.PlayMode;
import com.example.musiclib.service.IMusicPlayService;
import com.example.musiclib.service.IOnPlayerEventListener;
import com.example.musiclib.service.MusicPlayService;
import com.example.musiclib.service.OnPlayerEventListener;
import com.example.musiclib.service.SubjectObservable;

import java.util.ArrayList;
import java.util.List;
import java.util.Observer;
import java.util.WeakHashMap;

/**
 * @author lzx
 * @date 2017/12/14
 */

public class MusicManager {

    private WeakHashMap<Context, MusicPlayServiceConnection> mConnectionMap;
    private IMusicPlayService mService;
    private SubjectObservable observable;
    private OnPlayerEventListener mOnPlayerEventListener;
    private static final int MSG_MUSIC_CHANGE = 0;
    private static final int MSG_PLAYER_START = 1;
    private static final int MSG_PLAYER_PAUSE = 2;
    private static final int MSG_PROGRESS = 3;
    private static final int MSG_BUFFERING_UPDATE = 4;
    private static final int MSG_ON_TIMER = 5;

    private MusicManager() {
        mConnectionMap = new WeakHashMap<>();
        observable = new SubjectObservable();
    }

    public static MusicManager get() {
        return SingletonHolder.sInstance;
    }

    /**
     * 静态内部类
     */
    private static class SingletonHolder {
        private static final MusicManager sInstance = new MusicManager();
    }

    /**
     * 添加观察者
     */
    public void addObservable(Observer o) {
        if (observable != null) {
            observable.addObserver(o);
        }
    }

    /**
     * 绑定服务
     *
     * @param context
     * @param callback
     * @return
     */
    public ServiceToken bindToService(final Context context, final ServiceConnection callback) {
        context.startService(new Intent(context, MusicPlayService.class));
        final MusicPlayServiceConnection binder = new MusicPlayServiceConnection(callback, context);
        Intent intent = new Intent(context, MusicPlayService.class);
        if (context.bindService(intent, binder, Context.BIND_AUTO_CREATE)) {
            mConnectionMap.put(context, binder);
            return new ServiceToken(context);
        }
        return null;
    }

    /**
     * 解绑服务
     *
     * @param token
     */
    public void unbindService(ServiceToken token) {
        if (token == null) {
            return;
        }
        if (mService != null && mService.asBinder().isBinderAlive()) {
            try {
                mService.unregisterListener(mIOnPlayerEventListener);
                final Context mContextWrapper = token.mWrappedContext;
                final MusicPlayServiceConnection mBinder = mConnectionMap.remove(mContextWrapper);
                if (mBinder == null) {
                    return;
                }
                mContextWrapper.unbindService(mBinder);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Binder
     */
    public class MusicPlayServiceConnection implements ServiceConnection {

        private final ServiceConnection mCallback;
        private final Context mContext;

        public MusicPlayServiceConnection(ServiceConnection mCallback, Context mContext) {
            this.mCallback = mCallback;
            this.mContext = mContext;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            IMusicPlayService musicPlayService = IMusicPlayService.Stub.asInterface(service);
            try {
                mService = musicPlayService;
                musicPlayService.registerListener(mIOnPlayerEventListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            if (mCallback != null) {
                mCallback.onServiceConnected(name, service);
            }
            initPlaybackServiceWithSettings(mContext);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (mCallback != null) {
                mCallback.onServiceDisconnected(name);
            }
            mHandler.removeCallbacksAndMessages(null);
            mService = null;
        }
    }

    private IOnPlayerEventListener mIOnPlayerEventListener = new IOnPlayerEventListener.Stub() {
        @Override
        public void onMusicChange(MusicInfo music) throws RemoteException {
            mHandler.obtainMessage(MSG_MUSIC_CHANGE, music).sendToTarget();
        }

        @Override
        public void onPlayerStart() throws RemoteException {
            mHandler.obtainMessage(MSG_PLAYER_START).sendToTarget();
        }

        @Override
        public void onPlayerPause() throws RemoteException {
            mHandler.obtainMessage(MSG_PLAYER_PAUSE).sendToTarget();
        }

        @Override
        public void onProgress(int progress) throws RemoteException {
            mHandler.obtainMessage(MSG_PROGRESS, progress).sendToTarget();
        }

        @Override
        public void onBufferingUpdate(int percent) throws RemoteException {
            mHandler.obtainMessage(MSG_BUFFERING_UPDATE, percent).sendToTarget();
        }

        @Override
        public void onTimer(long remain) throws RemoteException {
            mHandler.obtainMessage(MSG_ON_TIMER, remain).sendToTarget();
        }
    };

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_MUSIC_CHANGE:
                    if (mOnPlayerEventListener != null) {
                        mOnPlayerEventListener.onMusicChange((MusicInfo) msg.obj);
                    }
                    break;
                case MSG_PLAYER_START:
                    observable.subjectNotifyObservers(MusicPlayService.STATE_PLAYING);
                    if (mOnPlayerEventListener != null) {
                        mOnPlayerEventListener.onPlayerStart();
                    }
                    break;
                case MSG_PLAYER_PAUSE:
                    observable.subjectNotifyObservers(MusicPlayService.STATE_PAUSE);
                    if (mOnPlayerEventListener != null) {
                        mOnPlayerEventListener.onPlayerPause();
                    }
                    break;
                case MSG_PROGRESS:
                    if (mOnPlayerEventListener != null) {
                        mOnPlayerEventListener.onProgress((Integer) msg.obj);
                    }
                    break;
                case MSG_BUFFERING_UPDATE:
                    if (mOnPlayerEventListener != null) {
                        mOnPlayerEventListener.onBufferingUpdate((Integer) msg.obj);
                    }
                    break;
                case MSG_ON_TIMER:
                    if (mOnPlayerEventListener != null) {
                        mOnPlayerEventListener.onTimer((Long) msg.obj);
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    };

    public void setOnPlayerEventListener(OnPlayerEventListener onPlayerEventListener) {
        mOnPlayerEventListener = onPlayerEventListener;
    }

    public static final class ServiceToken {
        public Context mWrappedContext;

        public ServiceToken(final Context context) {
            mWrappedContext = context;
        }
    }

    public static void initPlaybackServiceWithSettings(final Context context) {

    }

    private boolean isServiceNotNull() {
        return mService != null;
    }

    public List<MusicInfo> getMusicList() {
        if (isServiceNotNull()) {
            try {
                return mService.getMusicList();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return new ArrayList<>();
    }

    public void setMusicList(List<MusicInfo> musicList) {
        if (isServiceNotNull()) {
            try {
                mService.setMusicList(musicList);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 暂停/播放
     */
    public void playPause() {
        if (isServiceNotNull()) {
            if (isPreparing()) {
                //如果是准备状态，停止
                stop();
            } else if (isPlaying()) {
                //如果是正在播放，暂停
                pause();
            } else if (isPausing()) {
                //如果是暂停，开始播放
                startPlay();
            } else {
                //播放
                playByPosition(getPlayingPosition());
            }
        }
    }

    /**
     * 暂停/播放,没有监听
     */
    public void playPauseOnly() {
        if (isServiceNotNull()) {
            try {
                mService.playPause();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 开始播放
     */
    public void startPlay() {
        if (isServiceNotNull()) {
            try {
                mService.start();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 暂停播放
     */
    public void pause() {
        if (isServiceNotNull()) {
            try {
                mService.pause();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 停止播放
     */
    public void stop() {
        if (isServiceNotNull()) {
            try {
                mService.stop();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 上一首
     */
    public void prev() {
        if (isServiceNotNull()) {
            try {
                mService.prev();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 跳转到指定的时间位置
     *
     * @param msec 时间
     */
    public void seekTo(int msec) {
        if (isServiceNotNull()) {
            try {
                mService.seekTo(msec);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 是否在播放
     *
     * @return
     */
    public boolean isPlaying() {
        if (isServiceNotNull()) {
            try {
                return mService.isPlaying();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 是否暂停
     *
     * @return
     */
    public boolean isPausing() {
        if (isServiceNotNull()) {
            try {
                return mService.isPausing();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 是否准备
     *
     * @return
     */
    public boolean isPreparing() {
        if (isServiceNotNull()) {
            try {
                return mService.isPreparing();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 是否空闲
     *
     * @return
     */
    public boolean isIdle() {
        if (isServiceNotNull()) {
            try {
                return mService.isIdle();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    /**
     * 播放指定位置
     *
     * @param position
     */
    public void playByPosition(int position) {
        if (isServiceNotNull()) {
            try {
                mService.playByPosition(position);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 播放指定音乐
     *
     * @param music
     */
    public void playByMusicInfo(MusicInfo music) {
        if (isServiceNotNull()) {
            try {
                mService.playByMusicInfo(music);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取正在播放的本地歌曲的序号
     */
    public int getPlayingPosition() {
        if (isServiceNotNull()) {
            try {
                return mService.getPlayingPosition();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    /**
     * 设置位置
     *
     * @param playingPosition
     */
    public void setPlayingPosition(int playingPosition) {
        if (isServiceNotNull()) {
            try {
                mService.setPlayingPosition(playingPosition);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 播放下一首
     */
    public void playNext() {
        if (isServiceNotNull()) {
            try {
                mService.next();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取当前位置
     *
     * @return
     */
    public int getCurrentPosition() {
        if (isServiceNotNull()) {
            try {
                return (int) mService.getCurrentPosition();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    /**
     * 获取正在播放的歌曲[网络]
     */
    public MusicInfo getPlayingMusic() {
        if (isServiceNotNull()) {
            try {
                return mService.getPlayingMusic();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 退出
     */
    public void quit() {
        if (isServiceNotNull()) {
            try {
                mService.quit();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取播放模式
     *
     * @return
     */
    public String getPlayMode() {
        if (isServiceNotNull()) {
            try {
                return mService.getPlayMode();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return PlayMode.PLAY_IN_ORDER;
    }

    /**
     * 设置播放模式
     *
     * @param playMode
     */
    public void setPlayMode(String playMode) {
        if (isServiceNotNull()) {
            try {
                mService.setPlayMode(playMode);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }


}
