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
import android.widget.RemoteViews;

import com.lzx.musiclibrary.MusicService;
import com.lzx.musiclibrary.R;
import com.lzx.musiclibrary.aidl.model.SongInfo;
import com.lzx.musiclibrary.constans.State;
import com.lzx.musiclibrary.manager.MusicManager;
import com.lzx.musiclibrary.playback.PlaybackManager;
import com.lzx.musiclibrary.receiver.PlayerReceiver;
import com.lzx.musiclibrary.utils.AlbumArtCache;

/**
 * @author lzx
 * @date 2018/2/11
 */

public class MediaNotificationManager {

    public static final String CHANNEL_ID = "com.lzx.nicemusic.MUSIC_CHANNEL_ID";
    private static final int NOTIFICATION_ID = 412;
    private static final int REQUEST_CODE = 100;

    //action
    public static final String ACTION_PLAY_PAUSE = "com.lzx.nicemusic.play_pause";
    public static final String ACTION_PAUSE = "com.lzx.nicemusic.pause";
    public static final String ACTION_PLAY = "com.lzx.nicemusic.play";
    public static final String ACTION_PREV = "com.lzx.nicemusic.prev";
    public static final String ACTION_NEXT = "com.lzx.nicemusic.next";
    public static final String ACTION_STOP = "com.lzx.nicemusic.stop";
    public static final String ACTION_CLOSE = "com.lzx.nicemusic.close";
    public static final String ACTION_FAVORITE = "com.lzx.nicemusic.favorite";
    public static final String ACTION_LYRICS = "com.lzx.nicemusic.lyrics";
    public static final String ACTION_DOWNLOAD = "com.lzx.nicemusic.download";
    public static final String ACTION_INTENT_CLICK = "com.lzx.nicemusic.EXTRY_NOTIFICATION_TO_MAINACTIVITY";
    //布局
    private static final String LAYOUT_NOTIFY_DARK_PLAY = "view_notify_dark_play"; //黑色背景布局
    private static final String LAYOUT_NOTIFY_PLAY = "view_notify_light_play"; //白色背景布局
    private static final String LAYOUT_NOTIFY_BIG_DARK_PLAY = "view_notify_big_dark_play"; //黑色背景大布局
    private static final String LAYOUT_NOTIFY_BIG_PLAY = "view_notify_big_light_play"; //白色背景大布局
    //id
    private static final String ID_IMG_NOTIFY_PLAY = "img_notifyPlay"; //播放按钮id
    private static final String ID_IMG_NOTIFY_PAUSE = "img_notifyPause"; //暂停按钮id
    private static final String ID_IMG_NOTIFY_STOP = "img_notifyStop"; //停止按钮id
    private static final String ID_IMG_NOTIFY_FAVORITE = "img_notifyFavorite"; //喜欢/收藏按钮id
    private static final String ID_IMG_NOTIFY_LYRICS = "img_notifyLyrics"; //歌词按钮id
    private static final String ID_IMG_NOTIFY_DOWNLOAD = "img_notifyDownload"; //下载按钮id
    private static final String ID_IMG_NOTIFY_PLAY_OR_PAUSE = "img_notifyPlayOrPause"; //播放或暂停按钮id
    private static final String ID_IMG_NOTIFY_NEXT = "img_notifyNext"; //下一首按钮id
    private static final String ID_IMG_NOTIFY_PRE = "img_notifyPre"; //上一首按钮id
    private static final String ID_IMG_NOTIFY_CLOSE = "img_notifyClose"; //关闭按钮id
    private static final String ID_IMG_NOTIFY_ICON = "img_notifyIcon"; //封面图片id
    private static final String ID_TXT_NOTIFY_SONGNAME = "txt_notifySongName"; //歌名TextView id
    private static final String ID_TXT_NOTIFY_ARTISTNAME = "txt_notifyArtistName";//艺术家TextView id
    //资源
    private static final String DRAWABLE_ICON_NOTIFICATION = "icon_notification"; //通知栏 smallIcon 图片资源
    private static final String DRAWABLE_NOTIFY_BTN_FAVORITE = "notify_btn_favorite_checked";//喜欢按钮选中时的图片资源
    private static final String DRAWABLE_NOTIFY_BTN_LYRICS = "notify_btn_lyrics_checked";//歌词按钮选中时的图片资源
    //通知栏白色背景资源
    private static final String DRAWABLE_NOTIFY_BTN_LIGHT_PLAY_SELECTOR = "notify_btn_light_play_selector"; //白色背景时播放按钮selector
    private static final String DRAWABLE_NOTIFY_BTN_LIGHT_PAUSE_SELECTOR = "notify_btn_light_pause_selector";//白色背景时暂停按钮selector
    private static final String DRAWABLE_NOTIFY_BTN_LIGHT_FAVORITE = "notify_btn_light_favorite_normal";//白色背景时喜欢按钮的图片资源
    private static final String DRAWABLE_NOTIFY_BTN_LIGHT_LYRICS = "notify_btn_light_lyrics_normal";//白色背景时歌词按钮的图片资源
    private static final String DRAWABLE_NOTIFY_BTN_LIGHT_NEXT_PRESSED = "notify_btn_light_next_pressed";   //白色背景时下一首按钮按下时的图片资源
    private static final String DRAWABLE_NOTIFY_BTN_LIGHT_NEXT_SELECTOR = "notify_btn_light_next_selector"; //白色背景时下一首按钮selector
    private static final String DRAWABLE_NOTIFY_BTN_LIGHT_PREV_PRESSED = "notify_btn_light_prev_pressed";   //白色背景时上一首按钮按下时的图片资源
    private static final String DRAWABLE_NOTIFY_BTN_LIGHT_PREV_SELECTOR = "notify_btn_light_prev_selector"; //白色背景时上一首按钮selector
    //通知栏黑色背景资源
    private static final String DRAWABLE_NOTIFY_BTN_DARK_PLAY_SELECTOR = "notify_btn_dark_play_selector"; //黑色背景时播放按钮selector
    private static final String DRAWABLE_NOTIFY_BTN_DARK_PAUSE_SELECTOR = "notify_btn_dark_pause_selector";//黑色背景时暂停按钮selector
    private static final String DRAWABLE_NOTIFY_BTN_DARK_FAVORITE = "notify_btn_dark_favorite_normal";//黑色背景时喜欢按钮按下时的图片资源
    private static final String DRAWABLE_NOTIFY_BTN_DARK_LYRICS = "notify_btn_dark_lyrics_normal";//黑色背景时歌词按钮按下时的图片资源
    private static final String DRAWABLE_NOTIFY_BTN_DARK_NEXT_PRESSED = "notify_btn_dark_next_pressed";   //黑色背景时下一首按钮按下时的图片资源
    private static final String DRAWABLE_NOTIFY_BTN_DARK_NEXT_SELECTOR = "notify_btn_dark_next_selector"; //黑色背景时下一首按钮selector
    private static final String DRAWABLE_NOTIFY_BTN_DARK_PREV_PRESSED = "notify_btn_dark_prev_pressed";   //黑色背景时上一首按钮按下时的图片资源
    private static final String DRAWABLE_NOTIFY_BTN_DARK_PREV_SELECTOR = "notify_btn_dark_prev_selector"; //黑色背景时上一首按钮selector

