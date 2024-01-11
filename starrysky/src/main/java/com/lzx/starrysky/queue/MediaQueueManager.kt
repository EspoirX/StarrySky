package com.lzx.starrysky.queue

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import com.lzx.starrysky.SongInfo
import com.lzx.starrysky.StarrySky
import com.lzx.starrysky.control.RepeatMode
import com.lzx.starrysky.control.isModeOne
import com.lzx.starrysky.control.isModeShuffle
import com.lzx.starrysky.notification.imageloader.ImageLoaderCallBack
import com.lzx.starrysky.utils.isIndexPlayable

class MediaQueueManager(val provider: MediaSourceProvider) {
    private var currentIndex: Int = 0

    /**
     * ignoreShuffle 是否忽略随机模式
     */
    fun getCurrentSongInfo(ignoreShuffle: Boolean): SongInfo? {
        val repeatMode = RepeatMode.with.repeatMode
        val playingQueue = if (!ignoreShuffle && repeatMode.isModeShuffle()) {
            provider.getShuffleSongList()
        } else {
            provider.songList
        }
        return playingQueue.elementAtOrNull(currentIndex)
    }

    fun getCurrSongList(): MutableList<SongInfo> {
        val repeatMode = RepeatMode.with.repeatMode
        return if (repeatMode.isModeShuffle()) {
            provider.getShuffleSongList()
        } else {
            provider.songList
        }
    }

    fun skipQueuePosition(amount: Int): Boolean {
        val playingQueue = provider.songList

        if (playingQueue.size == 0) {
            return false
        }
        var index = currentIndex + amount
        if (index < 0) {
            val repeatMode = RepeatMode.with
            index = if (repeatMode.isLoop) {
                playingQueue.size - 1
            } else {
                if (repeatMode.repeatMode.isModeOne() || repeatMode.repeatMode.isModeShuffle()) {
                    playingQueue.lastIndex
                } else {
                    0
                }
            }
        } else {
            index %= playingQueue.size
        }
        if (!index.isIndexPlayable(playingQueue)) {
            return false
        }
        currentIndex = index
        StarrySky.log("skipQueuePosition#mCurrentIndex=$currentIndex")
        return true
    }

    fun currSongIsFirstSong(): Boolean {
        val firstSong = provider.getSongInfoByIndex(0)
        return getCurrentSongInfo(true)?.songId == firstSong?.songId
    }

    fun currSongIsLastSong(): Boolean {
        val lastSong = provider.getSongInfoByIndex(provider.songList.lastIndex)
        return getCurrentSongInfo(true)?.songId == lastSong?.songId
    }

    fun updateIndexBySongId(songId: String): Boolean {
        val index = if (RepeatMode.with.repeatMode.isModeShuffle()) {
            provider.getIndexById(songId,true)
        } else {
            provider.getIndexById(songId)
        }
        val list = provider.songList
        if (index.isIndexPlayable(list)) {
            currentIndex = index
        }
        return index >= 0
    }

    fun updateIndexByPlayingInfo(currInfo: SongInfo?) {
        currInfo?.let {
            updateIndexBySongId(it.songId)
        }
    }

    fun updateMusicArt(songInfo: SongInfo?) {
        //更新封面 bitmap
        val coverUrl = songInfo?.songCover.orEmpty()
        if (coverUrl.isNotEmpty() && songInfo?.coverBitmap == null) {
            StarrySky.getImageLoader()?.load(coverUrl, object : ImageLoaderCallBack {
                override fun onBitmapLoaded(bitmap: Bitmap?) {
                    songInfo?.let {
                        it.coverBitmap = bitmap
                        provider.updateMusicArt(songInfo)
                    }
                }

                override fun onBitmapFailed(errorDrawable: Drawable?) {
                }
            })
        }
    }

    fun getCurrIndex() = currentIndex
}