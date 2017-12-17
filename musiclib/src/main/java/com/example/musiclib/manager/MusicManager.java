package com.example.musiclib.manager;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;

import com.example.musiclib.model.MusicInfo;
import com.example.musiclib.model.PlayMode;
import com.example.musiclib.service.EventCallback;
import com.example.musiclib.service.IMusicPlayService;
import com.example.musiclib.service.MusicPlayService;
import com.example.musiclib.service.OnPlayerEventListener;
import com.example.musiclib.service.QuitTimer;
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

    private static final WeakHashMap<Context, MusicPlayServiceConnection> mConnectionMap;
    private static IMusicPlayService mService;
    public static List<MusicInfo> mMusicList;
    public static OnPlayerEventListener listener;
    public static Handler mHandler = new Handler();
    private static SubjectObservable observable;
    private static final long TIME_UPDATE = 1000L;

    static {
        mConnectionMap = new WeakHashMap<>();
        mMusicList = new ArrayList<>();
        observable = new SubjectObservable();
    }

    /**
     * 添加观察者
     */
    public static void addObservable(Observer o) {
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
    public static ServiceToken bindToService(final Context context, final ServiceConnection callback) {
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
    public static void unbindService(ServiceToken token) {
        if (token == null) {
            return;
        }
        final Context mContextWrapper = token.mWrappedContext;
        final MusicPlayServiceConnection mBinder = mConnectionMap.remove(mContextWrapper);
        if (mBinder == null) {
            return;
        }
        mContextWrapper.unbindService(mBinder);
        if (mConnectionMap.isEmpty()) {
            mService = null;
            mMusicList = null;
            mPublishRunnable = null;
        }
    }

    /**
     * Binder
     */
    public static final class MusicPlayServiceConnection implements ServiceConnection {

        private final ServiceConnection mCallback;
        private final Context mContext;

        public MusicPlayServiceConnection(ServiceConnection mCallback, Context mContext) {
            this.mCallback = mCallback;
            this.mContext = mContext;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = IMusicPlayService.Stub.asInterface(service);
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
            mService = null;
        }
    }

    public static final class ServiceToken {
        public Context mWrappedContext;

        public ServiceToken(final Context context) {
            mWrappedContext = context;
        }
    }

    public static void initPlaybackServiceWithSettings(final Context context) {
        QuitTimer.getInstance().init(mService, mHandler, new EventCallback<Long>() {
            @Override
            public void onEvent(Long aLong) {
                if (listener != null) {
                    listener.onTimer(aLong);
                }
            }
        });
    }

    private static boolean isServiceNotNull() {
        return mService != null;
    }

    public static List<MusicInfo> getMusicList() {
        return mMusicList;
    }

    public static void setMusicList(List<MusicInfo> musicList) {
        mMusicList = musicList;
    }

    public static OnPlayerEventListener getListener() {
        return listener;
    }

    public static void setListener(OnPlayerEventListener listener) {
        MusicManager.listener = listener;
    }

    /**
     * 暂停/播放
     */
    public static void playPause() {
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
    public static void playPauseOnly() {
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
    public static void startPlay() {
        if (isServiceNotNull()) {
            try {
                mService.start();
                mHandler.post(mPublishRunnable);
                if (listener != null) {
                    listener.onPlayerStart();
                }
                observable.subjectNotifyObservers(MusicPlayService.STATE_PLAYING);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 暂停播放
     */
    public static void pause() {
        if (isServiceNotNull()) {
            try {
                mService.pause();
                mHandler.removeCallbacks(mPublishRunnable);
                if (listener != null) {
                    listener.onPlayerPause();
                }
                observable.subjectNotifyObservers(MusicPlayService.STATE_PAUSE);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 停止播放
     */
    public static void stop() {
        if (isServiceNotNull()) {
            try {
                mService.stop();
                observable.subjectNotifyObservers(MusicPlayService.STATE_PAUSE);
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
    public static void seekTo(int msec) {
        if (isServiceNotNull()) {
            try {
                mService.seekTo(msec);
                if (listener != null) {
                    listener.onPublish(msec);
                }
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
    public static boolean isPlaying() {
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
    public static boolean isPausing() {
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
    public static boolean isPreparing() {
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
    public static boolean isIdle() {
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
    public static void playByPosition(int position) {
        if (isServiceNotNull()) {
            try {
                mService.playByPosition(position);
                mHandler.post(mPublishRunnable);
                if (listener != null) {
                    listener.onPlayerStart();
                    listener.onMusicChange(mMusicList.get(position));
                }
                observable.subjectNotifyObservers(MusicPlayService.STATE_PLAYING);
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
    public static void playByMusicInfo(MusicInfo music) {
        if (isServiceNotNull()) {
            try {
                mService.playByMusicInfo(music);
                mHandler.post(mPublishRunnable);
                if (listener != null) {
                    listener.onPlayerStart();
                    listener.onMusicChange(music);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取正在播放的本地歌曲的序号
     */
    public static int getPlayingPosition() {
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
    public static void setPlayingPosition(int playingPosition) {
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
    public static void playNext() {
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
    public static int getCurrentPosition() {
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
    public static MusicInfo getPlayingMusic() {
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

    public static Runnable mPublishRunnable = new Runnable() {
        @Override
        public void run() {
            if (isPlaying() && listener != null) {
                listener.onPublish(getCurrentPosition());
            }
            mHandler.postDelayed(this, TIME_UPDATE);
        }
    };
}
