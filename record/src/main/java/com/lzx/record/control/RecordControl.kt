package com.lzx.record.control

import com.lzx.record.RecordConfig

interface RecordControl {
    fun startRecord(filePath: String)
    fun startRecord(filePath: String, config: RecordConfig)
    fun cancelRecording()
    fun stopRecording(delete: Boolean)
}