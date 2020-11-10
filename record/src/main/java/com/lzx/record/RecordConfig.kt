package com.lzx.record

import android.media.AudioFormat
import android.media.MediaRecorder
import com.lzx.basecode.Playback
import com.lzx.basecode.SongInfo
import com.lzx.basecode.md5
import com.lzx.basecode.toSdcardPath
import com.lzx.record.recorder.IRecordByteDataListener
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

    //是否需要下载背景音乐（使用默认播放器的时候有用）
    var needDownloadBgMusic: Boolean = false

    //背景音乐下载保存路径（使用默认播放器的时候有用）
    var bgMusicFilePath: String = "StarrySky/download/".toSdcardPath()

    //背景音乐文件名（使用默认播放器的时候有用）
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


    var recordByteDataListener: IRecordByteDataListener? = null

    fun reset() {
        audioSource = MediaRecorder.AudioSource.MIC
        sampleRate = 44100
        channelConfig = AudioFormat.CHANNEL_IN_STEREO
        audioFormat = AudioFormat.ENCODING_PCM_16BIT
        outPutFilePath = null
        outPutFileName = null
        outPutFile = null
        isContinue = false
        headers = null
        quality = 3
        bitRate = 64
        recordMaxTime = 60000
        wax = 1F
        waveSpeed = 300
        bgMusicVolume = 0F
        recordCallback = null
        recordByteDataListener = null
    }

    fun setAudioSource(source: Int) = apply { this.audioSource = source }

    fun setSamplingRate(rate: Int) = apply { this.sampleRate = rate }

    fun setChannelConfig(channel: Int) = apply { this.channelConfig = channel }

    fun setRecordOutputFile(path: String) = apply { outPutFilePath = path }

    fun setRecordOutputFileName(name: String) = apply { outPutFileName = name }

    fun setRecordOutputFile(file: File) = apply { this.outPutFile = file }

    fun setIsContinue(isContinue: Boolean) = apply { this.isContinue = isContinue }

    fun setRecordCallback(callback: RecorderCallback?) = apply { this.recordCallback = callback }

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

    fun setRecordByteDataListener(listener: IRecordByteDataListener) = apply { this.recordByteDataListener = listener }


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

