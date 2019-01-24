package com.lzx.starrysky.notification.factory;

public interface INotification {
    //action
    String ACTION_PLAY_PAUSE = "com.lzx.starrysky.play_pause";
    String ACTION_PAUSE = "com.lzx.starrysky.pause";
    String ACTION_PLAY = "com.lzx.starrysky.play";
    String ACTION_PREV = "com.lzx.starrysky.prev";
    String ACTION_NEXT = "com.lzx.starrysky.next";
    String ACTION_STOP = "com.lzx.starrysky.stop";
    String ACTION_CLOSE = "com.lzx.starrysky.close";
    String ACTION_FAVORITE = "com.lzx.starrysky.favorite";
    String ACTION_LYRICS = "com.lzx.starrysky.lyrics";
    String ACTION_DOWNLOAD = "com.lzx.starrysky.download";
    String ACTION_INTENT_CLICK = "com.lzx.starrysky.EXTRY_NOTIFICATION_TO_MAINACTIVITY";

    String CHANNEL_ID = "com.lzx.starrysky.MUSIC_CHANNEL_ID";

    void startNotification();

    void stopNotification();


}
