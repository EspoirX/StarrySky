package com.lzx.starrysky.provider

class MediaResource constructor() {

    private lateinit var mediaId: String
    private var queueId: Long = 0L
    private lateinit var mediaUrl: String
    private var mMapHeadData: Map<String, String>? = hashMapOf()
    private var mCacheMediaResource = hashMapOf<String, MediaResource>()

    constructor(
        mediaId: String,
        mediaUrl: String,
        queueId: Long,
        headData: Map<String, String>?
    ) : this() {
        this.mediaId = mediaId
        this.queueId = queueId
        this.mediaUrl = mediaUrl
        this.mMapHeadData = headData
    }

    fun obtain(
        mediaId: String?, mediaUrl: String, queueId: Long, headData: Map<String, String>?
    ): MediaResource {
        if (!mediaId.isNullOrEmpty()) {
            var resource = mCacheMediaResource[mediaId]
            if (resource == null) {
                resource = MediaResource(mediaId, mediaUrl, queueId, headData)
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

    fun getMapHeadData(): Map<String, String>? {
        return mMapHeadData
    }
}