package com.lzx.record.impl

import com.lzx.record.IRecorder
import com.lzx.record.IntArrayList
import com.lzx.record.RecorderCallback
import java.io.File

class RecorderManager(private val recorder: IRecorder?) : RecorderCallback {
    private var deleteRecord = false
    private var recordingDuration: Long = 0
    private var recordingData: IntArrayList? = null
    var callback: OnRecordManagerCallback? = null
    private var audioDecoder: AudioDecoder? = null
    private var isProcessing = false

    init {
        recordingData = IntArrayList()
        audioDecoder = AudioDecoder()
        recorder?.setRecorderCallback(this)
    }

    fun startRecord(filePath: String, channels: Int, rate: Int, bitrate: Int) {
        when {
            recorder?.isPaused() == true -> {
                recorder.startRecording()
            }
            recorder?.isRecording() == false -> {
                recorder.prepare(filePath, channels, rate, bitrate)
            }
            else -> {
                recorder?.pauseRecording()
            }
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

    override fun onPrepareRecord() {
        recorder?.startRecording()
    }

    override fun onStartRecord(output: File?) {
        recordingDuration = 0
        callback?.onRecordingStarted(output)
    }

    override fun onPauseRecord() {
        callback?.onRecordingPaused()
    }

    override fun onRecordProgress(mills: Long, amplitude: Int) {
        recordingDuration = mills
        callback?.onRecordingProgress(mills, amplitude)
        recordingData?.add(amplitude)
    }

    override fun onStopRecord(output: File?) {
        callback?.onRecordingStopped(output)
//        heavy {
//
//            if (output == null) return@heavy null
//            if (!output.exists()) {
//                return@heavy null
//            }
//            val name = output.name?.toLowerCase(Locale.getDefault()) ?: ""
//            val components = name.split("\\.").toTypedArray()
//            if (components.size < 2) {
//                return@heavy null
//            }
//            val ext = components[components.lastIndex] //获取后缀
//            val isInTrash = "del".equals(ext, ignoreCase = true)   //判断后缀是否是 del，del是以删除的意思
//            if (!isInTrash && !ext.isSupportedExtension()) {   //如果不是支持的格式，则返回
//                return@heavy null
//            }
//            //获取录音信息
//            var format: MediaFormat? = null
//            val extractor = MediaExtractor()
//            extractor.setDataSource(output.path)
//            val numTracks = extractor.trackCount
//            var i = 0
//            while (i < numTracks) {
//                format = extractor.getTrackFormat(i)
//                if (format.getString(MediaFormat.KEY_MIME).startsWith("audio/")) {
//                    extractor.selectTrack(i)
//                    break
//                }
//                i++
//            }
//            if (i == numTracks || format == null) {
//                return@heavy null
//            }
//            val channelCount = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
//            val sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
//            val bitrate = format.getInteger(MediaFormat.KEY_BIT_RATE)
//            val duration = format.getLong(MediaFormat.KEY_DURATION)
//            val mimeType = format.getString(MediaFormat.KEY_MIME)
//
//            val nameWithoutExt = output.name.removeFileExtension()
//            val fileFormat = output.readFileFormat(mimeType)
//            val info = RecordInfo(nameWithoutExt, fileFormat, duration, output.length(),
//                output.absolutePath, output.lastModified(), sampleRate, channelCount,
//                bitrate, isInTrash)
//
//            if (info.duration <= 0) {
//                info.duration = recordingDuration
//            }
//            recordingDuration = 0
//
//            val waveForm = convertRecordingData(recordingData!!, (duration / 1000000f).toInt())
//            info.waveForm = waveForm
//            val path: String = output.path
//            if (path.isNotEmpty() && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
//                isProcessing = true
//                audioDecoder?.decodeFile(output, extractor, format, mimeType, decodeListener = object : AudioDecoder.DecodeListener {
//                    override fun onStartDecode(duration: Long, channelsCount: Int, sampleRate: Int) {
//                        runOnMainThread {
//                            callback?.onRecordProcessing()
//                        }
//                    }
//
//                    override fun onFinishDecode(data: IntArray?, duration: Long) {
//                        info.data = data?.int2byte()
//                        isProcessing = false
//                        runOnMainThread {
//                            callback?.onRecordFinishProcessing()
//                        }
//                    }
//
//                    override fun onError(exception: Exception?) {
//                        isProcessing = false
//                    }
//                })
//            } else {
//                isProcessing = false
//            }
//            return@heavy info
//        }.runOn(BG).onResponse {
//
//        }.onError {
//            it?.printStackTrace()
//        }.run()
    }

    override fun onError(throwable: Exception?) {
        callback?.onError(throwable)
    }

    interface OnRecordManagerCallback {
        fun onRecordingStarted(file: File?)
        fun onRecordingPaused()
        fun onRecordProcessing()
        fun onRecordFinishProcessing()
        fun onRecordingStopped(file: File?)
        fun onRecordingProgress(mills: Long, amp: Int)
        fun onError(throwable: Exception?)
    }
}