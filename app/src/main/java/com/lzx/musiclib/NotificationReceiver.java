package com.lzx.musiclib;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;

import com.lzx.starrysky.manager.MusicManager;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (TextUtils.isEmpty(action)) {
            return;
        }
        if (TestApplication.ACTION_PLAY_OR_PAUSE.equals(action)) {
            int state = MusicManager.getInstance().getState();
            if (state == PlaybackStateCompat.STATE_PLAYING) {
                MusicManager.getInstance().pauseMusic();
            } else {
                MusicManager.getInstance().playMusic();
            }
        }
        if (TestApplication.ACTION_NEXT.equals(action)) {
            MusicManager.getInstance().skipToNext();
        }
        if (TestApplication.ACTION_PRE.equals(action)) {
            MusicManager.getInstance().skipToPrevious();
        }
        if (TestApplication.ACTION_FAVORITE.equals(action)) {
            //这里实现自己的喜欢或收藏逻辑，如果选中可以传 true 把按钮变成选中状态，false 就非选中状态
            MusicManager.getInstance().updateFavoriteUI(true);
        }
        if (TestApplication.ACTION_LYRICS.equals(action)) {
            //这里实现自己的是否显示歌词逻辑，如果选中可以传 true 把按钮变成选中状态，false 就非选中状态
            MusicManager.getInstance().updateLyricsUI(true);
        }
    }
}
