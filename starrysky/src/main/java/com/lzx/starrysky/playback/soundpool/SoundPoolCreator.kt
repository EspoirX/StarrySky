package com.lzx.starrysky.playback.soundpool

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.AudioAttributes
import android.media.AudioManager
import com.lzx.starrysky.StarrySky
import java.io.FileDescriptor

class SoundPoolCreator internal constructor(builder: Builder) {
    //设置可以同时播放的最大同时流数。
    @get:JvmName("maxStreams")
    val maxStreams = builder.maxStreams

    //音频流类型，如AudioManager中所述
    @get:JvmName("streamType")
    val streamType = builder.streamType

    //设置{@link AudioAttributes}。 例如，游戏应用程序将使用通过使用信息设置为{@link AudioAttributes＃USAGE_GAME}构建的属性。
    @get:JvmName("audioAttributes")
    val audioAttributes: AudioAttributes? = builder.audioAttributes

    //循环播放次数
    @get:JvmName("loop")
    val loop: Int = builder.loop

    //左声道
    @get:JvmName("leftVolume")
    val leftVolume: Float = builder.leftVolume

    //右声道
    @get:JvmName("rightVolume")
    val rightVolume: Float = builder.rightVolume

    //优先级(只在同时播放的流的数量超过了预先设定的最大数量是起作用，
    //* 管理器将自动终止优先级低的播放流。
    //* 如果存在多个同样优先级的流，再进一步根据其创建事件来处理，新创建的流的年龄是最小的，将被终止；)
    @get:JvmName("priority")
    val priority: Int = builder.priority

    fun soundPool(): SoundPoolPlayback {
        if (StarrySky.getBridge()?.soundPoolPlayback == null) {
            throw NullPointerException("bridge or soundPoolPlayback is Null")
        }
        StarrySky.getBridge()?.soundPoolPlayback?.setSoundPoolCreator(this)
        return StarrySky.getBridge()?.soundPoolPlayback!!
    }

    constructor() : this(Builder())

    open fun newBuilder(): Builder = Builder(this)

    class Builder constructor() {
        private lateinit var context: Context
        internal var maxStreams = 10
        internal var streamType = AudioManager.STREAM_MUSIC
        internal var audioAttributes: AudioAttributes? = null
        internal var loop: Int = 1
        internal var leftVolume: Float = -1f
        internal var rightVolume: Float = -1f
        internal var priority: Int = 1

        internal constructor(config: SoundPoolCreator) : this() {
            this.maxStreams = config.maxStreams
            this.streamType = config.streamType
            this.audioAttributes = config.audioAttributes
            this.loop = config.loop
            this.leftVolume = config.leftVolume
            this.rightVolume = config.rightVolume
            this.priority = config.priority
        }

        fun setContext(context: Context) = apply { this.context = context }
        fun setMaxStreams(maxStreams: Int) = apply { this.maxStreams = maxStreams }
        fun setStreamType(streamType: Int) = apply { this.streamType = streamType }
        fun setAudioAttributes(audioAttributes: AudioAttributes) = apply { this.audioAttributes = audioAttributes }
        fun setLoop(loop: Int) = apply { this.loop = loop }
        fun setLeftVolume(leftVolume: Float) = apply { this.leftVolume = leftVolume }
        fun setRightVolume(rightVolume: Float) = apply { this.rightVolume = rightVolume }
        fun setPriority(priority: Int) = apply { this.priority = priority }

        fun build(): SoundPoolCreator {
            return SoundPoolCreator(this)
        }
    }
}

data class AssetData(var songId: String? = null, var assetFile: AssetFileDescriptor)

data class AssetResIdData(var songId: String? = null, var resId: Int)

data class PathData(var songId: String? = null, var path: String)

data class FileData(var songId: String? = null, var fd: FileDescriptor, var offset: Long, var length: Long)
