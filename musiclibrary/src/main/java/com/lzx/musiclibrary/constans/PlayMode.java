package com.lzx.musiclibrary.constans;

import android.content.Context;

import com.lzx.musiclibrary.utils.SPUtils;

/**
 * Created by xian on 2018/1/28.
 */

public class PlayMode {

    //单曲循环
    public static final int PLAY_IN_SINGLE_LOOP = 1;

    //随机播放
    public static final int PLAY_IN_RANDOM = 2;

    //列表循环
    public static final int PLAY_IN_LIST_LOOP = 3;

    //顺序播放
    public static final int PLAY_IN_ORDER = 4;

    private int currPlayMode = PLAY_IN_LIST_LOOP;

    public int getCurrPlayMode(Context context) {
        currPlayMode = (int) SPUtils.get(context, "music_key_play_model", PLAY_IN_LIST_LOOP);
        return currPlayMode;
    }

    public void setCurrPlayMode(Context context, int currPlayMode) {
        this.currPlayMode = currPlayMode;
        SPUtils.put(context, "music_key_play_model", currPlayMode);
    }

}
