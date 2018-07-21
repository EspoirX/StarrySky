package com.lzx.musiclibrary.notification;

import android.app.PendingIntent;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by xian on 2018/2/22.
 */

public class NotificationCreater implements Parcelable {


    private boolean isCreateSystemNotification;   //是否使用系统通知栏
    private boolean isNotificationCanClearBySystemBtn; //是否让通知栏当暂停的时候可以滑动清除
    private String targetClass; //通知栏点击转跳界面
    private String contentTitle; //通知栏标题
    private String contentText;  //通知栏内容
    private PendingIntent startOrPauseIntent; //播放或暂停按钮 PendingIntent
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

    private NotificationCreater(Builder builder) {
        this.isCreateSystemNotification = builder.isCreateSystemNotification;
        this.isNotificationCanClearBySystemBtn = builder.isNotificationCanClearBySystemBtn;
        this.targetClass = builder.targetClass;
        this.contentTitle = builder.contentTitle;
        this.contentText = builder.contentText;
        this.startOrPauseIntent = builder.startOrPauseIntent;
        this.nextIntent = builder.nextIntent;
        this.preIntent = builder.preIntent;
        this.closeIntent = builder.closeIntent;
        this.favoriteIntent = builder.favoriteIntent;
        this.lyricsIntent = builder.lyricsIntent;
        this.playIntent = builder.playIntent;
        this.pauseIntent = builder.pauseIntent;
        this.stopIntent = builder.stopIntent;
        this.downloadIntent = builder.downloadIntent;
        this.pendingIntentMode = builder.pendingIntentMode;
        this.isSystemNotificationShowTime = builder.isSystemNotificationShowTime;
    }

    public static class Builder {
        private boolean isCreateSystemNotification = false;
        private boolean isNotificationCanClearBySystemBtn = false;
        private String targetClass;
        private String contentTitle;
        private String contentText;
        private PendingIntent startOrPauseIntent;
        private PendingIntent nextIntent;
        private PendingIntent preIntent;
        private PendingIntent closeIntent;
        private PendingIntent favoriteIntent;
        private PendingIntent lyricsIntent;
        private PendingIntent playIntent;
        private PendingIntent pauseIntent;
        private PendingIntent stopIntent;
        private PendingIntent downloadIntent;
        private int pendingIntentMode;
        private boolean isSystemNotificationShowTime;

        public Builder setCreateSystemNotification(boolean createSystemNotification) {
            isCreateSystemNotification = createSystemNotification;
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

        public Builder setStartOrPauseIntent(PendingIntent startOrPauseIntent) {
            this.startOrPauseIntent = startOrPauseIntent;
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

        public Builder setStopIntent(PendingIntent stopIntent) {
            this.stopIntent = stopIntent;
            return this;
        }

        public Builder setDownloadIntent(PendingIntent downloadIntent) {
            this.downloadIntent = downloadIntent;
            return this;
        }

        public Builder setNotificationCanClearBySystemBtn(boolean notificationCanClearBySystemBtn) {
            this.isNotificationCanClearBySystemBtn = notificationCanClearBySystemBtn;
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

        public NotificationCreater build() {
            return new NotificationCreater(this);
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

    public PendingIntent getStartOrPauseIntent() {
        return startOrPauseIntent;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(this.isCreateSystemNotification ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isNotificationCanClearBySystemBtn ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isSystemNotificationShowTime ? (byte) 1 : (byte) 0);
        dest.writeString(this.targetClass);
        dest.writeString(this.contentTitle);
        dest.writeString(this.contentText);
        dest.writeParcelable(this.startOrPauseIntent, flags);
        dest.writeParcelable(this.nextIntent, flags);
        dest.writeParcelable(this.preIntent, flags);
        dest.writeParcelable(this.closeIntent, flags);
        dest.writeParcelable(this.favoriteIntent, flags);
        dest.writeParcelable(this.lyricsIntent, flags);
        dest.writeParcelable(this.playIntent, flags);
        dest.writeParcelable(this.pauseIntent, flags);
        dest.writeParcelable(this.stopIntent, flags);
        dest.writeParcelable(this.downloadIntent, flags);
        dest.writeInt(this.pendingIntentMode);
    }

    protected NotificationCreater(Parcel in) {
        this.isCreateSystemNotification = in.readByte() != 0;
        this.isNotificationCanClearBySystemBtn = in.readByte() != 0;
        this.isSystemNotificationShowTime = in.readByte() != 0;
        this.targetClass = in.readString();
        this.contentTitle = in.readString();
        this.contentText = in.readString();
        this.startOrPauseIntent = in.readParcelable(PendingIntent.class.getClassLoader());
        this.nextIntent = in.readParcelable(PendingIntent.class.getClassLoader());
        this.preIntent = in.readParcelable(PendingIntent.class.getClassLoader());
        this.closeIntent = in.readParcelable(PendingIntent.class.getClassLoader());
        this.favoriteIntent = in.readParcelable(PendingIntent.class.getClassLoader());
        this.lyricsIntent = in.readParcelable(PendingIntent.class.getClassLoader());
        this.playIntent = in.readParcelable(PendingIntent.class.getClassLoader());
        this.pauseIntent = in.readParcelable(PendingIntent.class.getClassLoader());
        this.stopIntent = in.readParcelable(PendingIntent.class.getClassLoader());
        this.downloadIntent = in.readParcelable(PendingIntent.class.getClassLoader());
        this.pendingIntentMode = in.readInt();
    }

    public static final Parcelable.Creator<NotificationCreater> CREATOR = new Parcelable.Creator<NotificationCreater>() {
        @Override
        public NotificationCreater createFromParcel(Parcel source) {
            return new NotificationCreater(source);
        }

        @Override
        public NotificationCreater[] newArray(int size) {
            return new NotificationCreater[size];
        }
    };
}
