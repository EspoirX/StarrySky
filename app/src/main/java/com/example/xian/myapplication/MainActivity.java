package com.example.xian.myapplication;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.example.musiclib.manager.MusicManager;
import com.example.musiclib.model.MusicInfo;
import com.example.musiclib.service.OnPlayerEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MusicAdapter musicAdapter;
    private MusicManager.ServiceToken mServiceToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.recycle_view);

        mServiceToken = MusicManager.get().bindToService(this, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                Log.i("xian", "音乐服务链接成功...");
                List<String> list = Arrays.asList(getResources().getStringArray(R.array.music_list));
                List<MusicInfo> musicInfos = new ArrayList<>();
                for (String string : list) {
                    MusicInfo info = new MusicInfo();
                    info.setMusicUrl(string);
                    musicInfos.add(info);
                }
                musicAdapter.setMusicInfos(musicInfos);
                MusicManager.get().setMusicList(musicInfos);
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                Log.i("xian", "音乐服务断开链接...");
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        musicAdapter = new MusicAdapter();
        recyclerView.setAdapter(musicAdapter);

        MusicManager.get().addObservable(musicAdapter);
        MusicManager.get().setOnPlayerEventListener(new OnPlayerEventListener() {
            @Override
            public void onMusicChange(MusicInfo music) {
                Log.i("xian", "== onMusicChange ==");
            }

            @Override
            public void onPlayerStart() {
                Log.i("xian", "== onPlayerStart ==");
            }

            @Override
            public void onPlayerPause() {
                Log.i("xian", "== onPlayerPause ==");
            }

            @Override
            public void onProgress(int progress) {
                Log.i("xian", "== onProgress == " + progress);
            }

            @Override
            public void onBufferingUpdate(int percent) {

            }

            @Override
            public void onTimer(long remain) {
                Log.i("xian", "== onTimer == " + remain);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MusicManager.get().unbindService(mServiceToken);
    }
}
