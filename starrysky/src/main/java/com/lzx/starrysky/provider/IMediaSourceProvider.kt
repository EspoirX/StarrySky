package com.lzx.starrysky.provider

import android.graphics.Bitmap
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat

interface IMediaSourceProvider {

    /**
     * 音频列表设置
     */
    var songList: MutableList<SongInfo>

    fun updateShuffleSongList()

    fun getShuffleSongList(): MutableList<SongInfo>

    /**
     * 获取MediaMetadataCompat列表
     */
    val mediaMetadataCompatList: List<MediaMetadataCompat>

    /**
     * 添加一首歌
     */
    fun addSongInfo(info: SongInfo)

    /**
     * 添加多首歌
     */
    fun addSongInfos(infos: MutableList<SongInfo>)

    /**
     * 根据id删除
     */
    fun deleteSongInfoById(songId: String)

    /**
     * 根据检查是否有某首音频
     */
    fun hasSongInfo(songId: String): Boolean

    /**
     * 根据songId获取SongInfo
     */
    fun getSongInfoById(songId: String): SongInfo?

    /**
     * 根据下标获取SongInfo
     */
    fun getSongInfoByIndex(index: Int): SongInfo?

    /**
     * 根据songId获取索引
     */
    fun getIndexById(songId: String): Int

    /**
     * 根据id获取对应的MediaMetadataCompat对象
     */
    fun getMediaMetadataById(songId: String?): MediaMetadataCompat?

    /**
     * 更新封面art
     */
    fun updateMusicArt(
        songId: String, changeData: MediaMetadataCompat, albumArt: Bitmap, icon: Bitmap
    )

    interface MetadataUpdateListener {
        fun onMetadataChanged(metadata: MediaMetadataCompat)

        fun onMetadataRetrieveError(songInfo: SongInfo?)

//        fun onCurrentQueueIndexUpdated(queueIndex: Int)

        fun onQueueUpdated(newQueue: List<MediaSessionCompat.QueueItem>)
    }
}
