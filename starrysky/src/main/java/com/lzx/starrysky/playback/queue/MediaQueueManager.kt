package com.lzx.starrysky.playback.queue

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.support.v4.media.session.PlaybackStateCompat
import com.lzx.starrysky.StarrySky
import com.lzx.starrysky.ext.albumArtUri
import com.lzx.starrysky.imageloader.ImageLoaderCallBack
import com.lzx.starrysky.provider.IMediaSourceProvider
import com.lzx.starrysky.provider.SongInfo
import com.lzx.starrysky.utils.StarrySkyUtils
import java.util.Arrays

open class MediaQueueManager : MediaQueue {

    private var mCurrentIndex: Int = 0
    private var mUpdateListener: IMediaSourceProvider.MetadataUpdateListener? = null

    override val currentIndex: Int
        get() = mCurrentIndex

    override val currentQueueSize: Int
        get() = StarrySky.get().mediaQueueProvider().getSongList().size

    override val currentSongInfo: SongInfo?
        get() = StarrySky.get().mediaQueueProvider().getSongInfoByIndex(currentIndex)

    override fun setMetadataUpdateListener(listener: IMediaSourceProvider.MetadataUpdateListener) {
        mUpdateListener = listener
    }

    override fun isSameSong(songId: String): Boolean {
        val current = currentSongInfo ?: return false
        return songId == current.songId
    }

    override fun skipQueuePosition(amount: Int): Boolean {
        val mPlayingQueue = StarrySky.get().mediaQueueProvider().getSongList()
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
        return currentSongInfo?.songId == firstSong?.songId
    }

    override fun currSongIsLastSong(): Boolean {
        val lastSong = StarrySky.get().mediaQueueProvider().getSongInfoByIndex(currentQueueSize - 1)
        return currentSongInfo?.songId == lastSong?.songId
    }

    override fun updateIndexBySongId(songId: String): Boolean {
        val index = StarrySky.get().mediaQueueProvider().getIndexById(songId)
        if (QueueHelper.isIndexPlayable(index, StarrySky.get().mediaQueueProvider().getSongList())) {
            mCurrentIndex = index
            mUpdateListener?.onCurrentQueueIndexUpdated(mCurrentIndex)
        }
        return index >= 0
    }

    override fun updateCurrPlayingSongInfo(songId: String) {
        var canReuseQueue = false
        if (isSameSong(songId)) {
            canReuseQueue = updateIndexBySongId(songId)
        }
        if (!canReuseQueue) {
            mCurrentIndex = StarrySky.get().mediaQueueProvider().getIndexById(songId)
        }
        updateMediaMetadata()
    }

    override fun updateMediaMetadata() {
        if (currentSongInfo == null) {
            mUpdateListener?.onMetadataRetrieveError()
            return
        }
        val musicId = currentSongInfo!!.songId
        if (musicId.isEmpty()) {
            mUpdateListener?.onMetadataRetrieveError()
            return
        }
        val metadata = StarrySky.get().mediaQueueProvider().getMediaMetadataById(musicId)
                ?: throw IllegalArgumentException("Invalid musicId $musicId")

        mUpdateListener?.onMetadataChanged(metadata)

        //更新封面 bitmap
        val coverUrl = currentSongInfo!!.songCover
        if (coverUrl.isNotEmpty()) {
            val imageLoader = StarrySky.get().imageLoader()
            imageLoader.load(coverUrl, object : ImageLoaderCallBack {
                override fun onBitmapLoaded(bitmap: Bitmap?) {
                    bitmap?.let {
                        StarrySky.get().mediaQueueProvider().updateMusicArt(musicId, metadata, bitmap, bitmap)
                    }
                    mUpdateListener?.onMetadataChanged(metadata)
                }

                override fun onBitmapFailed(errorDrawable: Drawable?) {
                }
            })
        }
    }
}