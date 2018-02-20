package com.lzx.musiclibrary.notification;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.widget.RemoteViews;

import com.lzx.musiclibrary.R;
import com.lzx.musiclibrary.aidl.model.SongInfo;
import com.lzx.musiclibrary.receiver.PlayerReceiver;
import com.lzx.musiclibrary.utils.AlbumArtCache;

/**
 * Created by xian on 2018/2/18.
 */

public class NotificationCreater {

    private Context mContext;
    private static NotificationCreater instanse;

    public static final String ACTION_PLAY_PAUSE = "com.lzx.nicemusic.play_pause";
    public static final String ACTION_PAUSE = "com.lzx.nicemusic.pause";
    public static final String ACTION_PLAY = "com.lzx.nicemusic.play";
    public static final String ACTION_PREV = "com.lzx.nicemusic.prev";
    public static final String ACTION_NEXT = "com.lzx.nicemusic.next";
    public static final String ACTION_STOP = "com.lzx.nicemusic.stop";
    public static final String ACTION_CLOSE = "com.lzx.nicemusic.close";
    public static final String ACTION_STOP_CASTING = "com.lzx.nicemusic.stop_cast";
    public static final String ACTION_INTENT_CLICK = "com.lzx.nicemusic.EXTRY_NOTIFICATION_TO_MAINACTIVITY";

    private RemoteViews mRemoteView;
    private PendingIntent startOrPauseIntent;
    private PendingIntent nextIntent;
    private PendingIntent preIntent;
    private PendingIntent closeIntent;
    private final NotificationManager mNotificationManager;

    private Resources res;
    private String packageName;

    public static final String CHANNEL_ID = "com.lzx.nicemusic.MUSIC_CHANNEL_ID";
    public static final int NOTIFICATION_ID = 412;

    private NotificationCreater(Context context) {
        mContext = context;
        res = context.getResources();
        mNotificationManager = (NotificationManager) mContext.getSystemService(Service.NOTIFICATION_SERVICE);
        packageName = mContext.getPackageName();
    }

    public <T> Notification initNotification(Context context, Class<T> targetClass) {
        return createNotification(context, targetClass);
    }

    public static NotificationCreater getInstanse(Context context) {
        if (instanse == null) {
            synchronized (NotificationCreater.class) {
                if (instanse == null) {
                    instanse = new NotificationCreater(context.getApplicationContext());
                }
            }
        }
        return instanse;
    }

