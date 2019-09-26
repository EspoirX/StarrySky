package com.lzx.starrysky.playback.queue

import com.lzx.starrysky.BaseMediaInfo
import com.lzx.starrysky.provider.MediaQueueProvider
import com.lzx.starrysky.provider.MediaResource
import com.lzx.starrysky.provider.SongInfo

/**
 * 播放队列管理
 */
interface MediaQueue {

    /**
     * 获取当前下标
     */
    fun getCurrentIndex(): Int

    val currMediaInfo: BaseMediaInfo?

    /**
     * 获取列表大小
     */
    val currentQueueSize: Int

    fun setMetadataUpdateListener(listener: MediaQueueProvider.MetadataUpdateListener)

    /**
     * 判断传入的媒体跟正在播放的媒体是否一样
     */
    fun isSameMedia(mediaId: String): Boolean

    /**
     * 转跳下一首或上一首
     *
     * @param amount 正为下一首，负为上一首
     */
    fun skipQueuePosition(amount: Int): Boolean

    /**
     * 获取当前播放的媒体
     */
    fun getCurrentMusic(): MediaResource?

    /**
     * 获取当前播放的媒体
     */
    fun getCurrentMusic(mediaInfo: BaseMediaInfo?): MediaResource?

    /**
     * 根据传入的媒体id来更新此媒体的下标并通知
     */
    fun updateIndexByMediaId(mediaId: String): Boolean

    /**
     * 根据当前传入的 mediaId 更新当前播放媒体下标和信息
     */
    fun updateCurrPlayingMedia(mediaId: String)

    fun songInfoToMediaInfo(songInfo: SongInfo?): BaseMediaInfo

    /**
     * 更新媒体信息
     */
    fun updateMetadata()

    fun setShuffledMode()

    fun setNormalMode(mediaId: String)
}
