package com.lzx.starrysky;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import com.lzx.starrysky.manager.MusicManager;
import com.lzx.starrysky.model.MusicProvider;
import com.lzx.starrysky.notification.NotificationConstructor;
import com.lzx.starrysky.notification.factory.NotificationFactory;
import com.lzx.starrysky.playback.LocalPlayback;
import com.lzx.starrysky.playback.PlaybackManager;
import com.lzx.starrysky.playback.QueueManager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media.MediaBrowserServiceCompat;
import androidx.media.session.MediaButtonReceiver;

public class MusicService extends MediaBrowserServiceCompat implements QueueManager.MetadataUpdateListener, PlaybackManager.PlaybackServiceCallback {

    public static final String UPDATE_PARENT_ID = "update";
    private static final String UAMP_BROWSABLE_ROOT = "/";
    private static final String UAMP_EMPTY_ROOT = "@empty@";
    private static final String UAMP_USER_AGENT = "uamp.next";
    public static final String ACTION_CMD = "com.lzx.starrysky.ACTION_CMD";
    public static final String CMD_NAME = "CMD_NAME";
    public static final String CMD_PAUSE = "CMD_PAUSE";

    private MediaSessionCompat mediaSession;

    private PackageValidator mPackageValidator;
    private PlaybackManager mPlaybackManager;

    private NotificationFactory mNotificationFactory;

    private static final int STOP_DELAY = 30000;
    private final DelayedStopHandler mDelayedStopHandler = new DelayedStopHandler(this);

    @Override
    public void onCreate() {
        super.onCreate();
        MusicProvider musicProvider = MusicProvider.getInstance();
        QueueManager queueManager = new QueueManager(musicProvider, this);
        LocalPlayback playback = new LocalPlayback(this, musicProvider);

        mPlaybackManager = new PlaybackManager(this, this, queueManager, playback);

        Intent sessionIntent = getPackageManager().getLaunchIntentForPackage(getPackageName());
        PendingIntent sessionActivityPendingIntent = PendingIntent.getActivity(this, 0, sessionIntent, 0);

        //会话连接
        mediaSession = new MediaSessionCompat(this, "MusicService");
        setSessionToken(mediaSession.getSessionToken());

        mediaSession.setSessionActivity(sessionActivityPendingIntent);
        mediaSession.setCallback(mPlaybackManager.getMediaSessionCallback());
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        Bundle mSessionExtras = new Bundle();
        mediaSession.setExtras(mSessionExtras);

        mPlaybackManager.updatePlaybackState(null);
        mPackageValidator = new PackageValidator(this);
        //通知栏相关
        NotificationConstructor constructor = MusicManager.getInstance().getConstructor();
        mNotificationFactory = new NotificationFactory(this, constructor);
        mNotificationFactory.createNotification();
        mPlaybackManager.setNotificationFactory(mNotificationFactory);
    }

    @Override
    public int onStartCommand(Intent startIntent, int flags, int startId) {
        Log.i("xian", "= onStartCommand = ");
        if (startIntent != null) {
            String action = startIntent.getAction();
            String command = startIntent.getStringExtra(CMD_NAME);
            if (ACTION_CMD.equals(action)) {
                if (CMD_PAUSE.equals(command)) {
                    mPlaybackManager.handlePauseRequest();
                }
            } else {
                MediaButtonReceiver.handleIntent(mediaSession, startIntent);
            }
        }
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        mDelayedStopHandler.sendEmptyMessageDelayed(0, STOP_DELAY);
        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPlaybackManager.handleStopRequest(null);
        mNotificationFactory.stopNotification();

        mDelayedStopHandler.removeCallbacksAndMessages(null);
        mediaSession.release();
    }

    @Override
    public void onMetadataChanged(MediaMetadataCompat metadata) {
        mediaSession.setMetadata(metadata);
    }

    @Override
    public void onMetadataRetrieveError() {
        mPlaybackManager.updatePlaybackState("Unable to retrieve metadata");
    }

    @Override
    public void onCurrentQueueIndexUpdated(int queueIndex) {
        mPlaybackManager.handlePlayRequest();
    }

    @Override
    public void onQueueUpdated(List<MediaSessionCompat.QueueItem> newQueue) {
        mediaSession.setQueue(newQueue);
    }

    @Override
    public void onPlaybackStart() {
        mediaSession.setActive(true);
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        startService(new Intent(getApplicationContext(), MusicService.class));
    }

    @Override
    public void onNotificationRequired() {
        mNotificationFactory.startNotification();
    }

    @Override
    public void onPlaybackStop() {
        mediaSession.setActive(false);
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        mDelayedStopHandler.sendEmptyMessageDelayed(0, STOP_DELAY);
        stopForeground(true);
    }

    @Override
    public void onPlaybackStateUpdated(PlaybackStateCompat newState) {
        mediaSession.setPlaybackState(newState);
    }

    private static class DelayedStopHandler extends Handler {
        private final WeakReference<MusicService> mWeakReference;

        private DelayedStopHandler(MusicService service) {
            mWeakReference = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            MusicService service = mWeakReference.get();
            if (service != null && service.mPlaybackManager.getPlayback() != null) {
                if (service.mPlaybackManager.getPlayback().isPlaying()) {
                    return;
                }
                service.stopSelf();
            }
        }
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
        if (UAMP_BROWSABLE_ROOT.equals(parentId)) {
            result.sendResult(new ArrayList<>());
        } else if (MusicProvider.getInstance().isInitialized()) {
            result.sendResult(MusicProvider.getInstance().getChildrenResult(parentId));
        } else {
            result.detach();
            MusicProvider.getInstance().retrieveMediaAsync(this, () -> {
                result.sendResult(MusicProvider.getInstance().getChildrenResult(parentId));
            });
        }
    }
}
