package com.lzx.starrysky.notification;

import android.app.PendingIntent;


public class NotificationBuilder {

    public static NotificationBuilder getInstance() {
        return SingletonHolder.sInstance;
    }

    private static class SingletonHolder {
        private static final NotificationBuilder sInstance = new NotificationBuilder();
    }

    public static final int MODE_ACTIVITY = 0;
    public static final int MODE_BROADCAST = 1;
    public static final int MODE_SERVICE = 2;

    private boolean isCreateSystemNotification;   //是否使用系统通知栏
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

    public NotificationBuilder setCreateSystemNotification(boolean createSystemNotification) {
        isCreateSystemNotification = createSystemNotification;
        return this;
    }

    public NotificationBuilder setTargetClass(String targetClass) {
        this.targetClass = targetClass;
        return this;
    }

    public NotificationBuilder setContentTitle(String contentTitle) {
        this.contentTitle = contentTitle;
        return this;
    }

    public NotificationBuilder setContentText(String contentText) {
        this.contentText = contentText;
        return this;
    }

    public NotificationBuilder setNextIntent(PendingIntent nextIntent) {
        this.nextIntent = nextIntent;
        return this;
    }

    public NotificationBuilder setPreIntent(PendingIntent preIntent) {
        this.preIntent = preIntent;
        return this;
    }

    public NotificationBuilder setCloseIntent(PendingIntent closeIntent) {
        this.closeIntent = closeIntent;
        return this;
    }

    public NotificationBuilder setFavoriteIntent(PendingIntent favoriteIntent) {
        this.favoriteIntent = favoriteIntent;
        return this;
    }

    public NotificationBuilder setLyricsIntent(PendingIntent lyricsIntent) {
        this.lyricsIntent = lyricsIntent;
        return this;
    }

    public NotificationBuilder setPlayIntent(PendingIntent playIntent) {
        this.playIntent = playIntent;
        return this;
    }

    public NotificationBuilder setPauseIntent(PendingIntent pauseIntent) {
        this.pauseIntent = pauseIntent;
        return this;
    }

    public NotificationBuilder setStopIntent(PendingIntent stopIntent) {
        this.stopIntent = stopIntent;
        return this;
    }

    public NotificationBuilder setDownloadIntent(PendingIntent downloadIntent) {
        this.downloadIntent = downloadIntent;
        return this;
    }

    public NotificationBuilder setNotificationCanClearBySystemBtn(boolean notificationCanClearBySystemBtn) {
        this.isNotificationCanClearBySystemBtn = notificationCanClearBySystemBtn;
        return this;
    }

    public NotificationBuilder setPendingIntentMode(int pendingIntentMode) {
        this.pendingIntentMode = pendingIntentMode;
        return this;
    }

    public NotificationBuilder setSystemNotificationShowTime(boolean systemNotificationShowTime) {
        isSystemNotificationShowTime = systemNotificationShowTime;
        return this;
    }

    public NotificationBuilder setSkipPreviousDrawableRes(int skipPreviousdrawableRes) {
        this.skipPreviousDrawableRes = skipPreviousdrawableRes;
        return this;
    }

    public NotificationBuilder setSkipPreviousTitle(String skipPreviousTitle) {
        this.skipPreviousTitle = skipPreviousTitle;
        return this;
    }

    public int getSmallIconRes() {
        return smallIconRes;
    }

    public NotificationBuilder setSmallIconRes(int smallIconRes) {
        this.smallIconRes = smallIconRes;
        return this;
    }

    public int getSkipNextDrawableRes() {
        return skipNextDrawableRes;
    }

    public NotificationBuilder setSkipNextDrawableRes(int skipNextDrawableRes) {
        this.skipNextDrawableRes = skipNextDrawableRes;
        return this;
    }

    public NotificationBuilder setSkipNextTitle(String skipNextTitle) {
        this.skipNextTitle = skipNextTitle;
        return this;
    }

    public String getSkipNextTitle() {
        return skipNextTitle;
    }

    public String getLabelPause() {
        return labelPause;
    }

    public void setLabelPause(String labelPause) {
        this.labelPause = labelPause;
    }

    public int getPauseDrawableRes() {
        return pauseDrawableRes;
    }

    public NotificationBuilder setPauseDrawableRes(int pauseDrawableRes) {
        this.pauseDrawableRes = pauseDrawableRes;
        return this;
    }

    public String getLabelPlay() {
        return labelPlay;
    }

    public NotificationBuilder setLabelPlay(String labelPlay) {
        this.labelPlay = labelPlay;
        return this;
    }

    public int getPlayDrawableRes() {
        return playDrawableRes;
    }

    public NotificationBuilder setPlayDrawableRes(int playDrawableRes) {
        this.playDrawableRes = playDrawableRes;
        return this;
    }

    public String getSkipPreviousTitle() {
        return skipPreviousTitle;
    }

    public int getSkipPreviousDrawableRes() {
        return skipPreviousDrawableRes;
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
}
