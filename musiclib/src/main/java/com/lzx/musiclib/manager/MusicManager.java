package com.lzx.musiclib.manager;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.lzx.musiclib.model.MusicInfo;
import com.lzx.musiclib.model.PlayMode;
import com.lzx.musiclib.service.MusicPlayService;
import com.lzx.musiclib.service.OnPlayerEventListener;
import com.lzx.musiclib.service.ServiceConnectionCallback;
import com.lzx.musiclib.service.SubjectObservable;

import java.util.ArrayList;
import java.util.List;
import java.util.Observer;

/**
 * @author lzx
 * @date 2017/12/14
 */

public class MusicManager implements OnPlayerEventListener {

    private MusicPlayService mService;
    private SubjectObservable observable;
    private List<OnPlayerEventListener> mOnPlayerEventListenerList;
    private MusicPlayServiceConnection mMusicPlayServiceConnection;
    private Context mContext;
    private ServiceConnectionCallback mConnectionCallback;

    private MusicManager() {
        observable = new SubjectObservable();
        mMusicPlayServiceConnection = new MusicPlayServiceConnection();
        mOnPlayerEventListenerList = new ArrayList<>();
    }

    public static MusicManager get() {
        return SingletonHolder.sInstance;
    }

    public void init(Context context) {
        mContext = context;
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
     * @return
     */
    public void bindToService(final Context context, ServiceConnectionCallback connectionCallback) {
        this.mConnectionCallback = connectionCallback;
        Intent intent = new Intent(context, MusicPlayService.class);
        context.startService(intent);
        context.bindService(intent, mMusicPlayServiceConnection, Context.BIND_AUTO_CREATE);
    }

    public void bindToService(final Context context) {
        bindToService(context, null);
    }

    /**
     * 解绑服务
     */
    public void unbindService(Context context) {
        if (mService != null) {
            if (mMusicPlayServiceConnection == null) {
                return;
            }
            context.unbindService(mMusicPlayServiceConnection);
        }
        mConnectionCallback = null;
    }

    public class MusicPlayServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicPlayService musicPlayService = ((MusicPlayService.PlayBinder) service).getService();
            mService = musicPlayService;
            musicPlayService.setOnPlayerEventListener(MusicManager.this);
            if (mConnectionCallback != null) {
                mConnectionCallback.onServiceConnected(musicPlayService);
            }
            Log.i("LogUtil", "音乐服务链接成功.....");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (mService != null) {
                mService.setOnPlayerEventListener(null);
            }
            mService = null;
            if (mConnectionCallback != null) {
                mConnectionCallback.onServiceDisconnected();
            }
            Log.i("LogUtil", "音乐服务已断开.....");
        }
    }


    @Override
    public void onMusicChange(MusicInfo music) {
        observable.subjectNotifyObservers(MusicPlayService.STATE_PLAYING);
        for (OnPlayerEventListener listener : mOnPlayerEventListenerList) {
            listener.onMusicChange(music);
        }
    }

    @Override
    public void onPlayerStart() {
        observable.subjectNotifyObservers(MusicPlayService.STATE_PLAYING);
        for (OnPlayerEventListener listener : mOnPlayerEventListenerList) {
            listener.onPlayerStart();
        }
    }

    @Override
    public void onPlayerPause() {
        observable.subjectNotifyObservers(MusicPlayService.STATE_PAUSE);
        for (OnPlayerEventListener listener : mOnPlayerEventListenerList) {
            listener.onPlayerPause();
        }
    }

    @Override
    public void onPlayCompletion() {
        for (OnPlayerEventListener listener : mOnPlayerEventListenerList) {
            listener.onPlayCompletion();
        }
    }

    @Override
    public void onProgress(int progress, int duration) {
        for (OnPlayerEventListener listener : mOnPlayerEventListenerList) {
            listener.onProgress(progress, duration);
        }
    }

    @Override
    public void onBufferingUpdate(int percent) {
        for (OnPlayerEventListener listener : mOnPlayerEventListenerList) {
            listener.onBufferingUpdate(percent);
        }
    }

    @Override
    public void onTimer() {
        for (OnPlayerEventListener listener : mOnPlayerEventListenerList) {
            listener.onTimer();
        }
    }

    @Override
    public void onError(int what, int extra) {
        for (OnPlayerEventListener listener : mOnPlayerEventListenerList) {
            listener.onError(what, extra);
        }
    }


    /**
     * 添加监听
     *
     * @param onPlayerEventListener
     */
    public void addOnPlayerEventListener(OnPlayerEventListener onPlayerEventListener) {
        if (onPlayerEventListener != null) {
            mOnPlayerEventListenerList.add(onPlayerEventListener);
        }
    }

    /**
     * 移除监听
     *
     * @param onPlayerEventListener
     */
    public void removePlayerEventListener(OnPlayerEventListener onPlayerEventListener) {
        if (onPlayerEventListener != null) {
            if (mOnPlayerEventListenerList.contains(onPlayerEventListener)) {
                mOnPlayerEventListenerList.remove(onPlayerEventListener);
            }
        }
    }

    /**
     * 清除监听
     */
    public void clearPlayerEventListener() {
        mOnPlayerEventListenerList.clear();
    }

    private boolean isServiceNotNull() {
        return mService != null;
    }

    /**
     * 得到播放列表
     *
     * @return
     */
    public List<MusicInfo> getMusicList() {
        if (isServiceNotNull()) {
            return mService.getMusicList();
        }
        return new ArrayList<>();
    }

    /**
     * 设置播放列表
     *
     * @param musicList
     */
    public void setMusicList(List<MusicInfo> musicList) {
        if (isServiceNotNull()) {
            mService.setMusicList(musicList);
        }
    }


    /**
     * 暂停/播放
     */
    public void playPause() {
        Intent intent = new Intent();
        intent.setAction(MusicPlayService.ACTION_PLAY_PAUSE);
        mContext.sendBroadcast(intent);
    }

    /**
     * 暂停/播放,没有监听
     */
    public void playPauseOnly() {
        if (isServiceNotNull()) {
            mService.playPause();
        }
    }

    /**
     * 开始播放
     */
    public void startPlay() {
        Intent intent = new Intent();
        intent.setAction(MusicPlayService.ACTION_STAR);
        mContext.sendBroadcast(intent);
    }

    /**
     * 暂停播放
     */
    public void pause() {
        Intent intent = new Intent();
        intent.setAction(MusicPlayService.ACTION_PAUSE);
        mContext.sendBroadcast(intent);
    }

    /**
     * 停止播放
     */
    public void stop() {
        Intent intent = new Intent();
        intent.setAction(MusicPlayService.ACTION_STOP);
        mContext.sendBroadcast(intent);
    }

    /**
     * 上一首
     */
    public void prev() {
        Intent intent = new Intent();
        intent.setAction(MusicPlayService.ACTION_PRE);
        mContext.sendBroadcast(intent);
    }

    /**
     * 跳转到指定的时间位置
     *
     * @param msec 时间
     */
    public void seekTo(int msec) {
        if (isServiceNotNull()) {
            mService.seekTo(msec);
        }
    }

    /**
     * 是否在播放
     *
     * @return
     */
    public boolean isPlaying() {
        if (isServiceNotNull()) {
            return mService.isPlaying();
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
            return mService.isPausing();
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
            return mService.isPreparing();
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
            return mService.isIdle();
        }
        return true;
    }

    /**
     * 播放指定位置
     *
     * @param position
     */
    public void playByPosition(int position) {
        Intent intent = new Intent();
        intent.setAction(MusicPlayService.ACTION_PLAY_BY_POSITION);
        intent.putExtra("position", position);
        mContext.sendBroadcast(intent);
    }

    /**
     * 播放指定音乐
     *
     * @param music
     */
    public void playByMusicInfo(MusicInfo music) {
        Intent intent = new Intent();
        intent.setAction(MusicPlayService.ACTION_PLAY_BY_MUSIC);
        intent.putExtra("MusicInfo", music);
        mContext.sendBroadcast(intent);
    }

    /**
     * 获取正在播放的本地歌曲的序号
     */
    public int getPlayingPosition() {
        if (isServiceNotNull()) {
            return mService.getPlayingPosition();
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
            mService.setPlayingPosition(playingPosition);
        }
    }

    /**
     * 播放下一首
     */
    public void playNext() {
        Intent intent = new Intent();
        intent.setAction(MusicPlayService.ACTION_NEXT);
        mContext.sendBroadcast(intent);
    }

    /**
     * 是否有上一首
     */
    public boolean hasPrev() {
        if (isServiceNotNull()) {
            return mService.hasPrev();
        }
        return false;
    }

    /**
     * 是否有下一首
     */
    public boolean hasNext() {
        if (isServiceNotNull()) {
            return mService.hasNext();
        }
        return false;
    }

    /**
     * 获取当前位置
     *
     * @return
     */
    public int getCurrentPosition() {
        if (isServiceNotNull()) {
            return (int) mService.getCurrentPosition();
        }
        return 0;
    }

    /**
     * 获取正在播放的歌曲[网络]
     */
    public MusicInfo getPlayingMusic() {
        if (isServiceNotNull()) {
            return mService.getPlayingMusic();
        }
        return new MusicInfo();
    }

    /**
     * 退出
     */
    public void quit() {
        if (isServiceNotNull()) {
            mService.quit();
        }
    }

    /**
     * 获取播放模式
     *
     * @return
     */
    public String getPlayMode() {
        if (isServiceNotNull()) {
            return mService.getPlayMode();
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
            mService.setPlayMode(playMode);
        }
    }

    /**
     * 设置停止播放时间
     *
     * @param milli
     */
    public void setQuitTimer(long milli) {
        if (isServiceNotNull()) {
            mService.setQuitTimer(milli);
        }
    }
}
