package com.lzx.starrysky;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.lzx.starrysky.notification.INotification;
import com.lzx.starrysky.notification.StarrySkyNotificationManager;
import com.lzx.starrysky.playback.manager.IPlaybackManager;
import com.lzx.starrysky.playback.manager.PlaybackManager;
import com.lzx.starrysky.provider.MediaQueueProvider;

import java.lang.ref.WeakReference;
import java.util.List;


public class MusicService extends MediaBrowserServiceCompat implements MediaQueueProvider.MetadataUpdateListener,
        PlaybackManager.PlaybackServiceCallback {

    public static final String UPDATE_PARENT_ID = "update";
    private static final String STARRYSKY_BROWSABLE_ROOT = "/";
    private static final String STARRYSKY_EMPTY_ROOT = "@empty@";

    private MediaSessionCompat mediaSession;
    private MediaControllerCompat mediaController;
    private MediaControllerCompat.TransportControls transportControls;

    private PackageValidator mPackageValidator;
    private IPlaybackManager mPlaybackManager;
    private INotification notification;
    private BecomingNoisyReceiver mBecomingNoisyReceiver;

    private static final int STOP_DELAY = 30000;
    private final DelayedStopHandler mDelayedStopHandler = new DelayedStopHandler(this);

    @Override
    public void onCreate() {
        super.onCreate();
        mPlaybackManager = StarrySky.get().getPlaybackManager();
        mPlaybackManager.setServiceCallback(this);
        mPlaybackManager.setMetadataUpdateListener(this);

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

        try {
            mediaController = new MediaControllerCompat(this, mediaSession.getSessionToken());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if (mediaController != null) {
            transportControls = mediaController.getTransportControls();
        }

        mBecomingNoisyReceiver = new BecomingNoisyReceiver(this, transportControls);

        mPlaybackManager.updatePlaybackState(false, null);
        mPackageValidator = new PackageValidator(this);
        //通知栏相关

        StarrySkyNotificationManager manager = StarrySky.get().getRegistry().getNotificationManager();
        notification = manager.getNotification(this);
        if (notification != null) {
            mPlaybackManager.registerNotification(notification);
        }
    }

    @Override
    public int onStartCommand(Intent startIntent, int flags, int startId) {
        if (startIntent != null) {
            //you can do something
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
        notification.stopNotification();

        mDelayedStopHandler.removeCallbacksAndMessages(null);
        mBecomingNoisyReceiver.unregister();
        mediaSession.release();
    }

    /**
     * 媒体信息更新时回调
     */
    @Override
    public void onMetadataChanged(MediaMetadataCompat metadata) {
        mediaSession.setMetadata(metadata);
    }

    /**
     * 当前播放媒体为 null 时回调
     */
    @Override
    public void onMetadataRetrieveError() {
        mPlaybackManager.updatePlaybackState(false, "Unable to retrieve metadata");
    }

    /**
     * 播放下标更新时回调
     */
    @Override
    public void onCurrentQueueIndexUpdated(int queueIndex) {
        //mPlaybackManager.handlePlayRequest(true);
    }

    /**
     * 播放队列更新时回调
     */
    @Override
    public void onQueueUpdated(List<MediaSessionCompat.QueueItem> newQueue) {
        mediaSession.setQueue(newQueue);
    }

    /**
     * 播放时回调
     */
    @Override
    public void onPlaybackStart() {
        mediaSession.setActive(true);
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        //startService(new Intent(getApplicationContext(), MusicService.class));
    }

    /**
     * 状态是播放或暂停时回调
     */
    @Override
    public void onNotificationRequired() {
        if (notification != null) {
            notification.startNotification();
        }
    }

    /**
     * 暂停或停止时回调
     */
    @Override
    public void onPlaybackStop() {
        mediaSession.setActive(false);
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        mDelayedStopHandler.sendEmptyMessageDelayed(0, STOP_DELAY);
        stopForeground(true);
    }

    /**
     * 播放状态改变时回调
     */
    @Override
    public void onPlaybackStateUpdated(PlaybackStateCompat newState, MediaMetadataCompat currMetadata) {
        mediaSession.setPlaybackState(newState);
        if (newState.getState() == PlaybackStateCompat.STATE_BUFFERING ||
                newState.getState() == PlaybackStateCompat.STATE_PLAYING) {
            mBecomingNoisyReceiver.register();
        } else {
            mBecomingNoisyReceiver.unregister();
        }
    }

    /**
     * 更新播放顺序
     */
    @Override
    public void onShuffleModeUpdated(int shuffleMode) {
        mediaSession.setShuffleMode(shuffleMode);
    }

    /**
     * 更新播放模式
     */
    @Override
    public void onRepeatModeUpdated(int repeatMode) {
        mediaSession.setRepeatMode(repeatMode);
    }

    private static class DelayedStopHandler extends Handler {
        private final WeakReference<MusicService> mWeakReference;

        private DelayedStopHandler(MusicService service) {
            mWeakReference = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            MusicService service = mWeakReference.get();
            if (service != null) {
                if (service.mPlaybackManager.isPlaying()) {
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
            return new BrowserRoot(STARRYSKY_BROWSABLE_ROOT, null);
        } else {
            return new BrowserRoot(STARRYSKY_EMPTY_ROOT, null);
        }
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        //可以不做任何事情
    }

    /**
     * 拔下耳机时暂停，具体意思可参考 AudioManager.ACTION_AUDIO_BECOMING_NOISY
     */
    private static class BecomingNoisyReceiver extends BroadcastReceiver {

        private Context context;
        private IntentFilter noisyIntentFilter;
        private MediaControllerCompat.TransportControls transportControls;
        private boolean registered = false;

        public BecomingNoisyReceiver(Context context, MediaControllerCompat.TransportControls transportControls) {
            this.context = context;
            this.transportControls = transportControls;
            noisyIntentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        }

        public void register() {
            if (!registered) {
                context.registerReceiver(this, noisyIntentFilter);
                registered = true;
            }
        }

        public void unregister() {
            if (registered) {
                context.unregisterReceiver(this);
                registered = false;
            }
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                if (transportControls != null) {
                    transportControls.pause();
                }
            }
        }
    }
}
