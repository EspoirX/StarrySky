package com.lzx.musiclibrary.notification;

import android.app.Notification;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.lzx.musiclibrary.aidl.model.SongInfo;

/**
 * Created by xian on 2018/3/17.
 */

public interface IMediaNotification {

    String CHANNEL_ID = "com.lzx.nicemusic.MUSIC_CHANNEL_ID";
    int NOTIFICATION_ID = 412;
    int REQUEST_CODE = 100;

    //action
    String ACTION_PLAY_PAUSE = "com.lzx.nicemusic.play_pause";
    String ACTION_PAUSE = "com.lzx.nicemusic.pause";
    String ACTION_PLAY = "com.lzx.nicemusic.play";
    String ACTION_PREV = "com.lzx.nicemusic.prev";
    String ACTION_NEXT = "com.lzx.nicemusic.next";
    String ACTION_STOP = "com.lzx.nicemusic.stop";
    String ACTION_CLOSE = "com.lzx.nicemusic.close";
    String ACTION_FAVORITE = "com.lzx.nicemusic.favorite";
    String ACTION_LYRICS = "com.lzx.nicemusic.lyrics";
    String ACTION_DOWNLOAD = "com.lzx.nicemusic.download";
    String ACTION_INTENT_CLICK = "com.lzx.nicemusic.EXTRY_NOTIFICATION_TO_MAINACTIVITY";

    void startNotification(SongInfo songInfo);

    void stopNotification();

    void updateViewStateAtStart();

    void updateViewStateAtPause();

    void updateFavorite(boolean isFavorite);

    void updateLyrics(boolean isChecked);

    void updateModelDetail(SongInfo songInfo);

    void updateContentIntent(Bundle bundle, String targetClass);
}
