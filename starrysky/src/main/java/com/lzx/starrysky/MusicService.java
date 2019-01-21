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
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.lzx.starrysky.model.MusicProvider;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media.MediaBrowserServiceCompat;

public class MusicService extends MediaBrowserServiceCompat {

    private static final String UAMP_BROWSABLE_ROOT = "/";
    private static final String UAMP_EMPTY_ROOT = "@empty@";
    private static final String UAMP_USER_AGENT = "uamp.next";

    private MediaSessionCompat mediaSession;
    private MediaControllerCompat mediaController;
    private MediaSessionConnector mediaSessionConnector;

    private PackageValidator mPackageValidator;

    @Override
    public void onCreate() {
        super.onCreate();
        Intent sessionIntent = getPackageManager().getLaunchIntentForPackage(getPackageName());
        PendingIntent sessionActivityPendingIntent = PendingIntent.getActivity(this, 0, sessionIntent, 0);

        //会话连接
        mediaSession = new MediaSessionCompat(this, "MusicService");
        mediaSession.setSessionActivity(sessionActivityPendingIntent);
        mediaSession.setActive(true);
        setSessionToken(mediaSession.getSessionToken());

        //媒体控制
        mediaController = new MediaControllerCompat(this, mediaSession);
        mediaController.registerCallback(new MediaControllerCallback());

        mediaSessionConnector = new MediaSessionConnector(mediaSession);
        // Produces DataSource instances through which media data is loaded.
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(
                this, Util.getUserAgent(this, UAMP_USER_AGENT), null);
        ExoPlayer exoPlayer = createrExoPlayer();

        // Create the PlaybackPreparer of the media session connector.
        UampPlaybackPreparer playbackPreparer = new UampPlaybackPreparer(createrExoPlayer(), dataSourceFactory);
        mediaSessionConnector.setPlayer(exoPlayer, playbackPreparer);
        mediaSessionConnector.setQueueNavigator(UampQueueNavigator(mediaSession));

        mPackageValidator = new PackageValidator(this);

    }

    private ExoPlayer createrExoPlayer() {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(C.CONTENT_TYPE_MUSIC)
                .setUsage(C.USAGE_MEDIA)
                .build();
        SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(this);
        player.setAudioAttributes(audioAttributes, true);
        return player;
    }

    private class MediaControllerCallback extends MediaControllerCompat.Callback {

    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        if (mPackageValidator.isCallerAllowed(this, clientPackageName, clientUid)) {
            return new BrowserRoot(UAMP_BROWSABLE_ROOT, null);
        } else {
            return new BrowserRoot(UAMP_EMPTY_ROOT, null);
        }
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        result.detach();
        MusicProvider.getInstance().retrieveMediaAsync(this, () -> {
            result.sendResult(MusicProvider.getInstance().getChildrenResult(parentId));
        });
    }
}
