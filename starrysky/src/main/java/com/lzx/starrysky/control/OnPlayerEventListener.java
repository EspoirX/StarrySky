package com.lzx.starrysky.control;

import com.lzx.starrysky.provider.SongInfo;

/**
 * 播放监听
 */
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
    void onPlayCompletion(SongInfo songInfo);

    /**
     * 正在缓冲
     */
    void onBuffering();

    /**
     * 发生错误
     */
    void onError(int errorCode, String errorMsg);
}
