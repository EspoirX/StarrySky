package com.lzx.record.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Point
import android.media.AudioFormat
import android.media.MediaExtractor
import android.media.MediaFormat
import android.os.Build
import android.view.WindowManager
import androidx.annotation.RequiresApi
import com.lzx.basecode.orDef
import com.lzx.record.RecordConst
import com.lzx.record.RecordInfo
import com.lzx.record.StarrySkyRecord
import java.io.File
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.floor



@Throws(OutOfMemoryError::class, IllegalStateException::class, Exception::class)
fun File.readRecordInfo(): RecordInfo? {
    var isInTrash = false
    return try {
        if (!this.exists()) {
            return null
        }
        val name = this.name.toLowerCase(Locale.getDefault())
        val components = name.split("\\.").toTypedArray()
        if (components.size < 2) {
            return null
        }
        val ext = components[components.lastIndex] //获取后缀
        isInTrash = "del".equals(ext, ignoreCase = true)   //判断后缀是否是 del，del是以删除的意思
        if (!isInTrash && !ext.isSupportedExtension()) {   //如果不是支持的格式，则返回
            return null
        }
        //获取录音信息
        var format: MediaFormat? = null
        val extractor = MediaExtractor()
        extractor.setDataSource(this.path)
        val numTracks = extractor.trackCount
        var i = 0
        while (i < numTracks) {
            format = extractor.getTrackFormat(i)
            if (format.getString(MediaFormat.KEY_MIME).startsWith("audio/")) {
                extractor.selectTrack(i)
                break
            }
            i++
        }
        if (i == numTracks || format == null) {
            return null
        }
        val channelCount = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
        val sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
        val bitrate = format.getInteger(MediaFormat.KEY_BIT_RATE)
        val duration = format.getLong(MediaFormat.KEY_DURATION)
        val mimeType = format.getString(MediaFormat.KEY_MIME)

        RecordInfo(
            this.name.removeFileExtension(),
            this.readFileFormat(mimeType),
            duration,
            this.length(),
            this.absolutePath,
            this.lastModified(),
            sampleRate,
            channelCount,
            bitrate,
            isInTrash
        )
    } catch (e: Exception) {
        e.printStackTrace()
        RecordInfo(
            this.name.removeFileExtension(), "", 0, this.length(),
            this.absolutePath, this.lastModified(), 0, 0, 0, isInTrash
        )
    }
}

val SUPPORTED_EXT = arrayOf("mp3", "wav", "3gpp", "3gp", "amr", "aac", "m4a", "mp4", "ogg", "flac")

fun String?.isSupportedExtension(): Boolean {
    for (i in SUPPORTED_EXT.indices) {
        if (SUPPORTED_EXT[i].equals(this, ignoreCase = true)) {
            return true
        }
    }
    return false
}


/**
 * 删除文件名字后缀
 */
fun String.removeFileExtension(): String {
    if (this.contains(".")) {
        val extIndex: Int = this.lastIndexOf(".")
        val ext = this.substring(extIndex + 1)
        if (extIndex >= 0 && extIndex + 1 < this.length && (ext.isSupportedExtension() || ext.isDelExtension())) {
            return this.substring(0, this.lastIndexOf("."))
        }
    }
    return this
}

fun String?.isDelExtension(): Boolean = "del".equals(this, ignoreCase = true)

fun File.readFileFormat(mime: String?): String {
    val name = this.name.toLowerCase(Locale.getDefault())
    if (name.contains(RecordConst.FORMAT_M4A) || mime != null && mime.contains("audio") && mime.contains("mp4a")) {
        return RecordConst.FORMAT_M4A
    } else if (name.contains(RecordConst.FORMAT_WAV) || mime != null && mime.contains("audio") && mime.contains("raw")) {
        return RecordConst.FORMAT_WAV
    } else if (name.contains(RecordConst.FORMAT_3GP) || mime != null && mime.contains("audio") && mime.contains("3gpp")) {
        return RecordConst.FORMAT_3GP
    } else if (name.contains(RecordConst.FORMAT_3GPP)) {
        return RecordConst.FORMAT_3GPP
    } else if (name.contains(RecordConst.FORMAT_MP3) || mime != null && mime.contains("audio") && mime.contains("mpeg")) {
        return RecordConst.FORMAT_MP3
    } else if (name.contains(RecordConst.FORMAT_AMR)) {
        return RecordConst.FORMAT_AMR
    } else if (name.contains(RecordConst.FORMAT_AAC)) {
        return RecordConst.FORMAT_AAC
    } else if (name.contains(RecordConst.FORMAT_MP4)) {
        return RecordConst.FORMAT_MP4
    } else if (name.contains(RecordConst.FORMAT_OGG)) {
        return RecordConst.FORMAT_OGG
    } else if (name.contains(RecordConst.FORMAT_FLAC) || mime != null && mime.contains("audio") && mime.contains(RecordConst.FORMAT_FLAC)) {
        return RecordConst.FORMAT_FLAC
    }
    return ""
}

fun convertRecordingData(list: IntArrayList, durationSec: Int): IntArray? {
    return if (durationSec > 20) {
        val sampleCount: Int = (1.5f * StarrySkyRecord.getContext()!!.getScreenWidth()).toInt()
        val waveForm = IntArray(sampleCount)
        if (list.size() < sampleCount * 2) {
            val scale = list.size().toFloat() / sampleCount.toFloat()
            for (i in 0 until sampleCount) {
                waveForm[i] = list[floor(i * scale.toDouble()).toInt()].toDouble().convertAmp()
            }
        } else {
            val scale = list.size().toFloat() / sampleCount.toFloat()
            for (i in 0 until sampleCount) {
                var value = 0
                val step = ceil(scale.toDouble()).toInt()
                for (j in 0 until step) {
                    value += list[(i * scale + j).toInt()]
                }
                value = (value.toFloat() / scale).toInt()
                waveForm[i] = value.toDouble().convertAmp()
            }
        }
        waveForm
    } else {
        val waveForm = IntArray(list.size())
        for (i in 0 until list.size()) {
            waveForm[i] = list[i].toDouble().convertAmp()
        }
        waveForm
    }
}

fun Double.convertAmp(): Int = (255 * (this / 32767f)).toInt()

fun Context.getScreenWidth(): Int {
    val wm = this.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val display = wm.defaultDisplay
    val size = Point()
    display.getSize(size)
    return size.x
}

fun IntArray.int2byte(): ByteArray? {
    val bytes = ByteArray(this.size)
    for (i in this.indices) {
        when {
            this[i] >= 255 -> {
                bytes[i] = 127
            }
            this[i] < 0 -> {
                bytes[i] = 0
            }
            else -> {
                bytes[i] = (this[i] - 128).toByte()
            }
        }
    }
    return bytes
}

fun ByteArray.byte2int(): IntArray? {
    val ints = IntArray(this.size)
    for (i in this.indices) {
        ints[i] = this[i] + 128
    }
    return ints
}

fun Activity.hasPermission(permission: String): Boolean {
    return !isMarshmallow() || isGranted(permission)
}

fun isMarshmallow(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M

@RequiresApi(Build.VERSION_CODES.M)
fun Activity.isGranted(permission: String): Boolean {
    return this.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
}

fun Int.format(): Int {
    return when (this) {
        AudioFormat.ENCODING_PCM_8BIT -> 8
        AudioFormat.ENCODING_PCM_16BIT -> 16
        else -> 0
    }
}

fun Int?.safeQuality(): Int = when {
    this.orDef() < 0 -> 0
    this.orDef() > 9 -> 9
    else -> this.orDef()
}