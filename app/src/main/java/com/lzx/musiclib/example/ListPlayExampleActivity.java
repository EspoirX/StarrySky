package com.lzx.musiclib.example;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;
import android.widget.Toast;

import com.lzx.musiclib.R;
import com.lzx.starrysky.StarrySky;
import com.lzx.starrysky.common.PlaybackStage;
import com.lzx.starrysky.provider.SongInfo;
import com.lzx.starrysky.utils.TimerTaskManager;

public class ListPlayExampleActivity extends AppCompatActivity {

    private ListPlayAdapter mListPlayAdapter;
    private TimerTaskManager mTimerTask;
    private TextView playPause;
    private TextView playMode;
    private boolean isSettingShuffleMode = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listplay_example);
        playPause = findViewById(R.id.play_pause);
        playMode = findViewById(R.id.play_mode);
        RecyclerView recyclerView = findViewById(R.id.recycle_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mListPlayAdapter = new ListPlayAdapter(this);
        recyclerView.setAdapter(mListPlayAdapter);
        mTimerTask = new TimerTaskManager();


        //状态监听
        StarrySky.with().playbackState().observe(this, playbackStage -> {
            if (playbackStage == null) {
                return;
            }
            switch (playbackStage.getStage()) {
                case PlaybackStage.NONE:
                    playPause.setText("播放/暂停");
                    break;
                case PlaybackStage.START:
                    mListPlayAdapter.notifyDataSetChanged();
                    mTimerTask.startToUpdateProgress();
                    playPause.setText("暂停");
                    break;
                case PlaybackStage.PAUSE:
                    mTimerTask.stopToUpdateProgress();
                    playPause.setText("播放");
                    mListPlayAdapter.notifyDataSetChanged();
                    break;
                case PlaybackStage.STOP:
                    mTimerTask.stopToUpdateProgress();
                    playPause.setText("播放");
                    break;
                case PlaybackStage.COMPLETION:
                    mTimerTask.stopToUpdateProgress();
                    playPause.setText("播放/暂停");
                    break;
                case PlaybackStage.BUFFERING:
                    playPause.setText("缓存中");
                    break;
                case PlaybackStage.ERROR:
                    mTimerTask.stopToUpdateProgress();
                    playPause.setText("播放/暂停");
                    Toast.makeText(this, playbackStage.getErrorMessage(), Toast.LENGTH_SHORT)
                            .show();
                    break;
                default:
                    break;
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
        //进度监听
        mTimerTask.setUpdateProgressTask(() -> {
            SongInfo songInfo = StarrySky.with().getNowPlayingSongInfo();
            int position = mListPlayAdapter.getSongInfos().indexOf(songInfo);
            mListPlayAdapter.updateItemProgress(position);
        });
        //上一首
        findViewById(R.id.previous).setOnClickListener(v -> {
            StarrySky.with().skipToPrevious();
        });
        //播放/暂停
        playPause.setOnClickListener(v -> {
            String text = playPause.getText().toString();
            if (text.equals("播放/暂停")) {
                StarrySky.with().playMusic(mListPlayAdapter.getSongInfos(), 0);
            } else if (text.equals("暂停")) {
                StarrySky.with().pauseMusic();
            } else if (text.equals("播放")) {
                StarrySky.with().playMusic();
            }
        });
        //停止
        findViewById(R.id.stop).setOnClickListener(v -> {
            StarrySky.with().stopMusic();
        });
        //下一首
        findViewById(R.id.next).setOnClickListener(v -> {
            StarrySky.with().skipToNext();
        });


        //获取数据
        MusicRequest musicRequest = new MusicRequest();
        musicRequest.getMusicList(this, list -> {
            runOnUiThread(() -> {
                StarrySky.with().updatePlayList(list);
                mListPlayAdapter.setSongInfos(list);
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTimerTask.removeUpdateProgressTask();
    }
}
