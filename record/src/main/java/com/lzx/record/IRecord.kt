package com.lzx.record

import java.io.File

interface RecorderCallback {
    fun onPrepareRecord()
    fun onStartRecord(output: File?)
    fun onPauseRecord()
    fun onRecordProgress(mills: Long, amp: Int)
    fun onStopRecord(output: File?)
    fun onError(throwable: Exception?)
}

interface IRecorder {
    fun setRecorderCallback(callback: RecorderCallback?)
    fun prepare(outputFile: String?, channelCount: Int, sampleRate: Int, bitrate: Int)
    fun startRecording()
    fun pauseRecording()
    fun stopRecording()
    fun isRecording(): Boolean
    fun isPaused(): Boolean
}