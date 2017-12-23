package com.lzx.musiclib.service;


import com.lzx.musiclib.model.MusicInfo;

/**
 * 播放进度监听器
 */
public interface OnPlayerEventListener {

    /**
     * 切换歌曲
     */
    void onMusicChange(MusicInfo music);

    /**
     * 继续播放
     */
    void onPlayerStart();

    /**
     * 暂停播放
     */
    void onPlayerPause();

    /**
     * 播放完成
     */
    void onPlayCompletion();

    /**
     * 更新进度
     */
    void onProgress(int progress,int duration);

    /**
     * 缓冲百分比
     */
    void onBufferingUpdate(int percent);

    /**
     * 更新定时停止播放时间
     */
    void onTimer();

    void onError(int what, int extra);
}
