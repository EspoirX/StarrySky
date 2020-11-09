package com.lzx.record

import android.media.AudioFormat
import android.media.MediaRecorder
import com.lzx.basecode.Playback
import com.lzx.basecode.SongInfo
import com.lzx.basecode.md5
import com.lzx.basecode.toSdcardPath
import com.lzx.record.recorder.IRecorder
import com.lzx.record.recorder.RecorderCallback
import java.io.File

open class RecordConfig {
    // 音源
    var audioSource = MediaRecorder.AudioSource.MIC

    // 采样率
    var sampleRate = 44100

    // 声道数
    var channelConfig: Int = AudioFormat.CHANNEL_IN_STEREO //双声道

    // 采样位数
    var audioFormat: Int = AudioFormat.ENCODING_PCM_16BIT

    //设置输出路径
    var outPutFilePath: String? = null

    //文件名，配合 outPutFilePath 使用
    var outPutFileName: String? = null

    //设置输出路径
    var outPutFile: File? = null

    //是否继续录制
    var isContinue: Boolean = false

    //是否需要下载背景音乐
    var needDownloadBgMusic: Boolean = false

    //背景音乐下载保存路径
    var bgMusicFilePath: String = "StarrySky/download/".toSdcardPath()

    //背景音乐文件名
    var bgMusicFileName: String = System.currentTimeMillis().toString()

    //如果是网络音乐，可以指定 headers
    var headers: HashMap<String, String>? = null

    //初始Lame录音输出质量
    var quality: Int = 3

    //设置比特率，关系声音的质量
    var bitRate: Int = 64

    //初始最大录制时间
    var recordMaxTime: Long = 60000

    //设置增强系数
    var wax: Float = 1F

    //波形速度
    var waveSpeed: Int = 300

    //背景音乐音量
    var bgMusicVolume: Float = 0F

    //录音回调
    var recordCallback: RecorderCallback? = null

    fun reset() {
        audioSource = MediaRecorder.AudioSource.MIC
        sampleRate = 44100
        channelConfig = AudioFormat.CHANNEL_IN_STEREO
        audioFormat = AudioFormat.ENCODING_PCM_16BIT
        outPutFilePath = null
        outPutFileName = null
        outPutFile = null
        isContinue = false
//        bgMusicUrl = null
        headers = null
        quality = 3
        bitRate = 64
        recordMaxTime = 60000
        wax = 1F
        waveSpeed = 300
        bgMusicVolume = 0F
        recordCallback = null
    }

    fun setAudioSource(source: Int) = apply { this.audioSource = source }

    fun setSamplingRate(rate: Int) = apply { this.sampleRate = rate }

    fun setChannelConfig(channel: Int) = apply { this.channelConfig = channel }

    fun setRecordOutputFile(path: String) = apply { outPutFilePath = path }

    fun setRecordOutputFileName(name: String) = apply { outPutFileName = name }

    fun setRecordOutputFile(file: File) = apply { this.outPutFile = file }

    fun setIsContinue(isContinue: Boolean) = apply { this.isContinue = isContinue }

    fun setRecordCallback(callback: RecorderCallback?) = apply { this.recordCallback = callback }

//    fun setPlayerListener(listener: PlayerListener?) = apply { this.playerListener = listener }

//    fun setBgMusicUrl(url: String) = apply { bgMusicUrl = url }

    fun isNeedDownloadBgMusic(isDownload: Boolean) = apply { needDownloadBgMusic = isDownload }

    fun setBgMusicFilePath(filePath: String) = apply { this.bgMusicFilePath = filePath }

    fun setBgMusicFileName(fileName: String) = apply { this.bgMusicFileName = fileName }

    fun setHeaders(headers: HashMap<String, String>?) = apply { this.headers = headers }

    fun setQuality(quality: Int) = apply { this.quality = quality }

    fun setBitRate(bitRate: Int) = apply { this.bitRate = bitRate }

    fun setRecordMaxTime(maxTime: Long) = apply { this.recordMaxTime = maxTime }

    fun setWax(wax: Float) = apply { this.wax = wax }

    fun setWaveSpeed(waveSpeed: Int) = apply { this.waveSpeed = waveSpeed }

    fun setBgMusicVolume(volume: Float) = apply { this.bgMusicVolume = volume }

    fun startRecord() {
        StarrySkyRecord.recorder?.setUpRecordConfig(this)
        StarrySkyRecord.recorder?.startRecording()
    }

    fun prepare(): IRecorder? {
        StarrySkyRecord.recorder?.setUpRecordConfig(this)
        return StarrySkyRecord.recorder
    }

