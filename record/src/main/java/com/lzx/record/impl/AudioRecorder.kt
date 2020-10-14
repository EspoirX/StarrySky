package com.lzx.record.impl

import android.media.MediaRecorder
import android.os.Build
import com.lzx.record.IRecorder
import com.lzx.record.RecorderCallback
import java.io.File
import java.util.Timer
import java.util.TimerTask


class AudioRecorder : IRecorder {

    private var recorder: MediaRecorder? = null
    private var recordFile: File? = null

    private var isPrepared = false
    private var isRecording = false
    private var isPaused = false
    private var progress: Long = 0
    private var timerProgress: Timer? = null

    private var recorderCallback: RecorderCallback? = null


    override fun setRecorderCallback(callback: RecorderCallback?) {
        recorderCallback = callback
    }

    override fun prepare(outputFile: String?, channelCount: Int, sampleRate: Int, bitrate: Int) {
        recordFile = File(outputFile)
        if (recordFile?.exists() == true && recordFile?.isFile == true) {
            recorder = MediaRecorder()
            recorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
            recorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            recorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            recorder?.setAudioChannels(channelCount)
            recorder?.setAudioSamplingRate(sampleRate)
            recorder?.setAudioEncodingBitRate(bitrate)
            recorder?.setMaxDuration(-1) //持续时间不受限制，或使用RECORD_MAX_DURATION
            recorder?.setOutputFile(recordFile?.absolutePath)
            try {
                recorder?.prepare()
                isPrepared = true
                recorderCallback?.onPrepareRecord()
            } catch (ex: Exception) {
                ex.printStackTrace()
                recorderCallback?.onError(ex)
            }
        } else {
            recorderCallback?.onError(IllegalArgumentException("invalid output file"))
        }
    }

    override fun startRecording() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && isPaused) {
            try {
                recorder?.resume()
                startRecordingTimer()
                recorderCallback?.onStartRecord(recordFile)
                isPaused = false
            } catch (ex: Exception) {
                ex.printStackTrace()
                recorderCallback?.onError(ex)
            }
        } else {
            if (isPrepared) {
                try {
                    recorder?.start()
                    isRecording = true
                    startRecordingTimer()
                    recorderCallback?.onStartRecord(recordFile)
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    recorderCallback?.onError(ex)
                }
            }
            isPaused = false
        }
    }

    override fun pauseRecording() {
        if (isRecording) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                try {
                    recorder?.pause()
                    pauseRecordingTimer()
                    recorderCallback?.onPauseRecord()
                    isPaused = true
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    recorderCallback?.onError(ex)
                }
            } else {
                stopRecording()
            }
        }
    }

    override fun stopRecording() {
        if (isRecording) {
            stopRecordingTimer()
            try {
                recorder?.stop()
            } catch (ex: Exception) {
                ex.printStackTrace()
                recorderCallback?.onError(ex)
            }
            recorder?.release()
            recorderCallback?.onStopRecord(recordFile)
            recordFile = null
            isPrepared = false
            isRecording = false
            isPaused = false
            recorder = null
        }
    }

    override fun isRecording(): Boolean = isRecording

    override fun isPaused(): Boolean = isPaused

    private fun startRecordingTimer() {
        timerProgress = Timer()
        timerProgress?.schedule(object : TimerTask() {
            override fun run() {
                try {
                    recorderCallback?.onRecordProgress(progress, recorder?.maxAmplitude ?: 0)
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
                progress += 1000 / 25
            }
        }, 0, 1000 / 25)
    }

    private fun stopRecordingTimer() {
        timerProgress?.cancel()
        timerProgress?.purge()
        progress = 0
    }

    private fun pauseRecordingTimer() {
        timerProgress?.cancel()
        timerProgress?.purge()
    }
}