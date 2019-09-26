package com.lzx.starrysky.provider

import android.graphics.Bitmap
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat

import com.lzx.starrysky.BaseMediaInfo

interface MediaQueueProvider {

    /**
     * 获取List#SongInfo
     */
    fun getMediaList(): MutableList<BaseMediaInfo>

    fun getSongList(): MutableList<SongInfo>

    /**
     * 获取List#MediaMetadataCompat
     */
    val mediaMetadataCompatList: List<MediaMetadataCompat>

    /**
     * 获取 List#MediaBrowserCompat.MediaItem 用于 onLoadChildren 回调
     */
    val childrenResult: List<MediaBrowserCompat.MediaItem>

    /**
     * 获取乱序列表
     */
    val shuffledMediaMetadataCompat: Iterable<MediaMetadataCompat>

    val shuffledMediaInfo: Iterable<BaseMediaInfo>

    /**
     * 更新播放列表
     */
    fun updateMediaList(mediaInfoList: List<BaseMediaInfo>)

    /**
     * 一次只添加一首歌曲（播放列表中只有一首）
     */
    fun onlyOneMediaBySongInfo(songInfo: SongInfo)

    /**
     * 更新列表
     */
    fun updateMediaListBySongInfo(songInfos: List<SongInfo>)

    /**
     * 添加一首歌
     */
    fun addMediaInfo(mediaInfo: BaseMediaInfo?)

    fun addMediaBySongInfo(info: SongInfo)

    fun deleteMediaById(songId: String)

    /**
     * 根据检查是否有某首音频
     */
    fun hasMediaInfo(songId: String): Boolean

    /**
     * 根据songId获取MediaInfo
     */
    fun getMediaInfo(songId: String): BaseMediaInfo?

    fun getMediaInfo(index: Int): BaseMediaInfo?

    fun getSongInfo(songId: String): SongInfo?

    fun getSongInfo(index: Int): SongInfo?

    /**
     * 根据songId获取索引
     */
    fun getIndexByMediaId(songId: String): Int

    /**
     * 根据id获取对应的MediaMetadataCompat对象
     */
    fun getMediaMetadataCompatById(songId: String?): MediaMetadataCompat?

    /**
     * 更新封面art
     */
    fun updateMusicArt(
        songId: String, changeData: MediaMetadataCompat, albumArt: Bitmap, icon: Bitmap
    )

    interface MetadataUpdateListener {
        fun onMetadataChanged(metadata: MediaMetadataCompat)

        fun onMetadataRetrieveError()

        fun onCurrentQueueIndexUpdated(queueIndex: Int)

        fun onQueueUpdated(newQueue: List<MediaSessionCompat.QueueItem>)
    }
}
