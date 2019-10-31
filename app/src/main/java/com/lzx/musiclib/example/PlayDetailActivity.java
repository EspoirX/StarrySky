package com.lzx.musiclib.example;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.lzx.musiclib.R;
import com.lzx.starrysky.StarrySky;
import com.lzx.starrysky.common.PlaybackStage;
import com.lzx.starrysky.provider.SongInfo;
import com.lzx.starrysky.utils.TimerTaskManager;

public class PlayDetailActivity extends AppCompatActivity {

    private TextView title;
    private TextView progress_text;
    private TextView time_text;
    private ImageView cover;
    private SeekBar mSeekBar;
    private RecyclerView mRecyclerView;
    private ListPlayAdapter mListPlayAdapter;
    private TimerTaskManager mTimerTask;
    private TextView playMode;
    private boolean isSettingShuffleMode = false;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_detail);
        title = findViewById(R.id.title);
        cover = findViewById(R.id.cover);
        mSeekBar = findViewById(R.id.seek_bar);
        progress_text = findViewById(R.id.progress_text);
        time_text = findViewById(R.id.time_text);
        mRecyclerView = findViewById(R.id.recycle_view);
        playMode = findViewById(R.id.play_mode);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mListPlayAdapter = new ListPlayAdapter(this);
        mRecyclerView.setAdapter(mListPlayAdapter);

        mTimerTask = new TimerTaskManager();

        //状态监听
        StarrySky.with().playbackState().observe(this, playbackStage -> {
            if (playbackStage == null) {
                return;
            }
            updateUIInfo(playbackStage);

            switch (playbackStage.getStage()) {
                case PlaybackStage.NONE:
                    title.setText("播放详情页示例");
                    break;
                case PlaybackStage.START:
                    mListPlayAdapter.notifyDataSetChanged();
                    mTimerTask.startToUpdateProgress();
                    break;
                case PlaybackStage.PAUSE:
                    mTimerTask.stopToUpdateProgress();
                    mListPlayAdapter.notifyDataSetChanged();
                    break;
                case PlaybackStage.STOP:
                    mTimerTask.stopToUpdateProgress();
                    break;
                case PlaybackStage.COMPLETION:
                    mTimerTask.stopToUpdateProgress();
                    mSeekBar.setProgress(0);
                    progress_text.setText("00:00");
                    break;
                case PlaybackStage.BUFFERING:
                    break;
                case PlaybackStage.ERROR:
                    mTimerTask.stopToUpdateProgress();
                    Toast.makeText(this, playbackStage.getErrorMessage(), Toast.LENGTH_SHORT)
                            .show();
                    break;
                default:
                    break;
            }
        });

        //进度更新
        mTimerTask.setUpdateProgressTask(() -> {
            long position = StarrySky.with().getPlayingPosition();
            long duration = StarrySky.with().getDuration();
            long buffered = StarrySky.with().getBufferedPosition();
            if (mSeekBar.getMax() != duration) {
                mSeekBar.setMax((int) duration);
            }
            Log.i("PlayDetailActivity", "duration = " + duration);
            mSeekBar.setProgress((int) position);
            mSeekBar.setSecondaryProgress((int) buffered);
            progress_text.setText(ListPlayAdapter.formatMusicTime(position) + "/" +
                    ListPlayAdapter.formatMusicTime(duration));
            time_text.setText(ListPlayAdapter.formatMusicTime(duration));
            mListPlayAdapter.notifyDataSetChanged();
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
                StarrySky.with().seekTo(seekBar.getProgress());
            }
        });

        playMode.setOnClickListener(v -> {
            int repeatMode = StarrySky.with().getRepeatMode();
            int shuffleMode = StarrySky.with().getShuffleMode();
            if (repeatMode == PlaybackStateCompat.REPEAT_MODE_NONE) {
                StarrySky.with().setRepeatMode(PlaybackStateCompat.REPEAT_MODE_ONE);
                playMode.setText("单曲循环"); //当前是顺序播放，设置为单曲循环
            } else if (repeatMode == PlaybackStateCompat.REPEAT_MODE_ONE) {
                StarrySky.with().setRepeatMode(PlaybackStateCompat.REPEAT_MODE_ALL);
                playMode.setText("列表循环"); //当前是单曲循环，设置为列表循环
            } else if (repeatMode == PlaybackStateCompat.REPEAT_MODE_ALL && !isSettingShuffleMode) {
                playMode.setText("随机播放"); //当前是列表循环，设置为随机播放
                StarrySky.with().setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_ALL);
                isSettingShuffleMode = true;
            } else if (shuffleMode == PlaybackStateCompat.SHUFFLE_MODE_ALL) {
                playMode.setText("顺序播放");  //当前是随机播放，设置为顺序播放
                StarrySky.with().setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_NONE);
                StarrySky.with().setRepeatMode(PlaybackStateCompat.REPEAT_MODE_NONE);
                isSettingShuffleMode = false;
            }
        });

        //获取数据
        MusicRequest musicRequest = new MusicRequest();
        musicRequest.getMusicList(this, list -> {
            runOnUiThread(() -> {
                mListPlayAdapter.setSongInfos(list);
                StarrySky.with().updatePlayList(list);
            });
        });
    }

    private void updateUIInfo(PlaybackStage playbackStage) {
        SongInfo songInfo = playbackStage.getSongInfo();
        if (songInfo != null) {
            title.setText(songInfo.getSongName());
            Glide.with(this).load(songInfo.getSongCover()).into(cover);
        } else {
            title.setText("播放详情页示例");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTimerTask.removeUpdateProgressTask();
    }
}
