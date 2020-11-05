package com.lzx.record

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import com.lzx.record.recorder.AudioMp3Recorder
import com.lzx.record.recorder.IRecorder

object StarrySkyRecord {

    //具体录音实现
    var recorder: IRecorder? = null
    private var context: Context? = null
    private var audioManager: AudioManager? = null
    var headsetConnected = false //耳机是否连接上
    var currVolumeF: Float = 1f  //音量
    private var headsetReceiver: HeadsetReceiver? = null
    private var voiceReceiver: VolumeReceiver? = null

    fun initStarrySkyRecord(context: Context) {
        this.context = context
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager?
        headsetConnected = audioManager?.isWiredHeadsetOn ?: false
        registerReceiver()
        recorder = AudioMp3Recorder()
    }

    fun with(): RecordConfig.Builder {
        return RecordConfig().newBuilder()
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