package com.lzx.record.recorder

import com.lzx.basecode.AudioDecoder
import com.lzx.basecode.Playback
import com.lzx.record.RecordConfig
import com.lzx.record.player.AudioTrackPlayer
import java.io.File

/**
 * 录音回调
 */
interface RecorderCallback {

    /**
     * 开始录音
     */
    fun onStart()

    /**
     * 重新
     */
    fun onResume()

    /**
     * 重置
     */
    fun onReset()

    /**
     * 正在录音
     * @param time 已经录制的时间
     * @param volume 当前声音大小
     */
    fun onRecording(time: Long, volume: Int)

    /**
     * 暂停
     */
    fun onPause()

    /**
     * 提醒快到最大时间了
     * 到达提醒时间，默认提醒时间是最大时间前10秒
     * @param duration
     */
    fun onRemind(duration: Long)

    /**
     * 录制成功
     * @param file 保存的文件
     * @param time 录制时间 （毫秒ms）
     */
    fun onSuccess(file: File?, time: Long)

    /**
     * 错误时
     */
    fun onError(msg: String)

    /**
     * 达到“最大”时间，自动完成的操作
     * （毫秒ms）
     */
    fun onAutoComplete(file: String, time: Long)
}

/**
 * 录音播放器回调
 */
interface PlayerListener {

    /**
     * 开始播放
     */
    fun onStart()

    /**
     * 暂停
     */
    fun onPause()

    /**
     * 继续播放
     */
    fun onResume()

    /**
     * 停止释放
     */
    fun onStop()

    /**
     * 播放结束
     */
    fun onCompletion()

    /**
     * 错误
     */
    fun onError(msg: String)

    /**
     * 进度条
     * @param current 当前位置
     * @param duration 总长度
     */
    fun onProgress(current: Int, duration: Int)
}

abstract class SimpleRecorderCallback : RecorderCallback {
    override fun onStart() {}

    override fun onResume() {}

    override fun onReset() {}

    override fun onRecording(time: Long, volume: Int) {}

    override fun onPause() {}

    override fun onRemind(duration: Long) {}

    override fun onSuccess(file: File?, time: Long) {}

    override fun onError(msg: String) {}

    override fun onAutoComplete(file: String, time: Long) {}
}

/**
 * 录音实现接口
 */
interface IRecorder {
    fun setUpRecordConfig(recordConfig: RecordConfig)
    fun getPlayer(): Playback?
    fun startRecording()
    fun pauseRecording()
    fun stopRecording()
    fun resumeRecording()
    fun onReset()
    fun setVolume(volume: Float)
    fun isRecording(): Boolean
    fun isPaused(): Boolean
    fun getRecordState(): Int
//    fun setOnDecodeListener(listener: AudioDecoder.OnDecodeListener)
}

/**
 * 录音状态
 */
object RecordState {
    const val RECORDING = 0
    const val PAUSED = 1
    const val STOPPED = 2
}