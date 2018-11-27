package com.lzx.musiclibrary.queue;

import android.graphics.Bitmap;

import com.lzx.musiclibrary.aidl.model.SongInfo;

import java.util.List;

/**
 * create by lzx
 * time:2018/10/30
 */
public interface IPlayQueue {

    /**
     * 设置音乐列表
     * Set music list
     */
    void setSongInfos(List<SongInfo> songInfos,int currentIndex);

    /**
     * 设置音乐列表
     * Set music list
     */
    void setSongInfos(List<SongInfo> songInfos);

    /**
     * 获取音乐列表
     * Get music list
     */
    List<SongInfo> getSongInfos();

    /**
     * 获取当前播放下标
     * Get the current playback subscript
     */
    int getCurrentIndex();

    /**
     * 添加一首音频
     * Add an audio
     */
    void addSongInfo(SongInfo songInfo);

    /**
     * 删除一首音频，删除后是否播放下一首
     * Delete an audio, whether to play the next one after deletion
     */
    void deleteSongInfo(SongInfo songInfo, boolean isNeedToPlayNext);

    /**
     * 获取列表长度
     * Get list length
     */
    int getSongInfosSize();

    /**
     * 获取当前播放音频
     * Get the currently playing audio
     */
    SongInfo getCurrentSongInfo();

    /**
     * 设置当前播放音频
     * Set the currently playing audio
     */
    void setCurrentSong(SongInfo songInfo);

    /**
     * 设置当前播放音频
     * Set the currently playing audio
     */
    void setCurrentSong(int currentIndex);

    /**
     * 更新当前音频封面信息
     * Update current audio cover information
     */
    void updateSongCoverBitmap(String musicId, Bitmap bitmap);

    /**
     * 获取上一首音频
     * Get the previous audio
     */
    SongInfo getPreMusicInfo(boolean isUpdateIndex);

    /**
     * 获取下一首音频
     * Get the next audio
     */
    SongInfo getNextMusicInfo(boolean isUpdateIndex);

    /**
     * 是否有下一首
     * Is there a next song?
     */
    boolean hasNextSong();

    /**
     * 是否有上一首
     *Is there a previous song?
     */
    boolean hasPreSong();

    /**
     * 更新媒体信息
     * Update media information
     */
    void updateMetadata();
}
