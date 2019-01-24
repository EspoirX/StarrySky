package com.lzx.starrysky.manager;

import com.lzx.starrysky.model.SongInfo;

public interface OnPlayerEventListener {
    /**
     * 切歌回调
     */
    void onMusicSwitch(SongInfo songInfo);

    /**
     * 开始播放,与 onMusicSwitch 的关系是先回调 onMusicSwitch，再回调 onPlayerStart
     */
    void onPlayerStart();

    /**
     * 暂停播放
     */
    void onPlayerPause();

    /**
     * 停止播放
     */
    void onPlayerStop();

    /**
     * 播放完成
     */
    void onPlayCompletion();

    /**
     * 正在缓冲
     */
    void onBuffering();

    /**
     * 发生错误
     */
    void onError(int errorCode, String errorMsg);
}