    private final NotificationManager mNotificationManager;

    private RemoteViews mRemoteView;
    private RemoteViews mBigRemoteView;
    private PendingIntent playIntent;
    private PendingIntent pauseIntent;
    private PendingIntent stopIntent;
    private PendingIntent startOrPauseIntent;
    private PendingIntent nextIntent;
    private PendingIntent preIntent;
    private PendingIntent closeIntent;
    private PendingIntent favoriteIntent;
    private PendingIntent lyricsIntent;
    private PendingIntent downloadIntent;
    private PendingIntent contentIntent;

    private Resources res;
    private String packageName;

    private boolean mStarted = false;
    private SongInfo mSongInfo;
    private MusicService mService;
    private NotificationCreater mNotificationCreater;
    private Notification mNotification;
    private PlaybackManager mPlaybackManager;

    public MediaNotificationManager(MusicService musicService, NotificationCreater creater, PlaybackManager playbackManager) {
        mService = musicService;
        mNotificationCreater = creater;
        mPlaybackManager = playbackManager;

        setStartOrPausePendingIntent(creater.getStartOrPauseIntent());
        setNextPendingIntent(creater.getNextIntent());
        setPrePendingIntent(creater.getPreIntent());
        setClosePendingIntent(creater.getCloseIntent());
        setFavoritePendingIntent(creater.getFavoriteIntent());
        setLyricsPendingIntent(creater.getLyricsIntent());
        setPlayPendingIntent(creater.getPlayIntent());
        setPausePendingIntent(creater.getPauseIntent());
        setStopPendingIntent(creater.getStopIntent());
        setDownloadPendingIntent(creater.getDownloadIntent());

        mNotificationManager = (NotificationManager) mService.getSystemService(Service.NOTIFICATION_SERVICE);
        packageName = mService.getApplicationContext().getPackageName();
        res = mService.getApplicationContext().getResources();
        if (mNotificationManager != null) {
            mNotificationManager.cancelAll();
        }
    }

