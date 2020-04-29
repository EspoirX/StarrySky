package com.lzx.starrysky.playback.queue

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import com.lzx.starrysky.StarrySky
import com.lzx.starrysky.control.RepeatMode
import com.lzx.starrysky.imageloader.ImageLoaderCallBack
import com.lzx.starrysky.provider.IMediaSourceProvider
import com.lzx.starrysky.provider.SongInfo
import com.lzx.starrysky.utils.StarrySkyUtils

open class
MediaQueueManager : MediaQueue {

    private var mCurrentIndex: Int = 0
    private var mUpdateListener: IMediaSourceProvider.MetadataUpdateListener? = null

    override val currentIndex: Int
        get() = mCurrentIndex

    override val currentQueueSize: Int
        get() = StarrySky.get().mediaQueueProvider().songList.size

    override fun getCurrentSongInfo(isActiveTrigger: Boolean): SongInfo? {
        val repeatMode = StarrySkyUtils.getRepeatMode().repeatMode
        val mPlayingQueue = if (!isActiveTrigger && repeatMode == RepeatMode.REPEAT_MODE_SHUFFLE) {
            StarrySky.get().mediaQueueProvider().getShuffleSongList()
        } else {
            StarrySky.get().mediaQueueProvider().songList
        }
        return mPlayingQueue.elementAtOrNull(currentIndex)
    }

    override fun setMetadataUpdateListener(listener: IMediaSourceProvider.MetadataUpdateListener) {
        mUpdateListener = listener
    }

    override fun skipQueuePosition(amount: Int): Boolean {
        val mPlayingQueue = StarrySky.get().mediaQueueProvider().songList
        if (mPlayingQueue.size == 0) {
            return false
        }
        var index = mCurrentIndex + amount
        if (index < 0) {
            index = 0
        } else {
            index %= mPlayingQueue.size
        }
        if (!QueueHelper.isIndexPlayable(index, mPlayingQueue)) {
            return false
        }
        mCurrentIndex = index
        StarrySkyUtils.log("skipQueuePosition#mCurrentIndex=$mCurrentIndex")
        return true
    }

    override fun currSongIsFirstSong(): Boolean {
        val firstSong = StarrySky.get().mediaQueueProvider().getSongInfoByIndex(0)
        return getCurrentSongInfo(true)?.songId == firstSong?.songId
    }

    override fun currSongIsLastSong(): Boolean {
        val lastSong = StarrySky.get().mediaQueueProvider().getSongInfoByIndex(currentQueueSize - 1)
        return getCurrentSongInfo(true)?.songId == lastSong?.songId
    }

    override fun updateIndexBySongId(songId: String): Boolean {
        val index = StarrySky.get().mediaQueueProvider().getIndexById(songId)
        val list = StarrySky.get().mediaQueueProvider().songList
        if (QueueHelper.isIndexPlayable(index, list)) {
            mCurrentIndex = index
        }
        return index >= 0
    }

    override fun updateMediaMetadata(songInfo: SongInfo?) {
        if (songInfo == null) {
            mUpdateListener?.onMetadataRetrieveError(songInfo)
            return
        }
        val musicId = songInfo.songId
        if (musicId.isEmpty()) {
            mUpdateListener?.onMetadataRetrieveError(songInfo)
            return
        }
        val metadata = StarrySky.get().mediaQueueProvider().getMediaMetadataById(musicId)
            ?: throw IllegalArgumentException("Invalid musicId $musicId")
        //更新封面 bitmap
        val coverUrl = songInfo.songCover
        if (coverUrl.isNotEmpty()) {
            val imageLoader = StarrySky.get().imageLoader()
            imageLoader.load(coverUrl, object : ImageLoaderCallBack {
                override fun onBitmapLoaded(bitmap: Bitmap?) {
                    bitmap?.let {
                        StarrySky.get().mediaQueueProvider()
                            .updateMusicArt(musicId, metadata, bitmap, bitmap)
                    }
                    mUpdateListener?.onMetadataChanged(metadata)
                }

                override fun onBitmapFailed(errorDrawable: Drawable?) {
                    mUpdateListener?.onMetadataChanged(metadata)
                }
            })
        } else {
            mUpdateListener?.onMetadataChanged(metadata)
        }
    }
}