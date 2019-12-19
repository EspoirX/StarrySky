package com.lzx.starrysky

class BaseMediaInfo : Cloneable {
    var mediaId = ""
    var mediaUrl: String? = null
    var mediaCover: String? = null
    var mediaTitle: String? = null
    var duration: Long = 0
    var mapHeadData: Map<String, String>? = null

    override fun equals(obj: Any?): Boolean {
        return if (obj is BaseMediaInfo) {
            obj.mediaId == mediaId && obj.mediaUrl == mediaUrl
        } else {
            false
        }
    }

    override fun clone(): Any {
        var obj: Any? = null
        //调用Object类的clone方法，返回一个Object实例
        try {
            obj = super.clone()
        } catch (e: CloneNotSupportedException) {
            e.printStackTrace()
        }
        return obj!!
    }

    override fun toString(): String {
        return "BaseMediaInfo{" +
            "mediaId='" + mediaId + '\'' +
            '}'
    }

    override fun hashCode(): Int {
        var result = mediaId.hashCode()
        result = 31 * result + (mediaUrl?.hashCode() ?: 0)
        result = 31 * result + (mediaCover?.hashCode() ?: 0)
        result = 31 * result + (mediaTitle?.hashCode() ?: 0)
        result = 31 * result + duration.hashCode()
        result = 31 * result + (mapHeadData?.hashCode() ?: 0)
        return result
    }
}