    /**
     * 关闭通知栏
     */
    public void stopNotification() {
        if (mStarted) {
            mStarted = false;
            try {
                mNotificationManager.cancel(NOTIFICATION_ID);
            } catch (IllegalArgumentException ex) {
                // ignore if the receiver is not registered.
            }
            mService.stopForeground(true);
        }
    }

    /**
     * 开始通知栏
     */
    public void startNotification(SongInfo songInfo) {
        if (songInfo == null) {
            return;
        }
        mSongInfo = songInfo;
        if (!mStarted) {
            if (mNotificationCreater != null && !TextUtils.isEmpty(mNotificationCreater.getTargetClass())) {
                Class clazz = getTargetClass(mNotificationCreater.getTargetClass());
                if (clazz == null) {
                    return;
                }
                boolean isDark = NotificationColorUtils.isDarkNotificationBar(mService);
                mRemoteView = createRemoteViews(isDark, false);
                mBigRemoteView = createRemoteViews(isDark, true);
                if (mRemoteView == null) {
                    return;
                }
                contentIntent = createContentIntent(mSongInfo, null, clazz);
                mNotification = createNotification(isDark);
                if (mNotification != null) {
                    mService.startForeground(NOTIFICATION_ID, mNotification);
                    mStarted = true;
                }
            }
        } else {
            updateModelDetail(mSongInfo);
        }
    }

