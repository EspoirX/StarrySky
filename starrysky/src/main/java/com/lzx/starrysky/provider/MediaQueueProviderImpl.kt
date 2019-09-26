package com.lzx.starrysky.provider

import android.graphics.Bitmap
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.text.TextUtils
import com.lzx.starrysky.BaseMediaInfo
import com.lzx.starrysky.ext.album
import com.lzx.starrysky.ext.albumArtUri
import com.lzx.starrysky.ext.artist
import com.lzx.starrysky.ext.duration
import com.lzx.starrysky.ext.genre
import com.lzx.starrysky.ext.id
import com.lzx.starrysky.ext.mediaUri
import com.lzx.starrysky.ext.title
import com.lzx.starrysky.ext.trackCount
import com.lzx.starrysky.ext.trackNumber
import com.lzx.starrysky.utils.StarrySkyUtils

open class MediaQueueProviderImpl : MediaQueueProvider {

    private var mediaListMap = linkedMapOf<String, BaseMediaInfo>()
    private var mMediaMetadataCompatMap = linkedMapOf<String, MediaMetadataCompat>()
    private var songListMap = linkedMapOf<String, SongInfo>()

    private val mediaList = mutableListOf<BaseMediaInfo>()
    private val songList = mutableListOf<SongInfo>()

    override val mediaMetadataCompatList: List<MediaMetadataCompat>
        get() = mediaMetadataCompatList.toList()

    override val childrenResult: List<MediaBrowserCompat.MediaItem>
        get() {
            val mediaItems = mutableListOf<MediaBrowserCompat.MediaItem>()
            for (metadata in mediaMetadataCompatList) {
                val mediaItem = MediaBrowserCompat.MediaItem(
                    metadata.description,
                    MediaBrowserCompat.MediaItem.FLAG_PLAYABLE)
                mediaItems.add(mediaItem)
            }
            return mediaItems
        }


    override val shuffledMediaMetadataCompat: Iterable<MediaMetadataCompat>
        get() {
            val shuffled = ArrayList(mMediaMetadataCompatMap.values)
            shuffled.shuffle()
            return shuffled
        }

    override val shuffledMediaInfo: Iterable<BaseMediaInfo>
        get() {
            mediaList.shuffle()
            return mediaList
        }

    override fun getMediaList(): MutableList<BaseMediaInfo> {
        StarrySkyUtils.log("mediaList = " + mediaList.size)
        return mediaList
    }

    override fun getSongList(): MutableList<SongInfo> {
        return songList
    }

    override fun updateMediaList(mediaInfoList: List<BaseMediaInfo>) {
        //更新 mediaList，mediaListMap
        mediaList.clear()
        mediaList.addAll(mediaInfoList)
        for (info in mediaInfoList) {
            mediaListMap[info.mediaId] = info
        }
    }

    override fun onlyOneMediaBySongInfo(songInfo: SongInfo) {
        val songInfos = ArrayList<SongInfo>()
        songInfos.add(songInfo)
        updateMediaListBySongInfo(songInfos)
    }

    override fun updateMediaListBySongInfo(songInfos: List<SongInfo>) {
        songListMap.clear()
        mMediaMetadataCompatMap.clear()
        songList.clear()
        songList.addAll(songInfos)
        val mediaInfos = mutableListOf<BaseMediaInfo>()
        for (songInfo in songList) {
            val mediaInfo = BaseMediaInfo()
            mediaInfo.mediaId = songInfo.songId
            mediaInfo.mediaTitle = songInfo.songName
            mediaInfo.mediaCover = songInfo.songCover
            mediaInfo.mediaUrl = songInfo.songUrl
            mediaInfo.duration = songInfo.duration
            mediaInfos.add(mediaInfo)
            songListMap[songInfo.songId] = songInfo
        }
        mMediaMetadataCompatMap = toMediaMetadata(songList)
        updateMediaList(mediaInfos)
    }

    override fun addMediaInfo(mediaInfo: BaseMediaInfo?) {
        if (mediaInfo == null) {
            return
        }
        if (!mediaList.contains(mediaInfo)) {
            mediaList.add(mediaInfo)
        }
        mediaListMap[mediaInfo.mediaId] = mediaInfo
    }

