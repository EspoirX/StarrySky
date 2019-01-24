package com.lzx.musiclib;


import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.lzx.starrysky.manager.MediaSessionConnection;
import com.lzx.starrysky.manager.MusicManager;
import com.lzx.starrysky.manager.OnPlayerEventListener;
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
        s1.setSongCover("https://www.qqkw.com/d/file/p/2018/04-21/c24fd86006670f964e63cb8f9c129fc6.jpg");
        s1.setSongName("心雨");
        s1.setArtist("贤哥");

        SongInfo s2 = new SongInfo();
        s2.setSongId("222");
        s2.setSongUrl("http://music.163.com/song/media/outer/url?id=281951.mp3");
        s2.setSongCover("https://n.sinaimg.cn/sinacn13/448/w1024h1024/20180504/7b5f-fzyqqiq8228305.jpg");
        s2.setSongName("我曾用心爱着你");
        s2.setArtist("潘美辰");

        SongInfo s3 = new SongInfo();
        s3.setSongId("333");
        s3.setSongUrl("http://music.163.com/song/media/outer/url?id=25906124.mp3");
        s3.setSongCover("http://cn.chinadaily.com.cn/img/attachement/jpg/site1/20180211/509a4c2df41d1bea45f73b.jpg");
        s3.setSongName("不要说话");
        s3.setArtist("陈奕迅");

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
        MusicManager.getInstance().addPlayerEventListener(new OnPlayerEventListener() {
            @Override
            public void onMusicSwitch(SongInfo songInfo) {
                //  Log.i("xian", "songInfo = " + songInfo.getSongId());
            }

            @Override
            public void onPlayerStart() {
                // Log.i("xian", "= onPlayerStart = ");
            }

            @Override
            public void onPlayerPause() {
                // Log.i("xian", "= onPlayerPause = ");
            }

            @Override
            public void onPlayCompletion() {
                //  Log.i("xian", "= onPlayCompletion = ");
            }

            @Override
            public void onBuffering() {
                // Log.i("xian", "= onBuffering = ");
            }

            @Override
            public void onPlayerStop() {
                //  Log.i("xian", "= onPlayerStop = ");
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                // Log.i("xian", "= onError = errorCode:" + errorCode + " errorMsg:" + errorMsg);
            }
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
