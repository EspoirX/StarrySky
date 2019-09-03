package com.lzx.starrysky.playback;

import android.support.annotation.NonNull;

import com.lzx.starrysky.provider.MediaQueueProvider;
import com.lzx.starrysky.provider.MediaResource;

public interface MediaQueue {

    void setMetadataUpdateListener(MediaQueueProvider.MetadataUpdateListener listener);

    /**
     * 判断传入的媒体跟正在播放的媒体是否一样
     */
    boolean isSameBrowsingCategory(@NonNull String mediaId);

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
     * 打乱当前的列表顺序
     */
    void setRandomQueue();

    /**
     * 获取当前播放的媒体
     */
    MediaResource getCurrentMusic();

    /**
     * 根据传入的媒体id来更新此媒体的下标并通知
     */
    boolean setCurrentQueueItem(String mediaId);

    /**
     * 根据当前传入的 mediaId 更新当前播放媒体下标和信息
     */
    void updateCurrPlayingMedia(String mediaId);

    /**
     * 更新媒体信息
     */
    void updateMetadata();
}
