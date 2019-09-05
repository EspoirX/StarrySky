package com.lzx.starrysky.playback.queue;

import android.support.annotation.NonNull;

import com.lzx.starrysky.BaseMediaInfo;
import com.lzx.starrysky.provider.MediaQueueProvider;
import com.lzx.starrysky.provider.MediaResource;
import com.lzx.starrysky.provider.SongInfo;

/**
 * 播放队列管理
 */
public interface MediaQueue {

    void setMetadataUpdateListener(MediaQueueProvider.MetadataUpdateListener listener);

    /**
     * 判断传入的媒体跟正在播放的媒体是否一样
     */
    boolean isSameMedia(@NonNull String mediaId);

    /**
     * 获取当前下标
     */
    int getCurrentIndex();

    /**
     * 转跳下一首或上一首
     *
     * @param amount 正为下一首，负为上一首
     */
    boolean skipQueuePosition(int amount);

    /**
     * 获取当前播放的媒体
     */
    MediaResource getCurrentMusic();

    MediaResource getCurrentMusic(BaseMediaInfo songInfo);

    BaseMediaInfo getCurrMediaInfo();

    /**
     * 根据传入的媒体id来更新此媒体的下标并通知
     */
    boolean updateIndexByMediaId(String mediaId);

    /**
     * 根据当前传入的 mediaId 更新当前播放媒体下标和信息
     */
    void updateCurrPlayingMedia(String mediaId);

    /**
     * 更新媒体信息
     */
    void updateMetadata();

    /**
     * 获取列表大小
     */
    int getCurrentQueueSize();

    void setShuffledMode();

    void setNormalMode();
}
