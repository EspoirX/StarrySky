package com.lzx.starrysky.playback

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.AsyncTask
import android.os.Build
import com.lzx.starrysky.utils.MainLooper
import com.lzx.starrysky.utils.md5
import com.lzx.starrysky.utils.orDef
import com.lzx.starrysky.utils.readAsBytes
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL


/**
 * SoundPool 播放器，调用 prepareForXXX 方法加载音频，只会加载一次，如果要重新加载，请调用 release 方法
 */
class SoundPoolPlayback(private val context: Context?) {

    private var soundPool: SoundPool? = null
    private var isLoaded: Boolean = false
    private var songIdList = mutableListOf<Int>()
    private var maxSongSize = 12
    private var hasLoaded = false
    private val volumeMap = hashMapOf<Int, SoundPoolVolume>()
    private val am = context?.getSystemService(Context.AUDIO_SERVICE) as AudioManager?
    private val maxVolume = am?.getStreamMaxVolume(AudioManager.STREAM_MUSIC)?.toFloat() ?: 0F
    private val currentVolume = am?.getStreamVolume(AudioManager.STREAM_MUSIC)?.toFloat() ?: 0F

    /**
     * 从 assets 加载，list 里面传文件名即可，不需要传整个路径
     */
    fun prepareForAssets(list: MutableList<String>, completionBlock: (player: SoundPoolPlayback) -> Unit) {
        if (list.isNullOrEmpty()) {
            run(completionBlock)
            return
        }
        setupSoundPool(list.toMutableList()) {
            if (hasLoaded) {
                run(completionBlock)
            }
        }
        if (!hasLoaded) {
            loadForAssets(list)
        }
    }

    /**
     * 从 raw 加载，list 里面传 R.raw.xxx
     */
    fun prepareForRaw(list: MutableList<Int>, completionBlock: (player: SoundPoolPlayback) -> Unit) {
        if (list.isNullOrEmpty()) {
            run(completionBlock)
            return
        }
        setupSoundPool(list.toMutableList()) {
            if (hasLoaded) {
                run(completionBlock)
            }
        }
        if (!hasLoaded) {
            loadForRaw(list)
        }
    }

    /**
     * 从 File 加载
     */
    fun prepareForFile(list: MutableList<File>, completionBlock: (player: SoundPoolPlayback) -> Unit) {
        if (list.isNullOrEmpty()) {
            run(completionBlock)
            return
        }
        setupSoundPool(list.toMutableList()) {
            if (hasLoaded) {
                run(completionBlock)
            }
        }
        if (!hasLoaded) {
            loadForFile(list)
        }
    }

    /**
     * 从 本地sd卡路径 加载,list 里面传完整路径
     */
    fun prepareForPath(list: MutableList<String>, completionBlock: (player: SoundPoolPlayback) -> Unit) {
        if (list.isNullOrEmpty()) {
            run(completionBlock)
            return
        }
        setupSoundPool(list.toMutableList()) {
            if (hasLoaded) {
                run(completionBlock)
            }
        }
        if (!hasLoaded) {
            loadForPath(list)
        }
    }

    /**
     * 从网络加载，list 里面传 url地址
     */
    fun prepareForHttp(list: MutableList<String>, completionBlock: (player: SoundPoolPlayback) -> Unit) {
        if (list.isNullOrEmpty()) {
            run(completionBlock)
            return
        }
        setupSoundPool(list.toMutableList()) {
            if (hasLoaded) {
                run(completionBlock)
            }
        }
        if (!hasLoaded) {
            loadForHttp(list)
        }
    }

    /**
     * 创建并加载SoundPool
     */
    private fun setupSoundPool(list: MutableList<Any>, completionBlock: (player: SoundPoolPlayback) -> Unit) {
        if (soundPool == null) {
            var soundLoaded = 0
            isLoaded = false
            songIdList.clear()
            soundPool = generateSoundPool(list)
            soundPool?.setOnLoadCompleteListener { _, sampleId, _ ->
                isLoaded = true
                soundLoaded++
                songIdList.add(sampleId)
                if (soundLoaded >= list.count()) {
                    completionBlock(this)
                }
            }
        } else {
            if (isLoaded) {
                completionBlock(this)
            }
        }
    }

    /**
     * 创建SoundPool
     */
    private fun generateSoundPool(entity: MutableList<Any>) = if (Build.VERSION.SDK_INT >= 21) {
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .build()
        SoundPool.Builder().setAudioAttributes(attributes)
            .setMaxStreams(maxSongSize.coerceAtMost(entity.count()))
            .build()
    } else {
        SoundPool(maxSongSize.coerceAtMost(entity.count()), AudioManager.STREAM_MUSIC, 0)
    }

    private fun loadForAssets(list: MutableList<String>) {
        val temp = mutableListOf<Int>()
        list.forEach { it ->
            context?.assets?.openFd(it)?.let {
                val id = soundPool?.load(it.fileDescriptor, it.startOffset, it.length, 1) ?: -1
                if (id > 0) {
                    temp.add(id)
                }
            }
        }
        if (temp.count() == list.count()) {
            hasLoaded = true
        }
    }

