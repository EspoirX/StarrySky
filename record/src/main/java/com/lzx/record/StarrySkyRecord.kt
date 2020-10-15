package com.lzx.record

import android.content.Context
import com.lzx.record.control.RecordControl
import com.lzx.record.control.RecordControlImpl
import com.lzx.record.recorder.AudioRecorder
import com.lzx.record.recorder.IRecorder
import com.lzx.record.recorder.RecordState
import com.lzx.record.recorder.RecorderManager

object StarrySkyRecord {

    //具体录音实现
    private var recorder: IRecorder? = null
    private var recordControl: RecordControl? = null
    private var context: Context? = null

    fun initRecorder(context: Context, recorder: IRecorder? = null) {
        this.context = context
        this.recorder = recorder ?: AudioRecorder()


        val manager = RecorderManager(this.recorder)
        recordControl = RecordControlImpl(manager)
        manager.callback = object : RecorderManager.OnRecordStateUpdated {
            override fun onRecordStateChange(state: RecordState) {
                recordControl?.onRecordStateChange(state)
            }
        }
    }

    fun with(): RecordControl {
        if (recordControl == null) {
            throw NullPointerException("recordControl is null")
        }
        return recordControl!!
    }

    fun getContext() = context
}