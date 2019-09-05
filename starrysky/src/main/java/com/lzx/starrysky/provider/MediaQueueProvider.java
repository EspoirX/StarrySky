package com.lzx.starrysky.provider;

import android.graphics.Bitmap;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;

import com.lzx.starrysky.BaseMediaInfo;
import com.lzx.starrysky.provider.SongInfo;

import java.util.List;

public interface MediaQueueProvider {

    /**
     * 获取List#SongInfo
     */
    List<BaseMediaInfo> getMediaList();

    List<SongInfo> getSongList();

    /**
     * 更新播放列表
     */
    void updateMediaList(List<BaseMediaInfo> mediaInfoList);

    void updateMediaListBySongInfo(List<SongInfo> songInfos);

    /**
     * 添加一首歌
     */
    void addMediaInfo(BaseMediaInfo mediaInfo);

    /**
     * 根据检查是否有某首音频
     */
    boolean hasMediaInfo(String songId);

    /**
     * 根据songId获取MediaInfo
     */
    BaseMediaInfo getMediaInfo(String songId);

    BaseMediaInfo getMediaInfo(int index);

    SongInfo getSongInfo(String songId);

    SongInfo getSongInfo(int index);

    /**
     * 根据songId获取索引
     */
    int getIndexByMediaId(String songId);

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
    Iterable<MediaMetadataCompat> getShuffledMediaMetadataCompat();

    Iterable<BaseMediaInfo> getShuffledMediaInfo();

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
