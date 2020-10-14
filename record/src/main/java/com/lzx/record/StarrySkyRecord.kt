package com.lzx.record

import android.content.Context
import com.lzx.record.control.RecordControl
import com.lzx.record.control.RecordControlImpl
import com.lzx.record.impl.AudioRecorder
import com.lzx.record.impl.RecorderManager

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
    }


    fun getContext() = context
}