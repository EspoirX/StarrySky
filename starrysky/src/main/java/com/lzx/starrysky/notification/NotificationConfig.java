package com.lzx.starrysky.notification;

import android.app.PendingIntent;

/**
 * 通知栏构建者，可设置各种通知栏配置
 */
public class NotificationConfig {

    public static final int MODE_ACTIVITY = 0;
    public static final int MODE_BROADCAST = 1;
    public static final int MODE_SERVICE = 2;

    private boolean isCreateSystemNotification = true;   //是否使用系统通知栏
    private boolean isNotificationCanClearBySystemBtn; //是否让通知栏当暂停的时候可以滑动清除
    private String targetClass; //通知栏点击转跳界面
    private String contentTitle; //通知栏标题
    private String contentText;  //通知栏内容
    private PendingIntent nextIntent; //下一首按钮 PendingIntent
    private PendingIntent preIntent; //上一首按钮 PendingIntent
    private PendingIntent closeIntent; //关闭按钮 PendingIntent
    private PendingIntent favoriteIntent; //喜欢或收藏按钮 PendingIntent
    private PendingIntent lyricsIntent; //桌面歌词按钮 PendingIntent
    private PendingIntent playIntent; //播放按钮 PendingIntent
    private PendingIntent pauseIntent; // 暂停按钮 PendingIntent
    private PendingIntent playOrPauseIntent; // 播放/暂停按钮 PendingIntent
    private PendingIntent stopIntent; //停止按钮 PendingIntent
    private PendingIntent downloadIntent; //下载按钮 PendingIntent
    private int pendingIntentMode; //通知栏点击模式
    private boolean isSystemNotificationShowTime; //系统通知栏是否显示时间

    private int skipPreviousDrawableRes = -1; //上一首的drawable res
    private String skipPreviousTitle = ""; //上一首的 title
    private int skipNextDrawableRes = -1; //下一首的drawable res
    private String skipNextTitle = ""; //下一首的 title
    private String labelPause = "";
    private int pauseDrawableRes = -1;
    private String labelPlay = "";
    private int playDrawableRes = -1;
    private int smallIconRes = -1;

    private NotificationConfig(Builder builder) {
        isCreateSystemNotification = builder.isCreateSystemNotification;
        isNotificationCanClearBySystemBtn = builder.isNotificationCanClearBySystemBtn;
        targetClass = builder.targetClass;
        contentTitle = builder.contentTitle;
        contentText = builder.contentText;
        nextIntent = builder.nextIntent;
        preIntent = builder.preIntent;
        closeIntent = builder.closeIntent;
        favoriteIntent = builder.favoriteIntent;
        lyricsIntent = builder.lyricsIntent;
        playIntent = builder.playIntent;
        pauseIntent = builder.pauseIntent;
        playOrPauseIntent = builder.playOrPauseIntent;
        stopIntent = builder.stopIntent;
        downloadIntent = builder.downloadIntent;
        pendingIntentMode = builder.pendingIntentMode;
        isSystemNotificationShowTime = builder.isSystemNotificationShowTime;
        skipPreviousDrawableRes = builder.skipPreviousDrawableRes;
        skipPreviousTitle = builder.skipPreviousTitle;
        skipNextDrawableRes = builder.skipNextDrawableRes;
        skipNextTitle = builder.skipNextTitle;
        labelPause = builder.labelPause;
        pauseDrawableRes = builder.pauseDrawableRes;
        labelPlay = builder.labelPlay;
        playDrawableRes = builder.playDrawableRes;
        smallIconRes = builder.smallIconRes;
    }

    public static class Builder {
        private boolean isCreateSystemNotification = true;   //是否使用系统通知栏
        private boolean isNotificationCanClearBySystemBtn; //是否让通知栏当暂停的时候可以滑动清除
        private String targetClass; //通知栏点击转跳界面
        private String contentTitle; //通知栏标题
        private String contentText;  //通知栏内容
        private PendingIntent nextIntent; //下一首按钮 PendingIntent
        private PendingIntent preIntent; //上一首按钮 PendingIntent
        private PendingIntent closeIntent; //关闭按钮 PendingIntent
        private PendingIntent favoriteIntent; //喜欢或收藏按钮 PendingIntent
        private PendingIntent lyricsIntent; //桌面歌词按钮 PendingIntent
        private PendingIntent playIntent; //播放按钮 PendingIntent
        private PendingIntent pauseIntent; // 暂停按钮 PendingIntent
        private PendingIntent playOrPauseIntent; // 播放/暂停按钮 PendingIntent
        private PendingIntent stopIntent; //停止按钮 PendingIntent
        private PendingIntent downloadIntent; //下载按钮 PendingIntent
        private int pendingIntentMode; //通知栏点击模式
        private boolean isSystemNotificationShowTime; //系统通知栏是否显示时间

        private int skipPreviousDrawableRes = -1; //上一首的drawable res
        private String skipPreviousTitle = ""; //上一首的 title
        private int skipNextDrawableRes = -1; //下一首的drawable res
        private String skipNextTitle = ""; //下一首的 title
        private String labelPause = "";
        private int pauseDrawableRes = -1;
        private String labelPlay = "";
        private int playDrawableRes = -1;
        private int smallIconRes = -1;

        public Builder setCreateSystemNotification(boolean createSystemNotification) {
            isCreateSystemNotification = createSystemNotification;
            return this;
        }

