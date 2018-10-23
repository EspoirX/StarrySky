package com.lzx.musiclibrary.notification;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import com.lzx.musiclibrary.MusicService;
import com.lzx.musiclibrary.R;
import com.lzx.musiclibrary.aidl.model.SongInfo;
import com.lzx.musiclibrary.constans.State;
import com.lzx.musiclibrary.playback.PlaybackManager;
import com.lzx.musiclibrary.receiver.PlayerReceiver;
import com.lzx.musiclibrary.utils.AlbumArtCache;
import com.lzx.musiclibrary.utils.LogUtil;

/**
 * 系统通知栏
 * Created by xian on 2018/3/17.
 */

public class SystemNotification implements IMediaNotification {

    private PendingIntent startOrPauseIntent;
    private PendingIntent playIntent;
    private PendingIntent pauseIntent;
    private PendingIntent stopIntent;
    private PendingIntent nextIntent;
    private PendingIntent preIntent;
    private PendingIntent closeIntent;
    private PendingIntent contentIntent;

    private final NotificationManager mNotificationManager;
    private Resources res;
    private String packageName;
    private boolean mStarted = false;
    private SongInfo mSongInfo;
    private MusicService mService;
    private NotificationCreater mNotificationCreater;
    private Notification mNotification;
    private NotificationCompat.Builder notificationBuilder;
    private PlaybackManager mPlaybackManager;

    public SystemNotification(MusicService musicService, NotificationCreater creater, PlaybackManager playbackManager) {
        mService = musicService;
        mNotificationCreater = creater;
        mPlaybackManager = playbackManager;
        mNotificationManager = (NotificationManager) mService.getSystemService(Service.NOTIFICATION_SERVICE);
        packageName = mService.getApplicationContext().getPackageName();
        res = mService.getApplicationContext().getResources();

        setStopIntent(creater.getStopIntent());
        setNextPendingIntent(creater.getNextIntent());
        setPrePendingIntent(creater.getPreIntent());
        setClosePendingIntent(creater.getCloseIntent());
        setPlayPendingIntent(creater.getPlayIntent());
        setPausePendingIntent(creater.getPauseIntent());
        setStartOrPausePendingIntent(creater.getStartOrPauseIntent());

        if (mNotificationManager != null) {
            mNotificationManager.cancelAll();
        }
    }

