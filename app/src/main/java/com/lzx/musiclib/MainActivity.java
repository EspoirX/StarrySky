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

    public static int getThemeColor(Context context, int attribute, int defaultColor) {
        int themeColor = 0;
        String packageName = context.getPackageName();
        try {
            Context packageContext = context.createPackageContext(packageName, 0);
            ApplicationInfo applicationInfo =
                    context.getPackageManager().getApplicationInfo(packageName, 0);
            packageContext.setTheme(applicationInfo.theme);
            Resources.Theme theme = packageContext.getTheme();
            TypedArray ta = theme.obtainStyledAttributes(new int[]{attribute});
            themeColor = ta.getColor(0, defaultColor);
            ta.recycle();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return themeColor;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= 21) {
            // Since our app icon has the same color as colorPrimary, our entry in the Recent Apps
            // list gets weird. We need to change either the icon or the color
            // of the TaskDescription.
            ActivityManager.TaskDescription taskDesc = new ActivityManager.TaskDescription(
                    getTitle().toString(),
                    BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_background),
                    getThemeColor(this, R.attr.colorPrimary,
                            android.R.color.darker_gray));
            setTaskDescription(taskDesc);
        }
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
        connection.subscribe("111", new MediaBrowserCompat.SubscriptionCallback() {
            @Override
            public void onChildrenLoaded(@NonNull String parentId, @NonNull List<MediaBrowserCompat.MediaItem> children) {
                super.onChildrenLoaded(parentId, children);
                Log.i("xian", "1");
                for (final MediaBrowserCompat.MediaItem mediaItem : children) {
                    connection.getMediaController().addQueueItem(mediaItem.getDescription());
                }
                connection.getTransportControls().prepare();
            }
        });
        findViewById(R.id.subscribe).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connection.getTransportControls().playFromMediaId("111", null);
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
