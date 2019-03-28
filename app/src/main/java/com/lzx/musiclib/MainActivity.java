package com.lzx.musiclib;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.lzx.starrysky.manager.MediaSessionConnection;
import com.lzx.starrysky.manager.MusicManager;
import com.lzx.starrysky.manager.OnPlayerEventListener;
import com.lzx.starrysky.model.SongInfo;
import com.lzx.starrysky.playback.download.ExoDownload;
import com.lzx.starrysky.utils.TimerTaskManager;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements OnPlayerEventListener {

    boolean isFavorite = false;
    boolean isChecked = false;

    TimerTaskManager mTimerTask;
    TextView currInfo, currTime;
    SeekBar mSeekBar;

    MediaSessionConnection mMediaSessionConnection;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currInfo = findViewById(R.id.currInfo);
        currTime = findViewById(R.id.currTime);
        mSeekBar = findViewById(R.id.seekBar);

        mTimerTask = new TimerTaskManager();
        mMediaSessionConnection = MediaSessionConnection.getInstance();


        SongInfo s1 = new SongInfo();
        s1.setSongId("111");
        s1.setSongUrl("http://music.163.com/song/media/outer/url?id=317151.mp3&a=我");
        s1.setSongCover("https://www.qqkw.com/d/file/p/2018/04-21/c24fd86006670f964e63cb8f9c129fc6.jpg");
        s1.setSongName("心雨");
        s1.setArtist("贤哥");

        SongInfo s2 = new SongInfo();
        s2.setSongId("222");
        s2.setSongUrl("http://music.163.com/song/media/outer/url?id=281951.mp3");
        s2.setSongCover("https://n.sinaimg.cn/sinacn13/448/w1024h1024/20180504/7b5f-fzyqqiq8228305.jpg");
        s2.setSongName("我曾用心爱着你");
        s2.setArtist("潘美辰");

        SongInfo s3 = new SongInfo();
        s3.setSongId("333");
        s3.setSongUrl("http://music.163.com/song/media/outer/url?id=25906124.mp3");
        s3.setSongCover("http://cn.chinadaily.com.cn/img/attachement/jpg/site1/20180211/509a4c2df41d1bea45f73b.jpg");
        s3.setSongName("不要说话");
        s3.setArtist("陈奕迅");

        List<SongInfo> songInfos = new ArrayList<>();
        songInfos.add(s1);
        songInfos.add(s2);
        songInfos.add(s3);

        //播放
        findViewById(R.id.play).setOnClickListener(v -> MusicManager.getInstance().playMusic(songInfos, 0));
        //暂停
        findViewById(R.id.pause).setOnClickListener(v -> MusicManager.getInstance().pauseMusic());
        //恢复播放
        findViewById(R.id.resum).setOnClickListener(v -> MusicManager.getInstance().playMusic());
        //停止播放
        findViewById(R.id.stop).setOnClickListener(v -> MusicManager.getInstance().stopMusic());
        //下一首
        findViewById(R.id.pre).setOnClickListener(v -> MusicManager.getInstance().skipToPrevious());
        //上一首
        findViewById(R.id.next).setOnClickListener(v -> MusicManager.getInstance().skipToNext());
        //快进
        findViewById(R.id.fastForward).setOnClickListener(v -> MusicManager.getInstance().fastForward());
        //快退
        findViewById(R.id.rewind).setOnClickListener(v -> MusicManager.getInstance().rewind());
        //当前歌曲信息
        findViewById(R.id.currSong).setOnClickListener(v -> {
            SongInfo songInfo = MusicManager.getInstance().getNowPlayingSongInfo();
            if (songInfo == null) {
                Toast.makeText(MainActivity.this, "songInfo is null", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "curr SongInfo = " + songInfo.getSongId(), Toast.LENGTH_SHORT).show();
            }
        });
        //当前歌曲id
        findViewById(R.id.currSongId).setOnClickListener(v -> {
            String songId = MusicManager.getInstance().getNowPlayingSongId();
            if (TextUtils.isEmpty(songId)) {
                Toast.makeText(MainActivity.this, "songId is null", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "songId = " + songId, Toast.LENGTH_SHORT).show();
            }
        });
        //当前歌曲下标
        findViewById(R.id.currSongIndex).setOnClickListener(v -> {
            int index = MusicManager.getInstance().getNowPlayingIndex();
            Toast.makeText(MainActivity.this, "index = " + index, Toast.LENGTH_SHORT).show();
        });
        //通知栏喜欢按钮
        findViewById(R.id.sendFavorite).setOnClickListener(v -> {
            if (isFavorite) {
                MusicManager.getInstance().updateFavoriteUI(false);
                isFavorite = false;
            } else {
                MusicManager.getInstance().updateFavoriteUI(true);
                isFavorite = true;
            }
        });
        //通知栏歌词按钮
        findViewById(R.id.sendLyrics).setOnClickListener(v -> {
            if (isChecked) {
                MusicManager.getInstance().updateLyricsUI(false);
                isChecked = false;
            } else {
                MusicManager.getInstance().updateLyricsUI(true);
                isChecked = true;
            }
        });
        //缓存大小
        findViewById(R.id.cacheSize).setOnClickListener(v -> {
            String size = ExoDownload.getInstance().getCachedSize() + "";
            Toast.makeText(MainActivity.this, "大小：" + size, Toast.LENGTH_SHORT).show();
        });
        //设置是否随机播放
        findViewById(R.id.shuffleMode).setOnClickListener(v -> {
            int repeatMode = MusicManager.getInstance().getShuffleMode();
            if (repeatMode == PlaybackStateCompat.SHUFFLE_MODE_NONE) {
                Toast.makeText(MainActivity.this, "设置为随机播放", Toast.LENGTH_SHORT).show();
                MusicManager.getInstance().setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_ALL);
            } else {
                Toast.makeText(MainActivity.this, "设置为顺序播放", Toast.LENGTH_SHORT).show();
                MusicManager.getInstance().setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_NONE);
            }
        });
        //获取是否随机播放
        findViewById(R.id.isShuffleMode).setOnClickListener(v -> {
            int repeatMode = MusicManager.getInstance().getShuffleMode();
            Toast.makeText(MainActivity.this, repeatMode == PlaybackStateCompat.SHUFFLE_MODE_NONE ? "否" : "是", Toast.LENGTH_SHORT).show();
        });
        //设置播放模式
        findViewById(R.id.playMode).setOnClickListener(v -> {
            int repeatMode = MusicManager.getInstance().getRepeatMode();
            if (repeatMode == PlaybackStateCompat.REPEAT_MODE_NONE) {
                MusicManager.getInstance().setRepeatMode(PlaybackStateCompat.REPEAT_MODE_ONE);
                Toast.makeText(MainActivity.this, "设置为单曲循环", Toast.LENGTH_SHORT).show();
            } else if (repeatMode == PlaybackStateCompat.REPEAT_MODE_ONE) {
                MusicManager.getInstance().setRepeatMode(PlaybackStateCompat.REPEAT_MODE_ALL);
                Toast.makeText(MainActivity.this, "设置为列表循环", Toast.LENGTH_SHORT).show();
            } else if (repeatMode == PlaybackStateCompat.REPEAT_MODE_ALL) {
                MusicManager.getInstance().setRepeatMode(PlaybackStateCompat.REPEAT_MODE_NONE);
                Toast.makeText(MainActivity.this, "设置为顺序播放", Toast.LENGTH_SHORT).show();
            }
        });
        //获取播放模式
        findViewById(R.id.currPlayMode).setOnClickListener(v -> {
            int repeatMode = MusicManager.getInstance().getRepeatMode();
            if (repeatMode == PlaybackStateCompat.REPEAT_MODE_NONE) {
                Toast.makeText(MainActivity.this, "当前为顺序播放", Toast.LENGTH_SHORT).show();
            } else if (repeatMode == PlaybackStateCompat.REPEAT_MODE_ONE) {
                Toast.makeText(MainActivity.this, "当前为单曲循环", Toast.LENGTH_SHORT).show();
            } else if (repeatMode == PlaybackStateCompat.REPEAT_MODE_ALL) {
                Toast.makeText(MainActivity.this, "当前为列表循环", Toast.LENGTH_SHORT).show();
            }
        });
        //是否有下一首
        findViewById(R.id.hasNext).setOnClickListener(v -> {
            boolean hasNext = MusicManager.getInstance().isSkipToNextEnabled();
            Toast.makeText(MainActivity.this, hasNext ? "有" : "没", Toast.LENGTH_SHORT).show();
        });
        //是否有上一首
        findViewById(R.id.hasPre).setOnClickListener(v -> {
            boolean hasPre = MusicManager.getInstance().isSkipToPreviousEnabled();
            Toast.makeText(MainActivity.this, hasPre ? "有" : "没", Toast.LENGTH_SHORT).show();
        });
        //获取播放速度
        findViewById(R.id.playSpeed).setOnClickListener(v -> {
            float speed = MusicManager.getInstance().getPlaybackSpeed();
            Toast.makeText(MainActivity.this, "speed = " + speed, Toast.LENGTH_SHORT).show();
        });
        //音量加
        findViewById(R.id.addvolume).setOnClickListener(v -> {
            float volume = MusicManager.getInstance().getVolume();
            volume = volume + 0.1f;
            if (volume > 1) {
                volume = 1;
            }
            MusicManager.getInstance().setVolume(volume);
        });
        //音量减
        findViewById(R.id.jianvolume).setOnClickListener(v -> {
            float volume = MusicManager.getInstance().getVolume();
            volume = volume - 0.1f;
            if (volume < 0) {
                volume = 0;
            }
            MusicManager.getInstance().setVolume(volume);
        });
        //获取当前音量
        findViewById(R.id.getVolume).setOnClickListener(v -> {
            float volume = MusicManager.getInstance().getVolume();
            Toast.makeText(MainActivity.this, "volume = " + volume, Toast.LENGTH_SHORT).show();
        });
        //获取本地音频信息
        findViewById(R.id.localSong).setOnClickListener(v -> {
            List<SongInfo> list = MusicManager.getInstance().querySongInfoInLocal();
            Toast.makeText(MainActivity.this, "list.size = " + list.size(), Toast.LENGTH_SHORT).show();
        });
        //连接
        findViewById(R.id.connect).setOnClickListener(v -> {
            mMediaSessionConnection.connect();
            Toast.makeText(this, "连接", Toast.LENGTH_SHORT).show();
        });
        //断开连接
        findViewById(R.id.disconnect).setOnClickListener(v -> {
            mMediaSessionConnection.disconnect();
            Toast.makeText(this, "断开连接", Toast.LENGTH_SHORT).show();
        });
        //添加监听
        MusicManager.getInstance().addPlayerEventListener(this);
        //进度更新
        mTimerTask.setUpdateProgressTask(() -> {
            long position = MusicManager.getInstance().getPlayingPosition();
            long duration = MusicManager.getInstance().getDuration();
            long buffered = MusicManager.getInstance().getBufferedPosition();
            if (mSeekBar.getMax() != duration) {
                mSeekBar.setMax((int) duration);
            }
            mSeekBar.setProgress((int) position);
            mSeekBar.setSecondaryProgress((int) buffered);
            currTime.setText(formatMusicTime(position) + "/" + formatMusicTime(duration));
        });
        //进度条滑动
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                MusicManager.getInstance().seekTo(seekBar.getProgress());
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        //连接音频服务
        mMediaSessionConnection.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //断开音频服务
        mMediaSessionConnection.disconnect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //回收资源
        MusicManager.getInstance().removePlayerEventListener(this);
        mTimerTask.removeUpdateProgressTask();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onMusicSwitch(SongInfo songInfo) {
        if (songInfo == null) {
            return;
        }
        currInfo.setText("当前播放：" + songInfo.getSongName());
        LogUtil.i("= onMusicSwitch = " + songInfo.getSongName());
    }

    @Override
    public void onPlayerStart() {
        //开始更新进度条
        mTimerTask.startToUpdateProgress();
        LogUtil.i("= onPlayerStart = ");
    }

    @Override
    public void onPlayerPause() {
        //停止更新进度条
        mTimerTask.stopToUpdateProgress();
        LogUtil.i("= onPlayerPause = ");
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onPlayerStop() {
        //停止更新进度条
        mTimerTask.stopToUpdateProgress();
        mSeekBar.setProgress(0);
        currTime.setText("00:00");
        LogUtil.i("= onPlayerStop = ");
    }

    @Override
    public void onPlayCompletion(SongInfo songInfo) {
        //songInfo maybe null
        if (songInfo == null) {
            return;
        }
        //停止更新进度条
        mTimerTask.stopToUpdateProgress();
        LogUtil.i("= onPlayCompletion = " + songInfo.getSongName());
    }

    @Override
    public void onBuffering() {
        LogUtil.i("= onBuffering = ");
    }

    @Override
    public void onError(int errorCode, String errorMsg) {
        //停止更新进度条
        mTimerTask.stopToUpdateProgress();
        LogUtil.i("= onError = errorCode:" + errorCode + " errorMsg:" + errorMsg);
    }

    public static String formatMusicTime(long duration) {
        String time = "";
        long minute = duration / 60000;
        long seconds = duration % 60000;
        long second = Math.round((int) seconds / 1000);
        if (minute < 10) {
            time += "0";
        }
        time += minute + ":";
        if (second < 10) {
            time += "0";
        }
        time += second;
        return time;
    }

    public static class LogUtil {
        public static void i(String msg) {
            Log.i("LogUtil", msg);
        }
    }

}
