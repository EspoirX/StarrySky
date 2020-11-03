package com.lzx.record

/**
 * lame 库
 */
object LameManager {

    init {
        System.loadLibrary("mp3lame")
    }

    /**
     * 自定义的方法，没啥用
     */
    external fun version(): Int

    /**
     * 初始化 lame 编码器
     * inSampleRate    输入采样率
     * outChannel      声道数
     * outSampleRate   输出采样率
     * outBitrate      比特率(kbps)
     * quality         0~9，0最好
     */
    external fun init(
        inSampleRate: Int,
        inChannel: Int,
        outSampleRate: Int,
        outBitrate: Int,
        quality: Int
    )

    /**
     *  编码，把 AudioRecord 录制的 PCM 数据转换成 mp3 格式
     *
     *  bufferLeft   左声道输入数据
     *  bufferRight  右声道输入数据
     *  samples      输入数据的size
     *  mp3buf       输出数据
     *  返回         输出到 mp3buf 的 byte 数量
     *
     * 如果单声道使用该方法
     * samples =bufferLeft.size
     */
    external fun encode(
        bufferLeft: ShortArray,
        bufferRight: ShortArray,
        samples: Int,
        mp3buf: ByteArray
    ): Int

    /**
     * 双声道使用该方法
     * samples = pcm.size/2
     */
    external fun encodeInterleaved(
        pcm: ShortArray,
        samples: Int,
        mp3buf: ByteArray
    ): Int


    external fun encodeByByte(
        bufferLeft: ByteArray,
        bufferRight: ByteArray,
        samples: Int,
        mp3buf: ByteArray
    ): Int

    /**
     *  刷写
     *
     *  mp3buf  mp3数据缓存区
     *  返回    返回刷写的数量
     */
    external fun flush(mp3buf: ByteArray): Int

    /**
     * 关闭 lame 编码器，释放资源
     */
    external fun close()
}