package com.lzx.basecode

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.os.AsyncTask
import java.nio.ByteBuffer
import java.util.ArrayList

class AudioDecoder {

    companion object {
        const val BUFFER_SIZE = 2048
        private val lockPCM = Any()
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

    private val chunkPCMDataContainer = ArrayList<PcmInfo>() //PCM数据块容器
    var isPCMExtractorEOS = true

    //记得加锁
    //每次取出index 0 的数据
    //取出后将此数据remove掉 既能保证PCM数据块的取出顺序 又能及时释放内存
    val pcmData: PcmInfo?
        get() = synchronized(lockPCM) {
            if (chunkPCMDataContainer.isEmpty()) {
                return null
            }
            return chunkPCMDataContainer.getOrNull(0)?.apply {
                chunkPCMDataContainer.removeAt(0)
            }
        }

    val bufferSize: Int
        get() = synchronized(lockPCM) {
            if (chunkPCMDataContainer.isEmpty()) {
                return BUFFER_SIZE
            }
            return chunkPCMDataContainer.getOrNull(0)?.bufferSize.orDef()
        }

    var bitRate: Int = 0
    var sampleRate: Int = 0
    var channelCount: Int = 0
    var duration: Long = 0

    fun initMediaDecode(url: String, headers: HashMap<String, String>?) {
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
            var mime: String? = null
            // 选中音轨，只有一条
            val numTracks = mediaExtractor?.trackCount.orDef()
            for (i in 0..numTracks) {
                mediaFormat = mediaExtractor?.getTrackFormat(i) // 获取音频格式信息
                mime = mediaFormat?.getString(MediaFormat.KEY_MIME).orEmpty()  // 获取音频类型
                bitRate = mediaFormat?.getInteger(MediaFormat.KEY_BIT_RATE).orDef()  // 获取比特率
                sampleRate = mediaFormat?.getInteger(MediaFormat.KEY_SAMPLE_RATE).orDef()  // 获取采样率
                channelCount = mediaFormat?.getInteger(MediaFormat.KEY_CHANNEL_COUNT).orDef()  // 获取频道数
                duration = mediaFormat?.getLong(MediaFormat.KEY_DURATION).orDef() //时长
                if (mime.startsWith("audio/")) {
                    mediaExtractor?.selectTrack(i) // 选中音轨
                    break
                }
            }
            if (mime.isNullOrEmpty()) return
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
    fun decodePcmInfo(upperLimit: Int = 0) {
        AsyncTask.THREAD_POOL_EXECUTOR.execute {
            isPCMExtractorEOS = false
            var sawInputEOS = false
            try {
                while (bufferInfo != null && !isPCMExtractorEOS) {
                    //加入限制，防止垃圾手机卡顿，- - 防止歌曲太大内存不够用了
                    if (upperLimit != 0) {
                        if (chunkPCMDataContainer.size > upperLimit) {
                            continue
                        }
                    }
                    if (!sawInputEOS) {
                        // 获取输入缓存器,-1代表一直等待，0表示不等待 建议-1,避免丢帧
                        val inputIndex = mediaCodec?.dequeueInputBuffer(-1).orDef(-1)
                        if (inputIndex >= 0) {
                            val inputBuffer = inputBuffers?.getOrNull(inputIndex) //拿到inputBuffer
                            inputBuffer?.let {
                                it.clear()   //清空之前传入inputBuffer内的数据
                                val sampleSize = mediaExtractor?.readSampleData(it, 0).orDef()  // 读取数据到输入缓存器
                                if (sampleSize < 0) { //小于0 代表所有数据已读取完成
                                    sawInputEOS = true
                                    mediaCodec?.queueInputBuffer(inputIndex, 0, 0, 0L, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                                } else {
                                    val presentationTimeUs = mediaExtractor?.sampleTime.orDef()
                                    mediaCodec?.queueInputBuffer(inputIndex, 0, sampleSize, presentationTimeUs, 0) //通知MediaDecode解码刚刚传入的数据
                                    mediaExtractor?.advance() //MediaExtractor移动到下一取样处
                                }
                            }
                        }
                    }
                    //获取解码得到的byte[]数据 参数BufferInfo上面已介绍 10000同样为等待时间 同上-1代表一直等待，0代表不等待。此处单位为微秒
                    //此处建议不要填-1 有些时候并没有数据输出，那么他就会一直卡在这 等待
                    // 输出缓存器
                    var outputIndex = mediaCodec?.dequeueOutputBuffer(bufferInfo!!, 0).orDef()
                    if (outputIndex == -2) {
                        // 格式变了，重新获取一次
                        outputIndex = mediaCodec?.dequeueOutputBuffer(bufferInfo!!, 0).orDef()
                    }
                    if (outputIndex >= 0) {
                        // Simply ignore codec config buffers.
                        if (bufferInfo!!.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
                            mediaCodec?.releaseOutputBuffer(outputIndex, false)
                            continue
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
                                val pcmInfo = PcmInfo(data, bufferInfo!!.size, mediaExtractor?.sampleTime.orDef())
                                putPcmInfo(pcmInfo)
                            }
                        }
                        //此操作一定要做，不然MediaCodec用完所有的Buffer后 将不能向外输出数据
                        mediaCodec?.releaseOutputBuffer(outputIndex, false)
                        if (bufferInfo!!.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                            isPCMExtractorEOS = true
                            releaseDecode()
                        }
                    } else if (outputIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                        outputBuffers = mediaCodec?.outputBuffers
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                releaseDecode()
            }
        }
    }

    private fun putPcmInfo(pcmInfo: PcmInfo) {
        synchronized(lockPCM) {
            //记得加锁
            chunkPCMDataContainer.add(pcmInfo)
        }
    }

    fun release() {
        isPCMExtractorEOS = true
        releaseDecode()
        synchronized(lockPCM) {
            chunkPCMDataContainer.clear()
        }
    }

    private fun releaseDecode() {
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

    interface OnDecodeListener {
        fun getBufferSize(): Int
        fun getPcmBufferBytes(): ByteArray?
    }

    interface OnDecodeCallback {
        fun onDecodeStart(decoder: AudioDecoder)
        fun onDecodeFail()
    }
}
