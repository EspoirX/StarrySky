package com.lzx.record.player

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaFormat
import android.os.AsyncTask
import android.os.Build
import com.lzx.basecode.AudioDecoder
import com.lzx.basecode.MainLooper
import com.lzx.record.RecordConfig
import java.util.concurrent.atomic.AtomicBoolean

/**
 * AudioTrack 播放器+解码 pcm 数据
 */
class AudioTrackPlayer(private val config: RecordConfig) {

    private var audioTrack: AudioTrack? = null
    private var volume = 0.3f

    private var isPlayingMusic = false
    private var isPause = false
    private var need = AtomicBoolean(false)
    private var audioDecoder: AudioDecoder? = null

    init {
        if (!config.bgMusicUrl.isNullOrEmpty()) {
            audioDecoder = AudioDecoder(config.bgMusicUrl!!, config.headers)
            audioDecoder?.initMediaDecode()
        }
    }

    fun playMusic() {
        isPlayingMusic = true
        config.playerListener?.onStart()
        AsyncTask.THREAD_POOL_EXECUTOR.execute {
            try {
                if (audioTrack == null) {
                    initAudioTrack()
                    setVolume(volume)
                }
                //播放
                audioTrack?.play()
                //延迟合成
                delayFrameArrive()

                audioDecoder?.sawInputEOS = false

                while (isPlayingMusic) {
                    if (isPause) continue

                    if (need.compareAndSet(true, false)) {
                        delayFrameArrive()
                    }

                    val pcm = audioDecoder?.getPcmInfo()
                    if (pcm?.bufferBytes == null) {
                        continue
                    }
                    audioTrack?.write(pcm.bufferBytes, 0, pcm.bufferBytes.size)
                    //回调进度
                    val duration = audioDecoder?.mediaFormat?.getLong(MediaFormat.KEY_DURATION) ?: 0
                    onProgress((pcm.time / 1000).toInt(), (duration / 1000).toInt())

                    onFrameArrive(pcm.bufferBytes)
                }
                //播放完成
                onStop()
                audioTrack?.stop()
                audioTrack?.flush()
                audioTrack?.release()
                audioTrack = null
            } catch (ex: Exception) {
                ex.printStackTrace()
                onError(ex.message.toString())
            } finally {
                isPlayingMusic = false
                isPause = false
            }
        }
    }

    fun pauseMusic() {
        if (isPlayingMusic) {
            isPause = true
            config.playerListener?.onPause()
        }
    }

    fun stopMusic() {
        isPlayingMusic = false
        onStop()
    }

    fun resumeMusic() {
        if (isPlayingMusic) {
            isPause = false
            need.compareAndSet(false, true)
            config.playerListener?.onResume()
        }
    }

    fun release() {
        isPlayingMusic = false
        isPause = false
        audioDecoder?.release()
    }

    private fun onError(msg: String) {
        MainLooper.instance.runOnUiThread {
            config.playerListener?.onError(msg)
        }
    }

    private fun onStop() {
        MainLooper.instance.runOnUiThread {
            config.playerListener?.onStop()
        }
    }

    private fun onProgress(current: Int, duration: Int) {
        MainLooper.instance.runOnUiThread {
            config.playerListener?.onProgress(current, duration)
        }
    }

    fun setVolume(volume: Float) {
        this.volume = volume
        if (audioTrack != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                audioTrack?.setVolume(volume)
            } else {
                audioTrack?.setStereoVolume(volume, volume)
            }
        }
    }

    private fun delayFrameArrive() {
        if (config.channelConfig == AudioFormat.CHANNEL_OUT_MONO) {
            //音乐实际开始会慢一点
            repeat(10) {
                onFrameArrive(ByteArray(1))
            }
        } else {
            //30 的时候 外放 快于 合成
            repeat(8) {
                onFrameArrive(ByteArray(1))
            }
        }
    }

    private fun onFrameArrive(bytes: ByteArray) {

    }

    private fun initAudioTrack() {
        val bufferSize = AudioTrack.getMinBufferSize(config.sampleRate, config.channelConfig, config.audioFormat)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(config.audioFormat)
                        .setSampleRate(config.sampleRate)
                        .setChannelMask(config.channelConfig)
                        .build()
                )
                .setTransferMode(AudioTrack.MODE_STREAM) //边读边播
                .setBufferSizeInBytes(bufferSize)
                .build()
        } else {
            audioTrack = AudioTrack(
                AudioManager.STREAM_MUSIC,
                config.sampleRate, config.channelConfig, config.audioFormat, bufferSize,
                AudioTrack.MODE_STREAM
            )
        }
    }


}