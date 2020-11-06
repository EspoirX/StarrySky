package com.lzx.record.player

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import android.util.Log
import com.lzx.basecode.AudioDecoder
import com.lzx.basecode.MainLooper
import com.lzx.record.RecordConfig
import com.lzx.record.StarrySkyRecord
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.atomic.AtomicBoolean

/**
 * AudioTrack 播放器+解码 pcm 数据
 */
class AudioTrackPlayer(private val config: RecordConfig) {

    private var audioTrack: AudioTrack? = null
    private var volume = 0.3f

    private var isPlayingMusic = false
    var isRecording = false
    private var isPause = false
    private var need = AtomicBoolean(false)

    private var audioDecoder = StarrySkyRecord.getAudioDecoder()
    private val pcmBufferBytes = LinkedBlockingDeque<ByteArray>()

    fun getBufferSize(): Int = audioDecoder.bufferSize

    fun getPcmBufferBytes(): ByteArray? {
        if (pcmBufferBytes.isEmpty()) {
            return null
        }
        return pcmBufferBytes.poll()
    }

    fun playMusic() {
        if (isPlaying()) {
            pauseMusic()
            return
        }
        if (isPause()) {
            resumeMusic()
            return
        }
        isPlayingMusic = true
        config.playerListener?.onStart()

        StarrySkyRecord.decodeMusic(
            config.bgMusicUrl,
            config.needDownloadBgMusic,
            config.headers,
            config.bgMusicFilePath,
            config.bgMusicFileName, object : AudioDecoder.OnDecodeCallback {
            override fun onDecodeStart(decoder: AudioDecoder) {
                playMusicImpl(decoder)
            }

            override fun onDecodeFail() {
               onError("音频解析失败")
            }
        })
    }

    private fun playMusicImpl(decoder: AudioDecoder) {
        try {
            if (audioTrack == null) {
                initAudioTrack()
                setVolume(volume)
            }
            //播放
            audioTrack?.play()
            //延迟合成
            delayFrameArrive()

            while (isPlayingMusic) {
                if (isPause) continue

                if (need.compareAndSet(true, false)) {
                    delayFrameArrive()
                }
                val pcmData = decoder.pcmData
                if (pcmData?.bufferBytes == null) {
                    continue
                }
                audioTrack?.write(pcmData.bufferBytes, 0, pcmData.bufferBytes.size)
                //回调进度
                onProgress((pcmData.time / 1000).toInt(), (decoder.duration / 1000).toInt())

                onFrameArrive(pcmData.bufferBytes)
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

    fun pauseMusic() {
        if (isPlayingMusic) {
            isPause = true
            config.playerListener?.onPause()
        }
    }

    fun resumeMusic() {
        if (isPlayingMusic) {
            isPause = false
            need.compareAndSet(false, true)
            config.playerListener?.onResume()
        }
    }

    fun stopMusic() {
        isPlayingMusic = false
        onStop()
    }

    fun release() {
        isPlayingMusic = false
        isPause = false
        audioDecoder.release()
        pcmBufferBytes.clear()
    }

    fun isPlaying() = isPlayingMusic && !isPause

    fun isPause() = isPlayingMusic && isPause

    private fun onError(msg: String) {
        MainLooper.instance.runOnUiThread {
            isPlayingMusic = false
            isPause = false
            config.playerListener?.onError(msg)
        }
    }

    private fun onStop() {
        MainLooper.instance.runOnUiThread {
            isPlayingMusic = false
            isPause = false
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

    fun getVolume(): Float = volume

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
        if (isPlayingMusic && isRecording) {
            pcmBufferBytes.add(bytes)
        }
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