package com.lzx.record

import android.media.AudioFormat
import android.media.MediaRecorder
import com.lzx.record.recorder.AudioMp3Recorder
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

    //背景音乐音量
    @get:JvmName("bgMusicVolume")
    internal var bgMusicVolume: Float = builder.bgMusicVolume

    //录音回调
    @get:JvmName("recordCallback")
    internal var recordCallback: RecorderCallback? = builder.recordCallback

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

        //初始Lame录音输出质量
        internal var quality: Int = 3

        //设置比特率，关系声音的质量
        internal var bitRate: Int = 64

        //初始最大录制时间
        internal var recordMaxTime: Long = 60000

        //设置增强系数
        internal var wax: Float = 0F

        //背景音乐音量
        internal var bgMusicVolume: Float = 0F

        //录音回调
        internal var recordCallback: RecorderCallback? = null

        fun setAudioSource(source: Int) = apply { this.audioSource = source }

        //设置采样率
        fun setSamplingRate(rate: Int) = apply { this.sampleRate = rate }

        //声道数
        fun setChannelConfig(channel: Int) = apply { this.channelConfig = channel }

        fun setOutputFile(path: String) = apply { outPutFilePath = path }

        fun setOutputFileName(name: String) = apply { outPutFileName = name }

        fun setOutputFile(file: File) = apply { this.outPutFile = file }

        fun setIsContinue(isContinue: Boolean) = apply { this.isContinue = isContinue }

        //设置录音监听
        fun setRecordCallback(callback: RecorderCallback?) = apply { this.recordCallback = callback }

        //设计背景音乐的url
        fun setBgMusicPath(url: String) = apply { bgMusicUrl = url }

        //初始Lame录音输出质量
        fun setQuality(quality: Int) = apply { this.quality = quality }

        //设置比特率，关系声音的质量
        fun setBitRate(bitRate: Int) = apply { this.bitRate = bitRate }

        //初始最大录制时间
        fun setRecordMaxTime(maxTime: Long) = apply { this.recordMaxTime = maxTime }

        //设置增强系数
        fun setWax(wax: Float) = apply { this.wax = wax }

        //设置背景声音大小
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
            this.quality = config.quality
            this.bitRate = config.bitRate
            this.recordMaxTime = config.recordMaxTime
            this.wax = config.wax
            this.bgMusicVolume = config.bgMusicVolume
            this.recordCallback = config.recordCallback
        }

        fun startRecord() {
            val config = RecordConfig(this)
            val recorder = AudioMp3Recorder(config)
            StarrySkyRecord.recorder = recorder
            recorder.startRecording()
        }
    }
}