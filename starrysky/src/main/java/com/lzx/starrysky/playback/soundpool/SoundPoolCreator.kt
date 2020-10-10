package com.lzx.starrysky.playback.soundpool

import android.content.res.AssetFileDescriptor
import android.media.AudioAttributes
import android.media.AudioManager
import java.io.FileDescriptor

class SoundPoolCreator {
    companion object {
        fun buildSoundPool(buildAction: SoundPoolCreator.() -> Unit) = SoundPoolCreator().apply(buildAction)
    }

    var maxStreams = 10
    var streamType = AudioManager.STREAM_MUSIC
    var audioAttributes: AudioAttributes? = null
}

data class AssetData(var songId: String? = null, var assetFile: AssetFileDescriptor)

data class AssetResIdData(var songId: String? = null, var resId: Int)

data class PathData(var songId: String? = null, var path: String)

data class FileData(var songId: String? = null, var fd: FileDescriptor, var offset: Long, var length: Long)
