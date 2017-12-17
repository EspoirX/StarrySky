package com.example.musiclib.service;


import com.example.musiclib.model.MusicInfo;

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
     * 更新进度
     */
    void onProgress(int progress);

    /**
     * 缓冲百分比
     */
    void onBufferingUpdate(int percent);

    /**
     * 更新定时停止播放时间
     */
    void onTimer(long remain);
}
