package com.example.musiclib.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.musiclib.contants.MusicAction;
import com.example.musiclib.manager.AudioFocusManager;
import com.example.musiclib.manager.MediaSessionManager;
import com.example.musiclib.model.MusicInfo;
import com.example.musiclib.model.PlayMode;
import com.example.musiclib.receiver.NoisyAudioStreamReceiver;
import com.example.musiclib.utils.CoverLoader;
import com.example.musiclib.utils.RunUtil;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * 音乐播放服务
 *
 * @author lzx
 * @date 2017/12/14
 */

public class MusicPlayService extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnBufferingUpdateListener {

    private static final String TAG = "MusicPlayService";

    public static final int STATE_IDLE = 0; //空闲状态
    public static final int STATE_PREPARING = 1; //准备状态
    public static final int STATE_PLAYING = 2; //正在播放
    public static final int STATE_PAUSE = 3; //暂停
    // 正在播放的本地歌曲的序号
    private int mPlayingPosition = -1;
    private int mPlayState = STATE_IDLE;
    private static final long TIME_UPDATE = 1000L;
    private MediaPlayer mPlayer = new MediaPlayer();
    private AudioFocusManager mAudioFocusManager;
    private MediaSessionManager mMediaSessionManager;

    private final NoisyAudioStreamReceiver mNoisyReceiver = new NoisyAudioStreamReceiver();
    private final IntentFilter mNoisyFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
    public static Handler mHandler = new Handler();
    private MusicInfo mPlayingMusic;
    private PlayMode playMode;

    public CopyOnWriteArrayList<MusicInfo> mMusicList = new CopyOnWriteArrayList<>();
    private RemoteCallbackList<IOnPlayerEventListener> mListenerList = new RemoteCallbackList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        CoverLoader.getInstance().init(this);
        mAudioFocusManager = new AudioFocusManager(this);
        mMediaSessionManager = new MediaSessionManager(this);
        playMode = new PlayMode();

