package com.lzx.record

class RecordConfig {


    //录制时保持屏幕开启
    var keepScreenOn: Boolean = true

    //录音格式
    var recordFormat: String = RecordConst.FORMAT_M4A

    //采样率
    var sampleRate: String = RecordConst.SAMPLE_RATE_44100

    //比特率
    var bitRate: String = RecordConst.BITRATE_128000

    //频道
    var channels: String = RecordConst.STEREO

    //记录进度可视化的时间间隔
    var visualizationInterval: Int = RecordConst.SHORT_RECORD_DP_PER_SECOND


    fun isKeepScreenOn(keepScreenOn: Boolean) = apply { this.keepScreenOn = keepScreenOn }
    fun setRecordFormat(recordFormat: String) = apply { this.recordFormat = recordFormat }
    fun setSampleRate(sampleRate: String) = apply { this.sampleRate = sampleRate }
    fun setBitRate(bitRate: String) = apply { this.bitRate = bitRate }
    fun setChannel(channels: String) = apply { this.channels = channels }
    fun setVisualization(visualization: Int) = apply { this.visualizationInterval = visualization }
}

object RecordConst {
    //格式
    const val FORMAT_M4A = "m4a"
    const val FORMAT_WAV = "wav"
    const val FORMAT_3GP = "3gp"
    const val FORMAT_3GPP = "3gpp"
    const val FORMAT_MP3 = "mp3"
    const val FORMAT_AMR = "amr"
    const val FORMAT_AAC = "aac"
    const val FORMAT_MP4 = "mp4"
    const val FORMAT_OGG = "ogg"
    const val FORMAT_FLAC = "flac"

    //采样率
    const val SAMPLE_RATE_8000 = "8000"
    const val SAMPLE_RATE_16000 = "16000"
    const val SAMPLE_RATE_22050 = "22050"
    const val SAMPLE_RATE_32000 = "32000"
    const val SAMPLE_RATE_44100 = "44100"
    const val SAMPLE_RATE_48000 = "48000"

    //比特率
    const val BITRATE_12000 = "12000"
    const val BITRATE_48000 = "48000"
    const val BITRATE_96000 = "96000"
    const val BITRATE_128000 = "128000"
    const val BITRATE_192000 = "192000"
    const val BITRATE_256000 = "256000"

    //频道
    const val STEREO = "STEREO"  //立体声
    const val MONO = "Mono"      //单声道

    const val SHORT_RECORD_DP_PER_SECOND = 25
    const val LONG_RECORD_THRESHOLD_SECONDS = 20
}