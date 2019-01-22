package com.lzx.musiclib;


import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.lzx.starrysky.MediaSessionConnection;
import com.lzx.starrysky.MusicService;
import com.lzx.starrysky.model.MusicProvider;
import com.lzx.starrysky.model.SongInfo;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {

    private MediaBrowserCompat mMediaBrowser;

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

        mMediaBrowser = new MediaBrowserCompat(this,
                new ComponentName(this, MusicService.class), mConnectionCallback, null);

        findViewById(R.id.subscribe).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMediaBrowser.subscribe("111", new MediaBrowserCompat.SubscriptionCallback() {
                    @Override
                    public void onChildrenLoaded(@NonNull String parentId, @NonNull List<MediaBrowserCompat.MediaItem> children) {
                        super.onChildrenLoaded(parentId, children);
                        Toast.makeText(MainActivity.this, "success", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        findViewById(R.id.unsubscribe).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaControllerCompat.getMediaController(MainActivity.this)
                        .getTransportControls().playFromMediaId("222", null);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mMediaBrowser.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        MediaControllerCompat controllerCompat = MediaControllerCompat.getMediaController(this);
        if (controllerCompat != null) {
            controllerCompat.unregisterCallback(mMediaControllerCallback);
        }
        mMediaBrowser.disconnect();
    }

    private final MediaBrowserCompat.ConnectionCallback mConnectionCallback =
            new MediaBrowserCompat.ConnectionCallback() {
                @Override
                public void onConnected() {
                    try {
                        connectToSession(mMediaBrowser);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            };

    private void connectToSession(MediaBrowserCompat mediaBrowser) throws RemoteException {
        MediaControllerCompat mediaController = new MediaControllerCompat(this, mediaBrowser.getSessionToken());
        MediaControllerCompat.setMediaController(this, mediaController);
        mediaController.registerCallback(mMediaControllerCallback);
    }

    private final MediaControllerCompat.Callback mMediaControllerCallback =
            new MediaControllerCompat.Callback() {
                @Override
                public void onPlaybackStateChanged(@NonNull PlaybackStateCompat state) {

                }

                @Override
                public void onMetadataChanged(MediaMetadataCompat metadata) {

                }
            };
}
