package com.lzx.musiclib;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

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
            Log.i("xian","state = "+state);
            if (state == PlaybackStateCompat.STATE_PLAYING) {
                MusicManager.getInstance().pauseMusic();
            } else {
                MusicManager.getInstance().playMusic();
            }
            Toast.makeText(context, "ACTION_PLAY_OR_PAUSE", Toast.LENGTH_SHORT).show();
        }
        if (TestApplication.ACTION_NEXT.equals(action)) {
            MusicManager.getInstance().skipToNext();
            Toast.makeText(context, "ACTION_NEXT", Toast.LENGTH_SHORT).show();
        }
        if (TestApplication.ACTION_PRE.equals(action)) {
            MusicManager.getInstance().skipToPrevious();
            Toast.makeText(context, "ACTION_PRE", Toast.LENGTH_SHORT).show();
        }
        if (TestApplication.ACTION_FAVORITE.equals(action)) {
            //这里实现自己的喜欢或收藏逻辑，如果选中可以传 true 把按钮变成选中状态，false 就非选中状态
            MusicManager.getInstance().updateFavoriteUI(true);
            Toast.makeText(context, "ACTION_FAVORITE", Toast.LENGTH_SHORT).show();
        }
        if (TestApplication.ACTION_LYRICS.equals(action)) {
            //这里实现自己的是否显示歌词逻辑，如果选中可以传 true 把按钮变成选中状态，false 就非选中状态
            Toast.makeText(context, "ACTION_LYRICS", Toast.LENGTH_SHORT).show();
            MusicManager.getInstance().updateLyricsUI(true);
        }
    }
}
