package com.lzx.starrysky.notification;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;
import android.util.Log;

import com.lzx.starrysky.MusicService;
import com.lzx.starrysky.R;
import com.lzx.starrysky.model.MusicProvider;
import com.lzx.starrysky.model.SongInfo;
import com.lzx.starrysky.notification.factory.INotification;

import java.util.List;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

public class SystemNotification extends BroadcastReceiver implements INotification {

    private static final int NOTIFICATION_ID = 412;
    private static final int REQUEST_CODE = 100;


    private PendingIntent mPlayIntent;
    private PendingIntent mPauseIntent;
    private PendingIntent mStopIntent;
    private PendingIntent mNextIntent;
    private PendingIntent mPreviousIntent;

    private final MusicService mService;
    private MediaSessionCompat.Token mSessionToken;
    private MediaControllerCompat mController;
    private MediaControllerCompat.TransportControls mTransportControls;
    private PlaybackStateCompat mPlaybackState;
    private MediaMetadataCompat mMetadata;

    private final NotificationManager mNotificationManager;
    private String packageName;
    private boolean mStarted = false;
    private NotificationBuilder mBuilder;

    public SystemNotification(MusicService service) throws RemoteException {
        mService = service;
        mBuilder = NotificationBuilder.getInstance();

        updateSessionToken();

        mNotificationManager = (NotificationManager) mService.getSystemService(Service.NOTIFICATION_SERVICE);
        packageName = mService.getApplicationContext().getPackageName();

        setStopIntent(mBuilder.getStopIntent());
        setNextPendingIntent(mBuilder.getNextIntent());
        setPrePendingIntent(mBuilder.getPreIntent());
        setPlayPendingIntent(mBuilder.getPlayIntent());
        setPausePendingIntent(mBuilder.getPauseIntent());

        if (mNotificationManager != null) {
            mNotificationManager.cancelAll();
        }
    }

    private void updateSessionToken() throws RemoteException {
        MediaSessionCompat.Token freshToken = mService.getSessionToken();
        if (mSessionToken == null && freshToken != null ||
                mSessionToken != null && !mSessionToken.equals(freshToken)) {
            if (mController != null) {
                mController.unregisterCallback(mCb);
            }
            mSessionToken = freshToken;
            if (mSessionToken != null) {
                mController = new MediaControllerCompat(mService, mSessionToken);
                mTransportControls = mController.getTransportControls();
                if (mStarted) {
                    mController.registerCallback(mCb);
                }
            }
        }
    }