    @Override
    public void startNotification(SongInfo songInfo) {
        try {
            if (mSongInfo == null && songInfo == null) {
                return;
            }
            if (songInfo != null) {
                mSongInfo = songInfo;
            }
            if (!mStarted) {
                mNotification = createNotification();
                if (mNotification != null) {
                    mService.startForeground(NOTIFICATION_ID, mNotification);
                    mStarted = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.i("e = " + e.getMessage());
        }
    }

    @Override
    public void stopNotification() {
        if (mStarted) {
            mStarted = false;
            try {
                mNotificationManager.cancel(NOTIFICATION_ID);
            } catch (IllegalArgumentException ex) {
                ex.printStackTrace();
            }
            mService.stopForeground(true);
        }
    }

    @Override
    public void updateViewStateAtStart() {
        if (!mStarted) {
            startNotification(mSongInfo);
        } else {
            mNotification = createNotification();
            if (mNotification != null) {
                mNotificationManager.notify(NOTIFICATION_ID, mNotification);
            }
        }
    }

    @Override
    public void updateViewStateAtPause() {
        if (mNotificationCreater != null && mNotificationCreater.isNotificationCanClearBySystemBtn()) {
            mService.stopForeground(false);
            mStarted = false;
        }
        mNotification = createNotification();
        if (mNotification != null) {
            mNotificationManager.notify(NOTIFICATION_ID, mNotification);
        }
    }

    @Override
    public void updateFavorite(boolean isFavorite) {

    }

    @Override
    public void updateLyrics(boolean isChecked) {

    }

    @Override
    public void updateModelDetail(SongInfo songInfo) {
        mSongInfo = songInfo;
        mNotification = createNotification();
        if (mNotification != null) {
            mNotificationManager.notify(NOTIFICATION_ID, mNotification);
        }
    }

    @Override
    public void updateContentIntent(Bundle bundle, String targetClass) {
        if (mNotification != null) {
            Class clazz = null;
            if (!TextUtils.isEmpty(targetClass)) {
                clazz = getTargetClass(targetClass);
            } else if (!TextUtils.isEmpty(mNotificationCreater.getTargetClass())) {
                clazz = getTargetClass(mNotificationCreater.getTargetClass());
            }
            if (clazz != null) {
                contentIntent = createContentIntent(mSongInfo, bundle, clazz);
                mNotification.contentIntent = contentIntent;
                mNotificationManager.notify(NOTIFICATION_ID, mNotification);
            }
        }
    }

    private Notification createNotification() {
        if (mSongInfo != null && mNotificationCreater != null && !TextUtils.isEmpty(mNotificationCreater.getTargetClass())) {
            Class clazz = getTargetClass(mNotificationCreater.getTargetClass());

            String fetchArtUrl = null;
            Bitmap art = null;
            if (!TextUtils.isEmpty(mSongInfo.getSongCover())) {
                String artUrl = mSongInfo.getSongCover();
                art = AlbumArtCache.getInstance().getBigImage(artUrl);
                if (art == null) {
                    fetchArtUrl = artUrl;
                    art = BitmapFactory.decodeResource(res, R.drawable.icon_notification);
                }
            }
            String contentTitle = mSongInfo != null ? mSongInfo.getSongName() : mNotificationCreater.getContentTitle();
            String contentText = mSongInfo != null ? mSongInfo.getArtist() : mNotificationCreater.getContentText();
            //创建NotificationChannel
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel();
            }
            notificationBuilder = new NotificationCompat.Builder(mService, CHANNEL_ID);
            //上一首action
            notificationBuilder.addAction(R.drawable.ic_skip_previous_white_24dp,
                    mService.getString(R.string.label_previous),
                    preIntent);
            //播放暂停action
            addPlayPauseAction(notificationBuilder);
            //下一首action
            notificationBuilder.addAction(R.drawable.ic_skip_next_white_24dp,
                    mService.getString(R.string.label_next),
                    nextIntent);
            //创建contentIntent
            if (clazz != null) {
                contentIntent = createContentIntent(mSongInfo, null, clazz);
            }
            //构建Builder
            notificationBuilder
                    .setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle()
                            .setShowActionsInCompactView(0, 1, 2)
                            .setShowCancelButton(true)
                            .setCancelButtonIntent(stopIntent)
                    )
                    .setDeleteIntent(closeIntent) //当用户点击”Clear All Notifications”按钮区删除所有的通知的时候，这个被设置的Intent被执行
                    .setSmallIcon(R.drawable.icon_notification)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setOnlyAlertOnce(true)
                    .setColorized(true)
                    .setContentTitle(contentTitle)
                    .setContentText(contentText)
                    .setLargeIcon(art);
            if (contentIntent != null) {
                notificationBuilder.setContentIntent(contentIntent);
            }
            setNotificationPlaybackState(notificationBuilder);
            //异步加载图片
            if (fetchArtUrl != null) {
                fetchBitmapFromURLAsync(fetchArtUrl, notificationBuilder);
            }
            //创建Notification
            return notificationBuilder.build();
        }
        return null;
    }