    /**
     * 得到目标界面 Class
     */
    private Class getTargetClass(String targetClass) {
        Class clazz = null;
        try {
            clazz = Class.forName(targetClass);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return clazz;
    }

    /**
     * 创建RemoteViews
     *
     * @param isDark 通知栏背景是否为黑色
     */
    private RemoteViews createRemoteViews(boolean isDark, boolean isBigRemoteViews) {
        RemoteViews remoteView;
        if (isBigRemoteViews) {
            remoteView = new RemoteViews(packageName, getResourceId(isDark ? LAYOUT_NOTIFY_BIG_DARK_PLAY :
                    LAYOUT_NOTIFY_BIG_PLAY, "layout"));
        } else {
            remoteView = new RemoteViews(packageName, getResourceId(isDark ? LAYOUT_NOTIFY_DARK_PLAY :
                    LAYOUT_NOTIFY_PLAY, "layout"));
        }
        if (playIntent != null) {
            remoteView.setOnClickPendingIntent(getResourceId(ID_IMG_NOTIFY_PLAY, "id"), playIntent);
        }
        if (pauseIntent != null) {
            remoteView.setOnClickPendingIntent(getResourceId(ID_IMG_NOTIFY_PAUSE, "id"), pauseIntent);
        }
        if (stopIntent != null) {
            remoteView.setOnClickPendingIntent(getResourceId(ID_IMG_NOTIFY_STOP, "id"), stopIntent);
        }
        if (favoriteIntent != null) {
            remoteView.setOnClickPendingIntent(getResourceId(ID_IMG_NOTIFY_FAVORITE, "id"), favoriteIntent);
        }
        if (lyricsIntent != null) {
            remoteView.setOnClickPendingIntent(getResourceId(ID_IMG_NOTIFY_LYRICS, "id"), lyricsIntent);
        }
        if (downloadIntent != null) {
            remoteView.setOnClickPendingIntent(getResourceId(ID_IMG_NOTIFY_DOWNLOAD, "id"), downloadIntent);
        }
        if (startOrPauseIntent != null) {
            remoteView.setOnClickPendingIntent(getResourceId(ID_IMG_NOTIFY_PLAY_OR_PAUSE, "id"), startOrPauseIntent);
        }
        if (nextIntent != null) {
            remoteView.setOnClickPendingIntent(getResourceId(ID_IMG_NOTIFY_NEXT, "id"), nextIntent);
        }
        if (preIntent != null) {
            remoteView.setOnClickPendingIntent(getResourceId(ID_IMG_NOTIFY_PRE, "id"), preIntent);
        }
        if (closeIntent != null) {
            remoteView.setOnClickPendingIntent(getResourceId(ID_IMG_NOTIFY_CLOSE, "id"), closeIntent);
        }
        return remoteView;
    }

    /**
     * 创建Notification
     */
    NotificationCompat.Builder notificationBuilder;

    private Notification createNotification(boolean isDark) {
        int smallIconRes = getResourceId(DRAWABLE_ICON_NOTIFICATION, "drawable");
        String contentTitle = mSongInfo != null ? mSongInfo.getSongName() : mNotificationCreater.getContentTitle();
        String contentText = mSongInfo != null ? mSongInfo.getArtist() : mNotificationCreater.getContentText();

        changeTextColor(mRemoteView, mBigRemoteView);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
        }
        notificationBuilder = new NotificationCompat.Builder(mService, CHANNEL_ID);


        notificationBuilder
                .setSmallIcon(smallIconRes)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOnlyAlertOnce(true)
                .setContentIntent(contentIntent)
                .setContentTitle(contentTitle)
                .setContentText(contentText);

        if (Build.VERSION.SDK_INT >= 16) {
            notificationBuilder.setPriority(2);
        }
        if (Build.VERSION.SDK_INT >= 24) {
            notificationBuilder.setCustomContentView(mRemoteView);
            if (mBigRemoteView != null) {
                notificationBuilder.setCustomBigContentView(mBigRemoteView);
            }
        }
        setNotificationPlaybackState(notificationBuilder);

        Notification notification;
        if (Build.VERSION.SDK_INT >= 16) {
            notification = notificationBuilder.build();
        } else {
            notification = notificationBuilder.getNotification();
        }
        if (Build.VERSION.SDK_INT < 24) {
            notification.contentView = mRemoteView;
            if (Build.VERSION.SDK_INT >= 16 && mBigRemoteView != null) {
                notification.bigContentView = mBigRemoteView;
            }
        }

        updateRemoteViewUI(notification, smallIconRes, isDark);

        return notification;
    }

    private void setNotificationPlaybackState(NotificationCompat.Builder builder) {
        if (mSongInfo == null || !mStarted) {
            mService.stopForeground(true);
            return;
        }
        builder.setOngoing(MusicManager.get().getStatus() == State.STATE_PLAYING);
    }

