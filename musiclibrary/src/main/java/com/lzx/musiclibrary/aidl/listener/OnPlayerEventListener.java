package com.lzx.musiclibrary.aidl.listener;

import com.lzx.musiclibrary.aidl.model.SongInfo;

/**
 * @author lzx
 * @date 2018/2/3
 */

public interface OnPlayerEventListener {
    void onMusicSwitch(SongInfo music);

    void onPlayerStart();

    void onPlayerPause();

    void onPlayCompletion();

    void onPlayerStop();

    void onError(String errorMsg);

    void onAsyncLoading(boolean isFinishLoading);
}
