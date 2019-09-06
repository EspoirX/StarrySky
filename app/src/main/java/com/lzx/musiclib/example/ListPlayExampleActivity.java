package com.lzx.musiclib.example;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.lzx.musiclib.R;
import com.lzx.starrysky.StarrySky;
import com.lzx.starrysky.common.PlaybackStage;
import com.lzx.starrysky.provider.SongInfo;
import com.lzx.starrysky.utils.TimerTaskManager;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ListPlayExampleActivity extends AppCompatActivity {
    OkHttpClient client = new OkHttpClient();
    private ListPlayAdapter mListPlayAdapter;
    private RecyclerView mRecyclerView;
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
        mRecyclerView = findViewById(R.id.recycle_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mListPlayAdapter = new ListPlayAdapter(this);
        mRecyclerView.setAdapter(mListPlayAdapter);
        mTimerTask = new TimerTaskManager();
        //获取数据
        getMusicList();

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

    }


    /**
     * 获取数据
     */
    private void getMusicList() {
        Request request = new Request.Builder()
                .url("https://music.163.com/api/playlist/highquality/list?limit=50")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Toast.makeText(ListPlayExampleActivity.this, "网易云接口请求失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try {
                    JSONObject object = new JSONObject(response.body().string());
                    JSONArray array = object.getJSONArray("playlists");
                    JSONObject jsonObject = array.getJSONObject(1);
                    if (jsonObject == null) {
                        return;
                    }
                    String playlistId = jsonObject.getString("id");
                    getPlayList(playlistId);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void getPlayList(String playlistId) {
        Request request = new Request.Builder()
                .url("https://music.163.com/api/playlist/detail?id=" + playlistId)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Toast.makeText(ListPlayExampleActivity.this, "网易云接口请求失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string()).getJSONObject("result");
                    JSONObject creator = jsonObject.getJSONObject("creator");
                    JSONArray tracks = jsonObject.getJSONArray("tracks");
                    List<SongInfo> list = new ArrayList<>();
                    for (int i = 0; i < tracks.length(); i++) {
                        JSONObject object = tracks.getJSONObject(i);
                        SongInfo info = new SongInfo();
                        info.setAlbumName(object.optString("name"));
                        info.setAlbumArtist(object.optString("nickname"));
                        info.setSongCover(object.optJSONObject("album").optString("picUrl"));
                        info.setSongId(object.optString("id"));
                        info.setSongName(object.optString("name"));
                        info.setDuration(object.optLong("duration"));
                        info.setSongUrl("http://music.163.com/song/media/outer/url?id=" + info.getSongId() + ".mp3");
                        list.add(info);
                    }
                    runOnUiThread(() -> {
                        Log.i("xian", "list = " + list.size());
                        mListPlayAdapter.setSongInfos(list);
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTimerTask.removeUpdateProgressTask();
    }
}
