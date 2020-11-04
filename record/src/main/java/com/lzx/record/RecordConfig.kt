package com.lzx.record

import android.media.AudioFormat
import android.media.MediaRecorder
import com.lzx.record.player.AudioTrackPlayer
import com.lzx.record.recorder.IRecorder
import com.lzx.record.recorder.PlayerListener
import com.lzx.record.recorder.RecorderCallback
import java.io.File

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
            StarrySkyRecord.recorder?.setUpRecordConfig(config)
            StarrySkyRecord.recorder?.startRecording()
        }

        fun prepare(): IRecorder? {
            val config = RecordConfig(this)
            StarrySkyRecord.recorder?.setUpRecordConfig(config)
            return StarrySkyRecord.recorder
        }

        fun player(): AudioTrackPlayer? {
            val config = RecordConfig(this)
            StarrySkyRecord.recorder?.setUpRecordConfig(config)
            return StarrySkyRecord.recorder?.getAudioTrackPlayer()
        }
    }
}