package com.lzx.record.recorder

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.audiofx.AcousticEchoCanceler
import android.media.audiofx.AutomaticGainControl
import android.media.audiofx.NoiseSuppressor
import android.os.AsyncTask
import android.util.Log
import com.lzx.basecode.AudioDecoder
import com.lzx.basecode.MainLooper
import com.lzx.basecode.Playback
import com.lzx.basecode.orDef
import com.lzx.record.LameManager
import com.lzx.record.RecordConfig
import com.lzx.record.StarrySkyRecord
import com.lzx.record.player.AudioTrackPlayer
import com.lzx.record.utils.BytesTransUtil
import com.lzx.record.utils.format
import com.lzx.record.utils.safeQuality
import java.io.File
import java.io.FileOutputStream
import kotlin.math.sqrt

/**
 * AudioRecord + lame 录制 mp3
 *
 * https://xmaihh.github.io/2019/07/30/Android录音/#边录边播（AudioRecord-AudioTrack）
 */
class AudioMp3Recorder : IRecorder {
    val mp3 = "mp3"
    val aac = "aac"
    val wav = "wav"
    val amr = "amr"

    companion object {
        private const val FRAME_COUNT = 160
    }

    // 系统自带的去噪音，增强以及回音问题
    private var noiseSuppressor: NoiseSuppressor? = null
    private var acousticEchoCanceler: AcousticEchoCanceler? = null
    private var automaticGainControl: AutomaticGainControl? = null

    private var config: RecordConfig? = null

    // 获取最小缓存区大小
    var bufferSizeInBytes: Int = 0

    private var audioRecord: AudioRecord? = null
    private var recordFile: File? = null
    private var isRecording = false
    private var isPause: Boolean = false

    private var state = RecordState.STOPPED   //当前状态
    private var recordVolume: Int = 0 //录制音量
    private var duration = 0L //录制时间
    private var player: Playback? = null //播放器
    private var hasBgMusic = false
    private var bgLevel: Float = 0.30f //背景音乐

    override fun setUpRecordConfig(recordConfig: RecordConfig) {
        if (this.config == null || this.config?.equals(recordConfig) == false) {
            this.config = recordConfig
        }
        initAudioTrackPlayer()
    }

    private fun initAudioTrackPlayer() {
        //如果设置了背景音乐，则初始化解码器，播放器等相关东西
        if (player == null) {
            player = if (StarrySkyRecord.getPlayer() != null) StarrySkyRecord.getPlayer() else AudioTrackPlayer(config!!)
        }
        hasBgMusic = !player?.currPlayInfo?.songUrl.isNullOrEmpty()
    }

    override fun getPlayer(): Playback? = player

