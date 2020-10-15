package com.lzx.record.control

import androidx.lifecycle.MutableLiveData
import com.lzx.record.RecordConst
import com.lzx.record.recorder.OnRecordEventListener
import com.lzx.record.recorder.RecordState
import com.lzx.record.recorder.RecorderManager
import com.lzx.record.utils.keyToBitrate
import com.lzx.record.utils.keyToSampleRate
import java.io.File

class RecordControlImpl(private val manager: RecorderManager) : RecordControl {

    private val recordState = MutableLiveData<RecordState>()
    private val recordEventListener = hashMapOf<String, OnRecordEventListener>()

    override fun startRecord(filePath: String, fileName: String, format: String) {
        val fileDir = File(filePath)
        if (!fileDir.exists()) {
            fileDir.mkdirs()
        }
        val file = File(filePath, fileName)
        if (!file.exists()) {
            file.createNewFile()
        }
        val channels = 2
        val rate = RecordConst.HZ_44100.keyToSampleRate()
        val bitrate = RecordConst.KBPS_128000.keyToBitrate()
        manager.startRecord(file.absolutePath, channels, rate, bitrate)
    }

    override fun startRecording(filePath: String, fileName: String, format: String,
                                channelCount: String, sampleRate: String, bitrate: String) {
        val fileDir = File(filePath)
        if (!fileDir.exists()) {
            fileDir.mkdirs()
        }
        val file = File(filePath, fileName)
        if (!file.exists()) {
            file.createNewFile()
        }
        val channels = if (channelCount == RecordConst.MONO) 1 else 2
        val rate = sampleRate.keyToSampleRate()
        val bit = bitrate.keyToBitrate()
        manager.startRecord(file.absolutePath, channels, rate, bit)
    }

    override fun pauseRecording() {
        manager.pauseRecording()
    }

    override fun resumeRecording() {
        manager.resumeRecording()
    }

    override fun cancelRecording() {
        manager.cancelRecording()
    }

    override fun stopRecording(delete: Boolean) {
        manager.stopRecording(delete)
    }

    override fun release() {
        manager.release()
    }

    override fun addRecordEventListener(listener: OnRecordEventListener?, tag: String) {
        listener?.let {
            if (!recordEventListener.containsKey(tag)) {
                recordEventListener[tag] = it
            }
        }
    }

    override fun removeRecordEventListener(tag: String) {
        recordEventListener.remove(tag)
    }

    override fun clearRecordEventListener() {
        recordEventListener.clear()
    }

    override fun recordState(): MutableLiveData<RecordState> = recordState

    override fun onRecordStateChange(state: RecordState) {
        recordState.value = state
    }


}