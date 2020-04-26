package com.lzx.starrysky.provider

import android.graphics.Bitmap
import android.support.v4.media.MediaMetadataCompat
import com.lzx.starrysky.ext.album
import com.lzx.starrysky.ext.albumArtUri
import com.lzx.starrysky.ext.duration
import com.lzx.starrysky.ext.id
import com.lzx.starrysky.ext.mediaUri
import com.lzx.starrysky.ext.title

class MediaSourceProvider : IMediaSourceProvider {

    //数据源
    var songSources = linkedMapOf<String, SongInfo>()
    var mediaMetadataSources = linkedMapOf<String, MediaMetadataCompat>()

    var shuffleSongSources = mutableListOf<SongInfo>()

    data class ShuffleSongSource(var shuffleVersion: Int, var list: MutableList<SongInfo>)

    override var songList: MutableList<SongInfo>
        get() {
            val list = mutableListOf<SongInfo>()
            songSources.forEach {
                list.add(it.value)
            }
            return list
        }
        set(value) {
            songSources.clear()
            mediaMetadataSources.clear()
            value.forEach {
                songSources[it.songId] = it
                mediaMetadataSources[it.songId] = toMediaMetadata(it)
            }
        }

    override fun updateShuffleSongList() {
        if (shuffleSongSources.isNotEmpty()) {
            shuffleSongSources.clear()
        }
        shuffleSongSources.addAll(songList)
        shuffleSongSources.shuffle()
    }

    override fun getShuffleSongList(): MutableList<SongInfo> {
        if (shuffleSongSources.isEmpty()) {
            updateShuffleSongList()
        }
        return shuffleSongSources
    }

    override val mediaMetadataCompatList: List<MediaMetadataCompat>
        get() {
            val list = mutableListOf<MediaMetadataCompat>()
            mediaMetadataSources.forEach {
                list.add(it.value)
            }
            return list
        }

    override fun addSongInfo(info: SongInfo) {
        if (!hasSongInfo(info.songId)) {
            songSources[info.songId] = info
            mediaMetadataSources[info.songId] = toMediaMetadata(info)
        }
    }

    override fun addSongInfos(infos: MutableList<SongInfo>) {
        infos.forEach {
            addSongInfo(it)
        }
    }

    override fun deleteSongInfoById(songId: String) {
        if (hasSongInfo(songId)) {
            songSources.remove(songId)
            mediaMetadataSources.remove(songId)
        }
    }

    override fun hasSongInfo(songId: String): Boolean {
        return songSources.containsKey(songId)
    }

    override fun getSongInfoById(songId: String): SongInfo? {
        if (songId.isEmpty()) {
            return null
        }
        return songSources.getOrElse(songId, { null })
    }

    override fun getSongInfoByIndex(index: Int): SongInfo? {
        return songList.elementAtOrNull(index)
    }

    override fun getIndexById(songId: String): Int {
        val info = getSongInfoById(songId)
        return if (info != null) songList.indexOf(info) else -1
    }

    override fun getMediaMetadataById(songId: String?): MediaMetadataCompat? {
        if (songId.isNullOrEmpty()) {
            return null
        }
        return mediaMetadataSources.getOrElse(songId, { null })
    }

    override fun updateMusicArt(
        songId: String, changeData: MediaMetadataCompat, albumArt: Bitmap, icon: Bitmap
    ) {
        val metadata = MediaMetadataCompat.Builder(changeData)
            .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt)
            .putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, icon)
            .build()
        mediaMetadataSources[songId] = metadata
    }

    /**
     * SongInfo 转 MediaMetadataCompat
     */
    @Synchronized
    private fun toMediaMetadata(info: SongInfo): MediaMetadataCompat {
        val albumTitle = if (info.songName.isNotEmpty()) info.songName else ""
        val songCover = if (info.songCover.isNotEmpty()) info.songCover else ""
        val builder = MediaMetadataCompat.Builder()
        builder.id = info.songId
        builder.mediaUri = info.songUrl
        if (albumTitle.isNotEmpty()) {
            builder.album = albumTitle
        }
        if (info.duration != -1L) {
            builder.duration = info.duration
        }
        if (songCover.isNotEmpty()) {
            builder.albumArtUri = songCover
        }
        if (info.songName.isNotEmpty()) {
            builder.title = info.songName
        }
        return builder.build()
    }
}