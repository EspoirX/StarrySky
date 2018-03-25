package com.lzx.musiclibrary.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.media.session.MediaSessionCompat;
import android.view.KeyEvent;

/**
 * 耳机线控，仅在5.0以下有效，5.0以上被{@link MediaSessionCompat}接管。
 *
 * @author lzx
 * @date 2018/2/11
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

                break;
            case KeyEvent.KEYCODE_MEDIA_NEXT:

                break;
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:

                break;
            default:
                break;
        }
    }
}
