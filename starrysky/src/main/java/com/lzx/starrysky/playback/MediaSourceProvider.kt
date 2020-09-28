package com.lzx.starrysky.playback

import com.lzx.starrysky.SongInfo
import com.lzx.starrysky.utils.isIndexPlayable

/**
 * 存储播放数据源
 */
class MediaSourceProvider {
    //数据源
    private var songSources = linkedMapOf<String, SongInfo>()

    //随机模式下的数据源
    private var shuffleSongSources = mutableListOf<SongInfo>()

    //副歌数据
    var refrain: SongInfo? = null

    var songList: MutableList<SongInfo>
        get() {
            val list = mutableListOf<SongInfo>()
            songSources.forEach {
                list.add(it.value)
            }
            return list
        }
        set(value) {
            songSources.clear()
            value.forEach {
                songSources[it.songId] = it
            }
        }

    fun updateShuffleSongList() {
        if (shuffleSongSources.isNotEmpty()) {
            shuffleSongSources.clear()
        }
        shuffleSongSources.addAll(songList)
        shuffleSongSources.shuffle()
    }

    fun getShuffleSongList(): MutableList<SongInfo> {
        if (shuffleSongSources.isEmpty()) {
            updateShuffleSongList()
        }
        return shuffleSongSources
    }

    fun addSongInfo(info: SongInfo) {
        if (!hasSongInfo(info.songId)) {
            songSources[info.songId] = info
        }
    }

    fun addSongInfo(index: Int, info: SongInfo) {
        if (!hasSongInfo(info.songId)) {
            val list = mutableListOf<Pair<String, SongInfo>>()
            songSources.forEach {
                list.add(Pair(it.key, it.value))
            }
            if (index.isIndexPlayable(list)) {
                list.add(index, Pair(info.songId, info))
            }
            songSources.clear()
            list.forEach {
                songSources[it.first] = it.second
            }
        }
    }

    fun addSongInfos(infos: MutableList<SongInfo>) {
        infos.forEach {
            addSongInfo(it)
        }
    }

    fun clearSongInfos() {
        songList.clear()
        songSources.clear()
    }

    fun deleteSongInfoById(songId: String) {
        if (hasSongInfo(songId)) {
            songSources.remove(songId)
        }
    }

    fun hasSongInfo(songId: String): Boolean {
        return songSources.containsKey(songId)
    }

    fun getSongInfoById(songId: String): SongInfo? {
        if (songId.isEmpty()) {
            return null
        }
        return songSources.getOrElse(songId, { null })
    }

    fun getSongInfoByIndex(index: Int): SongInfo? {
        return songList.elementAtOrNull(index)
    }

    fun getIndexById(songId: String): Int {
        val info = getSongInfoById(songId)
        return if (info != null) songList.indexOf(info) else -1
    }

    fun updateMusicArt(songInfo: SongInfo) {
        songSources[songInfo.songId] = songInfo
    }
}