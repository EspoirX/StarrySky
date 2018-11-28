package com.lzx.musiclib;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.lzx.musiclibrary.aidl.listener.OnPlayerEventListener;
import com.lzx.musiclibrary.aidl.model.SongInfo;
import com.lzx.musiclibrary.constans.PlayMode;
import com.lzx.musiclibrary.constans.State;
import com.lzx.musiclibrary.manager.MusicManager;
import com.lzx.musiclibrary.manager.TimerTaskManager;
import com.lzx.musiclibrary.utils.LogUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnPlayerEventListener {


    Button play_mode, btn_speed;
    TextView curr_info;
    SeekBar mSeekBar;
    TimerTaskManager manager;
    TextView mCurrProgress;
    int viewWidth = 0;
    int seekBarWidth = 0;
    float speed = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        manager = new TimerTaskManager();

        List<String> strings = new ArrayList<>();
        strings.add("http://music.163.com/song/media/outer/url?id=317151.mp3");
        strings.add("http://music.163.com/song/media/outer/url?id=281951.mp3");
        strings.add("http://music.163.com/song/media/outer/url?id=25906124.mp3");
        final List<SongInfo> list = new ArrayList<>();
        for (int i = 0; i < strings.size(); i++) {
            SongInfo songInfo = new SongInfo();
            songInfo.setSongId(String.valueOf(i));
            songInfo.setSongUrl(strings.get(i));
            if (i == 0) {
                songInfo.setSongName("心雨");
            } else if (i == 1) {
                songInfo.setSongName("我曾用心爱着你");
            } else if (i == 2) {
                songInfo.setSongName("不要说话");
            }
            list.add(songInfo);
        }

        play_mode = findViewById(R.id.play_mode);
        curr_info = findViewById(R.id.curr_info);
        mSeekBar = findViewById(R.id.seekBar);
        btn_speed = findViewById(R.id.btn_speed);
        mCurrProgress = findViewById(R.id.curr_progress);
        MusicManager.get().addPlayerEventListener(this);
        findViewById(R.id.play).setOnClickListener(v -> {
            MusicManager.get().playMusic(list, 0);
            curr_info.setText(getCurrInfo());
        });
        findViewById(R.id.pause).setOnClickListener(v -> {
            MusicManager.get().pauseMusic();
            curr_info.setText(getCurrInfo());
        });
        findViewById(R.id.resume).setOnClickListener(v -> {
            MusicManager.get().resumeMusic();
            curr_info.setText(getCurrInfo());
        });
        findViewById(R.id.pre).setOnClickListener(v -> {
            if (MusicManager.get().hasPre()) {
                MusicManager.get().playPre();
                curr_info.setText(getCurrInfo());
            } else {
                Toast.makeText(MainActivity.this, "没有上一首", Toast.LENGTH_SHORT).show();
            }
        });
        findViewById(R.id.next).setOnClickListener(v -> {
            if (MusicManager.get().hasNext()) {
                MusicManager.get().playNext();
                curr_info.setText(getCurrInfo());
            } else {
                Toast.makeText(MainActivity.this, "没有下一首", Toast.LENGTH_SHORT).show();
            }
        });
        findViewById(R.id.close_server).setOnClickListener(v -> TestApplication.getMusicLibrary().stopService());
        play_mode.setText(getMode());
        play_mode.setOnClickListener(v -> {
            int mode = MusicManager.get().getPlayMode();
            if (mode == PlayMode.PLAY_IN_FLASHBACK) {
                MusicManager.get().setPlayMode(PlayMode.PLAY_IN_LIST_LOOP);
            } else if (mode == PlayMode.PLAY_IN_LIST_LOOP) {
                MusicManager.get().setPlayMode(PlayMode.PLAY_IN_ORDER);
            } else if (mode == PlayMode.PLAY_IN_ORDER) {
                MusicManager.get().setPlayMode(PlayMode.PLAY_IN_RANDOM);
            } else if (mode == PlayMode.PLAY_IN_RANDOM) {
                MusicManager.get().setPlayMode(PlayMode.PLAY_IN_SINGLE_LOOP);
            } else if (mode == PlayMode.PLAY_IN_SINGLE_LOOP) {
                MusicManager.get().setPlayMode(PlayMode.PLAY_IN_FLASHBACK);
            }
            play_mode.setText(getMode());
            curr_info.setText(getCurrInfo());
        });
        findViewById(R.id.reset).setOnClickListener(v -> {
            MusicManager.get().reset();
            TestApplication.getMusicLibrary().stopService();
        });
        curr_info.setText(getCurrInfo());
        manager.setUpdateProgressTask(() -> {
            long progress = MusicManager.get().getProgress();
            mSeekBar.setProgress((int) progress);
        });

        mCurrProgress.post(() -> {
            viewWidth = mCurrProgress.getWidth();
            seekBarWidth = mSeekBar.getWidth();
        });


        btn_speed.setOnClickListener(v -> {
            speed += 0.5;
            if (speed >= 3) {
                speed = 1;
            }
            MusicManager.get().setPlaybackParameters(speed, 1);
            btn_speed.setText("变速 当前" + speed + "倍");
        });

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sDateFormat = new SimpleDateFormat("mm:ss");
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int halfWidth = viewWidth / 2;
                int max = mSeekBar.getMax();
                if (max == 0 || halfWidth == 0 || progress == 0) {
                    return;
                }
                int translationX = (seekBarWidth - viewWidth) * progress / max;
                mCurrProgress.setTranslationX(translationX);
                mCurrProgress.setText(sDateFormat.format(progress) + "/" + sDateFormat.format(max));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                MusicManager.get().seekTo(seekBar.getProgress());
            }
        });
    }

    private String getCurrInfo() {
        StringBuilder builder = new StringBuilder();
        List<SongInfo> songInfos = MusicManager.get().getPlayList();
        if (songInfos == null) {
            return "";
        }
        for (int i = 0; i < songInfos.size(); i++) {
            builder
                    .append(String.valueOf(i))
                    .append(" ")
                    .append(songInfos.get(i).getSongName())
                    .append("\n");
        }
        SongInfo currInfo = MusicManager.get().getCurrPlayingMusic();
        String name = currInfo == null ? "没有" : currInfo.getSongName();
        return " 当前播放：" + name + "\n 播放状态：" + getStatus() + "\n 下标：" + MusicManager.get().getCurrPlayingIndex() + " \n\n" +
                "当前播放列表：\n\n" + builder.toString();
    }

    public String getStatus() {
        int status = MusicManager.get().getStatus();
        if (status == State.STATE_IDLE) {
            return "空闲";
        } else if (status == State.STATE_PLAYING) {
            return "播放中";
        } else if (status == State.STATE_PAUSED) {
            return "暂停";
        } else if (status == State.STATE_ERROR) {
            return "错误";
        } else if (status == State.STATE_STOP) {
            return "停止";
        } else if (status == State.STATE_ASYNC_LOADING) {
            return "加载中";
        }
        return "其他";
    }

    private String getMode() {
        int mode = MusicManager.get().getPlayMode();
        if (mode == PlayMode.PLAY_IN_FLASHBACK) {
            return "倒序播放";
        } else if (mode == PlayMode.PLAY_IN_LIST_LOOP) {
            return "列表循环";
        } else if (mode == PlayMode.PLAY_IN_ORDER) {
            return "顺序播放";
        } else if (mode == PlayMode.PLAY_IN_RANDOM) {
            return "随机播放";
        } else if (mode == PlayMode.PLAY_IN_SINGLE_LOOP) {
            return "单曲循环";
        }
        return "不知道什么模式";
    }

    @Override
    public void onMusicSwitch(SongInfo music) {
        curr_info.setText(getCurrInfo());
        manager.stopSeekBarUpdate();
        mSeekBar.setMax((int) music.getDuration());
    }

    @Override
    public void onPlayerStart() {
        curr_info.setText(getCurrInfo());
        manager.scheduleSeekBarUpdate();
    }

    @Override
    public void onPlayerPause() {
        curr_info.setText(getCurrInfo());
        manager.stopSeekBarUpdate();
    }

    @Override
    public void onPlayCompletion(SongInfo songInfo) {
        curr_info.setText(getCurrInfo());
        manager.stopSeekBarUpdate();
        mSeekBar.setProgress(0);
    }

    @Override
    public void onPlayerStop() {
        curr_info.setText(getCurrInfo());
        manager.stopSeekBarUpdate();
    }

    @Override
    public void onError(String errorMsg) {
        curr_info.setText(getCurrInfo());
        manager.stopSeekBarUpdate();
    }

    @Override
    public void onAsyncLoading(boolean isFinishLoading) {
        curr_info.setText(getCurrInfo());
        if (isFinishLoading) {
            mSeekBar.setMax(MusicManager.get().getDuration());
        }
    }
}
