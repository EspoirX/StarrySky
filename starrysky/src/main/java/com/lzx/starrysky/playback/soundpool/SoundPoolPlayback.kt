package com.lzx.starrysky.playback.soundpool

import android.content.Context
import android.media.AudioManager
import android.media.SoundPool
import com.lzx.starrysky.utils.StarrySkyUtils


class SoundPoolPlayback(private val context: Context?, private val creator: SoundPoolCreator) {

    private var soundPool: SoundPool? = null
    private var isLoaded: Boolean = false
    private var isAllLoadedSuccess: Boolean = false
    private var songIdList = mutableListOf<Int>()

    fun loadSound(afdList: MutableList<Any>) {
        if (soundPool == null) {
            createSoundPool()
        }
        if (afdList.size > creator.maxStreams) {
            return
        }
        val soundData = hashMapOf<String, Int>()
        afdList.forEachIndexed { index, data ->
            when (data) {
                is AssetData -> {
                    val key = if (data.songId.isNullOrEmpty()) index.toString() else data.songId!!
                    val songId = soundPool?.load(data.assetFile, 1) ?: -1
                    soundData[key] = songId
                }
                is AssetResIdData -> {
                    val key = if (data.songId.isNullOrEmpty()) index.toString() else data.songId!!
                    val songId = soundPool?.load(context, data.resId, 1) ?: -1
                    soundData[key] = songId
                }
                is PathData -> {
                    val key = if (data.songId.isNullOrEmpty()) index.toString() else data.songId!!
                    val songId = soundPool?.load(data.path, 1) ?: -1
                    soundData[key] = songId

                }
                is FileData -> {
                    val key = if (data.songId.isNullOrEmpty()) index.toString() else data.songId!!
                    val songId = soundPool?.load(data.fd, data.offset, data.length, 1) ?: -1
                    soundData[key] = songId
                }
            }
        }
        var index = 0
        soundPool?.setOnLoadCompleteListener { soundPool, sampleId, status ->
            isLoaded = true
            index++
            if (status != 0) {
                StarrySkyUtils.log("id $sampleId 加载失败")
            } else {
                songIdList.add(sampleId)
            }
            if (index == afdList.size) {
                StarrySkyUtils.log("全部加载完成")
                isAllLoadedSuccess = true
                index = 0
            }
        }
    }

    /**
     * 播放，播放前请先调用 loadSound 加载音频，
     * 方法返回 streamID ，0 是失败，否则成功，streamID用于其他方法的参数
     *
     * index 音频下标
     * leftVolume 左声道
     * rightVolume 右声道
     *
     * priority  优先级(只在同时播放的流的数量超过了预先设定的最大数量是起作用，
     * 管理器将自动终止优先级低的播放流。
     * 如果存在多个同样优先级的流，再进一步根据其创建事件来处理，新创建的流的年龄是最小的，将被终止；)
     *
     * loop  循环播放次数
     * rate  回放速度，该值在0.5-2.0之间 1为正常速度
     */
    fun playSound(index: Int,
                  leftVolume: Float = -1f,
                  rightVolume: Float = -1f,
                  priority: Int = 1,
                  loop: Int = 1,
                  rate: Float = 1f): Int {

        if (!isLoaded) return 0

        var volumeRatio = 0f
        if (leftVolume == -1f || rightVolume == -1f) {
            val am = context?.getSystemService(Context.AUDIO_SERVICE) as AudioManager?
            val maxVolume = am?.getStreamMaxVolume(AudioManager.STREAM_MUSIC)?.toFloat() ?: 0F
            val currentVolume = am?.getStreamVolume(AudioManager.STREAM_MUSIC)?.toFloat() ?: 0F
            volumeRatio = currentVolume / maxVolume
        }
        val left = if (leftVolume == -1f) volumeRatio else leftVolume
        val right = if (rightVolume == -1f) volumeRatio else rightVolume
        return soundPool?.play(songIdList[index], left, right, priority, loop, rate) ?: 0
    }

    /**
     * 暂停指定播放流的音效，streamID 通过 playSound 返回
     */
    fun pause(streamID: Int) {
        soundPool?.pause(streamID)
    }

    /**
     * 继续播放指定播放流的音效，streamID 通过 playSound 返回
     */
    fun resume(streamID: Int) {
        soundPool?.pause(streamID)
    }

    /**
     * 终止指定播放流的音效，streamID 通过 playSound 返回
     */
    fun stop(streamID: Int) {
        soundPool?.pause(streamID)
    }

    /**
     * 设置指定播放流的循环.
     */
    fun setLoop(streamID: Int, loop: Int) {
        soundPool?.setLoop(streamID, loop)
    }

    /**
     * 设置指定播放流的音量.
     */
    fun setVolume(streamID: Int, leftVolume: Float, rightVolume: Float) {
        if (leftVolume >= 0 && rightVolume >= 0) {
            soundPool?.setVolume(streamID, leftVolume, rightVolume)
        }
    }

    /**
     * 设置指定播放流的优先级，playSound 中已说明 priority 的作用.
     */
    fun setPriority(streamID: Int, priority: Int) {
        soundPool?.setPriority(streamID, priority)
    }

    /**
     * 卸载一个指定的音频资源.注意参数是 soundID
     */
    fun unload(soundID: Int) {
        soundPool?.unload(soundID)
    }

    /**
     * 释放SoundPool中的所有音频资源.
     */
    fun release() {
        soundPool?.release()
        soundPool = null
    }

    private fun createSoundPool() {
        soundPool = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP && creator.audioAttributes != null) {
            SoundPool.Builder()
                .setMaxStreams(creator.maxStreams)
                .setAudioAttributes(creator.audioAttributes)
                .build()
        } else {
            SoundPool(creator.maxStreams, creator.streamType, 0)
        }
    }


}