package com.lzx.musiclibrary.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.lzx.musiclibrary.MusicService;
import com.lzx.musiclibrary.constans.State;
import com.lzx.musiclibrary.control.PlayControl;
import com.lzx.musiclibrary.control.PlayController;
import com.lzx.musiclibrary.notification.IMediaNotification;

/**
 * Created by xian on 2018/2/18.
 */

public class PlayerReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (TextUtils.isEmpty(action)) {
            return;
        }
        MusicService musicService = MusicService.getService();
        if (musicService == null) {
            return;
        }
        PlayControl binder = musicService.getBinder();
        if (binder == null) {
            return;
        }
        PlayController controller = binder.getController();
        if (controller == null) {
            return;
        }
        switch (action) {
            case IMediaNotification.ACTION_CLOSE:
                controller.stopMusic();
                controller.stopNotification();
                break;
            case IMediaNotification.ACTION_PLAY_PAUSE:
                if (controller.getState() == State.STATE_PLAYING) {
                    controller.pauseMusic();
                } else if (controller.getState() == State.STATE_PAUSED) {
                    controller.resumeMusic();
                }
                break;
            case IMediaNotification.ACTION_PREV:
                controller.playPre();
                break;
            case IMediaNotification.ACTION_NEXT:
                controller.playNext();
                break;
            default:
                break;
        }
    }
}
