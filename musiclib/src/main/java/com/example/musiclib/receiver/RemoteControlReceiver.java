package com.example.musiclib.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.media.session.MediaSessionCompat;
import android.view.KeyEvent;

import com.example.musiclib.contants.MusicAction;
import com.example.musiclib.service.MusicPlayService;


/**
 * 耳机线控，仅在5.0以下有效，5.0以上被{@link MediaSessionCompat}接管。
 * Created by lzx on 2016/1/21.
 */
public class RemoteControlReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
        if (event == null || event.getAction() != KeyEvent.ACTION_UP) {
            return;
        }

        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_MEDIA_PLAY:
            case KeyEvent.KEYCODE_MEDIA_PAUSE:
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
            case KeyEvent.KEYCODE_HEADSETHOOK:
                MusicPlayService.startCommand(context, MusicAction.ACTION_MEDIA_PLAY_PAUSE);
                break;
            case KeyEvent.KEYCODE_MEDIA_NEXT:
                MusicPlayService.startCommand(context, MusicAction.ACTION_MEDIA_NEXT);
                break;
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                MusicPlayService.startCommand(context, MusicAction.ACTION_MEDIA_PREVIOUS);
                break;
        }
    }
}
