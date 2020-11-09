package com.lzx.record

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.AsyncTask
import com.lzx.basecode.AudioDecoder
import com.lzx.basecode.Playback
import com.lzx.basecode.getFileNameFromUrl
import com.lzx.basecode.readAsBytes
import com.lzx.basecode.toSdcardPath
import com.lzx.record.recorder.AudioMp3Recorder
import com.lzx.record.recorder.IRecorder
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.atomic.AtomicBoolean

object StarrySkyRecord {

    //具体录音实现
    var recorder: IRecorder? = null
    private var player: Playback? = null
    var config = RecordConfig()
    private var context: Context? = null
    private var audioManager: AudioManager? = null
    var isDowloading = AtomicBoolean(false)  //是否正在下载

    var headsetConnected = false //耳机是否连接上
    var currVolumeF: Float = 1f  //音量
    private var headsetReceiver: HeadsetReceiver? = null
    private var voiceReceiver: VolumeReceiver? = null
    private val audioDecoder = AudioDecoder()

    fun initStarrySkyRecord(context: Context) {
        this.context = context
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager?
        headsetConnected = audioManager?.isWiredHeadsetOn ?: false
        registerReceiver()
        recorder = AudioMp3Recorder()
    }

    fun with(): RecordConfig {
        return config
    }

    fun getAudioDecoder() = audioDecoder

    fun setPlayer(player: Playback?) {
        this.player = player
    }

    fun getPlayer(): Playback? = player

    /**
     * 解码音频
     *
     * needDownload 是否要先下载再解码（效果会好点）
     * headers 如果是网络音频，可以设置 headers
     * filePath 下载时存储的文件夹
     * fileName 下载时存储的文件名
     */
    fun decodeMusic(
        musicUrl: String?,
        needDownload: Boolean = false,
        headers: HashMap<String, String>? = null,
        filePath: String? = "StarrySky/download/".toSdcardPath(),
        fileName: String? = musicUrl?.getFileNameFromUrl() ?: System.currentTimeMillis().toString(),
        callback: AudioDecoder.OnDecodeCallback) {
        if (musicUrl.isNullOrEmpty()) return
        if (isDowloading.get()) return
        AsyncTask.THREAD_POOL_EXECUTOR.execute {
            try {
                if (needDownload) {
                    val f = File(filePath + fileName)
                    if (f.isFile && f.exists()) {
                        decodeMusicImpl(f.absolutePath, headers, callback)
                        return@execute
                    }
                    isDowloading.compareAndSet(true, true)
                    val url = URL(musicUrl)
                    (url.openConnection() as? HttpURLConnection)?.let { http ->
                        http.connectTimeout = 20 * 1000
                        http.requestMethod = "GET"
                        http.connect()
                        http.inputStream.use { inputStream ->
                            inputStream.readAsBytes()?.let { bytes ->
                                val fileDir = File(filePath).apply {
                                    this.takeIf { !it.exists() }?.mkdirs()
                                }
                                val file = File(fileDir.absolutePath + "/" + fileName).apply {
                                    this.takeIf { !it.exists() }?.createNewFile()
                                }
                                FileOutputStream(file).use {
                                    it.write(bytes).also {
                                        decodeMusicImpl(file.absolutePath, headers, callback)
                                        http.disconnect()
                                    }
                                }
                            }
                        }
                    }
                } else {
                    decodeMusicImpl(musicUrl, headers, callback)
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                isDowloading.compareAndSet(false, true)
            }
        }
    }

    private fun decodeMusicImpl(url: String, headers: HashMap<String, String>?, callback: AudioDecoder.OnDecodeCallback) {
        isDowloading.compareAndSet(false, true)
        audioDecoder.initMediaDecode(url, headers)
        audioDecoder.decodePcmInfo(600)
        callback.onDecodeStart(audioDecoder)
    }

    fun getContext() = context

    fun release() {
        unregisterReceiver()
        recorder = null
    }

    private fun registerReceiver() {
        if (headsetReceiver == null) {
            val intentFilter = IntentFilter(Intent.ACTION_HEADSET_PLUG)
            headsetReceiver = HeadsetReceiver()
            context?.registerReceiver(headsetReceiver, intentFilter)
        }
        if (voiceReceiver == null) {
            val intentFilter = IntentFilter("android.media.VOLUME_CHANGED_ACTION")
            voiceReceiver = VolumeReceiver()
            context?.registerReceiver(voiceReceiver, intentFilter)
        }
    }

    private fun unregisterReceiver() {
        headsetReceiver?.let { context?.unregisterReceiver(it) }
        headsetReceiver = null
        voiceReceiver?.let { context?.unregisterReceiver(it) }
        voiceReceiver = null
    }

    fun setAudioVoiceF(volume: Float) {
        val max = voiceReceiver?.maxVolume ?: 1
        setAudioVoice((volume * max).toInt())
    }

    fun setAudioVoice(volume: Int) {
        audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0)
    }

    /**
     * 监听 是否连接上了耳机
     */
    class HeadsetReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_HEADSET_PLUG) {
                if (intent.hasExtra("state")) {
                    if (intent.getIntExtra("state", 0) == 0) {
                        headsetConnected = false
                    } else if (intent.getIntExtra("state", 0) == 1) {
                        headsetConnected = true
                    }
                }
            }
        }
    }

    /**
     * 声音监听广播
     */
    class VolumeReceiver : BroadcastReceiver() {
        var maxVolume: Int = 1
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action.equals("android.media.VOLUME_CHANGED_ACTION")) {
                currVolumeF = getCurVolume() / getMaxVoice().toFloat()
            }
        }

        private fun getMaxVoice(): Int {
            maxVolume = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 1
            return maxVolume
        }

        private fun getCurVolume(): Int {
            return audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 0
        }
    }
}