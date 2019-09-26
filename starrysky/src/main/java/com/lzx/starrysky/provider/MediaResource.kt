package com.lzx.starrysky.provider

class MediaResource constructor() {

    private lateinit var mediaId: String
    private var queueId: Long = 0L
    private lateinit var mediaUrl: String
    private var mCacheMediaResource = hashMapOf<String, MediaResource>()

    constructor(
        mediaId: String,
        mediaUrl: String,
        queueId: Long
    ) : this() {
        this.mediaId = mediaId
        this.queueId = queueId
        this.mediaUrl = mediaUrl
    }

    fun obtain(mediaId: String?, mediaUrl: String, queueId: Long): MediaResource {
        if (!mediaId.isNullOrEmpty()) {
            var resource = mCacheMediaResource[mediaId]
            if (resource == null) {
                resource = MediaResource(mediaId, mediaUrl, queueId)
                mCacheMediaResource[mediaId] = resource
            }
            if (resource.getMediaUrl() != mediaUrl) {
                resource.mediaUrl = mediaUrl
            }
            return resource
        } else {
            throw IllegalStateException("songId is null")
        }
    }

    fun getMediaId(): String? {
        return mediaId
    }

    fun getQueueId(): Long {
        return queueId
    }

    fun getMediaUrl(): String? {
        return mediaUrl
    }
}