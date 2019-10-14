package com.lzx.starrysky.playback.queue

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.text.TextUtils
import com.lzx.starrysky.BaseMediaInfo
import com.lzx.starrysky.StarrySky
import com.lzx.starrysky.ext.albumArtUrl
import com.lzx.starrysky.imageloader.ImageLoaderCallBack
import com.lzx.starrysky.provider.MediaQueueProvider
import com.lzx.starrysky.provider.MediaQueueProviderSurface
import com.lzx.starrysky.provider.MediaResource
import com.lzx.starrysky.provider.SongInfo
import com.lzx.starrysky.utils.StarrySkyUtils
import java.util.Arrays

open class MediaQueueManager(provider: MediaQueueProvider) : MediaQueueProviderSurface(provider),
    MediaQueue {


    private var mMediaResource: MediaResource? = null
    private var mCurrentIndex: Int = 0
    private var mUpdateListener: MediaQueueProvider.MetadataUpdateListener? = null
    private var backupMediaList: MutableList<BaseMediaInfo> = mutableListOf()

    override fun getCurrentIndex(): Int {
        return mCurrentIndex
    }

    override val currMediaInfo: BaseMediaInfo?
        get() = getMediaInfo(mCurrentIndex)

    override val currentQueueSize: Int
        get() = getMediaList().size

    override fun setMetadataUpdateListener(listener: MediaQueueProvider.MetadataUpdateListener) {
        mUpdateListener = listener
    }

    override fun isSameMedia(mediaId: String): Boolean {
        val current = getCurrentMusic() ?: return false
        return mediaId == current.getMediaId()
    }

    override fun skipQueuePosition(amount: Int): Boolean {
        val mPlayingQueue = getMediaList()
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

    override fun getCurrentMusic(): MediaResource? {
        return getCurrentMusic(null)
    }

    override fun getCurrentMusic(mediaInfo: BaseMediaInfo?): MediaResource? {
        val mediaList = getMediaList()
        if (!QueueHelper.isIndexPlayable(mCurrentIndex, mediaList)) {
            return null
        }
        val info: BaseMediaInfo
        if (mediaInfo != null) {
            mediaList[mCurrentIndex] = mediaInfo
            info = mediaInfo
        } else {
            info = mediaList[mCurrentIndex]
        }
        //由于MediaQueueManager在构建Starry时初始化，所以这里不能放在构造函数中
        if (mMediaResource == null) {
            mMediaResource = StarrySky.get().mediaResource
        }
        return mMediaResource?.obtain(info.mediaId, info.mediaUrl, System.currentTimeMillis())
    }

    override fun updateIndexByMediaId(mediaId: String): Boolean {
        val index = getIndexByMediaId(mediaId)
        if (QueueHelper.isIndexPlayable(index, getMediaList())) {
            mCurrentIndex = index
            mUpdateListener?.onCurrentQueueIndexUpdated(mCurrentIndex)
        }
        return index >= 0
    }

    override fun updateCurrPlayingMedia(mediaId: String) {
        var canReuseQueue = false
        if (isSameMedia(mediaId)) {
            canReuseQueue = updateIndexByMediaId(mediaId)
        }
        if (!canReuseQueue) {
            mCurrentIndex = getIndexByMediaId(mediaId)
        }
        updateMetadata()
    }

    override fun songInfoToMediaInfo(songInfo: SongInfo?): BaseMediaInfo {
        check(!(songInfo == null || songInfo.songId.isEmpty()))
        {
            "songInfo is null or song Id is Empty"
        }
        val mediaInfo = getMediaInfo(songInfo.songId)
            ?: throw NullPointerException("can find mediaInfo by songId:" + songInfo.songId)

        if (mediaInfo.mediaUrl != songInfo.songUrl) {
            mediaInfo.mediaUrl = songInfo.songUrl
        }
        if (mediaInfo.mediaTitle != songInfo.songName) {
            mediaInfo.mediaTitle = songInfo.songName
        }
        if (mediaInfo.mediaCover != songInfo.songCover) {
            mediaInfo.mediaCover = songInfo.songCover
        }
        if (mediaInfo.duration != songInfo.duration) {
            mediaInfo.duration = songInfo.duration
        }
        return mediaInfo
    }

    override fun updateMetadata() {
        val currentMusic = getCurrentMusic()
        if (currentMusic == null) {
            mUpdateListener?.onMetadataRetrieveError()
            return
        }
        val musicId = currentMusic.getMediaId()
        if (musicId.isNullOrEmpty()) {
            mUpdateListener?.onMetadataRetrieveError()
            return
        }
        val metadata = getMediaMetadataCompatById(musicId)
            ?: throw IllegalArgumentException("Invalid musicId $musicId")

        mUpdateListener?.onMetadataChanged(metadata)

        //更新封面 bitmap
        val coverUrl = metadata.albumArtUrl
        if (!coverUrl.isNullOrEmpty()) {
            val imageLoader = StarrySky.get().registry.imageLoader
            imageLoader.load(coverUrl, object : ImageLoaderCallBack {
                override fun onBitmapLoaded(bitmap: Bitmap?) {
                    if (bitmap != null) {
                        updateMusicArt(musicId, metadata, bitmap, bitmap)
                    }
                    mUpdateListener?.onMetadataChanged(metadata)
                }

                override fun onBitmapFailed(errorDrawable: Drawable?) {
                }
            })
        }
    }

    override fun setShuffledMode() {
        val mediaInfos = getMediaList()
        if (mediaInfos.size == 0) {
            return
        }
        //通过深拷贝备份一个 backupMediaList
        val backupArray = mediaInfos.toTypedArray()
        //打乱顺序
        mediaInfos.shuffle()
        //打乱顺序要在这个前面
        backupMediaList = Arrays.asList(
            *backupArray) /*backupArray.toMutableList() as MutableList<BaseMediaInfo>*/
    }

    override fun setNormalMode(mediaId: String) {
        //恢复备份
        updateMediaList(backupMediaList)
        updateCurrPlayingMedia(mediaId)  //恢复正常顺序后需要更新一下下标
    }
}