    override fun addMediaBySongInfo(info: SongInfo) {
        if (!hasMediaInfo(info.songId)) {
            songList.add(info)
            songListMap[info.songId] = info
            val metadataCompat = toMediaMetadata(info)
            mMediaMetadataCompatMap[info.songId] = metadataCompat

            val mediaInfo = BaseMediaInfo()
            mediaInfo.mediaId = info.songId
            mediaInfo.mediaTitle = info.songName
            mediaInfo.mediaCover = info.songCover
            mediaInfo.mediaUrl = info.songUrl
            mediaInfo.duration = info.duration

            addMediaInfo(mediaInfo)
        }
    }

    override fun deleteMediaById(songId: String) {
        if (hasMediaInfo(songId)) {
            val info = getSongInfo(songId)
            val mediaInfo = getMediaInfo(songId)
            mediaList.remove(mediaInfo)
            mediaListMap.remove(songId)
            songListMap.remove(songId)
            mMediaMetadataCompatMap.remove(songId)
            songList.remove(info)
        }
    }

    override fun hasMediaInfo(songId: String): Boolean {
        return mediaListMap.containsKey(songId)
    }

    override fun getMediaInfo(songId: String): BaseMediaInfo? {
        if (songId.isEmpty()) {
            return null
        }
        return mediaListMap.getOrElse(songId, { null })
    }

    override fun getMediaInfo(index: Int): BaseMediaInfo? {
        return mediaList.elementAtOrNull(index)
    }

    override fun getSongInfo(songId: String): SongInfo? {
        if (TextUtils.isEmpty(songId)) {
            return null
        }
        return songListMap.getOrElse(songId, { null })
    }

    override fun getSongInfo(index: Int): SongInfo? {
        return songList.elementAtOrNull(index)
    }

    override fun getIndexByMediaId(songId: String): Int {
        val info = getMediaInfo(songId)
        return if (info != null) mediaList.indexOf(info) else -1
    }

    override fun getMediaMetadataCompatById(songId: String?): MediaMetadataCompat? {
        if (songId.isNullOrEmpty()) {
            return null
        }
        return mMediaMetadataCompatMap.getOrElse(songId, { null })
    }

    override fun updateMusicArt(
        songId: String, changeData: MediaMetadataCompat, albumArt: Bitmap, icon: Bitmap
    ) {
        val metadata = MediaMetadataCompat.Builder(changeData)
            .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt)
            .putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, icon)
            .build()
        mMediaMetadataCompatMap[songId] = metadata
    }

    /**
     * List<SongInfo> 转 ConcurrentMap<String></String>, MediaMetadataCompat>
    </SongInfo> */
    @Synchronized
    private fun toMediaMetadata(
        songInfos: List<SongInfo>
    ): LinkedHashMap<String, MediaMetadataCompat> {
        val map = linkedMapOf<String, MediaMetadataCompat>()
        for (info in songInfos) {
            val metadataCompat = toMediaMetadata(info)
            map[info.songId] = metadataCompat
        }
        return map
    }

    /**
     * SongInfo 转 MediaMetadataCompat
     */
    @Synchronized
    private fun toMediaMetadata(info: SongInfo): MediaMetadataCompat {
        var albumTitle = ""
        if (!TextUtils.isEmpty(info.albumName)) {
            albumTitle = info.albumName
        } else if (!TextUtils.isEmpty(info.songName)) {
            albumTitle = info.songName
        }
        var songCover = ""
        if (!TextUtils.isEmpty(info.songCover)) {
            songCover = info.songCover
        } else if (!TextUtils.isEmpty(info.albumCover)) {
            songCover = info.albumCover
        }
        val builder = MediaMetadataCompat.Builder()
        builder.id = info.songId
        builder.mediaUri = info.songUrl
        if (albumTitle.isNotEmpty()) {
            builder.album = albumTitle
        }
        if (info.artist.isNotEmpty()) {
            builder.artist = info.artist
        }
        if (info.duration != -1L) {
            builder.duration = info.duration
        }
        if (info.genre.isNotEmpty()) {
            builder.genre = info.genre
        }
        if (songCover.isNotEmpty()) {
            builder.albumArtUri = songCover
        }
        if (info.songName.isNotEmpty()) {
            builder.title = info.songName
        }
        if (info.trackNumber != -1) {
            builder.trackNumber = info.trackNumber.toLong()
        }
        builder.trackCount = info.albumSongCount.toLong()
        return builder.build()
    }
}