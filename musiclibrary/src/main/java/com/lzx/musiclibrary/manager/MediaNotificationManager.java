package com.lzx.musiclibrary.manager;

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
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import com.lzx.musiclibrary.MusicService;
import com.lzx.musiclibrary.R;
import com.lzx.musiclibrary.aidl.model.SongInfo;
import com.lzx.musiclibrary.constans.State;
import com.lzx.musiclibrary.playback.PlaybackManager;
import com.lzx.musiclibrary.utils.AlbumArtCache;

/**
 * @author lzx
 * @date 2018/2/11
 */

public class MediaNotificationManager extends BroadcastReceiver {

    public static final String CHANNEL_ID = "com.lzx.nicemusic.MUSIC_CHANNEL_ID";

    private static final int NOTIFICATION_ID = 412;
    private static final int REQUEST_CODE = 100;

    public static final String ACTION_PAUSE = "com.lzx.nicemusic.pause";
    public static final String ACTION_PLAY = "com.lzx.nicemusic.play";
    public static final String ACTION_PREV = "com.lzx.nicemusic.prev";
    public static final String ACTION_NEXT = "com.lzx.nicemusic.next";
    public static final String ACTION_STOP = "com.lzx.nicemusic.stop";
    public static final String ACTION_STOP_CASTING = "com.lzx.nicemusic.stop_cast";

    private final NotificationManager mNotificationManager;

    private final PendingIntent mPlayIntent;
    private final PendingIntent mPauseIntent;
    private final PendingIntent mPreviousIntent;
    private final PendingIntent mNextIntent;
    private final PendingIntent mStopIntent;
    private final PendingIntent mStopCastIntent;

    private boolean mStarted = false;
    private SongInfo mSongInfo;
    private MusicService mService;
    private PlaybackManager mPlaybackManager;

    public MediaNotificationManager(MusicService musicService, PlaybackManager playbackManager) {
        mService = musicService;
        mPlaybackManager = playbackManager;

        mNotificationManager = (NotificationManager) mService.getSystemService(Service.NOTIFICATION_SERVICE);

        String pkg = mService.getPackageName();
        mPauseIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE, new Intent(ACTION_PAUSE).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
        mPlayIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE, new Intent(ACTION_PLAY).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
        mPreviousIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE, new Intent(ACTION_PREV).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
        mNextIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE, new Intent(ACTION_NEXT).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
        mStopIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE, new Intent(ACTION_STOP).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
        mStopCastIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE, new Intent(ACTION_STOP_CASTING).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);

        if (mNotificationManager != null) {
            mNotificationManager.cancelAll();
        }
    }

    public void stopNotification() {
        if (mStarted) {
            mStarted = false;
            try {
                mNotificationManager.cancel(NOTIFICATION_ID);
                mService.unregisterReceiver(this);
            } catch (IllegalArgumentException ex) {
                // ignore if the receiver is not registered.
            }
            mService.stopForeground(true);
        }
    }

    public void startNotification() {
        if (!mStarted) {
            Notification notification = createNotification();
            if (notification != null) {
                mSongInfo = MusicManager.get().getCurrPlayingMusic();
                IntentFilter filter = new IntentFilter();
                filter.addAction(ACTION_NEXT);
                filter.addAction(ACTION_PAUSE);
                filter.addAction(ACTION_PLAY);
                filter.addAction(ACTION_PREV);
                filter.addAction(ACTION_STOP_CASTING);
                mService.registerReceiver(this, filter);
                mService.startForeground(NOTIFICATION_ID, notification);
                mStarted = true;
            }
        }
    }

    private Notification createNotification() {
        if (mSongInfo == null) {
            return null;
        }
        String fetchArtUrl = null;
        Bitmap art = null;
        if (!TextUtils.isEmpty(mSongInfo.getSongCover())) {
            String artUrl = mSongInfo.getSongCover();
            art = AlbumArtCache.getInstance().getBigImage(artUrl);
            if (art == null) {
                fetchArtUrl = artUrl;
                art = BitmapFactory.decodeResource(mService.getResources(), R.drawable.icon_notification);
            }
        }
        // Notification channels are only supported on Android O+.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
        }
        final NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(mService, CHANNEL_ID);

        notificationBuilder
                .setDeleteIntent(mStopIntent)
                .setSmallIcon(R.drawable.icon_notification)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOnlyAlertOnce(true)
                .setContentIntent(createContentIntent(mSongInfo))
                .setContentTitle(mSongInfo.getSongName())
                .setContentText(mSongInfo.getArtist())
                .setLargeIcon(art);

        setNotificationPlaybackState(notificationBuilder);
        if (fetchArtUrl != null) {
            fetchBitmapFromURLAsync(fetchArtUrl, notificationBuilder);
        }

        return notificationBuilder.build();
    }

    private void setNotificationPlaybackState(NotificationCompat.Builder builder) {
        if (mSongInfo == null || !mStarted) {
            mService.stopForeground(true);
            return;
        }
        builder.setOngoing(MusicManager.get().getStatus() == State.STATE_PLAYING);
    }

    private void fetchBitmapFromURLAsync(final String bitmapUrl,
                                         final NotificationCompat.Builder builder) {
        AlbumArtCache.getInstance().fetch(bitmapUrl, new AlbumArtCache.FetchListener() {
            @Override
            public void onFetched(String artUrl, Bitmap bitmap, Bitmap icon) {
                if (mSongInfo != null && !TextUtils.isEmpty(mSongInfo.getSongCover()) && mSongInfo.getSongCover().equals(artUrl)) {
                    builder.setLargeIcon(bitmap);
                    mNotificationManager.notify(NOTIFICATION_ID, builder.build());
                }
            }
        });
    }

    private PendingIntent createContentIntent(SongInfo songInfo) {
//        Intent openUI = new Intent(mService, MusicPlayerActivity.class);
//        openUI.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        openUI.putExtra(MusicPlayerActivity.EXTRA_START_FULLSCREEN, true);
//        if (description != null) {
//            openUI.putExtra(MusicPlayerActivity.EXTRA_CURRENT_MEDIA_DESCRIPTION, description);
//        }
//        return PendingIntent.getActivity(mService, REQUEST_CODE, openUI,
//                PendingIntent.FLAG_CANCEL_CURRENT);

        return null;
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

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (TextUtils.isEmpty(action)) {
            return;
        }
        switch (action) {
            case ACTION_PAUSE:
                mPlaybackManager.handlePauseRequest();
                break;
            case ACTION_PLAY:
                mPlaybackManager.handlePlayRequest();
                break;
            case ACTION_NEXT:
                mPlaybackManager.playNextOrPre(1);
                break;
            case ACTION_PREV:
                mPlaybackManager.playNextOrPre(-1);
                break;
            case ACTION_STOP_CASTING:
//                Intent i = new Intent(context, MusicService.class);
//                i.setAction(MusicService.ACTION_CMD);
//                i.putExtra(MusicService.CMD_NAME, MusicService.CMD_STOP_CASTING);
//                mContext.startService(i);
                break;
            default:
                break;
        }
    }