    /**
     * 更新RemoteView
     */
    private void updateRemoteViewUI(Notification notification, int smallIconRes, boolean isDark) {
        mRemoteView.setTextViewText(getResourceId(ID_TXT_NOTIFY_SONGNAME, "id"), mSongInfo.getSongName());
        mRemoteView.setTextViewText(getResourceId(ID_TXT_NOTIFY_ARTISTNAME, "id"), mSongInfo.getArtist());

        mRemoteView.setImageViewResource(getResourceId(ID_IMG_NOTIFY_PLAY_OR_PAUSE, "id"),
                getResourceId(isDark ? DRAWABLE_NOTIFY_BTN_DARK_PAUSE_SELECTOR :
                        DRAWABLE_NOTIFY_BTN_LIGHT_PAUSE_SELECTOR, "drawable"));

        if (mBigRemoteView != null) {
            mBigRemoteView.setTextViewText(getResourceId(ID_TXT_NOTIFY_SONGNAME, "id"), mSongInfo.getSongName());
            mBigRemoteView.setTextViewText(getResourceId(ID_TXT_NOTIFY_ARTISTNAME, "id"), mSongInfo.getArtist());
            mBigRemoteView.setImageViewResource(getResourceId(ID_IMG_NOTIFY_PLAY_OR_PAUSE, "id"),
                    getResourceId(isDark ? DRAWABLE_NOTIFY_BTN_DARK_PAUSE_SELECTOR :
                            DRAWABLE_NOTIFY_BTN_LIGHT_PAUSE_SELECTOR, "drawable"));
            mBigRemoteView.setImageViewResource(getResourceId(ID_IMG_NOTIFY_FAVORITE, "id"),
                    getResourceId(isDark ? DRAWABLE_NOTIFY_BTN_DARK_FAVORITE :
                            DRAWABLE_NOTIFY_BTN_LIGHT_FAVORITE, "drawable"));
            mBigRemoteView.setImageViewResource(getResourceId(ID_IMG_NOTIFY_LYRICS, "id"),
                    getResourceId(isDark ? DRAWABLE_NOTIFY_BTN_DARK_LYRICS :
                            DRAWABLE_NOTIFY_BTN_LIGHT_LYRICS, "drawable"));
        }

        if (mPlaybackManager.hasNextOrPre()) {
            disableNextBtn(true, isDark);
            disablePreBtn(true, isDark);
        } else {
            disableNextBtn(false, isDark);
            disablePreBtn(false, isDark);
        }

        mNotificationManager.notify(NOTIFICATION_ID, notification);

        String fetchArtUrl = null;
        Bitmap art = null;
        if (!TextUtils.isEmpty(mSongInfo.getSongCover())) {
            String artUrl = mSongInfo.getSongCover();
            art = AlbumArtCache.getInstance().getBigImage(artUrl);
            if (art == null) {
                fetchArtUrl = artUrl;
                art = BitmapFactory.decodeResource(mService.getResources(), smallIconRes);
            }
        }
        if (fetchArtUrl != null) {
            fetchBitmapFromURLAsync(fetchArtUrl, notification);
        } else {
            mRemoteView.setImageViewBitmap(getResourceId(ID_IMG_NOTIFY_ICON, "id"), art);
            if (mBigRemoteView != null) {
                mBigRemoteView.setImageViewBitmap(getResourceId(ID_IMG_NOTIFY_ICON, "id"), art);
            }
            mNotificationManager.notify(NOTIFICATION_ID, notification);
        }
    }

    /**
     * 下一首按钮样式
     */
    private void disableNextBtn(boolean disable, boolean isDark) {
        int res;
        if (mRemoteView != null || mBigRemoteView != null) {
            if (disable) {
                if (isDark) {
                    res = this.getResourceId(DRAWABLE_NOTIFY_BTN_DARK_NEXT_PRESSED, "drawable");
                } else {
                    res = this.getResourceId(DRAWABLE_NOTIFY_BTN_LIGHT_NEXT_PRESSED, "drawable");
                }
            } else if (isDark) {
                res = this.getResourceId(DRAWABLE_NOTIFY_BTN_DARK_NEXT_SELECTOR, "drawable");
            } else {
                res = this.getResourceId(DRAWABLE_NOTIFY_BTN_LIGHT_NEXT_SELECTOR, "drawable");
            }
            mRemoteView.setImageViewResource(getResourceId(ID_IMG_NOTIFY_NEXT, "id"), res);
            if (mBigRemoteView != null) {
                mBigRemoteView.setImageViewResource(getResourceId(ID_IMG_NOTIFY_NEXT, "id"), res);
            }
        }
    }

