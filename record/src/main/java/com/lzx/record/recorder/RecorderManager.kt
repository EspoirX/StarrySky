package com.lzx.record.recorder

import android.media.MediaExtractor
import android.media.MediaFormat
import android.os.AsyncTask
import com.lzx.basecode.MainLooper
import com.lzx.record.RecordInfo
import com.lzx.record.utils.IntArrayList
import com.lzx.record.utils.convertRecordingData
import com.lzx.record.utils.int2byte
import com.lzx.record.utils.isSupportedExtension
import com.lzx.record.utils.readFileFormat
import com.lzx.record.utils.removeFileExtension
import java.io.File
import java.util.Locale

class RecorderManager(private val recorder: IRecorder?) : RecorderCallback {
    private var deleteRecord = false
    private var recordingDuration: Long = 0
    private var recordingData: IntArrayList? = null
    var callback: OnRecordStateUpdated? = null
    private var audioDecoder: AudioDecoder? = null
    private var isProcessing = false

    init {
        recordingData = IntArrayList()
        audioDecoder = AudioDecoder()
        recorder?.setRecorderCallback(this)
    }

    fun startRecord(filePath: String, fileName: String, format: String, channels: Int, rate: Int, bitrate: Int) {
        if (recorder == null) return

        val fileDir = File(filePath)
        if (!fileDir.exists()) {
            fileDir.mkdirs()
        }
        val file = File(filePath, "$fileName.$format")
        if (!file.exists()) {
            file.createNewFile()
        }

        if (recorder.isPaused()) {
            recorder.startRecording()
        } else if (!recorder.isRecording()) {
            recorder.prepare(file.absolutePath, channels, rate, bitrate)
        } else {
            recorder.pauseRecording()
        }
    }

    fun cancelRecording() {
        deleteRecord = true
        recorder?.pauseRecording()
    }

    fun stopRecording(delete: Boolean) {
        if (recorder?.isRecording() == true) {
            deleteRecord = delete
            recorder.stopRecording()
        }
    }

    fun pauseRecording() {
        if (recorder?.isRecording() == true) {
            recorder.pauseRecording()
        }
    }

    fun resumeRecording() {
        if (recorder?.isPaused() == true) {
            recorder.startRecording()
        }
    }

    fun release() {
        recordingData?.clear()
        recorder?.stopRecording()
    }

    override fun onPrepareRecord() {
        recorder?.startRecording()
    }

    override fun onStartRecord(output: File?) {
        recordingDuration = 0
        onRecordStateChange(RecordState.STATE_START, output)
    }

    override fun onPauseRecord() {
        onRecordStateChange(RecordState.STATE_PAUSE)
    }

    override fun onRecordProgress(mills: Long, amplitude: Int) {
        recordingDuration = mills
        onRecordStateChange(RecordState.STATE_PROGRESS, mills = mills, amplitude = amplitude)
        recordingData?.add(amplitude)
    }

    override fun onStopRecord(output: File?) {
        onRecordStateChange(RecordState.STATE_STOP, output)
        AsyncTask.THREAD_POOL_EXECUTOR.execute {
            if (output == null) return@execute
            if (!output.exists()) {
                onRecordStateChange(RecordState.STATE_ERROR, throwable = IllegalAccessException("file is not exists"))
                return@execute
            }
            val name = output.name?.toLowerCase(Locale.getDefault()) ?: ""
            val components = name.split(".").toTypedArray()
            if (components.size < 2) {
                onRecordStateChange(RecordState.STATE_ERROR, throwable = IllegalAccessException("file name error format,file name = " + output.name))
                return@execute
            }
            val ext = components[components.lastIndex] //获取后缀
            val isInTrash = "del".equals(ext, ignoreCase = true)   //判断后缀是否是 del，del是以删除的意思
            if (!isInTrash && !ext.isSupportedExtension()) {   //如果不是支持的格式，则返回
                onRecordStateChange(RecordState.STATE_ERROR, throwable = IllegalAccessException("the file format not supported"))
                return@execute
            }
            //获取录音信息
            var format: MediaFormat? = null
            val extractor = MediaExtractor()
            extractor.setDataSource(output.path)
            val numTracks = extractor.trackCount
            var i = 0
            while (i < numTracks) {
                format = extractor.getTrackFormat(i)
                if (format.getString(MediaFormat.KEY_MIME).startsWith("audio/")) {
                    extractor.selectTrack(i)
                    break
                }
                i++
            }
            if (i == numTracks || format == null) {
                onRecordStateChange(RecordState.STATE_ERROR, throwable = IllegalAccessException("没有找到合适的音轨"))
                return@execute
            }
            val channelCount = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
            val sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
            val bitrate = format.getInteger(MediaFormat.KEY_BIT_RATE)
            val duration = format.getLong(MediaFormat.KEY_DURATION)
            val mimeType = format.getString(MediaFormat.KEY_MIME)

            val nameWithoutExt = output.name.removeFileExtension()
            val fileFormat = output.readFileFormat(mimeType)
            val info = RecordInfo(nameWithoutExt, fileFormat, duration, output.length(),
                output.absolutePath, output.lastModified(), sampleRate, channelCount,
                bitrate, isInTrash)

            if (info.duration <= 0) {
                info.duration = recordingDuration
            }
            recordingDuration = 0
            val waveForm = convertRecordingData(recordingData!!, (duration / 1000000f).toInt())
            info.waveForm = waveForm
            val path: String = output.path
            if (path.isNotEmpty() && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                isProcessing = true
                audioDecoder?.decodeFile(output, info, extractor, format, mimeType, decodeListener = object : AudioDecoder.DecodeListener {
                    override fun onStartDecode(duration: Long, channelsCount: Int, sampleRate: Int) {
                        onRecordStateChange(RecordState.STATE_PROCESSING)
                    }

                    override fun onFinishDecode(data: IntArray?, duration: Long) {
                        info.data = data?.int2byte()
                        isProcessing = false
                        onRecordStateChange(RecordState.STATE_PROCESSING_FINISH)
                        onRecordStateChange(RecordState.STATE_STOP, recordInfo = info)
                    }

                    override fun onError(exception: Exception?) {
                        isProcessing = false
                        onRecordStateChange(RecordState.STATE_ERROR, throwable = exception)
                    }
                })
            } else {
                isProcessing = false
                onRecordStateChange(RecordState.STATE_STOP, recordInfo = info)
            }
        }
    }

    override fun onError(throwable: Exception?) {
        onRecordStateChange(RecordState.STATE_ERROR, throwable = throwable)
    }

    private fun onRecordStateChange(state: String,
                                    output: File? = null,
                                    recordInfo: RecordInfo? = null,
                                    mills: Long = 0,
                                    amplitude: Int = 0,
                                    throwable: Exception? = null) {
        MainLooper.instance.runOnUiThread(Runnable {
            val recordState = RecordState()
            recordState.state = state
            when (state) {
                RecordState.STATE_START -> {
                    recordState.recordFile = output
                }
                RecordState.STATE_STOP -> {
                    recordState.recordFile = output
                    recordState.recordInfo = recordInfo
                }
                RecordState.STATE_PROGRESS -> {
                    recordState.recordMills = mills
                    recordState.amplitude = amplitude
                }
                RecordState.STATE_ERROR -> {
                    recordState.error = throwable
                }
            }
            callback?.onRecordStateChange(recordState)
        })
    }


    interface OnRecordStateUpdated {
        fun onRecordStateChange(state: RecordState)
    }
}