//    private Notification createNotification() {
//        if (mSongInfo == null) {
//            return null;
//        }
//        String fetchArtUrl = null;
//        Bitmap art = null;
//        if (!TextUtils.isEmpty(mSongInfo.getSongCover())) {
//            String artUrl = mSongInfo.getSongCover();
//            art = AlbumArtCache.getInstance().getBigImage(artUrl);
//            if (art == null) {
//                fetchArtUrl = artUrl;
//                art = BitmapFactory.decodeResource(mService.getResources(), R.drawable.icon_notification);
//            }
//        }
//
//        // Notification channels are only supported on Android O+.
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            createNotificationChannel();
//        }
//
//        final NotificationCompat.Builder notificationBuilder =
//                new NotificationCompat.Builder(mService, CHANNEL_ID);
//        notificationBuilder
//                .setSmallIcon(R.drawable.icon_notification)
//                .setLargeIcon(art)
//                .setWhen(System.currentTimeMillis())
//                .setContentIntent(createContentIntent(Notification.FLAG_ONGOING_EVENT))
//                .setContent(createContentView())
//                .setPriority(NotificationCompat.PRIORITY_HIGH)
//                .setTicker("正在播放")
//                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
//                .setOnlyAlertOnce(true);
//        setNotificationPlaybackState(notificationBuilder);
//        if (fetchArtUrl != null) {
//            fetchBitmapFromURLAsync(fetchArtUrl, notificationBuilder);
//        }
//        return notificationBuilder.build();
//    }

//    /**
//     * Creates Notification Channel. This is required in Android O+ to display notifications.
//     */
//    @RequiresApi(Build.VERSION_CODES.O)
//    private void createNotificationChannel() {
//        if (mNotificationManager.getNotificationChannel(CHANNEL_ID) == null) {
//            NotificationChannel notificationChannel =
//                    new NotificationChannel(CHANNEL_ID,
//                            mContext.getString(R.string.notification_channel),
//                            NotificationManager.IMPORTANCE_LOW);
//
//            notificationChannel.setDescription(
//                    mContext.getString(R.string.notification_channel_description));
//
//            mNotificationManager.createNotificationChannel(notificationChannel);
//        }
//    }

//    private PendingIntent createContentIntent(int flags) {
//        return PendingIntent.getActivity(mContext, 1, new Intent(), flags);
//    }
//
//    private void setNotificationPlaybackState(NotificationCompat.Builder builder) {
//        if (!mStarted) {
//            mContext.stopForeground(true);
//            return;
//        }
//        builder.setOngoing(mPlaybackManager.getPlayback().getState() == State.STATE_PLAYING);
//    }
//
//    private void fetchBitmapFromURLAsync(final String bitmapUrl, final NotificationCompat.Builder builder) {
//        AlbumArtCache.getInstance().fetch(bitmapUrl, new AlbumArtCache.FetchListener() {
//            @Override
//            public void onFetched(String artUrl, Bitmap bitmap, Bitmap icon) {
//                if (mSongInfo != null && !TextUtils.isEmpty(mSongInfo.getSongCover()) && mSongInfo.getSongCover().equals(artUrl)) {
//                    builder.setLargeIcon(bitmap);
//                    mNotificationManager.notify(NOTIFICATION_ID, builder.build());
//                }
//            }
//        });
//    }
//
//    private RemoteViews createContentView() {
//        return null;
//    }
//
//
//    @Override
//    public IBinder asBinder() {
//        return null;
//    }
}