    /**
     * 开始录音
     */
    override fun startRecording() {
        if (!checkRecordParameter()) {
            config?.recordCallback?.onError("录音参数有错误，请检查")
            return
        }
        if (isRecording) {
            config?.recordCallback?.onError("正在录音中")
            return
        }
        player?.getAudioDecoder()?.let {
            config?.setBitRate(it.bitRate)?.setSamplingRate(it.sampleRate)?.setChannelConfig(it.channelCount)
        }
        isRecording = true
        duration = 0
        AsyncTask.THREAD_POOL_EXECUTOR.execute {
            try {
                //获取最小缓存区大小
                bufferSizeInBytes = AudioRecord.getMinBufferSize(
                    config?.sampleRate.orDef(), config?.channelConfig.orDef(), config?.audioFormat.orDef())

                val bytesPerFrame = if (config?.audioFormat == AudioFormat.ENCODING_PCM_8BIT) 1 else 2

                //使能被整除，方便下面的周期性通知
                var frameSize = bufferSizeInBytes / bytesPerFrame
                if (frameSize % FRAME_COUNT != 0) {
                    frameSize += FRAME_COUNT - frameSize % FRAME_COUNT
                    bufferSizeInBytes = frameSize * bytesPerFrame
                }

                //创建 AudioRecord
                audioRecord = AudioRecord(config?.audioSource.orDef(),
                    config?.sampleRate.orDef(),
                    config?.channelConfig.orDef(),
                    config?.audioFormat.orDef(), bufferSizeInBytes)

                initAEC(audioRecord?.audioSessionId.orDef())

                val pcmBuffer = ShortArray(bufferSizeInBytes)

                //初始化lame
                LameManager.init(config?.sampleRate.orDef(),
                    config?.channelConfig.orDef(),
                    config?.sampleRate.orDef(),
                    config?.bitRate.orDef(),
                    config?.quality?.safeQuality().orDef())

                audioRecord?.positionNotificationPeriod = FRAME_COUNT

                val fos = FileOutputStream(recordFile, config?.isContinue.orDef())
                val mp3buffer = ByteArray((7200 + pcmBuffer.size * 2.0 * 1.25).toInt())

                audioRecord?.startRecording()

                //PCM文件大小 = 采样率采样时间采样位深 / 8*通道数（Bytes）
                val bytesPerSecond = audioRecord!!.sampleRate * audioRecord!!.audioFormat.format() / 8 * audioRecord!!.channelCount

                onStart()

                //开始录音
                while (isRecording) {
                    var buffer: ByteArray? = null
                    val readSize = if (hasBgMusic) {
                        val samplesPerFrame = player?.getBufferSize().orDef(AudioDecoder.BUFFER_SIZE)
                        buffer = ByteArray(samplesPerFrame)
                        audioRecord?.read(buffer, 0, samplesPerFrame).orDef()
                    } else {
                        audioRecord?.read(pcmBuffer, 0, bufferSizeInBytes).orDef()
                    }
                    if (readSize == AudioRecord.ERROR_INVALID_OPERATION || readSize == AudioRecord.ERROR_BAD_VALUE) {
                        //错误
                        onError("需要录音权限")
                    } else if (readSize > 0) {
                        //暂停
                        if (isPause) continue

                        val readTime = 1000.0 * readSize.toDouble() * 2 / bytesPerSecond
                        //short 是2个字节 byte 是1个字节8位
                        onRecording(readTime)

                        if (hasBgMusic) {
                            calculateRealVolume(buffer)
                        } else {
                            calculateRealVolume(pcmBuffer, readSize)
                        }

                        if (hasBgMusic) {
                            if (buffer != null) {
                                val bgData = player?.getPcmBufferBytes()
                                val readMixTask = ReadMixTask(buffer, config?.wax.orDef(), bgData, StarrySkyRecord.currVolumeF)
                                val mixBuffer = readMixTask.getData()
                                val encodedSize: Int
                                val mixReadSize: Int
                                if (config?.channelConfig == AudioFormat.CHANNEL_IN_STEREO) {
                                    mixReadSize = mixBuffer.size / 2
                                    encodedSize = LameManager.encodeInterleaved(mixBuffer, mixReadSize, mp3buffer)
                                } else {
                                    mixReadSize = mixBuffer.size
                                    encodedSize = LameManager.encode(mixBuffer, mixBuffer, mixReadSize, mp3buffer)
                                }
                                if (encodedSize > 0) {
                                    fos.write(mp3buffer, 0, encodedSize)
                                }
                            }
                        } else {
                            val encodeSize: Int = LameManager.encode(pcmBuffer, pcmBuffer, readSize, mp3buffer)
                            if (encodeSize > 0) {
                                fos.write(mp3buffer, 0, encodeSize)
                            }
                        }
                    } else {
                        //错误
                        onError("需要录音权限")
                    }
                }

                //录音结束 将MP3结尾信息写入buffer中
                val flushResult = LameManager.flush(mp3buffer)
                fos.use {
                    if (flushResult > 0) {
                        it.write(mp3buffer, 0, flushResult)
                    }
                }
                //关闭资源
                audioRecord?.stop()
                audioRecord?.release()
                audioRecord = null
                LameManager.close()
                autoStop()
            } catch (ex: Exception) {
                ex.printStackTrace()
                onError(ex.message.toString())
            }
        }
    }

    /**
     * 开始录音回调
     */
    private fun onStart() {
        if (state != RecordState.RECORDING) {
            state = RecordState.RECORDING
            duration = 0
            MainLooper.instance.runOnUiThread {
                player?.setRecording(true)
                config?.recordCallback?.onStart()
            }
        }
    }

    /**
     * 暂停录音
     */
    override fun pauseRecording() {
        if (state == RecordState.RECORDING) {
            isPause = true
            state = RecordState.PAUSED
            MainLooper.instance.runOnUiThread {
                player?.setRecording(false)
                config?.recordCallback?.onPause()
            }
        }
    }

    /**
     * 停止录音
     */
    override fun stopRecording() {
        if (state != RecordState.STOPPED) {
            isPause = false
            isRecording = false
            state = RecordState.STOPPED
            MainLooper.instance.runOnUiThread {
                player?.setRecording(false)
                config?.recordCallback?.onSuccess(recordFile, duration)
            }
        }
    }

    /**
     * 暂停后恢复录音
     */
    override fun resumeRecording() {
        if (state == RecordState.PAUSED) {
            isPause = false
            state = RecordState.RECORDING
            MainLooper.instance.runOnUiThread {
                player?.setRecording(true)
                config?.recordCallback?.onResume()
            }
        }
    }

