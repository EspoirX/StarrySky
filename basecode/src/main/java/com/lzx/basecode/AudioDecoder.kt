package com.lzx.basecode

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import java.nio.ByteBuffer

class AudioDecoder(private val url: String, private val headers: HashMap<String, String>?) {

    companion object {
        const val BUFFER_SIZE = 2048
    }

    //音轨提取器
    private var mediaExtractor: MediaExtractor? = null

    //音频格式信息
    var mediaFormat: MediaFormat? = null

    //解码器
    private var mediaCodec: MediaCodec? = null

    // 解码器在此缓存中获取输入数据
    private var inputBuffers: Array<ByteBuffer>? = null

    // 编码器将解码后的数据放入此缓存中，存放的是pcm数据
    private var outputBuffers: Array<ByteBuffer>? = null

    // 用于描述解码得到的byte[]数据的相关信息
    private var bufferInfo: MediaCodec.BufferInfo? = null

    var sawInputEOS = false

    fun initMediaDecode() {
        try {
            if (url.isEmpty()) return
            mediaExtractor = MediaExtractor()
            //给音轨提取器设置文件路径
            val isNetUrl = url.startsWith("http")
            if (isNetUrl) {
                mediaExtractor?.setDataSource(url, headers)
            } else {
                mediaExtractor?.setDataSource(url)
            }
            // 获取音频格式信息
            mediaFormat = mediaExtractor?.getTrackFormat(0)
            // 获取音频类型
            val mime = mediaFormat?.getString(MediaFormat.KEY_MIME) ?: ""
            //检查是否是音频文件
            if (!mime.startsWith("audio/")) {
                return
            }
            // 选中音轨，只有一条
            mediaExtractor?.selectTrack(0)
            //创建解码器
            mediaCodec = MediaCodec.createDecoderByType(mime)
            // 配置解码器
            mediaCodec?.configure(mediaFormat, null, null, 0)
            //开始解码，等待传入数据
            mediaCodec?.start()
            // 解码器在此缓存中获取输入数据
            inputBuffers = mediaCodec?.inputBuffers
            // 编码器将解码后的数据放入此缓存中，存放的是pcm数据
            outputBuffers = mediaCodec?.outputBuffers
            // 用于描述解码得到的byte[]数据的相关信息
            bufferInfo = MediaCodec.BufferInfo()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    /**
     * 解码背景音乐，运行在子线程
     */
    fun getPcmInfo(): PcmInfo? {
        var pcmInfo: PcmInfo? = null
        try {
            if (bufferInfo == null) return null

            if (!sawInputEOS) {
                // 获取输入缓存器,-1代表一直等待，0表示不等待 建议-1,避免丢帧
                val inputIndex = mediaCodec?.dequeueInputBuffer(-1) ?: -1
                if (inputIndex >= 0) {
                    val inputBuffer = inputBuffers?.getOrNull(inputIndex) //拿到inputBuffer
                    inputBuffer?.let {
                        it.clear()   //清空之前传入inputBuffer内的数据
                        val sampleSize = mediaExtractor?.readSampleData(it, 0) ?: 0  // 读取数据到输入缓存器
                        if (sampleSize < 0) { //小于0 代表所有数据已读取完成
                            sawInputEOS = true
                            mediaCodec?.queueInputBuffer(inputIndex, 0, 0, 0L, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                        } else {
                            val presentationTimeUs = mediaExtractor?.sampleTime ?: 0
                            mediaCodec?.queueInputBuffer(inputIndex, 0, sampleSize, presentationTimeUs, 0) //通知MediaDecode解码刚刚传入的数据
                            mediaExtractor?.advance() //MediaExtractor移动到下一取样处
                        }
                    }
                }
            }
            //获取解码得到的byte[]数据 参数BufferInfo上面已介绍 10000同样为等待时间 同上-1代表一直等待，0代表不等待。此处单位为微秒
            //此处建议不要填-1 有些时候并没有数据输出，那么他就会一直卡在这 等待
            // 输出缓存器
            var outputIndex = mediaCodec?.dequeueOutputBuffer(bufferInfo!!, 0) ?: 0
            if (outputIndex == -2) {
                // 格式变了，重新获取一次
                outputIndex = mediaCodec?.dequeueOutputBuffer(bufferInfo!!, 0) ?: 0
            }
            if (outputIndex >= 0) {
                // Simply ignore codec config buffers.
                if (bufferInfo!!.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
                    mediaCodec?.releaseOutputBuffer(outputIndex, false)
                    return null
                }
                if (bufferInfo!!.size != 0) {
                    val outBuf = outputBuffers?.getOrNull(outputIndex) //拿到用于存放PCM数据的Buffer
                    outBuf?.position(bufferInfo!!.offset)
                    outBuf?.limit(bufferInfo!!.offset + bufferInfo!!.size)
                    val data = ByteArray(bufferInfo!!.size)//BufferInfo内定义了此数据块的大小
                    var successBufferGet = true
                    try {
                        //某些机器上buf可能 isAccessible false
                        outBuf?.get(data)//将Buffer内的数据取出到字节数组中
                        outBuf?.clear()
                    } catch (e: Exception) {
                        successBufferGet = false
                        e.printStackTrace()
                    }
                    if (successBufferGet) {
                        pcmInfo = PcmInfo(data, bufferInfo!!.size, mediaExtractor?.sampleTime ?: 0)
                    }
                }
                //此操作一定要做，不然MediaCodec用完所有的Buffer后 将不能向外输出数据
                mediaCodec?.releaseOutputBuffer(outputIndex, false)
                if (bufferInfo!!.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                    release()
                }
            } else if (outputIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                outputBuffers = mediaCodec?.outputBuffers
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            release()
        }
        return pcmInfo
    }

    fun release() {
        try {
            mediaCodec?.stop()
            mediaCodec?.release()
            mediaExtractor?.release()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    class PcmInfo constructor(
        var bufferBytes: ByteArray,
        var bufferSize: Int,
        var time: Long//当前时间
    )
}
