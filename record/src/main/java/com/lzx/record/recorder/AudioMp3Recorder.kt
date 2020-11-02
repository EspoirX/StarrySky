package com.lzx.record.recorder

import android.media.AudioFormat
import android.media.AudioRecord
import android.os.AsyncTask
import com.lzx.basecode.MainLooper
import com.lzx.record.RecordConfig
import com.lzx.record.RecordState
import com.lzx.record.test.LameManager
import java.io.File
import java.io.FileOutputStream
import kotlin.math.sqrt

/**
 * AudioRecord + lame 录制 mp3
 */
class AudioMp3Recorder(private val config: RecordConfig) : IRecorder {
    val mp3 = "mp3"
    val aac = "aac"
    val wav = "wav"
    val amr = "amr"

    companion object {
        private const val FRAME_COUNT = 160
    }

    // 获取最小缓存区大小
    var bufferSizeInBytes: Int = 0

    private var audioRecord: AudioRecord? = null
    private var recordFile: File? = null
    private var isRecording = false
    private var isPause: Boolean = false

    private var state = RecordState.STOPPED   //当前状态
    private var recordVolume: Int = 0 //录制音量
    private var duration = 0L //录制时间

    override fun startRecording() {
        if (!checkRecordParameter()) {
            config.recordCallback?.onError("录音参数有错误，请检查")
            return
        }
        if (isRecording) {
            config.recordCallback?.onError("正在录音中")
            return
        }
        isRecording = true

        AsyncTask.THREAD_POOL_EXECUTOR.execute {
            try {
                //获取最小缓存区大小
                bufferSizeInBytes = AudioRecord.getMinBufferSize(config.sampleRate, config.channelConfig, config.audioFormat)

                val bytesPerFrame = if (config.audioFormat == AudioFormat.ENCODING_PCM_8BIT) 1 else 2

                //使能被整除，方便下面的周期性通知
                var frameSize = bufferSizeInBytes / bytesPerFrame
                if (frameSize % FRAME_COUNT != 0) {
                    frameSize += FRAME_COUNT - frameSize % FRAME_COUNT
                    bufferSizeInBytes = frameSize * bytesPerFrame
                }

                //创建 AudioRecord
                audioRecord = AudioRecord(config.audioSource, config.sampleRate, config.channelConfig, config.audioFormat, bufferSizeInBytes)

                val pcmBuffer = ShortArray(bufferSizeInBytes)

                //初始化lame
                LameManager.init(config.sampleRate, config.channelConfig, config.sampleRate, config.bitRate, config.quality)

                audioRecord?.positionNotificationPeriod = FRAME_COUNT

                val fos = FileOutputStream(recordFile, config.isContinue)
                val mp3buffer = ByteArray((7200 + pcmBuffer.size * 2.0 * 1.25).toInt())

                audioRecord?.startRecording()

                //PCM文件大小 = 采样率采样时间采样位深 / 8*通道数（Bytes）
                val bytesPerSecond = audioRecord!!.sampleRate * audioRecord!!.audioFormat.format() / 8 * audioRecord!!.channelCount

                onStart()

                while (isRecording) {
                    val readSize: Int = audioRecord?.read(pcmBuffer, 0, bufferSizeInBytes) ?: 0
                    if (readSize == AudioRecord.ERROR_INVALID_OPERATION || readSize == AudioRecord.ERROR_BAD_VALUE) {
                        //错误
                        onError("需要录音权限")
                    } else if (readSize > 0) {
                        if (isPause) continue

                        val readTime = 1000.0 * readSize.toDouble() * 2 / bytesPerSecond

                        val encodeSize: Int = LameManager.encode(pcmBuffer, pcmBuffer, readSize, mp3buffer)
                        if (encodeSize > 0) {
                            fos.write(mp3buffer, 0, encodeSize)
                        }
                        calculateRealVolume(pcmBuffer, readSize)
                        //short 是2个字节 byte 是1个字节8位
                        onRecording(readTime)
                    } else {
                        //错误
                        onError("需要录音权限")
                    }
                }

                //录音结束 将MP3结尾信息写入buffer中
                val flushResult = LameManager.flush(mp3buffer)
                if (flushResult > 0) {
                    fos.use {
                        it.write(mp3buffer, 0, flushResult)
                    }
                }
                //关闭资源
                audioRecord?.stop()
                audioRecord?.release()
                LameManager.close()
            } catch (ex: Exception) {
                ex.printStackTrace()
                onError(ex.message.toString())
            }
        }
    }

    private fun onRecording(readTime: Double) {
        duration += readTime.toLong()
        MainLooper.instance.runOnUiThread {
            //录制回调
            config.recordCallback?.onRecording(duration, recordVolume)
            //提示快到录音时间了
            if (config.recordMaxTime > 15000 && duration > config.recordMaxTime - 10000) {
                config.recordCallback?.onRemind(duration)
            }
            config.recordCallback?.onRecording(duration, recordVolume)
        }
        if (duration > config.recordMaxTime) {
            autoStop()
        }
    }

    private fun autoStop() {
        if (state != RecordState.STOPPED) {
            isPause = false
            isRecording = false
            MainLooper.instance.runOnUiThread { config.recordCallback?.onSuccess(recordFile, duration) }
            state = RecordState.STOPPED
        }
    }

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

    private fun onStart() {
        if (state != RecordState.RECORDING) {
            MainLooper.instance.runOnUiThread { config.recordCallback?.onStart() }
            state = RecordState.RECORDING
            duration = 0
        }
    }

    private fun onError(msg: String) {
        isPause = false
        isRecording = false
        MainLooper.instance.runOnUiThread { config.recordCallback?.onError(msg) }
        state = RecordState.STOPPED
    }

    private fun checkRecordParameter(): Boolean {
        var isOk = true
        if (config.outPutFile != null) {
            recordFile = config.outPutFile.apply { takeIf { it?.exists() == false }?.mkdirs() }
        } else if (!config.outPutFilePath.isNullOrEmpty()) {
            val fileName = config.outPutFileName ?: System.currentTimeMillis().toString()
            val dir = File(config.outPutFilePath).apply { takeIf { !it.exists() }?.mkdirs() }
            recordFile = File(dir, fileName).apply { takeIf { !it.exists() }?.createNewFile() }
        } else {
            isOk = false
        }
        return isOk
    }

    override fun pauseRecording() {
        if (state == RecordState.RECORDING) {
            isPause = true
            state = RecordState.PAUSED
            MainLooper.instance.runOnUiThread { config.recordCallback?.onPause() }
        }
    }

    override fun stopRecording() {
        if (state != RecordState.STOPPED) {
            isPause = false
            isRecording = false
            MainLooper.instance.runOnUiThread { config.recordCallback?.onSuccess(recordFile, duration) }
            state = RecordState.STOPPED
        }
    }

    override fun resumeRecording() {
        if (state == RecordState.PAUSED) {
            isPause = false
            state = RecordState.RECORDING
            MainLooper.instance.runOnUiThread { config.recordCallback?.onResume() }
        }
    }

    override fun isRecording(): Boolean = isRecording

    override fun isPaused(): Boolean = isPause

    fun Int.format(): Int {
        return when (this) {
            AudioFormat.ENCODING_PCM_8BIT -> 8
            AudioFormat.ENCODING_PCM_16BIT -> 16
            else -> 0
        }
    }


}