    private <T> Notification createNotification(Context context, Class<T> targetClass) {
        mRemoteView = createRemoteViews(context);
        // Notification channels are only supported on Android O+.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
        }
        final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(mContext, CHANNEL_ID);
        Intent notificationIntent = new Intent(context, targetClass);
        notificationIntent.putExtra("notification_entry", ACTION_INTENT_CLICK);
        @SuppressLint("WrongConstant")
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 134217728);
        notificationBuilder
                .setContentIntent(pendingIntent)
                .setContentTitle("Nice Music")
                .setContentText("随时随地 听我所想")
                .setSmallIcon(getResourceId("icon_notification", "drawable"));
        if (Build.VERSION.SDK_INT >= 16) {
            notificationBuilder.setPriority(2);
        }
        if (Build.VERSION.SDK_INT >= 24) {
            notificationBuilder.setCustomContentView(mRemoteView);
        }
        Notification notification;
        if (Build.VERSION.SDK_INT >= 16) {
            notification = notificationBuilder.build();
        } else {
            notification = notificationBuilder.getNotification();
        }
        if (Build.VERSION.SDK_INT < 24) {
            notification.contentView = mRemoteView;
        }
        return notification;
    }

    private RemoteViews createRemoteViews(Context context) {
        RemoteViews remoteView = new RemoteViews(context.getPackageName()
                , getResourceId("view_notify_play", "layout"));
        if (startOrPauseIntent == null) {
            setStartOrPausePendingIntent(null);
        }
        if (startOrPauseIntent != null) {
            remoteView.setOnClickPendingIntent(getResourceId("img_notifyPlayOrPause", "id"), startOrPauseIntent);
        }
        if (nextIntent == null) {
            setNextPendingIntent(null);
        }
        if (nextIntent != null) {
            remoteView.setOnClickPendingIntent(getResourceId("img_notifyNext", "id"), nextIntent);
        }
        if (preIntent == null) {
            setPrePendingIntent(null);
        }
        if (preIntent != null) {
            remoteView.setOnClickPendingIntent(getResourceId("img_notifyPre", "id"), preIntent);
        }
        if (closeIntent == null) {
            setClosePendingIntent(null);
        }
        if (closeIntent != null) {
            remoteView.setOnClickPendingIntent(getResourceId("img_notifyClose", "id"), closeIntent);
        }
        return remoteView;
    }

    private int getResourceId(String name, String className) {
        return res.getIdentifier(name, className, packageName);
    }

    public void setStartOrPausePendingIntent(PendingIntent pendingIntent) {
        if (pendingIntent == null) {
            Intent intent = new Intent(ACTION_PLAY_PAUSE);
            intent.setClass(mContext, PlayerReceiver.class);
            startOrPauseIntent = PendingIntent.getBroadcast(mContext, 0, intent, 0);
        } else {
            startOrPauseIntent = pendingIntent;
        }
    }

    public void setNextPendingIntent(PendingIntent pendingIntent) {
        if (pendingIntent == null) {
            Intent intent = new Intent(ACTION_NEXT);
            intent.setClass(mContext, PlayerReceiver.class);
            nextIntent = PendingIntent.getBroadcast(mContext, 0, intent, 0);
        } else {
            nextIntent = pendingIntent;
        }
    }

    public void setPrePendingIntent(PendingIntent pendingIntent) {
        if (pendingIntent == null) {
            Intent intent = new Intent(ACTION_PREV);
            intent.setClass(mContext, PlayerReceiver.class);
            preIntent = PendingIntent.getBroadcast(mContext, 0, intent, 0);
        } else {
            preIntent = pendingIntent;
        }
    }

    public void setClosePendingIntent(PendingIntent pendingIntent) {
        if (pendingIntent == null) {
            Intent intent = new Intent(ACTION_CLOSE);
            intent.setClass(mContext, PlayerReceiver.class);
            closeIntent = PendingIntent.getBroadcast(mContext, 0, intent, 0);
        } else {
            closeIntent = pendingIntent;
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        if (mNotificationManager.getNotificationChannel(CHANNEL_ID) == null) {
            NotificationChannel notificationChannel =
                    new NotificationChannel(CHANNEL_ID,
                            mContext.getString(R.string.notification_channel),
                            NotificationManager.IMPORTANCE_LOW);

            notificationChannel.setDescription(
                    mContext.getString(R.string.notification_channel_description));

            mNotificationManager.createNotificationChannel(notificationChannel);
        }
    }

    /**
     * 切歌的时候刷新
     */
    public void updateModelDetail(final SongInfo info, final Notification notification) {
        if (notification != null) {
            mRemoteView = createRemoteViews(mContext);
            notification.contentView = mRemoteView;
            if (info != null) {
                mRemoteView.setTextViewText(getResourceId("txt_notifySongName", "id"), info.getSongName());
                mRemoteView.setTextViewText(getResourceId("txt_notifyArtistName", "id"), info.getArtist());
                mRemoteView.setImageViewResource(getResourceId("img_notifyPlayOrPause", "id"),
                        getResourceId("notify_btn_pause", "drawable"));
                String fetchArtUrl = null;
                Bitmap art = null;
                if (!TextUtils.isEmpty(info.getSongCover())) {
                    String artUrl = info.getSongCover();
                    art = AlbumArtCache.getInstance().getBigImage(artUrl);
                    if (art == null) {
                        fetchArtUrl = artUrl;
                        art = BitmapFactory.decodeResource(res, R.drawable.icon_notification);
                    }
                }
                if (fetchArtUrl != null) {
                    final Bitmap finalArt = art;
                    AlbumArtCache.getInstance().fetch(fetchArtUrl, new AlbumArtCache.FetchListener() {
                        @Override
                        public void onFetched(String artUrl, Bitmap bitmap, Bitmap icon) {
                            if (!TextUtils.isEmpty(info.getSongCover()) && info.getSongCover().equals(artUrl)) {
                                mRemoteView.setImageViewBitmap(getResourceId("img_notifyIcon", "id"), bitmap);
                            } else {
                                mRemoteView.setImageViewBitmap(getResourceId("img_notifyIcon", "id"), finalArt);
                            }
                            mNotificationManager.notify(NOTIFICATION_ID, notification);
                        }
                    });
                } else {
                    mNotificationManager.notify(NOTIFICATION_ID, notification);
                }
            }
        }
    }

    /**
     * 开始播放的时候
     */
    public void updateViewStateAtStart(Notification notification) {
        if (notification != null) {
            mRemoteView = createRemoteViews(mContext);
            notification.contentView = mRemoteView;
            if (mRemoteView != null) {
                mRemoteView.setImageViewResource(getResourceId("img_notifyPlayOrPause", "id"),
                        getResourceId("notify_btn_pause", "drawable"));
                mNotificationManager.notify(NOTIFICATION_ID, notification);
            }
        }
    }

    /**
     * 暂停播放和播放完成的时候
     */
    public void updateViewStateAtPause(Notification notification) {
        if (notification != null) {
            mRemoteView = createRemoteViews(mContext);
            notification.contentView = mRemoteView;
            if (mRemoteView != null) {
                mRemoteView.setImageViewResource(getResourceId("img_notifyPlayOrPause", "id"),
                        getResourceId("notify_btn_play", "drawable"));
                mNotificationManager.notify(NOTIFICATION_ID, notification);
            }
        }
    }

    public void closeNotification() {
        if (mNotificationManager != null) {
            //  stopForeground(true);
            mNotificationManager.cancel(NOTIFICATION_ID);
        }
    }

    public static void release() {
        if (instanse != null) {
            instanse.mRemoteView = null;
            instanse = null;
        }
    }

}
