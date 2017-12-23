package com.example.xian.myapplication;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.lzx.musiclib.manager.MusicManager;
import com.lzx.musiclib.model.MusicInfo;
import com.lzx.musiclib.service.OnPlayerEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnPlayerEventListener {

    private RecyclerView recyclerView;
    private MusicAdapter musicAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.recycle_view);

        MusicManager.get().bindToService(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        musicAdapter = new MusicAdapter();
        recyclerView.setAdapter(musicAdapter);
        MusicManager.get().addObservable(musicAdapter);
        initData();
    }

    private void initData() {
        List<String> list = Arrays.asList(getResources().getStringArray(R.array.music_list));
        List<MusicInfo> musicInfos = new ArrayList<>();
        for (String string : list) {
            MusicInfo info = new MusicInfo();
            info.setMusicUrl(string);
            musicInfos.add(info);
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
    public void onMusicChange(MusicInfo music) {

    }

    @Override
    public void onPlayerStart() {

    }

    @Override
    public void onPlayerPause() {

    }

    @Override
    public void onPlayCompletion() {

    }

    @Override
    public void onProgress(int progress, int duration) {

    }

    @Override
    public void onBufferingUpdate(int percent) {

    }

    @Override
    public void onTimer() {

    }

    @Override
    public void onError(int what, int extra) {

    }
}
