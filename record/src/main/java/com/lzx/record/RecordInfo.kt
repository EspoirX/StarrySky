package com.lzx.record

class RecordInfo(val name: String,
                 val format: String,
                 var duration: Long,
                 val size: Long,
                 val location: String,
                 val created: Long,
                 val sampleRate: Int,
                 val channelCount: Int,
                 val bitrate: Int,
                 val isInTrash: Boolean) {

    var waveForm: IntArray? = null
    var data: ByteArray? = null


    val nameWithExtension: String
        get() = "$name.$format"

}