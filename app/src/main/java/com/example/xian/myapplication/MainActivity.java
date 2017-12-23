package com.example.xian.myapplication;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.lzx.musiclib.manager.MusicManager;
import com.lzx.musiclib.model.MusicInfo;
import com.lzx.musiclib.service.MusicPlayService;
import com.lzx.musiclib.service.OnPlayerEventListener;
import com.lzx.musiclib.service.ServiceConnectionCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnPlayerEventListener, ServiceConnectionCallback {

    private RecyclerView recyclerView;
    private MusicAdapter musicAdapter;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.recycle_view);

        MusicManager.get().bindToService(this,this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        musicAdapter = new MusicAdapter(this);
        recyclerView.setAdapter(musicAdapter);
        MusicManager.get().addObservable(musicAdapter);
    }

    private void initData() {
        List<String> musicList = Arrays.asList(getResources().getStringArray(R.array.music_list));
        List<String> coverList = Arrays.asList(getResources().getStringArray(R.array.cover_list));
        List<MusicInfo> musicInfos = new ArrayList<>();
        for (int i = 0; i < musicList.size(); i++) {
            MusicInfo musicInfo = new MusicInfo();
            musicInfo.setMusicCover(coverList.get(i));
            musicInfo.setMusicUrl(musicList.get(i));
            musicInfos.add(musicInfo);
        }
        musicAdapter.setMusicInfos(musicInfos);
        MusicManager.get().setMusicList(musicInfos);
        MusicManager.get().addOnPlayerEventListener(this);
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
        Log.i(TAG,"------onMusicChange------");
    }

    @Override
    public void onPlayerStart() {
        Log.i(TAG,"------onPlayerStart------");
    }

    @Override
    public void onPlayerPause() {
        Log.i(TAG,"------onPlayerPause------");
    }

    @Override
    public void onPlayCompletion() {
        Log.i(TAG,"------onPlayCompletion------");
    }

    @Override
    public void onProgress(int progress, int duration) {
        Log.i(TAG,"------onProgress------ "+progress);
    }

    @Override
    public void onBufferingUpdate(int percent) {
        Log.i(TAG,"------onBufferingUpdate------ "+percent);
    }

    @Override
    public void onTimer() {
        Log.i(TAG,"------onTimer------");
    }

    @Override
    public void onError(int what, int extra) {
        Log.i(TAG,"------onMusicChange------");
    }


}
