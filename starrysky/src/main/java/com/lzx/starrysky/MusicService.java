package com.lzx.starrysky;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationManagerCompat;
import androidx.media.MediaBrowserServiceCompat;

public class MusicService extends MediaBrowserServiceCompat {
//    private lateinit var mediaSession: MediaSessionCompat //媒体会画
//    private lateinit var mediaController: MediaControllerCompat //媒体控制器
//    private lateinit var becomingNoisyReceiver: BecomingNoisyReceiver //线控相关广播
//    private lateinit var notificationManager: NotificationManagerCompat //通知栏管理
//    private lateinit var notificationBuilder: NotificationBuilder
//    private lateinit var mediaSource: MusicSource //
//    private lateinit var mediaSessionConnector: MediaSessionConnector
//    private lateinit var packageValidator: PackageValidator

    private MediaSessionCompat mediaSession; //媒体会话
    private MediaControllerCompat mediaController;//媒体控制器
    private NotificationManagerCompat notificationManager; //通知栏管理
    private MediaSessionConnector mediaSessionConnector;

    private boolean isForegroundService = false;

    private ExoPlayer createExoPlayer() {
        SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(this);
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(C.CONTENT_TYPE_MUSIC)
                .setUsage(C.USAGE_MEDIA)
                .build();
        player.setAudioAttributes(audioAttributes, true); //第二个参数可以让ExoPlayer自动管理焦点
        return player;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Intent sessionIntent = getPackageManager().getLaunchIntentForPackage(getPackageName());
        PendingIntent sessionActivityPendingIntent = PendingIntent.getActivity(this, 0, sessionIntent, 0);

    }


    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        return null;
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {

    }
}