        public Builder setNotificationCanClearBySystemBtn(boolean notificationCanClearBySystemBtn) {
            isNotificationCanClearBySystemBtn = notificationCanClearBySystemBtn;
            return this;
        }

        public Builder setTargetClass(String targetClass) {
            this.targetClass = targetClass;
            return this;
        }

        public Builder setContentTitle(String contentTitle) {
            this.contentTitle = contentTitle;
            return this;
        }

        public Builder setContentText(String contentText) {
            this.contentText = contentText;
            return this;
        }

        public Builder setNextIntent(PendingIntent nextIntent) {
            this.nextIntent = nextIntent;
            return this;
        }

        public Builder setPreIntent(PendingIntent preIntent) {
            this.preIntent = preIntent;
            return this;
        }

        public Builder setCloseIntent(PendingIntent closeIntent) {
            this.closeIntent = closeIntent;
            return this;
        }

        public Builder setFavoriteIntent(PendingIntent favoriteIntent) {
            this.favoriteIntent = favoriteIntent;
            return this;
        }

        public Builder setLyricsIntent(PendingIntent lyricsIntent) {
            this.lyricsIntent = lyricsIntent;
            return this;
        }

        public Builder setPlayIntent(PendingIntent playIntent) {
            this.playIntent = playIntent;
            return this;
        }

        public Builder setPauseIntent(PendingIntent pauseIntent) {
            this.pauseIntent = pauseIntent;
            return this;
        }

        public Builder setPlayOrPauseIntent(PendingIntent playOrPauseIntent) {
            this.playOrPauseIntent = playOrPauseIntent;
            return this;
        }

        public Builder setStopIntent(PendingIntent stopIntent) {
            this.stopIntent = stopIntent;
            return this;
        }

        public Builder setDownloadIntent(PendingIntent downloadIntent) {
            this.downloadIntent = downloadIntent;
            return this;
        }

        public Builder setPendingIntentMode(int pendingIntentMode) {
            this.pendingIntentMode = pendingIntentMode;
            return this;
        }

        public Builder setSystemNotificationShowTime(boolean systemNotificationShowTime) {
            isSystemNotificationShowTime = systemNotificationShowTime;
            return this;
        }

        public Builder setSkipPreviousDrawableRes(int skipPreviousDrawableRes) {
            this.skipPreviousDrawableRes = skipPreviousDrawableRes;
            return this;
        }

        public Builder setSkipPreviousTitle(String skipPreviousTitle) {
            this.skipPreviousTitle = skipPreviousTitle;
            return this;
        }

        public Builder setSkipNextDrawableRes(int skipNextDrawableRes) {
            this.skipNextDrawableRes = skipNextDrawableRes;
            return this;
        }

        public Builder setSkipNextTitle(String skipNextTitle) {
            this.skipNextTitle = skipNextTitle;
            return this;
        }

        public Builder setLabelPause(String labelPause) {
            this.labelPause = labelPause;
            return this;
        }

        public Builder setPauseDrawableRes(int pauseDrawableRes) {
            this.pauseDrawableRes = pauseDrawableRes;
            return this;
        }

        public Builder setLabelPlay(String labelPlay) {
            this.labelPlay = labelPlay;
            return this;
        }

        public Builder setPlayDrawableRes(int playDrawableRes) {
            this.playDrawableRes = playDrawableRes;
            return this;
        }

        public Builder setSmallIconRes(int smallIconRes) {
            this.smallIconRes = smallIconRes;
            return this;
        }

        public NotificationConfig bulid() {
            return new NotificationConfig(this);
        }
    }

    public boolean isCreateSystemNotification() {
        return isCreateSystemNotification;
    }

    public boolean isNotificationCanClearBySystemBtn() {
        return isNotificationCanClearBySystemBtn;
    }

    public String getTargetClass() {
        return targetClass;
    }

    public String getContentTitle() {
        return contentTitle;
    }

    public String getContentText() {
        return contentText;
    }

    public PendingIntent getNextIntent() {
        return nextIntent;
    }

    public PendingIntent getPreIntent() {
        return preIntent;
    }

    public PendingIntent getCloseIntent() {
        return closeIntent;
    }

    public PendingIntent getFavoriteIntent() {
        return favoriteIntent;
    }

    public PendingIntent getLyricsIntent() {
        return lyricsIntent;
    }

    public PendingIntent getPlayIntent() {
        return playIntent;
    }

    public PendingIntent getPauseIntent() {
        return pauseIntent;
    }

    public PendingIntent getPlayOrPauseIntent() {
        return playOrPauseIntent;
    }

    public PendingIntent getStopIntent() {
        return stopIntent;
    }

    public PendingIntent getDownloadIntent() {
        return downloadIntent;
    }

    public int getPendingIntentMode() {
        return pendingIntentMode;
    }

    public boolean isSystemNotificationShowTime() {
        return isSystemNotificationShowTime;
    }

    public int getSkipPreviousDrawableRes() {
        return skipPreviousDrawableRes;
    }

    public String getSkipPreviousTitle() {
        return skipPreviousTitle;
    }

    public int getSkipNextDrawableRes() {
        return skipNextDrawableRes;
    }

    public String getSkipNextTitle() {
        return skipNextTitle;
    }

    public String getLabelPause() {
        return labelPause;
    }

    public int getPauseDrawableRes() {
        return pauseDrawableRes;
    }

    public String getLabelPlay() {
        return labelPlay;
    }

    public int getPlayDrawableRes() {
        return playDrawableRes;
    }

    public int getSmallIconRes() {
        return smallIconRes;
    }
}
