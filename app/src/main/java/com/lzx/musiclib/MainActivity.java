package com.lzx.musiclib;


import android.content.ComponentName;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.util.Log;
import android.view.View;

import com.lzx.starrysky.MediaSessionConnection;
import com.lzx.starrysky.MusicService;
import com.lzx.starrysky.model.MusicProvider;
import com.lzx.starrysky.model.SongInfo;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {


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

        MusicProvider.getInstance().setSongInfos(songInfos);

        MediaSessionConnection connection = new MediaSessionConnection(this, new ComponentName(this, MusicService.class));

        findViewById(R.id.subscribe).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                connection.subscribe("111", new MediaBrowserCompat.SubscriptionCallback() {
//                    @Override
//                    public void onChildrenLoaded(@NonNull String parentId, @NonNull List<MediaBrowserCompat.MediaItem> children) {
//                        super.onChildrenLoaded(parentId, children);
//                        Log.i("LogUtil", "parentId=" + parentId + " children=" + children.get(0).getDescription().getMediaUri());
//                    }
//                });
                connection.getTransportControls().playFromMediaId("111",null);
            }
        });
        findViewById(R.id.unsubscribe).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connection.unsubscribe("111", new MediaBrowserCompat.SubscriptionCallback() {
                    @Override
                    public void onChildrenLoaded(@NonNull String parentId, @NonNull List<MediaBrowserCompat.MediaItem> children) {
                        super.onChildrenLoaded(parentId, children);
                        Log.i("LogUtil", "parentId=" + parentId + " children=" + children.size());
                    }
                });
            }
        });
    }

}