        //当流媒体播放完毕的时候回调
        mPlayer.setOnCompletionListener(this);
        //当播放中发生错误的时候回调
        mPlayer.setOnErrorListener(this);
        //当使用seekTo()设置播放位置的时候回调。
        mPlayer.setOnSeekCompleteListener(this);
        QuitTimer.getInstance().init(this, mHandler, new EventCallback<Long>() {
            @Override
            public void onEvent(Long aLong) {
                int N = mListenerList.beginBroadcast();
                for (int i = 0; i < N; i++) {
                    IOnPlayerEventListener listener = mListenerList.getBroadcastItem(i);
                    if (listener != null) {
                        try {
                            listener.onTimer(aLong);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                }
                mListenerList.finishBroadcast();
            }
        });
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MusicPlayServiceStub(this);
    }

    public class MusicPlayServiceStub extends IMusicPlayService.Stub {

        private MusicPlayService mService;

        public MusicPlayServiceStub(MusicPlayService musicPlayService) {
            this.mService = musicPlayService;
        }

        @Override
        public void playPause() throws RemoteException {
            mService.playPause();
        }

        @Override
        public void start() throws RemoteException {
            mService.start();
        }

        @Override
        public void pause() throws RemoteException {
            mService.pause();
        }

        @Override
        public void stop() throws RemoteException {
            mService.stop();
        }

        @Override
        public void prev() throws RemoteException {
            mService.prev();
        }

        @Override
        public void seekTo(int msec) throws RemoteException {
            mService.seekTo(msec);
        }

        @Override
        public boolean isPlaying() throws RemoteException {
            return mService.isPlaying();
        }

        @Override
        public boolean isPausing() throws RemoteException {
            return mService.isPausing();
        }

        @Override
        public boolean isPreparing() throws RemoteException {
            return mService.isPreparing();
        }

        @Override
        public boolean isIdle() throws RemoteException {
            return mService.isIdle();
        }

        @Override
        public void playByPosition(int position) throws RemoteException {
            mService.play(position);
        }

        @Override
        public void playByMusicInfo(MusicInfo music) throws RemoteException {
            mService.play(music);
        }

        @Override
        public int getPlayingPosition() throws RemoteException {
            return mService.getPlayingPosition();
        }

        @Override
        public void setPlayingPosition(int playingPosition) throws RemoteException {
            mService.setPlayingPosition(playingPosition);
        }

        @Override
        public void next() throws RemoteException {
            mService.next();
        }

        @Override
        public long getCurrentPosition() throws RemoteException {
            return mService.getCurrentPosition();
        }

        @Override
        public MusicInfo getPlayingMusic() throws RemoteException {
            return mService.getPlayingMusic();
        }

        @Override
        public void quit() throws RemoteException {
            mService.quit();
        }

        @Override
        public String getPlayMode() throws RemoteException {
            return mService.getPlayMode();
        }

        @Override
        public void setPlayMode(String playMode) throws RemoteException {
            mService.setPlayMode(playMode);
        }

        @Override
        public void registerListener(final IOnPlayerEventListener listener) throws RemoteException {
            RunUtil.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    mListenerList.register(listener);
                }
            });

        }

        @Override
        public void unregisterListener(IOnPlayerEventListener listener) throws RemoteException {
            mListenerList.unregister(listener);
        }

        @Override
        public void setMusicList(List<MusicInfo> musicList) throws RemoteException {
            if (musicList == null) {
                return;
            }
            mService.setMusicList(musicList);
        }

        @Override
        public List<MusicInfo> getMusicList() throws RemoteException {
            return mService.getMusicList();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            switch (intent.getAction()) {
                case MusicAction.ACTION_MEDIA_PLAY_PAUSE:
                    playPause();
                    break;
                case MusicAction.ACTION_MEDIA_NEXT:
                    next();
                    break;
                case MusicAction.ACTION_MEDIA_PREVIOUS:
                    prev();
                    break;
            }
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPlayer.reset();
        mPlayer.release();
        mPlayer = null;
        mAudioFocusManager.abandonAudioFocus();
        mMediaSessionManager.release();
        unregisterReceiver(mNoisyReceiver);
    }

    public CopyOnWriteArrayList<MusicInfo> getMusicList() {
        return mMusicList;
    }

    public void setMusicList(List<MusicInfo> musicList) {
        mMusicList.clear();
        mMusicList.addAll(musicList);
    }

    /**
     * 播放完毕回调
     *
     * @param mp
     */
    @Override
    public void onCompletion(MediaPlayer mp) {
        next();
    }

    /**
     * 播放失败回调
     *
     * @param mediaPlayer
     * @param i
     * @param i1
     * @return
     */
    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }

    /**
     * 准备完毕回调
     *
     * @param mediaPlayer
     */
    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        if (isPreparing()) {
            start();
        }
    }

    /**
     * 滑动进度条回调
     *
     * @param mediaPlayer
     */
    @Override
    public void onSeekComplete(MediaPlayer mediaPlayer) {

    }

    /**
     * 网络流媒体的缓冲监听回调
     *
     * @param mediaPlayer
     * @param percent
     */
    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int percent) {
        int N = mListenerList.beginBroadcast();
        for (int i = 0; i < N; i++) {
            IOnPlayerEventListener listener = mListenerList.getBroadcastItem(i);
            if (listener != null) {
                try {
                    listener.onBufferingUpdate(percent);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
        mListenerList.finishBroadcast();
    }

    /**
     * 开启服务
     *
     * @param context
     * @param action
     */
    public static void startCommand(Context context, String action) {
        Intent intent = new Intent(context, MusicPlayService.class);
        intent.setAction(action);
        context.startService(intent);
    }

    /**
     * 暂停/播放
     */
    public void playPause() {
        if (isPreparing()) {
            //如果是准备状态，停止
            stop();
        } else if (isPlaying()) {
            //如果是正在播放，暂停
            pause();
        } else if (isPausing()) {
            //如果是暂停，开始播放
            start();
        } else {
            //播放
            play(getPlayingPosition());
        }
    }

    /**
     * 开始播放
     */
    public void start() {
        if (!isPreparing() && !isPausing()) {
            return;
        }
        if (mAudioFocusManager.requestAudioFocus()) {
            mPlayer.start();
            mPlayState = STATE_PLAYING;
            //  Notifier.showPlay(mPlayingMusic);
            mMediaSessionManager.updatePlaybackState();
            registerReceiver(mNoisyReceiver, mNoisyFilter);
            mHandler.post(mPublishRunnable);
            int N = mListenerList.beginBroadcast();
            for (int i = 0; i < N; i++) {
                IOnPlayerEventListener listener = mListenerList.getBroadcastItem(i);
                if (listener != null) {
                    try {
                        listener.onPlayerStart();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
            mListenerList.finishBroadcast();
        }
    }

    /**
     * 暂停播放
     */
    public void pause() {
        if (!isPlaying()) {
            return;
        }
        mPlayer.pause();
        mPlayState = STATE_PAUSE;
        // Notifier.showPause(mPlayingMusic);
        mMediaSessionManager.updatePlaybackState();
        unregisterReceiver(mNoisyReceiver);
        mHandler.removeCallbacks(mPublishRunnable);
        int N = mListenerList.beginBroadcast();
        for (int i = 0; i < N; i++) {
            IOnPlayerEventListener listener = mListenerList.getBroadcastItem(i);
            if (listener != null) {
                try {
                    listener.onPlayerPause();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
        mListenerList.finishBroadcast();
    }

    /**
     * 停止播放
     */
    public void stop() {
        if (isIdle()) {
            return;
        }
        pause();
        mPlayer.reset();
        mPlayState = STATE_IDLE;
    }


    /**
     * 跳转到指定的时间位置
     *
     * @param msec 时间
     */
    public void seekTo(int msec) {
        if (isPlaying() || isPausing()) {
            mPlayer.seekTo(msec);
            mMediaSessionManager.updatePlaybackState();
            int N = mListenerList.beginBroadcast();
            for (int i = 0; i < N; i++) {
                IOnPlayerEventListener listener = mListenerList.getBroadcastItem(i);
                if (listener != null) {
                    try {
                        listener.onProgress(msec);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
            mListenerList.finishBroadcast();
        }
    }

    public boolean isPlaying() {
        return mPlayState == STATE_PLAYING;
    }

    public boolean isPausing() {
        return mPlayState == STATE_PAUSE;
    }

    public boolean isPreparing() {
        return mPlayState == STATE_PREPARING;
    }

    public boolean isIdle() {
        return mPlayState == STATE_IDLE;
    }

    /**
     * 播放
     *
     * @param position 歌曲在列表中的位置
     */
    public void play(int position) {
        if (getMusicList().size() == 0) {
            return;
        }
        mPlayingPosition = position;
        MusicInfo music = getMusicList().get(mPlayingPosition);
        play(music);
    }

    /**
     * 播放
     *
     * @param music 歌曲信息
     */
    public void play(MusicInfo music) {
        if (music == null) {
            return;
        }
        mPlayingMusic = music;
        try {
            mPlayer.reset();
            mPlayer.setDataSource(music.getMusicUrl());
            mPlayer.prepareAsync();
            mPlayState = STATE_PREPARING;
            //当装载流媒体完毕的时候回调
            mPlayer.setOnPreparedListener(this);
            //网络流媒体的缓冲监听
            mPlayer.setOnBufferingUpdateListener(this);
            //     Notifier.showPlay(music);
            mMediaSessionManager.updateMetaData(mPlayingMusic);
            mMediaSessionManager.updatePlaybackState();
            mHandler.post(mPublishRunnable);

            int N = mListenerList.beginBroadcast();
            for (int i = 0; i < N; i++) {
                IOnPlayerEventListener listener = mListenerList.getBroadcastItem(i);
                if (listener != null) {
                    try {
                        listener.onPlayerStart();
                        listener.onMusicChange(music);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
            mListenerList.finishBroadcast();

        } catch (IOException e) {
            e.printStackTrace();
            Log.i(TAG, "MusicPlayService#play = " + e.getMessage());
        }
    }

    /**
     * 获取正在播放的本地歌曲的序号
     */
    public int getPlayingPosition() {
        return mPlayingPosition;
    }

    /**
     * 设置正在播放的本地歌曲的序号
     *
     * @param playingPosition
     */
    public void setPlayingPosition(int playingPosition) {
        mPlayingPosition = playingPosition;
    }

    /**
     * 下一首
     */
    public void next() {
        int size = getMusicList().size();
        if (size == 0) {
            return;
        }
        String mode = getPlayMode();
        switch (mode) {
            //顺序播放
            case PlayMode.PLAY_IN_ORDER:
                mPlayingPosition = mPlayingPosition + 1;
                if (mPlayingPosition <= size - 1) {
                    play(mPlayingPosition);
                }
                break;
            //随机播放
            case PlayMode.PLAY_IN_RANDOM:
                mPlayingPosition = new Random().nextInt(size);
                play(mPlayingPosition);
                break;
            //单曲循环
            case PlayMode.PLAY_IN_SINGLE_LOOP:
                play(mPlayingPosition);
                break;
            //列表循环
            case PlayMode.PLAY_IN_LIST_LOOP:
                //如果正在播放的是最后一首歌，点下一首，播第一首
                mPlayingPosition = mPlayingPosition + 1;
                if (mPlayingPosition >= size - 1) {
                    mPlayingPosition = 0;
                }
                play(mPlayingPosition);
                break;
        }
    }

    /**
     * 上一首
     */
    public void prev() {
        int size = getMusicList().size();
        if (size == 0) {
            return;
        }
        String mode = getPlayMode();
        switch (mode) {
            //顺序播放
            case PlayMode.PLAY_IN_ORDER:
                if (mPlayingPosition >= 1) {
                    play(mPlayingPosition - 1);
                }
                break;
            //随机播放
            case PlayMode.PLAY_IN_RANDOM:
                mPlayingPosition = new Random().nextInt(size);
                play(mPlayingPosition);
                break;
            //单曲循环
            case PlayMode.PLAY_IN_SINGLE_LOOP:
                play(mPlayingPosition);
                break;
            //列表循环
            case PlayMode.PLAY_IN_LIST_LOOP:
                //如果正在播放的是第一首歌，点上一首，播最后一首
                mPlayingPosition = mPlayingPosition - 1;
                if (mPlayingPosition < 0) {
                    mPlayingPosition = size - 1;
                }
                play(mPlayingPosition);
                break;
            default:
                break;
        }
    }

    /**
     * 获取当前进度
     *
     * @return
     */
    public long getCurrentPosition() {
        if (isPlaying() || isPausing()) {
            return mPlayer.getCurrentPosition();
        } else {
            return 0;
        }
    }

    /**
     * 获取正在播放的歌曲[网络]
     */
    public MusicInfo getPlayingMusic() {
        return mPlayingMusic;
    }


    public String getPlayMode() {
        return playMode.getCurrPlayMode(this);
    }

    public void setPlayMode(String playMode) {
        this.playMode.setCurrPlayMode(this, playMode);
    }

    /**
     * 退出
     */
    public void quit() {
        stop();
        QuitTimer.getInstance().stop();
        stopSelf();
    }

    public Runnable mPublishRunnable = new Runnable() {
        @Override
        public void run() {
//            int N = mListenerList.beginBroadcast();
//            for (int i = 0; i < N; i++) {
//                IOnPlayerEventListener listener = mListenerList.getBroadcastItem(i);
//                if (listener != null && isPlaying()) {
//                    try {
//                        listener.onProgress((int) getCurrentPosition());
//                    } catch (RemoteException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//            mListenerList.finishBroadcast();
            mHandler.postDelayed(this, TIME_UPDATE);
        }
    };
}
