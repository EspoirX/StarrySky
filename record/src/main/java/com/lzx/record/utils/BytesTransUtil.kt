package com.lzx.record.utils

import android.util.Log

import java.nio.ByteOrder
import kotlin.experimental.and
import kotlin.experimental.or

object BytesTransUtil {

    fun shorts2Bytes(s: ShortArray): ByteArray {
        val bLength: Byte = 2
        val buf = ByteArray(s.size * bLength)
        for (iLoop in s.indices) {
            val temp = getBytes(s[iLoop])
            System.arraycopy(temp, 0, buf, iLoop * bLength, bLength.toInt())
        }
        return buf
    }

    fun bytes2Shorts(buf: ByteArray): ShortArray {
        val bLength: Byte = 2
        val s = ShortArray(buf.size / bLength)
        for (iLoop in s.indices) {
            val temp = ByteArray(bLength.toInt())
            System.arraycopy(buf, iLoop * bLength, temp, 0, bLength.toInt())
            s[iLoop] = getShort(temp)
        }
        return s
    }

    /**
     * 将short转成byte[2]
     */
    fun short2Byte(a: Short): ByteArray {
        val b = ByteArray(2)

        b[0] = (a.toInt() shr 8).toByte()
        b[1] = a.toByte()

        return b
    }



    /**
     * 将byte[2]转换成short
     */
    fun byte2Short(high: Byte, low: Byte): Short {
        return (high.toInt() and 0xff shl 8 or (low.toInt() and 0xff)).toShort()
    }


    /**
     * 噪音消除算法
     */
    fun noiseClear(lin: ShortArray, off: Int, len: Int) {
        var i  = 0
        var j: Int
        while (i < len) {
            j = lin[i + off].toInt()
            lin[i + off] = (j shr 2).toShort()
            i++
        }
    }

    fun noiseClear(bytes: ByteArray, off: Int, len: Int): ByteArray {
        val data = bytes2Shorts(bytes)
        noiseClear(data, off, len)
        return shorts2Bytes(data)
    }

    /**
     * 改变音量
     */
    fun changeDataWithVolume(buffer: ByteArray, volumeValue: Float): ByteArray {
        if (volumeValue != 1f) {
            var i = 0
            while (i < buffer.size) {
                var value = byte2Short(buffer[i + 1], buffer[i])
                value = (volumeValue * value).toInt().toShort()
                value = if (value > 0x7fff) 0x7fff else value
                value = if (value < -0x8000) -0x8000 else value
                val newValue = value
                val array = short2Byte(newValue)
                buffer[i + 1] = array[0]
                buffer[i] = array[1]
                i += 2
            }
        }
        return buffer
    }

    /**
     * 调节 音量
     * @param level 音量
     */
    fun adjustVoice(buffer: ByteArray, level: Int) {
        for (i in buffer.indices) {
            buffer[i] = (buffer[i] * level).toByte()
        }
    }

    fun adjustVoice(buffer: ShortArray, level: Int): ShortArray {
        val temp = shorts2Bytes(buffer)
        adjustVoice(temp, level)
        return bytes2Shorts(temp)
    }

    fun averageMix(src1: ByteArray, src2: ByteArray): ByteArray? {
        return averageMixSelectShort(arrayOf(src1, src2))
    }

    /**
     * 采用简单的平均算法 average audio mixing algorithm
     * code from :    http://www.codexiu.cn/android/blog/3618/
     * 测试发现这种算法会降低 录制的音量
     * 需要两个音频帧的数据长度相同才可以混合
     * bMulRoadAudioes[0] 原始默认的音频
     */
    fun averageMix(bMulRoadAudioes: Array<ByteArray>?): ByteArray? {

        if (bMulRoadAudioes == null || bMulRoadAudioes.isEmpty())
            return null
        val realMixAudio = bMulRoadAudioes[0]

        if (bMulRoadAudioes.size == 1)
            return realMixAudio

        /**
         * 保证每一帧数据的长度都是一样的
         */
        for (rw in bMulRoadAudioes.indices) {
            if (bMulRoadAudioes[rw].size != realMixAudio.size) {
                Log.e("app", "column of the road of audio + $rw is diffrent.")
                return realMixAudio
            }
        }

        val row = bMulRoadAudioes.size
        val column = realMixAudio.size / 2
        val sMulRoadAudioes = Array(row) { ShortArray(column) }
        /** byte --> short
         * 对多维数组进行遍历，类似一个表格，row 行 column列
         * 对每一列 把相邻的两个数据，比如 （0，1） （2，3）（4，5）...  2 byte 合并为一个short（高位,地位）
         * byte只有8位,其范围是-128~127，第一位为符号位
         * 0xff二进制就是  (000...24个...0 )1111 1111。
         * & 运算是，如果对应的两个bit都是1，则那个bit结果为1，否则为0,比如 1010 & 1101 = 1000 （二进制）
         * java中的数值 为 int  所以 0xff 是int 型，
         * number & 0xff 意思是只取低八位，其他高位都是0
         * | 是逻辑“或”运算 如果对应的两个bit只要有一个是1，则那个bit结果为1，否则为0
         * << 8  左移8位  每次左移一位相当于乘2
         * short 2个字节 16位
         */
        for (r in 0 until row) {
            for (c in 0 until column) {
                sMulRoadAudioes[r][c] =
                    (bMulRoadAudioes[r][c * 2].toInt() and 0xff or (bMulRoadAudioes[r][c * 2 + 1].toInt() and 0xff shl 8)).toShort()
            }
        }
        val sMixAudio = ShortArray(column)
        var mixVal: Int
        var sr: Int
        /**
         * 对于 column列 上的每一项，合并 row 值  （累加取平均值）
         * column1 column2 column3 column4 ...
         * row1   .11.    .12.    .13.    .14.
         * row2   .21.    .22.    .23.    .24.
         * row3   .31.    .32.    .33.    .34.
         * .
         * .
         */
        for (sc in 0 until column) {
            mixVal = 0
            sr = 0
            while (sr < row) {
                mixVal += sMulRoadAudioes[sr][sc].toInt()
                ++sr
            }
            sMixAudio[sc] = (mixVal / row).toShort()
        }
        /**
         * short --> byte
         */
        sr = 0
        while (sr < column) {
            realMixAudio[sr * 2] = (sMixAudio[sr].toInt() and 0x00FF).toByte()
            realMixAudio[sr * 2 + 1] = (sMixAudio[sr].toInt() and 0xFF00 shr 8).toByte()
            ++sr
        }
        return realMixAudio
    }