    private PendingIntent createContentIntent(SongInfo songInfo, Bundle bundle, Class targetClass) {
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
        switch (mNotificationCreater.getPendingIntentMode()) {
            case PendingIntentMode.MODE_ACTIVITY:
                pendingIntent = PendingIntent.getActivity(mService, REQUEST_CODE, openUI, PendingIntent.FLAG_CANCEL_CURRENT);
                break;
            case PendingIntentMode.MODE_BROADCAST:
                pendingIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE, openUI, PendingIntent.FLAG_CANCEL_CURRENT);
                break;
            case PendingIntentMode.MODE_SERVICE:
                pendingIntent = PendingIntent.getService(mService, REQUEST_CODE, openUI, PendingIntent.FLAG_CANCEL_CURRENT);
                break;
            default:
                pendingIntent = PendingIntent.getActivity(mService, REQUEST_CODE, openUI, PendingIntent.FLAG_CANCEL_CURRENT);
                break;
        }
        return pendingIntent;
    }

    private void addPlayPauseAction(NotificationCompat.Builder builder) {
        String label;
        int icon;
        PendingIntent intent;
        if (mPlaybackManager.getPlayback().getState() == State.STATE_PLAYING || mPlaybackManager.getPlayback().getState() == State.STATE_ASYNC_LOADING) {
            label = mService.getString(R.string.label_pause);
            icon = R.drawable.uamp_ic_pause_white_24dp;
            intent = startOrPauseIntent;
        } else {
            label = mService.getString(R.string.label_play);
            icon = R.drawable.uamp_ic_play_arrow_white_24dp;
            intent = startOrPauseIntent;
        }
        builder.addAction(new NotificationCompat.Action(icon, label, intent));
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

    private void setNotificationPlaybackState(NotificationCompat.Builder builder) {
        if (mSongInfo == null || !mStarted) {
            mService.stopForeground(true);
            return;
        }
        //通知栏时间
        if (mNotificationCreater != null && mNotificationCreater.isSystemNotificationShowTime()) {
            if (mPlaybackManager.getPlayback().getState() == State.STATE_PLAYING && mPlaybackManager.getCurrentPosition() >= 0) {
                builder.setWhen(System.currentTimeMillis() - mPlaybackManager.getCurrentPosition())
                        .setShowWhen(true)
                        .setUsesChronometer(true);
            } else {
                builder.setWhen(0).setShowWhen(false).setUsesChronometer(false);
            }
        }
        builder.setOngoing(mPlaybackManager.getPlayback().getState() == State.STATE_PLAYING);
    }

    private void fetchBitmapFromURLAsync(final String bitmapUrl, final NotificationCompat.Builder builder) {
        AlbumArtCache.getInstance().fetch(bitmapUrl, new AlbumArtCache.FetchListener() {
            @Override
            public void onFetched(String artUrl, Bitmap bitmap, Bitmap icon) {
                if (!TextUtils.isEmpty(mSongInfo.getSongCover()) && mSongInfo.getSongCover().equals(artUrl)) {
                    builder.setLargeIcon(bitmap);
                    mNotificationManager.notify(NOTIFICATION_ID, builder.build());
                }
            }
        });
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        if (mNotificationManager.getNotificationChannel(CHANNEL_ID) == null) {
            NotificationChannel notificationChannel =
                    new NotificationChannel(CHANNEL_ID,
                            mService.getString(R.string.notification_channel),
                            NotificationManager.IMPORTANCE_LOW);

            notificationChannel.setDescription(
                    mService.getString(R.string.notification_channel_description));

            mNotificationManager.createNotificationChannel(notificationChannel);
        }
    }

    private void setStopIntent(PendingIntent pendingIntent) {
        stopIntent = pendingIntent == null ? getPendingIntent(ACTION_STOP) : pendingIntent;
    }

    private void setNextPendingIntent(PendingIntent pendingIntent) {
        nextIntent = pendingIntent == null ? getPendingIntent(ACTION_NEXT) : pendingIntent;
    }

    private void setPrePendingIntent(PendingIntent pendingIntent) {
        preIntent = pendingIntent == null ? getPendingIntent(ACTION_PREV) : pendingIntent;
    }

    private void setClosePendingIntent(PendingIntent pendingIntent) {
        closeIntent = pendingIntent == null ? getPendingIntent(ACTION_CLOSE) : pendingIntent;
    }

    private void setPlayPendingIntent(PendingIntent pendingIntent) {
        playIntent = pendingIntent == null ? getPendingIntent(ACTION_PLAY) : pendingIntent;
    }

    private void setPausePendingIntent(PendingIntent pendingIntent) {
        pauseIntent = pendingIntent == null ? getPendingIntent(ACTION_PAUSE) : pendingIntent;
    }

    private void setStartOrPausePendingIntent(PendingIntent pendingIntent) {
        startOrPauseIntent = pendingIntent == null ? getPendingIntent(ACTION_PLAY_PAUSE) : pendingIntent;
    }


    private PendingIntent getPendingIntent(String action) {
        Intent intent = new Intent(action);
        intent.setClass(mService, PlayerReceiver.class);
        return PendingIntent.getBroadcast(mService, 0, intent, 0);
    }
}
