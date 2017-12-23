package com.lzx.musiclib.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.lzx.musiclib.R;
import com.lzx.musiclib.contants.MusicAction;
import com.lzx.musiclib.loader.ImageLoader;
import com.lzx.musiclib.manager.AudioFocusManager;
import com.lzx.musiclib.manager.MediaSessionManager;
import com.lzx.musiclib.model.MusicInfo;
import com.lzx.musiclib.model.PlayMode;
import com.lzx.musiclib.receiver.NoisyAudioStreamReceiver;

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

    public static final String ACTION_PLAY_BY_POSITION = "ACTION_PLAY_BY_POSITION";
    public static final String ACTION_PLAY_BY_MUSIC = "ACTION_PLAY_BY_MUSIC";
    public static final String ACTION_PLAY_PAUSE = "ACTION_PLAY_PAUSE";
    public static final String ACTION_PAUSE = "ACTION_PAUSE";
    public static final String ACTION_STOP = "ACTION_STOP";
    public static final String ACTION_NEXT = "ACTION_NEXT";
    public static final String ACTION_PRE = "ACTION_PRE";
    public static final String ACTION_STAR = "ACTION_STAR";

    // 正在播放的本地歌曲的序号
    private int mPlayingPosition = -1;
    private int mPlayState = STATE_IDLE;
    private MediaPlayer mPlayer = new MediaPlayer();
    private AudioFocusManager mAudioFocusManager;
    private MediaSessionManager mMediaSessionManager;

    private final NoisyAudioStreamReceiver mNoisyReceiver = new NoisyAudioStreamReceiver();
    private final IntentFilter mNoisyFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
    public Handler mHandler = new Handler();
    private MusicInfo mPlayingMusic;
    private PlayMode playMode;

    public CopyOnWriteArrayList<MusicInfo> mMusicList = new CopyOnWriteArrayList<>();
    private OnPlayerEventListener mOnPlayerEventListener;

    private long mServiceTimer = 0;

    private ServiceReceiver mServiceReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        mServiceReceiver = new ServiceReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_PLAY_BY_POSITION);
        filter.addAction(ACTION_PLAY_BY_MUSIC);
        filter.addAction(ACTION_PLAY_PAUSE);
        filter.addAction(ACTION_PAUSE);
        filter.addAction(ACTION_STOP);
        filter.addAction(ACTION_NEXT);
        filter.addAction(ACTION_PRE);
        filter.addAction(ACTION_STAR);
        registerReceiver(mServiceReceiver, filter);

        mAudioFocusManager = new AudioFocusManager(this);
        mMediaSessionManager = new MediaSessionManager(this);
        playMode = new PlayMode();
        //当流媒体播放完毕的时候回调
        mPlayer.setOnCompletionListener(this);
        //当播放中发生错误的时候回调
        mPlayer.setOnErrorListener(this);
        //当使用seekTo()设置播放位置的时候回调。
        mPlayer.setOnSeekCompleteListener(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new PlayBinder();
    }

    public class PlayBinder extends Binder {
        public MusicPlayService getService() {
            return MusicPlayService.this;
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

    /**
     * 用户下载网络图片的handler
     */
    private Handler mMainHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            ImageLoader.LoaderResult result = (ImageLoader.LoaderResult) msg.obj;
            Log.i(TAG, "bitmap = " + result.bitmap);
            Bitmap albumArt;
            if (result.bitmap == null) {
                albumArt = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
            } else {
                Bitmap.Config config = result.bitmap.getConfig();
                if (config == null) {
                    config = Bitmap.Config.ARGB_8888;
                }
                albumArt = result.bitmap.copy(config, false);
            }
            mMediaSessionManager.updateMetaData(mPlayingMusic, albumArt);
            mMediaSessionManager.updatePlaybackState();
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPlayer.reset();
        mPlayer.release();
        mPlayer = null;
        mAudioFocusManager.abandonAudioFocus();
        mMediaSessionManager.release();
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;
        mMainHandler.removeCallbacksAndMessages(null);
        mMainHandler = null;
        unregisterReceiver(mNoisyReceiver);
        unregisterReceiver(mServiceReceiver);
    }

    public CopyOnWriteArrayList<MusicInfo> getMusicList() {
        return mMusicList;
    }

    public void setMusicList(List<MusicInfo> musicList) {
        mMusicList.clear();
        mMusicList.addAll(musicList);
    }

    public OnPlayerEventListener getOnPlayerEventListener() {
        return mOnPlayerEventListener;
    }

    public void setOnPlayerEventListener(OnPlayerEventListener onPlayerEventListener) {
        mOnPlayerEventListener = onPlayerEventListener;
    }

    /**
     * 播放完毕回调
     */
    @Override
    public void onCompletion(MediaPlayer mp) {
        if (mOnPlayerEventListener != null) {
            mOnPlayerEventListener.onPlayCompletion();
        }
        next();
    }

    /**
     * 播放失败回调
     */
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        if (mPlayState != STATE_IDLE) {
            stop();
            mHandler.removeCallbacks(mPublishRunnable);
            if (mOnPlayerEventListener != null) {
                mOnPlayerEventListener.onError(what, extra);
            }
        }
        return false;
    }

    /**
     * 准备完毕回调
     */
    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        if (isPreparing()) {
            playStart(false);
        }
    }

    /**
     * 滑动进度条回调
     */
    @Override
    public void onSeekComplete(MediaPlayer mediaPlayer) {

    }

    /**
     * 网络流媒体的缓冲监听回调
     */
    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int percent) {
        if (mOnPlayerEventListener != null) {
            mOnPlayerEventListener.onBufferingUpdate(percent);
        }
    }

    /**
     * 开启服务
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
            playStart(true);
        } else {
            //播放
            play(getPlayingPosition());
        }
    }

    /**
     * 开始播放
     */
    public void playStart(boolean isNeedListener) {
        if (!isPreparing() && !isPausing()) {
            return;
        }
        if (mAudioFocusManager.requestAudioFocus()) {
            mPlayer.start();
            mPlayState = STATE_PLAYING;
            mMediaSessionManager.updatePlaybackState();
            registerReceiver(mNoisyReceiver, mNoisyFilter);
            mHandler.post(mPublishRunnable);
            if (isNeedListener && mOnPlayerEventListener != null) {
                mOnPlayerEventListener.onPlayerStart();
            }
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
        if (mOnPlayerEventListener != null) {
            mOnPlayerEventListener.onPlayerPause();
        }
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
            ImageLoader.build(this).bindBitmap(music.getMusicCover(), mMainHandler);
            mHandler.post(mPublishRunnable);
            if (mOnPlayerEventListener != null) {
                mOnPlayerEventListener.onMusicChange(music);
                mOnPlayerEventListener.onPlayerStart();
            }
        } catch (IOException e) {
            e.printStackTrace();
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
                if (size > 1) {
                    if (mPlayingPosition <= size - 1) {
                        play(mPlayingPosition);
                    }
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
            default:
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
     * 是否有上一首
     */
    public boolean hasPrev() {
        int size = getMusicList().size();
        if (size == 0) {
            return false;
        }
        String mode = getPlayMode();
        switch (mode) {
            //顺序播放
            case PlayMode.PLAY_IN_ORDER: {
                return mPlayingPosition - 1 >= 0;
            }
            //随机播放 //单曲循环 //列表循环
            case PlayMode.PLAY_IN_RANDOM:
            case PlayMode.PLAY_IN_SINGLE_LOOP:
            case PlayMode.PLAY_IN_LIST_LOOP:
                return true;
            default: {
                return false;
            }
        }
    }

    /**
     * 是否有下一首
     */
    public boolean hasNext() {
        int size = getMusicList().size();
        if (size == 0) {
            return false;
        }
        String mode = getPlayMode();
        switch (mode) {
            //顺序播放
            case PlayMode.PLAY_IN_ORDER: {
                return mPlayingPosition <= size - 1;
            }
            //随机播放    //单曲循环    //列表循环
            case PlayMode.PLAY_IN_RANDOM:
            case PlayMode.PLAY_IN_SINGLE_LOOP:
            case PlayMode.PLAY_IN_LIST_LOOP:
                return true;
            default: {
                return false;
            }
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
        if (mPlayingMusic == null) {
            return new MusicInfo();
        }
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
        stopSelf();
    }

    /**
     * 设置定时关闭时间
     */
    public void setQuitTimer(long milli) {
        mServiceTimer = milli;
        mHandler.post(mTimerRunnable);
    }

    private Runnable mPublishRunnable = new Runnable() {
        @Override
        public void run() {
            if (isPlaying() && mOnPlayerEventListener != null) {
                mOnPlayerEventListener.onProgress((int) getCurrentPosition(), mPlayer.getDuration());
            }
            mHandler.postDelayed(this, 1000);
        }
    };

    private int currIndex = 0;
    private Runnable mTimerRunnable = new Runnable() {
        @Override
        public void run() {
            if (mServiceTimer != 0 && mOnPlayerEventListener != null) {
                mHandler.postDelayed(this, 1000);
                currIndex++;
                Log.i("LogUtil", "定时时间 = " + currIndex);
                if (currIndex == mServiceTimer) {
                    stop();
                    mHandler.removeCallbacks(this);
                    currIndex = 0;
                    mServiceTimer = 0;
                    if (mOnPlayerEventListener != null) {
                        mOnPlayerEventListener.onTimer();
                    }
                }
            }
        }
    };


    private class ServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TextUtils.isEmpty(action)) {
                return;
            }
            switch (action) {
                case ACTION_PLAY_BY_POSITION:
                    int position = intent.getIntExtra("position", 0);
                    play(position);
                    break;
                case ACTION_PLAY_BY_MUSIC:
                    MusicInfo info = intent.getParcelableExtra("MusicInfo");
                    play(info);
                    break;
                case ACTION_PLAY_PAUSE:
                    playPause();
                    break;
                case ACTION_PAUSE:
                    pause();
                    break;
                case ACTION_STOP:
                    stop();
                    break;
                case ACTION_NEXT:
                    next();
                    break;
                case ACTION_PRE:
                    prev();
                    break;
                case ACTION_STAR:
                    playStart(true);
                    break;
            }
        }
    }


}
