// IMusicPlayService.aidl
package com.example.musiclib.service;

import com.example.musiclib.model.MusicInfo;

interface IMusicPlayService {
    //暂停/播放
    void playPause();
    //开始播放
    void start();
    //暂停播放
    void pause();
    //停止播放
    void stop();
    //上一首
    void prev();
    //跳转到指定的时间位置
    void seekTo(int msec);
    //是否在播放
    boolean isPlaying();
    //是否暂停
    boolean isPausing();
    //是否准备
    boolean isPreparing();
    //是否空闲
    boolean isIdle();
    //根据位置播放
    void playByPosition(int position);
    //根据音乐信息播放
    void playByMusicInfo(in MusicInfo music);
    //获取正在播放的本地歌曲的序号
    int getPlayingPosition();
    //得到正在播放的本地歌曲的序号
    void setPlayingPosition(int playingPosition);
    //下一首
    void next();
    //获取当前进度
    long getCurrentPosition();
    //获取正在播放的歌曲[网络]
    MusicInfo getPlayingMusic();
    //退出
    void quit();
    //获取播放模式
    String getPlayMode();
    //设置播放模式
    void setPlayMode(String playMode);
}
