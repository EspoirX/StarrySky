package com.lzx.record

import android.content.Context
import com.lzx.record.recorder.IRecorder

object StarrySkyRecord {

    //具体录音实现
    var recorder: IRecorder? = null

    private var context: Context? = null

    fun with(): RecordConfig.Builder {
        return RecordConfig.Builder()
    }

    fun getContext() = context
}