package com.lzx.starrysky;

import com.lzx.starrysky.model.SongInfo;

public interface OnPlayerEventListener {
    void onMusicSwitch(SongInfo music);

    void onPlayerStart();

    void onPlayerPause();

    void onPlayCompletion(SongInfo songInfo);

    void onPlayerStop();

    void onError(String errorMsg, String errorCode);
}
