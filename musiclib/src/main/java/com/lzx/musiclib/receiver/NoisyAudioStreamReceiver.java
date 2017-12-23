package com.lzx.musiclib.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.lzx.musiclib.contants.MusicAction;
import com.lzx.musiclib.service.MusicPlayService;


/**
 * 来电/耳机拔出时暂停播放
 * @author lzx
 * @date 2017/12/14
 */

public class NoisyAudioStreamReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        MusicPlayService.startCommand(context, MusicAction.ACTION_MEDIA_PLAY_PAUSE);
    }
}
