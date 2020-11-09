package com.lzx.record.player

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import com.lzx.basecode.AudioDecoder
import com.lzx.basecode.MainLooper
import com.lzx.basecode.Playback
import com.lzx.basecode.SongInfo
import com.lzx.basecode.orDef
import com.lzx.record.RecordConfig
import com.lzx.record.StarrySkyRecord
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.atomic.AtomicBoolean

/**
 * AudioTrack 播放器+解码 pcm 数据
 *
 * 有些手机播着播着就报错，暂不知道原因
 */
class AudioTrackPlayer(private val config: RecordConfig) : Playback {

    private var audioTrack: AudioTrack? = null

    private var callback: Playback.Callback? = null
    private var isPlayingMusic = false
    private var isRecording = false
    private var volume: Float = 0.3f
    private var isPause = false
    private var need = AtomicBoolean(false)
    private var audioProgress = 0L
    private var audioDuration = 0L
    private var sessionId = 0
    private var currSongInfo: SongInfo? = null

    private var audioDecoder = StarrySkyRecord.getAudioDecoder()
    private val pcmBufferBytes = LinkedBlockingDeque<ByteArray>()

    override val playbackState: Int
        get() = 0

    override val isConnected: Boolean
        get() = true

    override val isPlaying: Boolean
        get() = isPlayingMusic && !isPause

    override val currentStreamPosition: Long
        get() = audioProgress

    override val bufferedPosition: Long
        get() = 0L

    override val duration: Long
        get() = audioDuration

    override var currentMediaId: String
        get() = ""
        set(value) {}

    override fun getVolume(): Float = volume

    override fun setVolume(volume: Float) {
        this.volume = volume
        if (audioTrack != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                audioTrack?.setVolume(volume)
            } else {
                audioTrack?.setStereoVolume(volume, volume)
            }
        }
    }

    override val currPlayInfo: SongInfo?
        get() = currSongInfo

    override val audioSessionId: Int
        get() = sessionId

    private fun isPause() = isPlayingMusic && isPause

    override fun getBufferSize(): Int = audioDecoder.bufferSize

    override fun getPcmBufferBytes(): ByteArray? {
        if (pcmBufferBytes.isEmpty()) {
            return null
        }
        return pcmBufferBytes.poll()
    }

    override fun getAudioDecoder(): AudioDecoder? = audioDecoder

    override fun setRecording(isRecording: Boolean) {
        this.isRecording = isRecording
    }

    override fun play(songInfo: SongInfo, isPlayWhenReady: Boolean) {
        if (songInfo.songUrl.isEmpty()) {
            callback?.onPlaybackError(songInfo, "audio url is Empty")
            return
        }
        currSongInfo = songInfo
        if (isPlaying) {
            pause()
            return
        }
        if (isPause()) {
            resumeMusic()
            return
        }
        isPlayingMusic = true
        callback?.onPlayerStateChanged(songInfo, true, Playback.STATE_PLAYING)
        StarrySkyRecord.decodeMusic(
            songInfo.songUrl,
            config.needDownloadBgMusic,
            config.headers,
            config.bgMusicFilePath,
            config.bgMusicFileName, object : AudioDecoder.OnDecodeCallback {
            override fun onDecodeStart(decoder: AudioDecoder) {
                config.setBitRate(decoder.bitRate)
                config.setSamplingRate(decoder.sampleRate)
                config.setChannelConfig(decoder.channelCount)
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

            sessionId = audioTrack?.audioSessionId.orDef()

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

    private fun resumeMusic() {
        if (isPlayingMusic) {
            isPause = false
            need.compareAndSet(false, true)
            callback?.onPlayerStateChanged(currSongInfo, true, Playback.STATE_PLAYING)
        }
    }

    override fun stop() {
        isPlayingMusic = false
        audioDecoder.release()
        pcmBufferBytes.clear()
        onStop()
    }

    override fun pause() {
        if (isPlayingMusic) {
            isPause = true
            callback?.onPlayerStateChanged(currSongInfo, true, Playback.STATE_PAUSED)
        }
    }

    private fun onStop() {
        MainLooper.instance.runOnUiThread {
            isPlayingMusic = false
            isPause = false
            callback?.onPlayerStateChanged(currSongInfo, true, Playback.STATE_STOPPED)
        }
    }

    private fun onProgress(current: Int, duration: Int) {
        MainLooper.instance.runOnUiThread {
            audioProgress = current.toLong()
            audioDuration = duration.toLong()
        }
    }

    private fun onError(msg: String) {
        MainLooper.instance.runOnUiThread {
            isPlayingMusic = false
            isPause = false
            callback?.onPlaybackError(currSongInfo, msg)
        }
    }

    override fun seekTo(position: Long) {
        //nothing
    }

    override fun onFastForward() {
        //nothing
    }

    override fun onRewind() {
        //nothing
    }

    override fun onDerailleur(refer: Boolean, multiple: Float) {
        //nothing
    }

    override fun getPlaybackSpeed(): Float = 0f

    override fun setCallback(callback: Playback.Callback) {
        this.callback = callback
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
