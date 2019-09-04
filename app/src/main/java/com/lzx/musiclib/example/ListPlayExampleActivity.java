package com.lzx.musiclib.example;

import android.arch.lifecycle.Observer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.lzx.musiclib.R;
import com.lzx.starrysky.StarrySky;
import com.lzx.starrysky.common.PlaybackStage;
import com.lzx.starrysky.provider.SongInfo;

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listplay_example);
        mRecyclerView = findViewById(R.id.recycle_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mListPlayAdapter = new ListPlayAdapter(this);
        mRecyclerView.setAdapter(mListPlayAdapter);
        getMusicList();

        StarrySky.with().playbackState().observe(this, playbackStage -> {
            if (playbackStage == null) {
                return;
            }
            switch (playbackStage.getStage()) {
                case PlaybackStage.NONE:
                    break;
                case PlaybackStage.START:
                    mListPlayAdapter.notifyDataSetChanged();
                    break;
                case PlaybackStage.PAUSE:
                    break;
                case PlaybackStage.STOP:
                    break;
                case PlaybackStage.COMPLETION:
                    break;
                case PlaybackStage.BUFFERING:
                    break;
                case PlaybackStage.ERROR:
                    break;
                default:
                    break;
            }
        });
    }


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
                    JSONObject jsonObject = null;
                    for (int i = 0; i < array.length(); i++) {
                        jsonObject = array.getJSONObject(i);
                        if (jsonObject != null) {
                            break;
                        }
                    }
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
                        Log.i("xian", "id = " + info.getSongId());
                        list.add(info);
                    }
                    runOnUiThread(() -> {
                        mListPlayAdapter.setSongInfos(list);
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
