package com.lzx.starrysky.playback

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import com.lzx.starrysky.SongInfo
import com.lzx.starrysky.control.RepeatMode
import com.lzx.starrysky.imageloader.ImageLoader
import com.lzx.starrysky.imageloader.ImageLoaderCallBack
import com.lzx.starrysky.utils.StarrySkyUtils
import com.lzx.starrysky.utils.isIndexPlayable

class MediaQueueManager(val provider: MediaSourceProvider, private val imageLoader: ImageLoader) {
    var currentIndex: Int = 0
    val currentQueueSize: Int = provider.songList.size


    fun getCurrentSongInfo(isActiveTrigger: Boolean): SongInfo? {
        val repeatMode = StarrySkyUtils.repeatMode.repeatMode
        val mPlayingQueue = if (!isActiveTrigger && repeatMode == RepeatMode.REPEAT_MODE_SHUFFLE) {
            provider.getShuffleSongList()
        } else {
            provider.songList
        }
        return mPlayingQueue.elementAtOrNull(currentIndex)
    }

    fun skipQueuePosition(amount: Int): Boolean {
        val playingQueue = provider.songList
        if (playingQueue.size == 0) {
            return false
        }
        var index = currentIndex + amount
        if (index < 0) {
            val repeatMode = StarrySkyUtils.repeatMode
            index = if (repeatMode.isLoop) {
                playingQueue.size - 1
            } else {
                if (repeatMode.repeatMode == RepeatMode.REPEAT_MODE_ONE ||
                    repeatMode.repeatMode == RepeatMode.REPEAT_MODE_SHUFFLE) {
                    playingQueue.size - 1
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
        StarrySkyUtils.log("skipQueuePosition#mCurrentIndex=$currentIndex")
        return true
    }

    fun currSongIsFirstSong(): Boolean {
        val firstSong = provider.getSongInfoByIndex(0)
        return getCurrentSongInfo(true)?.songId == firstSong?.songId
    }

    fun currSongIsLastSong(): Boolean {
        val lastSong = provider.getSongInfoByIndex(currentQueueSize - 1)
        return getCurrentSongInfo(true)?.songId == lastSong?.songId
    }

    fun updateIndexBySongId(songId: String): Boolean {
        val index = provider.getIndexById(songId)
        val list = provider.songList
        if (index.isIndexPlayable(list)) {
            currentIndex = index
        }
        return index >= 0
    }

    fun updateMusicArt(songInfo: SongInfo?) {
        if (songInfo?.artBitmap == null) {
            return
        }
        //更新封面 bitmap
        val coverUrl = songInfo.songCover
        if (coverUrl.isNotEmpty()) {
            imageLoader.load(coverUrl, object : ImageLoaderCallBack {
                override fun onBitmapLoaded(bitmap: Bitmap?) {
                    songInfo.artBitmap = bitmap
                    provider.updateMusicArt(songInfo)
                }

                override fun onBitmapFailed(errorDrawable: Drawable?) {
                }
            })
        }
    }

}