    fun getBgPlayer(): Playback? {
        StarrySkyRecord.recorder?.setUpRecordConfig(this)
        return StarrySkyRecord.recorder?.getPlayer()
    }

    fun playBgMusic(url: String) {
        StarrySkyRecord.recorder?.setUpRecordConfig(this)
        val headers = hashMapOf<String, String>()
        headers["StarrySkyRecord"] = "StarrySkyRecord"
        val songInfo = SongInfo(songId = url.md5(), songUrl = url, headData = headers)
        StarrySkyRecord.recorder?.getPlayer()?.play(songInfo, true)
    }

}

/*
open class RecordConfig internal constructor(builder: Builder) {

    // 音源
    @get:JvmName("audioSource")
    internal var audioSource = builder.audioSource

    // 采样率
    @get:JvmName("sampleRate")
    internal var sampleRate = builder.sampleRate

    // 声道数
    @get:JvmName("channelConfig")
    internal var channelConfig: Int = builder.channelConfig

    // 采样位数
    @get:JvmName("audioFormat")
    internal var audioFormat: Int = builder.audioFormat

    //设置输出路径
    @get:JvmName("outPutFilePath")
    internal var outPutFilePath: String? = builder.outPutFilePath

    @get:JvmName("outPutFileName")
    internal var outPutFileName: String? = builder.outPutFileName

    //设置输出路径
    @get:JvmName("outPutFile")
    internal var outPutFile: File? = builder.outPutFile

    //是否继续录制
    @get:JvmName("isContinue")
    internal var isContinue: Boolean = builder.isContinue

    //背景音乐url
    @get:JvmName("bgMusicUrl")
    internal var bgMusicUrl: String? = builder.bgMusicUrl

    //如果是网络音乐，可以指定 headers
    @get:JvmName("headers")
    internal var headers: HashMap<String, String>? = builder.headers

    //初始Lame录音输出质量
    @get:JvmName("quality")
    internal var quality: Int = builder.quality

    //设置比特率，关系声音的质量
    @get:JvmName("bitRate")
    internal var bitRate: Int = builder.bitRate

    //初始最大录制时间
    @get:JvmName("recordMaxTime")
    internal var recordMaxTime: Long = builder.recordMaxTime

    //设置增强系数
    @get:JvmName("wax")
    internal var wax: Float = builder.wax

    //波形速度
    @get:JvmName("waveSpeed")
    internal var waveSpeed: Int = builder.waveSpeed

    //背景音乐音量
    @get:JvmName("bgMusicVolume")
    internal var bgMusicVolume: Float = builder.bgMusicVolume

    //录音回调
    @get:JvmName("recordCallback")
    internal var recordCallback: RecorderCallback? = builder.recordCallback

    //播放器回调
    @get:JvmName("playerListener")
    internal var playerListener: PlayerListener? = builder.playerListener

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as RecordConfig
        if (audioSource != other.audioSource) return false
        if (sampleRate != other.sampleRate) return false
        if (channelConfig != other.channelConfig) return false
        if (audioFormat != other.audioFormat) return false
        if (outPutFilePath != other.outPutFilePath) return false
        if (outPutFileName != other.outPutFileName) return false
        if (outPutFile != other.outPutFile) return false
        if (isContinue != other.isContinue) return false
        if (bgMusicUrl != other.bgMusicUrl) return false
        if (headers != other.headers) return false
        if (quality != other.quality) return false
        if (bitRate != other.bitRate) return false
        if (recordMaxTime != other.recordMaxTime) return false
        if (wax != other.wax) return false
        if (waveSpeed != other.waveSpeed) return false
        if (bgMusicVolume != other.bgMusicVolume) return false
        return true
    }

    override fun hashCode(): Int {
        var result = audioSource.hashCode()
        result = 31 * result + sampleRate.hashCode()
        result = 31 * result + channelConfig.hashCode()
        result = 31 * result + audioFormat.hashCode()
        result = 31 * result + outPutFilePath.hashCode()
        result = 31 * result + outPutFileName.hashCode()
        result = 31 * result + outPutFile.hashCode()
        result = 31 * result + isContinue.hashCode()
        result = 31 * result + bgMusicUrl.hashCode()
        result = 31 * result + (headers?.hashCode() ?: 0)
        result = 31 * result + quality.hashCode()
        result = 31 * result + bitRate.hashCode()
        result = 31 * result + recordMaxTime.hashCode()
        result = 31 * result + wax.hashCode()
        result = 31 * result + waveSpeed.hashCode()
        result = 31 * result + bgMusicVolume.hashCode()
        return result
    }

    constructor() : this(Builder())

    open fun newBuilder(): Builder = Builder(this)

    class Builder constructor() {
        // 音源
        internal var audioSource = MediaRecorder.AudioSource.MIC

        // 采样率
        internal var sampleRate = 44100

        // 声道数
        internal var channelConfig: Int = AudioFormat.CHANNEL_IN_STEREO //双声道

        // 采样位数
        internal var audioFormat: Int = AudioFormat.ENCODING_PCM_16BIT

        //设置输出路径
        internal var outPutFilePath: String? = null

        //文件名，配合 outPutFilePath 使用
        internal var outPutFileName: String? = null

        //设置输出路径
        internal var outPutFile: File? = null

        //是否继续录制
        internal var isContinue: Boolean = false

        //背景音乐url
        internal var bgMusicUrl: String? = null

        //如果是网络音乐，可以指定 headers
        internal var headers: HashMap<String, String>? = null

        //初始Lame录音输出质量
        internal var quality: Int = 3

        //设置比特率，关系声音的质量
        internal var bitRate: Int = 64

        //初始最大录制时间
        internal var recordMaxTime: Long = 60000

        //设置增强系数
        internal var wax: Float = 1F

        //波形速度
        internal var waveSpeed: Int = 300

        //背景音乐音量
        internal var bgMusicVolume: Float = 0F

        //录音回调
        internal var recordCallback: RecorderCallback? = null

        //播放器回调
        internal var playerListener: PlayerListener? = null

        fun setAudioSource(source: Int) = apply { this.audioSource = source }

        fun setSamplingRate(rate: Int) = apply { this.sampleRate = rate }

        fun setChannelConfig(channel: Int) = apply { this.channelConfig = channel }

        fun setOutputFile(path: String) = apply { outPutFilePath = path }

        fun setOutputFileName(name: String) = apply { outPutFileName = name }

        fun setOutputFile(file: File) = apply { this.outPutFile = file }

        fun setIsContinue(isContinue: Boolean) = apply { this.isContinue = isContinue }

        fun setRecordCallback(callback: RecorderCallback?) = apply { this.recordCallback = callback }

        fun setPlayerListener(listener: PlayerListener?) = apply { this.playerListener = listener }

        fun setBgMusicUrl(url: String) = apply { bgMusicUrl = url }

        fun setHeaders(headers: HashMap<String, String>?) = apply { this.headers = headers }

        fun setQuality(quality: Int) = apply { this.quality = quality }

        fun setBitRate(bitRate: Int) = apply { this.bitRate = bitRate }

        fun setRecordMaxTime(maxTime: Long) = apply { this.recordMaxTime = maxTime }

        fun setWax(wax: Float) = apply { this.wax = wax }

        fun setWaveSpeed(waveSpeed: Int) = apply { this.waveSpeed = waveSpeed }

        fun setBgMusicVolume(volume: Float) = apply { this.bgMusicVolume = volume }

        internal constructor(config: RecordConfig) : this() {
            this.audioSource = config.audioSource
            this.sampleRate = config.sampleRate
            this.channelConfig = config.channelConfig
            this.audioFormat = config.audioFormat
            this.outPutFilePath = config.outPutFilePath
            this.outPutFileName = config.outPutFileName
            this.outPutFile = config.outPutFile
            this.isContinue = config.isContinue
            this.bgMusicUrl = config.bgMusicUrl
            this.headers = config.headers
            this.quality = config.quality
            this.bitRate = config.bitRate
            this.recordMaxTime = config.recordMaxTime
            this.wax = config.wax
            this.waveSpeed = config.waveSpeed
            this.bgMusicVolume = config.bgMusicVolume
            this.recordCallback = config.recordCallback
            this.playerListener = config.playerListener
        }

        fun startRecord() {
            val config = RecordConfig(this)
            StarrySkyRecord.recorder?.setUpRecordConfig(config, false)
            StarrySkyRecord.recorder?.startRecording()
        }

        fun prepare(): IRecorder? {
            val config = RecordConfig(this)
            StarrySkyRecord.recorder?.setUpRecordConfig(config, true)
            return StarrySkyRecord.recorder
        }

        fun player(): AudioTrackPlayer? {
            val config = RecordConfig(this)
            StarrySkyRecord.recorder?.setUpRecordConfig(config, false)
            return StarrySkyRecord.recorder?.getAudioTrackPlayer()
        }
    }
    */
/**
 * 调 record 的时候创建，其他的时候更新，其实都是更新
 *//*

}*/
