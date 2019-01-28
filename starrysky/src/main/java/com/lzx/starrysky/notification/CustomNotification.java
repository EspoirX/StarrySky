package com.lzx.starrysky.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;

import com.lzx.starrysky.MusicService;
import com.lzx.starrysky.R;
import com.lzx.starrysky.model.MusicProvider;
import com.lzx.starrysky.model.SongInfo;
import com.lzx.starrysky.notification.factory.INotification;
import com.lzx.starrysky.notification.utils.NotificationColorUtils;
import com.lzx.starrysky.notification.utils.NotificationUtils;

import java.util.List;


/**
 * 自定义通知栏
 */
public class CustomNotification extends BroadcastReceiver implements INotification {

    private RemoteViews mRemoteView;
    private RemoteViews mBigRemoteView;

    private PendingIntent mPlayOrPauseIntent;
    private PendingIntent mPlayIntent;
    private PendingIntent mPauseIntent;
    private PendingIntent mStopIntent;
    private PendingIntent mNextIntent;
    private PendingIntent mPreviousIntent;
    private PendingIntent mFavoriteIntent;
    private PendingIntent mLyricsIntent;
    private PendingIntent mDownloadIntent;
    private PendingIntent mCloseIntent;

    private final MusicService mService;
    private MediaSessionCompat.Token mSessionToken;
    private MediaControllerCompat mController;
    private MediaControllerCompat.TransportControls mTransportControls;
    private PlaybackStateCompat mPlaybackState;
    private MediaMetadataCompat mMetadata;

    private final NotificationManager mNotificationManager;
    private String packageName;
    private boolean mStarted = false;
    private NotificationConstructor mConstructor;
    private Notification mNotification;

    private Resources res;
    private NotificationColorUtils mColorUtils;