    /**
     * 上一首按钮样式
     */
    private void disablePreBtn(boolean disable, boolean isDark) {
        int res;
        if (mRemoteView != null || mBigRemoteView != null) {
            if (disable) {
                res = this.getResourceId(isDark ? DRAWABLE_NOTIFY_BTN_DARK_PREV_PRESSED :
                        DRAWABLE_NOTIFY_BTN_LIGHT_PREV_PRESSED, "drawable");
            } else {
                res = this.getResourceId(isDark ? DRAWABLE_NOTIFY_BTN_DARK_PREV_SELECTOR :
                        DRAWABLE_NOTIFY_BTN_LIGHT_PREV_SELECTOR, "drawable");
            }
            if (mBigRemoteView != null) {
                mRemoteView.setImageViewResource(getResourceId(ID_IMG_NOTIFY_PRE, "id"), res);
            }
        }
    }

    /**
     * 加载歌曲封面
     */
    private void fetchBitmapFromURLAsync(final String bitmapUrl, final Notification notification) {
        AlbumArtCache.getInstance().fetch(bitmapUrl, new AlbumArtCache.FetchListener() {
            @Override
            public void onFetched(String artUrl, Bitmap bitmap, Bitmap icon) {
                if (mSongInfo != null && !TextUtils.isEmpty(mSongInfo.getSongCover()) && mSongInfo.getSongCover().equals(artUrl)) {
                    mRemoteView.setImageViewBitmap(getResourceId(ID_IMG_NOTIFY_ICON, "id"), bitmap);
                    if (mBigRemoteView != null) {
                        mBigRemoteView.setImageViewBitmap(getResourceId(ID_IMG_NOTIFY_ICON, "id"), bitmap);
                    }
                    mNotificationManager.notify(NOTIFICATION_ID, notification);
                }
            }
        });
    }

    /**
     * 开始播放的时候
     */
    public void updateViewStateAtStart() {
        if (mNotification != null) {
            boolean isDark = NotificationColorUtils.isDarkNotificationBar(mService);
            mRemoteView = createRemoteViews(isDark, false);
            mBigRemoteView = createRemoteViews(isDark, true);
            if (Build.VERSION.SDK_INT >= 16) {
                mNotification.bigContentView = mBigRemoteView;
            }
            mNotification.contentView = mRemoteView;
            if (mRemoteView != null) {
                mRemoteView.setImageViewResource(getResourceId(ID_IMG_NOTIFY_PLAY_OR_PAUSE, "id"),
                        getResourceId(isDark ? DRAWABLE_NOTIFY_BTN_DARK_PAUSE_SELECTOR :
                                DRAWABLE_NOTIFY_BTN_LIGHT_PAUSE_SELECTOR, "drawable"));
                if (mBigRemoteView != null) {
                    mBigRemoteView.setImageViewResource(getResourceId(ID_IMG_NOTIFY_PLAY_OR_PAUSE, "id"),
                            getResourceId(isDark ? DRAWABLE_NOTIFY_BTN_DARK_PAUSE_SELECTOR :
                                    DRAWABLE_NOTIFY_BTN_LIGHT_PAUSE_SELECTOR, "drawable"));
                }
                mNotificationManager.notify(NOTIFICATION_ID, mNotification);
            }
        }
    }

