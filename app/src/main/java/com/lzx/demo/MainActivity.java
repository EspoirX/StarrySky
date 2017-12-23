package com.lzx.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;


import com.lzx.musiclib.manager.MusicManager;
import com.lzx.musiclib.model.MusicInfo;
import com.lzx.musiclib.service.MusicPlayService;
import com.lzx.musiclib.service.OnPlayerEventListener;
import com.lzx.musiclib.service.ServiceConnectionCallback;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

public class MainActivity extends AppCompatActivity implements OnPlayerEventListener, ServiceConnectionCallback {

    private RecyclerView recyclerView;
    private MusicAdapter musicAdapter;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.recycle_view);

        MusicManager.get().bindToService(this, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        musicAdapter = new MusicAdapter(this);
        recyclerView.setAdapter(musicAdapter);
        MusicManager.get().addObservable(musicAdapter);
    }

    private void initData() {
        Map<String, String> map = new HashMap<>();
        map.put("showapi_appid", "22640");
        map.put("showapi_sign", "0676cf5617eb46f1a6da7bcf7853f423");
        map.put("topid", "4");
        OkHttpUtils.get().url("http://route.showapi.com/213-4").params(map).build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                Toast.makeText(MainActivity.this, "网络请求失败了，请检查你的网络或者自己换一个音乐链接吧。", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(String response, int id) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray array = jsonObject.getJSONObject("showapi_res_body").getJSONObject("pagebean").getJSONArray("songlist");
                    List<MusicInfo> musicInfos = new ArrayList<>();
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject object = array.getJSONObject(i);
                        MusicInfo musicInfo = new MusicInfo();
                        musicInfo.setMusicId(object.getString("songid"));
                        musicInfo.setMusicCover(object.getString("albumpic_big"));
                        musicInfo.setMusicUrl(object.getString("url"));
                        musicInfo.setMusicTitle(object.getString("songname"));
                        musicInfo.setAlbumNickname(object.getString("singername"));
                        musicInfos.add(musicInfo);
                    }
                    musicAdapter.setMusicInfos(musicInfos);
                    MusicManager.get().addOnPlayerEventListener(MainActivity.this);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MusicManager.get().removePlayerEventListener(this);
        MusicManager.get().unbindService(this);
    }

    @Override
    public void onServiceConnected(MusicPlayService musicPlayService) {
        initData();
    }

    @Override
    public void onServiceDisconnected() {

    }

    @Override
    public void onMusicChange(MusicInfo music) {
        Log.i(TAG, "------onMusicChange------");
    }

    @Override
    public void onPlayerStart() {
        Log.i(TAG, "------onPlayerStart------");
    }

    @Override
    public void onPlayerPause() {
        Log.i(TAG, "------onPlayerPause------");
    }

    @Override
    public void onPlayCompletion() {
        Log.i(TAG, "------onPlayCompletion------");
    }

    @Override
    public void onProgress(int progress, int duration) {
        Log.i(TAG, "------onProgress------ " + progress);
    }

    @Override
    public void onBufferingUpdate(int percent) {
        Log.i(TAG, "------onBufferingUpdate------ " + percent);
    }

    @Override
    public void onTimer() {
        Log.i(TAG, "------onTimer------");
    }

    @Override
    public void onError(int what, int extra) {
        Log.i(TAG, "------onMusicChange------");
    }


}
