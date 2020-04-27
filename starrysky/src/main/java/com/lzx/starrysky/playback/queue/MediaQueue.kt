package com.lzx.starrysky.playback.queue

import com.lzx.starrysky.provider.IMediaSourceProvider
import com.lzx.starrysky.provider.SongInfo

/**
 * 播放队列管理
 */
interface MediaQueue {

    /**
     * 获取当前正在播放的下标
     */
    val currentIndex: Int

    /**
     * 获取列表大小
     */
    val currentQueueSize: Int

    /**
     * 获取当前播放的songInfo
     */
    fun getCurrentSongInfo(isActiveTrigger: Boolean): SongInfo?

    /**
     * 转跳下一首或上一首
     *
     * @param amount 正为下一首，负为上一首
     */
    fun skipQueuePosition(amount: Int): Boolean

    /**
     * 当前歌曲是否是列表中的第一首
     */
    fun currSongIsFirstSong(): Boolean

    /**
     * 当前歌曲是否是列表中的最后一首
     */
    fun currSongIsLastSong(): Boolean

    /**
     * 根据传入的媒体id来更新此媒体的下标并通知
     */
    fun updateIndexBySongId(songId: String): Boolean

    /**
     * 更新媒体信息,比如封面之类的
     */
    fun updateMediaMetadata(songInfo: SongInfo?)

    /**
     * 更新媒体信息后的回调
     */
    fun setMetadataUpdateListener(listener: IMediaSourceProvider.MetadataUpdateListener)
}
