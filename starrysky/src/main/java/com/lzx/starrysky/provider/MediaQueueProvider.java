package com.lzx.starrysky.provider;

import android.graphics.Bitmap;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;

import com.lzx.starrysky.provider.SongInfo;

import java.util.List;

public interface MediaQueueProvider {

    /**
     * 获取List#SongInfo
     */
    List<SongInfo> getSongInfos();

    /**
     * 设置播放列表
     */
    void setSongInfos(List<SongInfo> songInfos);

    /**
     * 添加一首歌
     */
    void addSongInfo(SongInfo songInfo);

    /**
     * 根据检查是否有某首音频
     */
    boolean hasSongInfo(String songId);

    /**
     * 根据songId获取SongInfo
     */
    SongInfo getSongInfo(String songId);

    /**
     * 根据songId获取索引
     */
    int getIndexBySongInfo(String songId);

    /**
     * 获取List#MediaMetadataCompat
     */
    List<MediaMetadataCompat> getMusicList();

    /**
     * 获取 List#MediaBrowserCompat.MediaItem 用于 onLoadChildren 回调
     */
    List<MediaBrowserCompat.MediaItem> getChildrenResult();

    /**
     * 获取乱序列表
     */
    Iterable<MediaMetadataCompat> getShuffledMusic();

    /**
     * 根据id获取对应的MediaMetadataCompat对象
     */
    MediaMetadataCompat getMusic(String songId);

    /**
     * 更新封面art
     */
    void updateMusicArt(String songId, MediaMetadataCompat changeData, Bitmap albumArt, Bitmap icon);


    interface MetadataUpdateListener {
        void onMetadataChanged(MediaMetadataCompat metadata);

        void onMetadataRetrieveError();

        void onCurrentQueueIndexUpdated(int queueIndex);

        void onQueueUpdated(List<MediaSessionCompat.QueueItem> newQueue);
    }

}
