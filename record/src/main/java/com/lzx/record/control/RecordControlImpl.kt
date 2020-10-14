package com.lzx.record.control

import com.lzx.record.RecordConfig
import com.lzx.record.RecordConst
import com.lzx.record.impl.RecorderManager
import com.lzx.record.keyToBitrate
import com.lzx.record.keyToSampleRate

class RecordControlImpl(private val manager: RecorderManager) : RecordControl {


    override fun startRecord(filePath: String) {
        val config = RecordConfig()
        val channels = if (config.channels == RecordConst.MONO) 1 else 2
        val rate = config.sampleRate.keyToSampleRate()
        val bitrate = config.bitRate.keyToBitrate()
        manager.startRecord(filePath, channels, rate, bitrate)
    }

    override fun startRecord(filePath: String, config: RecordConfig) {
        val channels = if (config.channels == RecordConst.MONO) 1 else 2
        val rate = config.sampleRate.keyToSampleRate()
        val bitrate = config.bitRate.keyToBitrate()
        manager.startRecord(filePath, channels, rate, bitrate)
    }

    override fun cancelRecording() {
        manager.cancelRecording()
    }

    override fun stopRecording(delete: Boolean) {
        manager.stopRecording(delete)
    }
}