    /**
     * 暂停播放和播放完成的时候
     */
    public void updateViewStateAtPause() {
        if (mNotification != null) {
            boolean isDark = NotificationColorUtils.isDarkNotificationBar(mService);
            mRemoteView = createRemoteViews(isDark, false);
            mBigRemoteView = createRemoteViews(isDark, true);
            if (Build.VERSION.SDK_INT >= 16) {
                mNotification.bigContentView = mBigRemoteView;
            }
            mNotification.contentView = mRemoteView;
            if (mRemoteView != null) {
                mRemoteView.setImageViewResource(getResourceId(ID_IMG_NOTIFY_PLAY_OR_PAUSE, "id"),
                        getResourceId(isDark ? DRAWABLE_NOTIFY_BTN_DARK_PLAY_SELECTOR :
                                DRAWABLE_NOTIFY_BTN_LIGHT_PLAY_SELECTOR, "drawable"));
                if (mBigRemoteView != null) {
                    mBigRemoteView.setImageViewResource(getResourceId(ID_IMG_NOTIFY_PLAY_OR_PAUSE, "id"),
                            getResourceId(isDark ? DRAWABLE_NOTIFY_BTN_DARK_PLAY_SELECTOR :
                                    DRAWABLE_NOTIFY_BTN_LIGHT_PLAY_SELECTOR, "drawable"));
                }
                mNotificationManager.notify(NOTIFICATION_ID, mNotification);
            }
        }
    }

    /**
     * 切歌的时候
     */
    private void updateModelDetail(SongInfo songInfo) {
        if (mNotification != null) {
            boolean isDark = NotificationColorUtils.isDarkNotificationBar(mService);
            mRemoteView = createRemoteViews(isDark, false);
            mBigRemoteView = createRemoteViews(isDark, true);
            if (Build.VERSION.SDK_INT >= 16) {
                mNotification.bigContentView = mBigRemoteView;
            }
            mNotification.contentView = mRemoteView;
            int smallIconRes = getResourceId(DRAWABLE_ICON_NOTIFICATION, "drawable");
            if (mRemoteView != null && songInfo != null) {
                updateRemoteViewUI(mNotification, smallIconRes, isDark);
            }
        }
    }

    /**
     * 喜欢按钮
     */
    public void updateFavorite(boolean isFavorite) {
        if (mNotification != null) {
            boolean isDark = NotificationColorUtils.isDarkNotificationBar(mService);
            mRemoteView = createRemoteViews(isDark, false);
            mBigRemoteView = createRemoteViews(isDark, true);
            if (Build.VERSION.SDK_INT >= 16) {
                mNotification.bigContentView = mBigRemoteView;
            }
            mNotification.contentView = mRemoteView;
            if (mRemoteView != null && mBigRemoteView != null) {
                if (isFavorite) {
                    mBigRemoteView.setImageViewResource(getResourceId(ID_IMG_NOTIFY_FAVORITE, "id"),
                            getResourceId(DRAWABLE_NOTIFY_BTN_FAVORITE, "drawable"));
                } else {
                    mBigRemoteView.setImageViewResource(getResourceId(ID_IMG_NOTIFY_FAVORITE, "id"),
                            getResourceId(isDark ? DRAWABLE_NOTIFY_BTN_DARK_FAVORITE :
                                    DRAWABLE_NOTIFY_BTN_LIGHT_FAVORITE, "drawable"));
                }
                mNotificationManager.notify(NOTIFICATION_ID, mNotification);
            }
        }
    }

    /**
     * 歌词按钮
     */
    public void updateLyrics(boolean isChecked) {
        if (mNotification != null) {
            boolean isDark = NotificationColorUtils.isDarkNotificationBar(mService);
            mRemoteView = createRemoteViews(isDark, false);
            mBigRemoteView = createRemoteViews(isDark, true);
            if (Build.VERSION.SDK_INT >= 16) {
                mNotification.bigContentView = mBigRemoteView;
            }
            mNotification.contentView = mRemoteView;
            if (mRemoteView != null && mBigRemoteView != null) {
                if (isChecked) {
                    mBigRemoteView.setImageViewResource(getResourceId(ID_IMG_NOTIFY_LYRICS, "id"),
                            getResourceId(DRAWABLE_NOTIFY_BTN_LYRICS, "drawable"));
                } else {
                    mBigRemoteView.setImageViewResource(getResourceId(ID_IMG_NOTIFY_LYRICS, "id"),
                            getResourceId(isDark ? DRAWABLE_NOTIFY_BTN_DARK_LYRICS :
                                    DRAWABLE_NOTIFY_BTN_LIGHT_LYRICS, "drawable"));
                }
                mNotificationManager.notify(NOTIFICATION_ID, mNotification);
            }
        }
    }

