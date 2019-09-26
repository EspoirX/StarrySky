package com.lzx.starrysky.provider

import android.graphics.Bitmap
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat

import com.lzx.starrysky.BaseMediaInfo

/**
 * 数据提供类
 */
open class MediaQueueProviderSurface(private val provider: MediaQueueProvider) :
    MediaQueueProvider {
    override fun addMediaInfo(mediaInfo: BaseMediaInfo?) {
        provider.addMediaInfo(mediaInfo)
    }

    override fun getMediaList(): MutableList<BaseMediaInfo> {
        return provider.getMediaList()
    }

    override fun getSongList(): MutableList<SongInfo> {
        return provider.getSongList()
    }

    override fun getMediaInfo(songId: String): BaseMediaInfo? {
        return provider.getMediaInfo(songId)
    }

    override fun getMediaInfo(index: Int): BaseMediaInfo? {
        return provider.getMediaInfo(index)
    }

    override fun getSongInfo(songId: String): SongInfo? {
        return provider.getSongInfo(songId)
    }

    override fun getSongInfo(index: Int): SongInfo? {
        return provider.getSongInfo(index)
    }

    override val mediaMetadataCompatList: List<MediaMetadataCompat>
        get() = provider.mediaMetadataCompatList

    override val childrenResult: List<MediaBrowserCompat.MediaItem>
        get() = provider.childrenResult

    override val shuffledMediaMetadataCompat: Iterable<MediaMetadataCompat>
        get() = provider.shuffledMediaMetadataCompat

    override val shuffledMediaInfo: Iterable<BaseMediaInfo>
        get() = provider.shuffledMediaInfo

    override fun updateMediaList(mediaInfoList: List<BaseMediaInfo>) {
        provider.updateMediaList(mediaInfoList)
    }

    override fun onlyOneMediaBySongInfo(songInfo: SongInfo) {
        provider.onlyOneMediaBySongInfo(songInfo)
    }

    override fun updateMediaListBySongInfo(songInfos: List<SongInfo>) {
        provider.updateMediaListBySongInfo(songInfos)
    }

    override fun addMediaBySongInfo(info: SongInfo) {
        provider.addMediaBySongInfo(info)
    }

    override fun deleteMediaById(songId: String) {
        provider.deleteMediaById(songId)
    }

    override fun hasMediaInfo(songId: String): Boolean {
        return provider.hasMediaInfo(songId)
    }

    override fun getIndexByMediaId(songId: String): Int {
        return provider.getIndexByMediaId(songId)
    }

    override fun getMediaMetadataCompatById(songId: String?): MediaMetadataCompat? {
        return provider.getMediaMetadataCompatById(songId)
    }

    override fun updateMusicArt(
        songId: String, changeData: MediaMetadataCompat, albumArt: Bitmap, icon: Bitmap
    ) {
        provider.updateMusicArt(songId, changeData, albumArt, icon)
    }
}