    /**
     *  重置
     */
    override fun onReset() {
        player?.setRecording(false)
        isRecording = false
        isPause = false
        state = RecordState.STOPPED
        duration = 0L
        recordFile = null
    }

    /**
     * 设置声音
     */
    override fun setVolume(volume: Float) {
        bgLevel = when {
            volume < 0 -> 0f
            volume > 1 -> 1f
            else -> volume
        }
        StarrySkyRecord.setAudioVoiceF(volume)
    }

    /**
     * 回调正在录音
     */
    private fun onRecording(readTime: Double) {
        duration += readTime.toLong()
        MainLooper.instance.runOnUiThread({
            //提示快到录音时间了
            if (config?.recordMaxTime.orDef() > 15000 && duration > config?.recordMaxTime.orDef() - 10000) {
                config?.recordCallback?.onRemind(duration)
            }
            //录制回调
            config?.recordCallback?.onRecording(duration, recordVolume)
        }, config?.waveSpeed.orDef().toLong())

        if (duration > config?.recordMaxTime.orDef()) {
            autoStop()
        }
    }

    /**
     * 录音出错回调
     */
    private fun onError(msg: String) {
        isPause = false
        isRecording = false
        state = RecordState.STOPPED
        MainLooper.instance.runOnUiThread {
            player?.setRecording(false)
            config?.recordCallback?.onError(msg)
        }
    }

    /**
     * 到最大时间自动结束
     */
    private fun autoStop() {
        if (state != RecordState.STOPPED) {
            isPause = false
            isRecording = false
            state = RecordState.STOPPED
            MainLooper.instance.runOnUiThread({
                player?.setRecording(false)
                config?.recordCallback?.onSuccess(recordFile, duration)
            }, config?.waveSpeed.orDef().toLong())
        }
    }

    /**
     * 计算声音
     */
    private fun calculateRealVolume(buffer: ShortArray, readSize: Int) {
        var sum = 0.0
        for (i in 0 until readSize) {
            // 这里没有做运算的优化，为了更加清晰的展示代码
            sum += (buffer[i] * buffer[i]).toDouble()
        }
        if (readSize > 0) {
            val amplitude = sum / readSize
            recordVolume = sqrt(amplitude).toInt()
            if (recordVolume < 5) {
                for (i in 0 until readSize) {
                    buffer[i] = 0
                }
            }
        }
    }

    private fun calculateRealVolume(buffer: ByteArray?) {
        buffer?.let {
            val shorts = BytesTransUtil.bytes2Shorts(it)
            val readSize = shorts.size
            calculateRealVolume(shorts, readSize)
        }
    }

    /**
     * 检查参数
     */
    private fun checkRecordParameter(): Boolean {
        var isOk = true
        if (config?.outPutFile != null) {
            recordFile = config?.outPutFile.apply { takeIf { it?.exists() == false }?.mkdirs() }
        } else if (!config?.outPutFilePath.isNullOrEmpty()) {
            val fileName = config?.outPutFileName ?: System.currentTimeMillis().toString()
            val dir = File(config?.outPutFilePath).apply { takeIf { !it.exists() }?.mkdirs() }
            recordFile = File(dir, fileName).apply { takeIf { !it.exists() }?.createNewFile() }
        } else {
            isOk = false
        }
        return isOk
    }

    private fun initAEC(audioSessionId: Int) {
        if (audioSessionId == 0) return
        if (NoiseSuppressor.isAvailable()) {
            noiseSuppressor?.release()
            noiseSuppressor = null
            noiseSuppressor = NoiseSuppressor.create(audioSessionId)
            noiseSuppressor?.enabled = true
        }
        if (AcousticEchoCanceler.isAvailable()) {
            acousticEchoCanceler?.release()
            acousticEchoCanceler = null
            acousticEchoCanceler = AcousticEchoCanceler.create(audioSessionId)
            acousticEchoCanceler?.enabled = true
        }
        if (AutomaticGainControl.isAvailable()) {
            automaticGainControl?.release()
            automaticGainControl = null
            automaticGainControl = AutomaticGainControl.create(audioSessionId)
            automaticGainControl?.enabled = true
        }
    }

    /**
     * 是否正在录音
     */
    override fun isRecording(): Boolean = isRecording

    /**
     * 是否正在暂停
     */
    override fun isPaused(): Boolean = isPause

    /**
     * 获取录音状态
     */
    override fun getRecordState(): Int = state
}