    /**
     * 以最短的帧 为主 ，会丢失一部分数据
     * 哎 会出现杂音
     */
    fun averageMixSelectShort(bMulRoadAudioes: Array<ByteArray>?): ByteArray? {

        if (bMulRoadAudioes == null || bMulRoadAudioes.isEmpty())
            return null
        var realMixAudio = bMulRoadAudioes[0]

        if (bMulRoadAudioes.size == 1)
            return realMixAudio

        /**
         * 以最短的帧 为主
         */
        for (rw in bMulRoadAudioes.indices) {
            if (bMulRoadAudioes[rw].size < realMixAudio.size) {
                realMixAudio = bMulRoadAudioes[rw]
            }
        }

        val row = bMulRoadAudioes.size
        val column = realMixAudio.size / 2
        val sMulRoadAudioes = Array(row) { ShortArray(column) }

        for (r in 0 until row) {
            for (c in 0 until column) {
                sMulRoadAudioes[r][c] =
                    (bMulRoadAudioes[r][c * 2].toInt() and 0xff or (bMulRoadAudioes[r][c * 2 + 1].toInt() and 0xff shl 8)).toShort()
            }
        }
        val sMixAudio = ShortArray(column)
        var mixVal: Int
        var sr: Int

        for (sc in 0 until column) {
            mixVal = 0
            sr = 0
            while (sr < row) {
                mixVal += sMulRoadAudioes[sr][sc].toInt()
                ++sr
            }
            sMixAudio[sc] = (mixVal / row).toShort()
        }

        sr = 0
        while (sr < column) {
            realMixAudio[sr * 2] = (sMixAudio[sr] and 0x00FF).toByte()
            realMixAudio[sr * 2 + 1] = (sMixAudio[sr].toInt() and 0xFF00 shr 8).toByte()
            ++sr
        }
        return realMixAudio
    }

    fun getBytes(s: Short): ByteArray {
        return getBytes(s.toLong(), thisCPU())
    }

    fun getShort(buf: ByteArray?):Short{
        return getShort(buf, thisCPU())
    }

    ///////////////// private /////////////////////////////////////////////

    /**
     * 大端小端 问题
     */
    private fun thisCPU(): Boolean {
        return ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN
    }

    private fun getBytes(s: Long, bBigEnding: Boolean): ByteArray {
        var size = s
        val buf = ByteArray(8)
        if (bBigEnding)
            for (i in buf.indices.reversed()) {
                buf[i] = (size and 0x00000000000000ff).toByte()
                size = size shr 8
            }
        else
            for (i in buf.indices) {
                buf[i] = (size and 0x00000000000000ff).toByte()
                size = size shr 8
            }
        return buf
    }



    private fun getShort(buf: ByteArray?, bBigEnding: Boolean = thisCPU()): Short {
        requireNotNull(buf) { "byte array is null!" }
        require(buf.size <= 2) { "byte array size > 2 !" }
        var r: Short = 0
        if (bBigEnding) {
            for (aBuf in buf) {
                r = (r.toInt() shl 8).toShort()
                r = r or (aBuf.toInt() and 0x00ff).toShort()
            }
        } else {
            for (i in buf.indices.reversed()) {
                r = (r.toInt() shl 8).toShort()
                r = r or (buf[i].toInt() and 0x00ff).toShort()
            }
        }
        return r
    }
}
