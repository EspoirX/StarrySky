package com.lzx.record.recorder

interface OnRecordEventListener {
    fun onRecordStateChange(state: RecordState)
}