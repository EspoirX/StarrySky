package com.lzx.musiclib;


import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.lzx.starrysky.MediaSessionConnection;
import com.lzx.starrysky.MusicManager;
import com.lzx.starrysky.model.SongInfo;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {

    //    private MediaBrowserCompat mMediaBrowser;
    private MediaSessionConnection mMediaSessionConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SongInfo s1 = new SongInfo();
        s1.setSongId("111");
        s1.setSongUrl("http://music.163.com/song/media/outer/url?id=317151.mp3");

        SongInfo s2 = new SongInfo();
        s2.setSongId("222");
        s2.setSongUrl("http://music.163.com/song/media/outer/url?id=281951.mp3");

        SongInfo s3 = new SongInfo();
        s3.setSongId("333");
        s3.setSongUrl("http://music.163.com/song/media/outer/url?id=25906124.mp3");

        List<SongInfo> songInfos = new ArrayList<>();
        songInfos.add(s1);
        songInfos.add(s2);
        songInfos.add(s3);

        mMediaSessionConnection = MediaSessionConnection.getInstance(this);

        findViewById(R.id.play).setOnClickListener(v -> MusicManager.getInstance().playMusic(songInfos, 0));
        findViewById(R.id.pause).setOnClickListener(v -> MusicManager.getInstance().pauseMusic());
        findViewById(R.id.resum).setOnClickListener(v -> MusicManager.getInstance().playMusic());
        findViewById(R.id.stop).setOnClickListener(v -> MusicManager.getInstance().stopMusic());
        findViewById(R.id.pre).setOnClickListener(v -> MusicManager.getInstance().skipToPrevious());
        findViewById(R.id.next).setOnClickListener(v -> MusicManager.getInstance().skipToNext());
        findViewById(R.id.fastForward).setOnClickListener(v -> MusicManager.getInstance().fastForward());
        findViewById(R.id.rewind).setOnClickListener(v -> MusicManager.getInstance().rewind());
        findViewById(R.id.currSong).setOnClickListener(v -> {
            SongInfo songInfo = MusicManager.getInstance().getNowPlayingSongInfo();
            if (songInfo == null) {
                Toast.makeText(MainActivity.this, "songInfo is null", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "curr SongInfo = " + songInfo.getSongId(), Toast.LENGTH_SHORT).show();
            }
        });
        findViewById(R.id.currSongId).setOnClickListener(v -> {
            String songId = MusicManager.getInstance().getNowPlayingSongId();
            if (TextUtils.isEmpty(songId)) {
                Toast.makeText(MainActivity.this, "songId is null", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "songId = " + songId, Toast.LENGTH_SHORT).show();
            }
        });
        findViewById(R.id.currSongIndex).setOnClickListener(v -> {
            int index = MusicManager.getInstance().getNowPlayingIndex();
            Toast.makeText(MainActivity.this, "index = " + index, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mMediaSessionConnection.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMediaSessionConnection.disconnect();
    }
}