    /**
     * 更新 ContentIntent
     */
    public void updateContentIntent(Bundle bundle) {
        if (mNotification != null && mNotificationCreater != null && !TextUtils.isEmpty(mNotificationCreater.getTargetClass())) {
            Class clazz = getTargetClass(mNotificationCreater.getTargetClass());
            if (clazz == null) {
                return;
            }
            contentIntent = createContentIntent(mSongInfo, bundle, clazz);
            startNotification(mSongInfo);
        }
    }

    private void setStartOrPausePendingIntent(PendingIntent pendingIntent) {
        startOrPauseIntent = pendingIntent == null ? getPendingIntent(ACTION_PLAY_PAUSE) : pendingIntent;
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

    private void setFavoritePendingIntent(PendingIntent pendingIntent) {
        favoriteIntent = pendingIntent == null ? getPendingIntent(ACTION_FAVORITE) : pendingIntent;
    }

    private void setLyricsPendingIntent(PendingIntent pendingIntent) {
        lyricsIntent = pendingIntent == null ? getPendingIntent(ACTION_LYRICS) : pendingIntent;
    }

    private void setPlayPendingIntent(PendingIntent pendingIntent) {
        playIntent = pendingIntent == null ? getPendingIntent(ACTION_PLAY) : pendingIntent;
    }

    private void setPausePendingIntent(PendingIntent pendingIntent) {
        pauseIntent = pendingIntent == null ? getPendingIntent(ACTION_PAUSE) : pendingIntent;
    }

    private void setStopPendingIntent(PendingIntent pendingIntent) {
        stopIntent = pendingIntent == null ? getPendingIntent(ACTION_STOP) : pendingIntent;
    }

    private void setDownloadPendingIntent(PendingIntent pendingIntent) {
        downloadIntent = pendingIntent == null ? getPendingIntent(ACTION_DOWNLOAD) : pendingIntent;
    }

    private PendingIntent getPendingIntent(String action) {
        Intent intent = new Intent(action);
        intent.setClass(mService, PlayerReceiver.class);
        return PendingIntent.getBroadcast(mService, 0, intent, 0);
    }

    private <T> PendingIntent createContentIntent(SongInfo songInfo, Bundle bundle, Class<T> targetClass) {
        Intent openUI = new Intent(mService, targetClass);
        openUI.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        openUI.putExtra("notification_entry", ACTION_INTENT_CLICK);
        if (songInfo != null) {
            openUI.putExtra("SongInfo", songInfo);
        }
        @SuppressLint("WrongConstant")
        PendingIntent pendingIntent = PendingIntent.getActivity(mService, REQUEST_CODE, openUI, PendingIntent.FLAG_CANCEL_CURRENT);
        return pendingIntent;
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

    private int getResourceId(String name, String className) {
        return res.getIdentifier(name, className, packageName);
    }

    private void changeTextColor(RemoteViews remoteView, RemoteViews bigRemoteView) {
        if (bigRemoteView != null) {
            NotificationColorUtils.setTitleTextColor(mService, bigRemoteView, this.getResourceId(ID_TXT_NOTIFY_SONGNAME, "id"));
            NotificationColorUtils.setContentTextColor(mService, bigRemoteView, this.getResourceId(ID_TXT_NOTIFY_ARTISTNAME, "id"));
        }
        NotificationColorUtils.setTitleTextColor(mService, remoteView, this.getResourceId(ID_TXT_NOTIFY_SONGNAME, "id"));
        NotificationColorUtils.setContentTextColor(mService, remoteView, this.getResourceId(ID_TXT_NOTIFY_ARTISTNAME, "id"));
    }
}
