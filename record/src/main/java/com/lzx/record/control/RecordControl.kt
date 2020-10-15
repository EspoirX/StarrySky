package com.lzx.record.control

import androidx.lifecycle.MutableLiveData
import com.lzx.record.RecordConst
import com.lzx.record.recorder.OnRecordEventListener
import com.lzx.record.recorder.RecordState

interface RecordControl {
    fun startRecord(filePath: String, fileName: String, format: String)
    fun cancelRecording()
    fun stopRecording(delete: Boolean)
    fun startRecording(filePath: String,
                       fileName: String,
                       format: String,
                       channelCount: String = RecordConst.STEREO,
                       sampleRate: String = RecordConst.HZ_44100,
                       bitrate: String = RecordConst.KBPS_128000)

    fun pauseRecording()
    fun resumeRecording()
    fun release()
    fun addRecordEventListener(listener: OnRecordEventListener?, tag: String)
    fun removeRecordEventListener(tag: String)
    fun clearRecordEventListener()
    fun recordState(): MutableLiveData<RecordState>
    fun onRecordStateChange(state: RecordState)
}