    /**
     * 通知栏点击监听
     */
    private final MediaControllerCompat.Callback mCb = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);
            mPlaybackState = state;
            if (state.getState() == PlaybackStateCompat.STATE_STOPPED || state.getState() == PlaybackStateCompat.STATE_NONE) {
                stopNotification();
            } else {
                Notification notification = createNotification();
                if (notification != null) {
                    mNotificationManager.notify(NOTIFICATION_ID, notification);
                }
            }
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            super.onMetadataChanged(metadata);
            mMetadata = metadata;
            Notification notification = createNotification();
            if (notification != null) {
                mNotificationManager.notify(NOTIFICATION_ID, notification);
            }
        }

        @Override
        public void onSessionDestroyed() {
            super.onSessionDestroyed();
            try {
                updateSessionToken();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public void startNotification() {
        if (!mStarted) {
            mMetadata = mController.getMetadata();
            mPlaybackState = mController.getPlaybackState();

            // The notification must be updated after setting started to true
            Notification notification = createNotification();
            if (notification != null) {
                mController.registerCallback(mCb);
                IntentFilter filter = new IntentFilter();
                filter.addAction(ACTION_NEXT);
                filter.addAction(ACTION_PAUSE);
                filter.addAction(ACTION_PLAY);
                filter.addAction(ACTION_PREV);

                mService.registerReceiver(this, filter);

                mService.startForeground(NOTIFICATION_ID, notification);
                mStarted = true;
            }
        }
    }

    @Override
    public void stopNotification() {
        if (mStarted) {
            mStarted = false;
            mController.unregisterCallback(mCb);
            try {
                mNotificationManager.cancel(NOTIFICATION_ID);
                mService.unregisterReceiver(this);
            } catch (IllegalArgumentException ex) {
                ex.printStackTrace();
            }
            mService.stopForeground(true);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        Log.i("xian", "onReceive#action = " + action);
        if (action == null) {
            return;
        }
        switch (action) {
            case ACTION_PAUSE:
                mTransportControls.pause();
                break;
            case ACTION_PLAY:
                mTransportControls.play();
                break;
            case ACTION_NEXT:
                mTransportControls.skipToNext();
                break;
            case ACTION_PREV:
                mTransportControls.skipToPrevious();
                break;
            default:
                break;
        }
    }

    private Notification createNotification() {
        if (mMetadata == null || mPlaybackState == null) {
            return null;
        }

        MediaDescriptionCompat description = mMetadata.getDescription();


        Bitmap art = mMetadata.getBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART);

        // Notification channels are only supported on Android O+.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
        }

        final NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(mService, CHANNEL_ID);

        final int playPauseButtonPosition = addActions(notificationBuilder);
        notificationBuilder
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        // show only play/pause in compact view
                        .setShowActionsInCompactView(playPauseButtonPosition)
                        .setShowCancelButton(true)
                        .setCancelButtonIntent(mStopIntent)
                        .setMediaSession(mSessionToken))
                .setDeleteIntent(mStopIntent)
                //.setColor(mNotificationColor)
                .setOnlyAlertOnce(true)
                .setColorized(true)
                .setSmallIcon(mBuilder.getSmallIconRes() != -1 ? mBuilder.getSmallIconRes() : R.drawable.ic_notification)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOnlyAlertOnce(true)
                .setContentTitle(description.getTitle()) //歌名
                .setContentText(description.getSubtitle()) //艺术家
                .setLargeIcon(art);
        if (!TextUtils.isEmpty(mBuilder.getTargetClass())) {
            Class clazz = getTargetClass(mBuilder.getTargetClass());
            if (clazz != null) {
                String songId = mMetadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
                notificationBuilder.setContentIntent(createContentIntent(songId, null, clazz));
            }
        }

        setNotificationPlaybackState(notificationBuilder);

        return notificationBuilder.build();
    }

    private void setNotificationPlaybackState(NotificationCompat.Builder builder) {
        if (mPlaybackState == null || !mStarted) {
            mService.stopForeground(true);
            return;
        }
        builder.setOngoing(mPlaybackState.getState() == PlaybackStateCompat.STATE_PLAYING);
    }

    /**
     * 设置content点击事件
     */
    private PendingIntent createContentIntent(String songId, Bundle bundle, Class targetClass) {
        SongInfo songInfo = null;
        List<SongInfo> songInfos = MusicProvider.getInstance().getSongInfos();
        for (SongInfo info : songInfos) {
            if (info.getSongId().equals(songId)) {
                songInfo = info;
                break;
            }
        }
        Intent openUI = new Intent(mService, targetClass);
        openUI.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        openUI.putExtra("notification_entry", ACTION_INTENT_CLICK);
        if (songInfo != null) {
            openUI.putExtra("songInfo", songInfo);
        }
        if (bundle != null) {
            openUI.putExtra("bundleInfo", bundle);
        }
        @SuppressLint("WrongConstant")
        PendingIntent pendingIntent;
        switch (mBuilder.getPendingIntentMode()) {
            case NotificationBuilder.MODE_ACTIVITY:
                pendingIntent = PendingIntent.getActivity(mService, REQUEST_CODE, openUI, PendingIntent.FLAG_CANCEL_CURRENT);
                break;
            case NotificationBuilder.MODE_BROADCAST:
                pendingIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE, openUI, PendingIntent.FLAG_CANCEL_CURRENT);
                break;
            case NotificationBuilder.MODE_SERVICE:
                pendingIntent = PendingIntent.getService(mService, REQUEST_CODE, openUI, PendingIntent.FLAG_CANCEL_CURRENT);
                break;
            default:
                pendingIntent = PendingIntent.getActivity(mService, REQUEST_CODE, openUI, PendingIntent.FLAG_CANCEL_CURRENT);
                break;
        }
        return pendingIntent;
    }

    /**
     * 得到目标界面 Class
     */
    private Class getTargetClass(String targetClass) {
        Class clazz = null;
        try {
            if (!TextUtils.isEmpty(targetClass)) {
                clazz = Class.forName(targetClass);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return clazz;
    }

    /**
     * 添加上一首，下一首，播放，暂停按钮
     */
    private int addActions(final NotificationCompat.Builder notificationBuilder) {
        int playPauseButtonPosition = 0;
        // 如果有上一首
        if ((mPlaybackState.getActions() & PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS) != 0) {
            notificationBuilder.addAction(
                    mBuilder.getSkipPreviousDrawableRes() != -1 ? mBuilder.getSkipPreviousDrawableRes() : R.drawable.ic_skip_previous_white_24dp,
                    !TextUtils.isEmpty(mBuilder.getSkipPreviousTitle()) ? mBuilder.getSkipPreviousTitle() : mService.getString(R.string.label_previous),
                    mPreviousIntent);

            playPauseButtonPosition = 1;
        }

        // 播放和暂停按钮
        final String label;
        final int icon;
        final PendingIntent intent;

        if (mPlaybackState.getState() == PlaybackStateCompat.STATE_PLAYING) {
            label = !TextUtils.isEmpty(mBuilder.getLabelPlay()) ? mBuilder.getLabelPlay() : mService.getString(R.string.label_pause);
            icon = mBuilder.getPauseDrawableRes() != -1 ? mBuilder.getPauseDrawableRes() : R.drawable.ic_pause_white_24dp;
            intent = mPauseIntent;
        } else {
            label = !TextUtils.isEmpty(mBuilder.getLabelPause()) ? mBuilder.getLabelPause() : mService.getString(R.string.label_play);
            icon = mBuilder.getPlayDrawableRes() != -1 ? mBuilder.getPlayDrawableRes() : R.drawable.ic_play_arrow_white_24dp;
            intent = mPlayIntent;
        }

        notificationBuilder.addAction(new NotificationCompat.Action(icon, label, intent));

        // 如果有下一首
        if ((mPlaybackState.getActions() & PlaybackStateCompat.ACTION_SKIP_TO_NEXT) != 0) {
            notificationBuilder.addAction(
                    mBuilder.getSkipNextDrawableRes() != -1 ? mBuilder.getSkipNextDrawableRes() : R.drawable.ic_skip_next_white_24dp,
                    !TextUtils.isEmpty(mBuilder.getSkipNextTitle()) ? mBuilder.getSkipNextTitle() : mService.getString(R.string.label_next),
                    mNextIntent);
        }

        return playPauseButtonPosition;
    }

    /**
     * 兼容8.0
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        if (mNotificationManager.getNotificationChannel(CHANNEL_ID) == null) {
            NotificationChannel notificationChannel =
                    new NotificationChannel(CHANNEL_ID, mService.getString(R.string.notification_channel),
                            NotificationManager.IMPORTANCE_LOW);

            notificationChannel.setDescription(mService.getString(R.string.notification_channel_description));

            mNotificationManager.createNotificationChannel(notificationChannel);
        }
    }

    private void setStopIntent(PendingIntent pendingIntent) {
        mStopIntent = pendingIntent == null ? getPendingIntent(ACTION_STOP) : pendingIntent;
    }

    private void setNextPendingIntent(PendingIntent pendingIntent) {
        mNextIntent = pendingIntent == null ? getPendingIntent(ACTION_NEXT) : pendingIntent;
    }

    private void setPrePendingIntent(PendingIntent pendingIntent) {
        mPreviousIntent = pendingIntent == null ? getPendingIntent(ACTION_PREV) : pendingIntent;
    }

    private void setPlayPendingIntent(PendingIntent pendingIntent) {
        mPlayIntent = pendingIntent == null ? getPendingIntent(ACTION_PLAY) : pendingIntent;
    }

    private void setPausePendingIntent(PendingIntent pendingIntent) {
        mPauseIntent = pendingIntent == null ? getPendingIntent(ACTION_PAUSE) : pendingIntent;
    }

    private PendingIntent getPendingIntent(String action) {
        Intent intent = new Intent(action);
        intent.setPackage(packageName);
        return PendingIntent.getBroadcast(mService, REQUEST_CODE, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }
}
