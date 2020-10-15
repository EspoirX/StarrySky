package com.lzx.record.recorder

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.os.Build
import androidx.annotation.RequiresApi
import com.lzx.record.utils.IntArrayList
import com.lzx.record.StarrySkyRecord
import com.lzx.record.utils.getScreenWidth
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.sqrt


class AudioDecoder {
    companion object {
        const val QUEUE_INPUT_BUFFER_EFFECTIVE = 1 // Most effective and fastest
        const val QUEUE_INPUT_BUFFER_SIMPLE = 2 // Less effective and slower
    }

    private var dpPerSec: Float = 25f

    private var sampleRate = 0
    private var channelCount = 0
    private var oneFrameAmps: IntArray? = null
    private var frameIndex = 0

    private var duration: Long = 0
    private var TRASH_EXT = "del"

    private var gains: IntArrayList? = null

    interface DecodeListener {
        fun onStartDecode(duration: Long, channelsCount: Int, sampleRate: Int)
        fun onFinishDecode(data: IntArray?, duration: Long)
        fun onError(exception: Exception?)
    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun decodeFile(file: File,
                   extractor: MediaExtractor,
                   format: MediaFormat,
                   mimeType: String,
                   queueType: Int = QUEUE_INPUT_BUFFER_EFFECTIVE,
                   decodeListener: DecodeListener) {
        gains = IntArrayList()
        val decoder = MediaCodec.createDecoderByType(mimeType)
        decodeListener.onStartDecode(duration, channelCount, sampleRate)
        decoder.setCallback(object : MediaCodec.Callback() {
            private var mOutputEOS = false
            private var mInputEOS = false
            override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
                if (mOutputEOS or mInputEOS) return
                try {
                    val inputBuffer: ByteBuffer = codec.getInputBuffer(index) ?: return
                    var sampleTime: Long = 0
                    var result: Int
                    if (queueType == QUEUE_INPUT_BUFFER_EFFECTIVE) {
                        var total = 0
                        var advanced = false
                        var maxresult = 0
                        do {
                            result = extractor.readSampleData(inputBuffer, total)
                            if (result >= 0) {
                                total += result
                                sampleTime = extractor.sampleTime
                                advanced = extractor.advance()
                                maxresult = maxresult.coerceAtLeast(result)
                            }
                        } while (result >= 0
                            && total < maxresult * 5
                            && advanced
                            && inputBuffer.capacity() - inputBuffer.limit() > maxresult * 3) //3这只是为了保险。 当删除它时，会发生崩溃。 如果将其替换为2号就可以了。

                        if (advanced) {
                            codec.queueInputBuffer(index, 0, total, sampleTime, 0)
                        } else {
                            codec.queueInputBuffer(index, 0, 0, -1, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                            mInputEOS = true
                        }
                    } else {
                        result = extractor.readSampleData(inputBuffer, 0)
                        if (result >= 0) {
                            sampleTime = extractor.sampleTime
                            codec.queueInputBuffer(index, 0, result, sampleTime, 0)
                            extractor.advance()
                        } else {
                            codec.queueInputBuffer(index, 0, 0, -1, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                            mInputEOS = true
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onOutputBufferAvailable(codec: MediaCodec, index: Int, info: MediaCodec.BufferInfo) {
                try {
                    val outputBuffer = codec.getOutputBuffer(index)
                    if (outputBuffer != null) {
                        outputBuffer.rewind()
                        outputBuffer.order(ByteOrder.LITTLE_ENDIAN)
                        while (outputBuffer.remaining() > 0) {
                            oneFrameAmps!![frameIndex] = outputBuffer.short.toInt()
                            frameIndex++
                            if (frameIndex >= oneFrameAmps!!.size - 1) {
                                var gain: Int
                                var value: Int
                                gain = -1
                                var j = 0
                                while (j < oneFrameAmps!!.size) {
                                    value = 0
                                    for (k in 0 until channelCount) {
                                        value += oneFrameAmps!![j + k]
                                    }
                                    value /= channelCount
                                    if (gain < value) {
                                        gain = value
                                    }
                                    j += channelCount
                                }
                                gains!!.add(sqrt(gain.toDouble()).toInt())
                                frameIndex = 0
                            }
                        }
                    }
                    mOutputEOS = mOutputEOS or (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0)
                    codec.releaseOutputBuffer(index, false)
                    if (mOutputEOS) {
                        decodeListener.onFinishDecode(gains!!.data, duration)
                        codec.stop()
                        codec.release()
                        extractor.release()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onError(codec: MediaCodec, exception: MediaCodec.CodecException) {
                if (queueType == QUEUE_INPUT_BUFFER_EFFECTIVE) {
                    try {
                        val decoder = AudioDecoder()
                        decoder.decodeFile(file, extractor, format, mimeType, QUEUE_INPUT_BUFFER_SIMPLE, decodeListener)
                    } catch (e: Exception) {
                        decodeListener.onError(exception)
                    }
                } else {
                    decodeListener.onError(exception)
                }
            }

            override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
            }
        })
        decoder.configure(format, null, null, 0)
        decoder.start()
    }

    fun getDpPerSecond(durationSec: Float): Float {
        return if (durationSec > 20) {
            1.5f * StarrySkyRecord.getContext()!!.getScreenWidth() / durationSec
        } else {
            25f
        }
    }

    private fun calculateSamplesPerFrame(): Int {
        return (sampleRate / dpPerSec).toInt()
    }
}