    private fun loadForRaw(list: MutableList<Int>) {
        val temp = mutableListOf<Int>()
        list.forEach { it ->
            context?.resources?.openRawResourceFd(it)?.let {
                val id = soundPool?.load(it.fileDescriptor, it.startOffset, it.length, 1) ?: -1
                if (id > 0) {
                    temp.add(id)
                }
            }
        }
        if (temp.count() == list.count()) {
            hasLoaded = true
        }
    }

    private fun loadForFile(list: MutableList<File>) {
        val temp = mutableListOf<Int>()
        list.filter { it.exists() && it.isFile }.forEach { file ->
            FileInputStream(file).use {
                val id = soundPool?.load(it.fd, 0, file.length(), 1) ?: -1
                if (id > 0) {
                    temp.add(id)
                }
            }
        }
        if (temp.count() == list.count()) {
            hasLoaded = true
        }
    }

    private fun loadForPath(list: MutableList<String>) {
        val temp = mutableListOf<Int>()
        list.filter { it.isNotEmpty() }.forEach {
            val id = soundPool?.load(it, 1) ?: -1
            if (id > 0) {
                temp.add(id)
            }
        }
        if (temp.count() == list.count()) {
            hasLoaded = true
        }
    }

    private fun loadForHttp(list: MutableList<String>) {
        AsyncTask.THREAD_POOL_EXECUTOR.execute {
            try {
                val temp = mutableListOf<Int>()
                list.forEach {
                    loadForHttpImpl(it, list, temp)
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    private fun loadForHttpImpl(voiceUrl: String, list: MutableList<String>, temp: MutableList<Int>) {
        val cachePath = context?.cacheDir?.absolutePath + "/StarrySky/soundPool/"
        val url = URL(voiceUrl)
        (url.openConnection() as? HttpURLConnection)?.let { it ->
            it.connectTimeout = 20 * 1000
            it.requestMethod = "GET"
            it.connect()
            it.inputStream.use { inputStream ->
                inputStream.readAsBytes()?.let { bytes ->
                    val fileDir = File(cachePath).apply {
                        this.takeIf { !it.exists() }?.mkdirs()
                    }
                    val file = File(fileDir.absolutePath + voiceUrl.md5() + ".mp3").apply {
                        this.takeIf { !it.exists() }?.createNewFile()
                    }
                    FileOutputStream(file).use {
                        it.write(bytes).also {
                            MainLooper.instance.runOnUiThread(Runnable {
                                val id = soundPool?.load(file.absolutePath, 1) ?: -1
                                if (id > 0) {
                                    temp.add(id)
                                }
                                if (temp.count() == list.count()) {
                                    hasLoaded = true
                                }
                            })
                        }
                    }
                }
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
                  loop: Int = 0,
                  rate: Float = 1f): Int {

        if (!isLoaded) return 0

        var volumeRatio = 0f
        if (leftVolume == -1f || rightVolume == -1f) {
            volumeRatio = currentVolume / maxVolume
        }
        val left = if (leftVolume == -1f) volumeRatio else leftVolume
        val right = if (rightVolume == -1f) volumeRatio else rightVolume
        return songIdList.getOrNull(index)?.let {
            volumeMap[it] = SoundPoolVolume(left, right)
            soundPool?.play(it, left, right, priority, loop, rate).orDef()
        }.orDef()
    }

    /**
     * 暂停指定播放流的音效，streamID 通过 playSound 返回
     */
    fun pause(streamID: Int) = apply { soundPool?.pause(streamID) }

    /**
     * 继续播放指定播放流的音效，streamID 通过 playSound 返回
     */
    fun resume(streamID: Int) = apply { soundPool?.pause(streamID) }

    /**
     * 终止指定播放流的音效，streamID 通过 playSound 返回
     */
    fun stop(streamID: Int) = apply { soundPool?.pause(streamID) }

    /**
     * 设置指定播放流的循环.
     */
    fun setLoop(streamID: Int, loop: Int) = apply { soundPool?.setLoop(streamID, loop) }

    /**
     * 设置指定播放流的音量.
     */
    fun setVolume(streamID: Int, leftVolume: Float, rightVolume: Float) = apply {
        if (leftVolume >= 0 && rightVolume >= 0) {
            soundPool?.setVolume(streamID, leftVolume, rightVolume)
            volumeMap[streamID] = SoundPoolVolume(leftVolume, rightVolume)
        }
    }

    /**
     * 获取当前音量
     */
    fun getVolume(streamID: Int): SoundPoolVolume? = volumeMap[streamID]

    /**
     * 设置指定播放流的优先级，playSound 中已说明 priority 的作用.
     */
    fun setPriority(streamID: Int, priority: Int) = apply { soundPool?.setPriority(streamID, priority) }

    /**
     * 卸载一个指定的音频资源.注意参数是 soundID
     */
    fun unload(soundID: Int) = apply { soundPool?.unload(soundID) }

    /**
     * 释放SoundPool中的所有音频资源.
     */
    fun release() = apply {
        hasLoaded = false
        soundPool?.release()
        soundPool = null
    }
}

data class SoundPoolVolume(var leftVolume: Float, var rightVolume: Float)