    public CustomNotification(MusicService service, NotificationConstructor constructor) throws RemoteException {
        mService = service;
        mConstructor = constructor;

        updateSessionToken();

        mNotificationManager = (NotificationManager) mService.getSystemService(Service.NOTIFICATION_SERVICE);
        packageName = mService.getApplicationContext().getPackageName();
        res = mService.getApplicationContext().getResources();
        mColorUtils = new NotificationColorUtils();

        setStopIntent(mConstructor.getStopIntent());
        setNextPendingIntent(mConstructor.getNextIntent());
        setPrePendingIntent(mConstructor.getPreIntent());
        setPlayPendingIntent(mConstructor.getPlayIntent());
        setPausePendingIntent(mConstructor.getPauseIntent());
        setFavoritePendingIntent(mConstructor.getFavoriteIntent());
        setLyricsPendingIntent(mConstructor.getLyricsIntent());
        setDownloadPendingIntent(mConstructor.getDownloadIntent());
        setClosePendingIntent(mConstructor.getCloseIntent());
        setPlayOrPauseIntent(mConstructor.getPlayOrPauseIntent());

        mRemoteView = createRemoteViews(false);
        mBigRemoteView = createRemoteViews(true);

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

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        Log.i("xian", "action  =  " + action);
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
            case ACTION_PLAY_OR_PAUSE:
                if (mPlaybackState.getState() == PlaybackStateCompat.STATE_PLAYING) {
                    mTransportControls.pause();
                } else {
                    mTransportControls.play();
                }
                break;
            case ACTION_NEXT:
                mTransportControls.skipToNext();
                break;
            case ACTION_PREV:
                mTransportControls.skipToPrevious();
                break;
            case ACTION_CLOSE:
                stopNotification();
                break;
            default:
                break;
        }
    }

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
                filter.addAction(ACTION_PLAY_OR_PAUSE);
                filter.addAction(ACTION_CLOSE);

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

    private Notification createNotification() {
        if (mMetadata == null || mPlaybackState == null) {
            return null;
        }
        MediaDescriptionCompat description = mMetadata.getDescription();

        String songId = mMetadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
        int smallIcon = mConstructor.getSmallIconRes() != -1 ? mConstructor.getSmallIconRes() : R.drawable.ic_notification;
        //适配8.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationUtils.createNotificationChannel(mService, mNotificationManager);
        }
        final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(mService, CHANNEL_ID);
        notificationBuilder
                .setOnlyAlertOnce(true)
                .setSmallIcon(smallIcon)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentTitle(description.getTitle()) //歌名
                .setContentText(description.getSubtitle()); //艺术家
        //setContentIntent
        if (!TextUtils.isEmpty(mConstructor.getTargetClass())) {
            Class clazz = NotificationUtils.getTargetClass(mConstructor.getTargetClass());
            if (clazz != null) {
                notificationBuilder.setContentIntent(NotificationUtils.createContentIntent(mService, mConstructor, songId, null, clazz));
            }
        }
        //setCustomContentView and setCustomBigContentView
        if (Build.VERSION.SDK_INT >= 24) {
            notificationBuilder.setCustomContentView(mRemoteView);
            notificationBuilder.setCustomBigContentView(mBigRemoteView);
        }

        setNotificationPlaybackState(notificationBuilder);

        //create Notification
        mNotification = notificationBuilder.build();
        mNotification.contentView = mRemoteView;
        mNotification.bigContentView = mBigRemoteView;
        SongInfo songInfo = null;
        List<SongInfo> songInfos = MusicProvider.getInstance().getSongInfos();
        for (SongInfo info : songInfos) {
            if (info.getSongId().equals(songId)) {
                songInfo = info;
                break;
            }
        }
        updateRemoteViewUI(mNotification, songInfo, smallIcon);

        return mNotification;
    }

    private void setNotificationPlaybackState(NotificationCompat.Builder builder) {
        if (mPlaybackState == null || !mStarted) {
            mService.stopForeground(true);
            return;
        }
        builder.setOngoing(mPlaybackState.getState() == PlaybackStateCompat.STATE_PLAYING);
    }

    /**
     * 创建RemoteViews
     */
    private RemoteViews createRemoteViews(boolean isBigRemoteViews) {
        RemoteViews remoteView;
        if (isBigRemoteViews) {
            remoteView = new RemoteViews(packageName, getResourceId(LAYOUT_NOTIFY_BIG_PLAY, "layout"));
        } else {
            remoteView = new RemoteViews(packageName, getResourceId(LAYOUT_NOTIFY_PLAY, "layout"));
        }
        if (mPlayIntent != null) {
            remoteView.setOnClickPendingIntent(getResourceId(ID_IMG_NOTIFY_PLAY, "id"), mPlayIntent);
        }
        if (mPauseIntent != null) {
            remoteView.setOnClickPendingIntent(getResourceId(ID_IMG_NOTIFY_PAUSE, "id"), mPauseIntent);
        }
        if (mStopIntent != null) {
            remoteView.setOnClickPendingIntent(getResourceId(ID_IMG_NOTIFY_STOP, "id"), mStopIntent);
        }
        if (mFavoriteIntent != null) {
            remoteView.setOnClickPendingIntent(getResourceId(ID_IMG_NOTIFY_FAVORITE, "id"), mFavoriteIntent);
        }
        if (mLyricsIntent != null) {
            remoteView.setOnClickPendingIntent(getResourceId(ID_IMG_NOTIFY_LYRICS, "id"), mLyricsIntent);
        }
        if (mDownloadIntent != null) {
            remoteView.setOnClickPendingIntent(getResourceId(ID_IMG_NOTIFY_DOWNLOAD, "id"), mDownloadIntent);
        }
        if (mNextIntent != null) {
            remoteView.setOnClickPendingIntent(getResourceId(ID_IMG_NOTIFY_NEXT, "id"), mNextIntent);
        }
        if (mPreviousIntent != null) {
            remoteView.setOnClickPendingIntent(getResourceId(ID_IMG_NOTIFY_PRE, "id"), mPreviousIntent);
        }
        if (mCloseIntent != null) {
            remoteView.setOnClickPendingIntent(getResourceId(ID_IMG_NOTIFY_CLOSE, "id"), mCloseIntent);
        }
        if (mPlayOrPauseIntent != null) {
            remoteView.setOnClickPendingIntent(getResourceId(ID_IMG_NOTIFY_PLAY_OR_PAUSE, "id"), mPlayOrPauseIntent);
        }
        return remoteView;
    }

    /**
     * 更新RemoteViews
     */
    private void updateRemoteViewUI(Notification notification, SongInfo songInfo, int smallIcon) {
        boolean isDark = mColorUtils.isDarkNotificationBar(mService, notification);

        Bitmap art = mMetadata.getBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART);
        String artistName = songInfo != null ? songInfo.getArtist() : "";
        String songName = songInfo != null ? songInfo.getSongName() : "";
        //设置文字内容
        mRemoteView.setTextViewText(getResourceId(ID_TXT_NOTIFY_SONGNAME, "id"), songName);
        mRemoteView.setTextViewText(getResourceId(ID_TXT_NOTIFY_ARTISTNAME, "id"), artistName);
        //设置播放暂停按钮

        if (mPlaybackState.getState() == PlaybackStateCompat.STATE_PLAYING) {
            mRemoteView.setImageViewResource(getResourceId(ID_IMG_NOTIFY_PLAY_OR_PAUSE, "id"),
                    getResourceId(isDark ? DRAWABLE_NOTIFY_BTN_DARK_PAUSE_SELECTOR :
                            DRAWABLE_NOTIFY_BTN_LIGHT_PAUSE_SELECTOR, "drawable"));
        } else {
            mRemoteView.setImageViewResource(getResourceId(ID_IMG_NOTIFY_PLAY_OR_PAUSE, "id"),
                    getResourceId(isDark ? DRAWABLE_NOTIFY_BTN_DARK_PLAY_SELECTOR :
                            DRAWABLE_NOTIFY_BTN_LIGHT_PLAY_SELECTOR, "drawable"));
        }

        //大布局
        //设置文字内容
        mBigRemoteView.setTextViewText(getResourceId(ID_TXT_NOTIFY_SONGNAME, "id"), songName);
        mBigRemoteView.setTextViewText(getResourceId(ID_TXT_NOTIFY_ARTISTNAME, "id"), artistName);
        //设置播放暂停按钮
        if (mPlaybackState.getState() == PlaybackStateCompat.STATE_PLAYING) {
            mBigRemoteView.setImageViewResource(getResourceId(ID_IMG_NOTIFY_PLAY_OR_PAUSE, "id"),
                    getResourceId(isDark ? DRAWABLE_NOTIFY_BTN_DARK_PAUSE_SELECTOR :
                            DRAWABLE_NOTIFY_BTN_LIGHT_PAUSE_SELECTOR, "drawable"));
        } else {
            mBigRemoteView.setImageViewResource(getResourceId(ID_IMG_NOTIFY_PLAY_OR_PAUSE, "id"),
                    getResourceId(isDark ? DRAWABLE_NOTIFY_BTN_DARK_PLAY_SELECTOR :
                            DRAWABLE_NOTIFY_BTN_LIGHT_PLAY_SELECTOR, "drawable"));
        }
        //设置喜欢或收藏按钮
        mBigRemoteView.setImageViewResource(getResourceId(ID_IMG_NOTIFY_FAVORITE, "id"),
                getResourceId(isDark ? DRAWABLE_NOTIFY_BTN_DARK_FAVORITE :
                        DRAWABLE_NOTIFY_BTN_LIGHT_FAVORITE, "drawable"));
        //设置歌词按钮
        mBigRemoteView.setImageViewResource(getResourceId(ID_IMG_NOTIFY_LYRICS, "id"),
                getResourceId(isDark ? DRAWABLE_NOTIFY_BTN_DARK_LYRICS :
                        DRAWABLE_NOTIFY_BTN_LIGHT_LYRICS, "drawable"));
        //设置下载按钮
        mBigRemoteView.setImageViewResource(getResourceId(ID_IMG_NOTIFY_DOWNLOAD, "id"),
                getResourceId(isDark ? DRAWABLE_NOTIFY_BTN_DARK_DOWNLOAD :
                        DRAWABLE_NOTIFY_BTN_LIGHT_DOWNLOAD, "drawable"));

        //上一首下一首按钮
        boolean hasNextSong = (mPlaybackState.getActions() & PlaybackStateCompat.ACTION_SKIP_TO_NEXT) != 0;
        boolean hasPreSong = (mPlaybackState.getActions() & PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS) != 0;
        disableNextBtn(hasNextSong, isDark);
        disablePreviousBtn(hasPreSong, isDark);

        //封面
        mRemoteView.setImageViewBitmap(getResourceId(ID_IMG_NOTIFY_ICON, "id"), art);
        mBigRemoteView.setImageViewBitmap(getResourceId(ID_IMG_NOTIFY_ICON, "id"), art);
        mNotificationManager.notify(NOTIFICATION_ID, notification);
    }

    /**
     * 下一首按钮样式
     */
    private void disableNextBtn(boolean disable, boolean isDark) {
        int res;
        if (disable) {
            res = this.getResourceId(isDark ? DRAWABLE_NOTIFY_BTN_DARK_NEXT_PRESSED :
                    DRAWABLE_NOTIFY_BTN_LIGHT_NEXT_PRESSED, "drawable");
        } else {
            res = this.getResourceId(isDark ? DRAWABLE_NOTIFY_BTN_DARK_NEXT_SELECTOR :
                    DRAWABLE_NOTIFY_BTN_LIGHT_NEXT_SELECTOR, "drawable");
        }
        mRemoteView.setImageViewResource(getResourceId(ID_IMG_NOTIFY_NEXT, "id"), res);
        mBigRemoteView.setImageViewResource(getResourceId(ID_IMG_NOTIFY_NEXT, "id"), res);
    }

    /**
     * 上一首按钮样式
     */
    private void disablePreviousBtn(boolean disable, boolean isDark) {
        int res;
        if (disable) {
            res = this.getResourceId(isDark ? DRAWABLE_NOTIFY_BTN_DARK_PREV_PRESSED :
                    DRAWABLE_NOTIFY_BTN_LIGHT_PREV_PRESSED, "drawable");
        } else {
            res = this.getResourceId(isDark ? DRAWABLE_NOTIFY_BTN_DARK_PREV_SELECTOR :
                    DRAWABLE_NOTIFY_BTN_LIGHT_PREV_SELECTOR, "drawable");
        }
        mRemoteView.setImageViewResource(getResourceId(ID_IMG_NOTIFY_PRE, "id"), res);
        if (mBigRemoteView != null) {
            mBigRemoteView.setImageViewResource(getResourceId(ID_IMG_NOTIFY_PRE, "id"), res);
        }
    }

    /**
     * 更新喜欢或收藏按钮样式
     */
    @Override
    public void updateFavoriteUI(boolean isFavorite) {
        if (mNotification == null) {
            return;
        }
        boolean isDark = mColorUtils.isDarkNotificationBar(mService, mNotification);
        //喜欢或收藏按钮选中时样式
        if (isFavorite) {
            mBigRemoteView.setImageViewResource(getResourceId(ID_IMG_NOTIFY_FAVORITE, "id"),
                    getResourceId(DRAWABLE_NOTIFY_BTN_FAVORITE, "drawable"));
        } else {
            //喜欢或收藏按钮没选中时样式
            mBigRemoteView.setImageViewResource(getResourceId(ID_IMG_NOTIFY_FAVORITE, "id"),
                    getResourceId(isDark ? DRAWABLE_NOTIFY_BTN_DARK_FAVORITE :
                            DRAWABLE_NOTIFY_BTN_LIGHT_FAVORITE, "drawable"));
        }
        mNotificationManager.notify(NOTIFICATION_ID, mNotification);
    }

    /**
     * 更新歌词按钮UI
     */
    @Override
    public void updateLyricsUI(boolean isChecked) {
        if (mNotification == null) {
            return;
        }
        boolean isDark = mColorUtils.isDarkNotificationBar(mService, mNotification);
        //歌词按钮选中时样式
        if (isChecked) {
            mBigRemoteView.setImageViewResource(getResourceId(ID_IMG_NOTIFY_LYRICS, "id"),
                    getResourceId(DRAWABLE_NOTIFY_BTN_LYRICS, "drawable"));
        } else {
            //歌词按钮没选中时样式
            mBigRemoteView.setImageViewResource(getResourceId(ID_IMG_NOTIFY_LYRICS, "id"),
                    getResourceId(isDark ? DRAWABLE_NOTIFY_BTN_DARK_LYRICS :
                            DRAWABLE_NOTIFY_BTN_LIGHT_LYRICS, "drawable"));
        }
        mNotificationManager.notify(NOTIFICATION_ID, mNotification);
    }


    private int getResourceId(String name, String className) {
        return res.getIdentifier(name, className, packageName);
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

    private void setFavoritePendingIntent(PendingIntent pendingIntent) {
        mFavoriteIntent = pendingIntent == null ? getPendingIntent(ACTION_FAVORITE) : pendingIntent;
    }

    private void setLyricsPendingIntent(PendingIntent pendingIntent) {
        mLyricsIntent = pendingIntent == null ? getPendingIntent(ACTION_LYRICS) : pendingIntent;
    }

    private void setDownloadPendingIntent(PendingIntent pendingIntent) {
        mDownloadIntent = pendingIntent == null ? getPendingIntent(ACTION_DOWNLOAD) : pendingIntent;
    }

    private void setClosePendingIntent(PendingIntent pendingIntent) {
        mCloseIntent = pendingIntent == null ? getPendingIntent(ACTION_CLOSE) : pendingIntent;
    }

    private void setPlayOrPauseIntent(PendingIntent pendingIntent) {
        mPlayOrPauseIntent = pendingIntent == null ? getPendingIntent(ACTION_PLAY_OR_PAUSE) : pendingIntent;
    }

    private PendingIntent getPendingIntent(String action) {
        Intent intent = new Intent(action);
        intent.setPackage(packageName);
        return PendingIntent.getBroadcast(mService, REQUEST_CODE, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

}
