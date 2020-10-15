package com.lzx.record.recorder

import com.lzx.record.RecordInfo
import java.io.File

class RecordState {
    companion object {
        const val STATE_START = "Record#STATE_START"
        const val STATE_PAUSE = "Record#STATE_PAUSE"
        const val STATE_PROCESSING = "Record#STATE_PROCESSING"
        const val STATE_PROCESSING_FINISH = "Record#STATE_PROCESSING_FINISH"
        const val STATE_STOP = "Record#STATE_STOP"
        const val STATE_PROGRESS = "Record#STATE_PROGRESS"
        const val STATE_ERROR = "Record#STATE_ERROR"
        const val STATE_IDEA = "Record#STATE_IDEA"
    }

    var state: String = STATE_IDEA
    var recordFile: File? = null
    var recordInfo: RecordInfo? = null
    var recordMills: Long = 0
    var amplitude: Int = 0
    var error: Exception? = null
}