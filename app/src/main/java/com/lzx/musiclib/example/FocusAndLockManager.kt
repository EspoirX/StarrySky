package com.lzx.musiclib.example

import android.content.Context
import android.media.AudioManager
import android.media.AudioManager.OnAudioFocusChangeListener

import android.net.wifi.WifiManager
import android.net.wifi.WifiManager.WifiLock

/**
 * 焦点管理和WifiLock
 */
class FocusAndLockManager {
    private var mAudioManager: AudioManager? = null
    private var mWifiLock: WifiLock? = null
    var currentAudioFocusState = AUDIO_NO_FOCUS_NO_DUCK

    private var mListener: AudioFocusChangeListener? = null

    constructor(context: Context, listener: AudioFocusChangeListener?) {
        val applicationContext: Context = context.applicationContext
        mAudioManager =
            applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager?
        mWifiLock = (applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager)
            .createWifiLock(WifiManager.WIFI_MODE_FULL, "uAmp_lock")
        mListener = listener
    }

    constructor(context: Context) {
        FocusAndLockManager(context, null)
    }

    fun giveUpAudioFocus() {
        if (mAudioManager?.abandonAudioFocus(mOnAudioFocusChangeListener)
            == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            currentAudioFocusState = AUDIO_NO_FOCUS_NO_DUCK
        }
    }

    fun tryToGetAudioFocus() {
        val result = mAudioManager!!.requestAudioFocus(
            mOnAudioFocusChangeListener,
            AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN)
        currentAudioFocusState = if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            AUDIO_FOCUSED
        } else {
            AUDIO_NO_FOCUS_NO_DUCK
        }
    }

    fun releaseWifiLock() {
        if (mWifiLock?.isHeld == true) {
            mWifiLock?.release()
        }
    }

    fun acquireWifiLock() {
        mWifiLock?.acquire()
    }

    private val mOnAudioFocusChangeListener: OnAudioFocusChangeListener =
        OnAudioFocusChangeListener { focusChange ->
            when (focusChange) {
                AudioManager.AUDIOFOCUS_GAIN -> {
                    currentAudioFocusState = AUDIO_FOCUSED
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                    currentAudioFocusState = AUDIO_NO_FOCUS_CAN_DUCK
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                    currentAudioFocusState = AUDIO_NO_FOCUS_NO_DUCK
                    mListener?.onAudioFocusLossTransient()
                }
                AudioManager.AUDIOFOCUS_LOSS -> {
                    currentAudioFocusState = AUDIO_NO_FOCUS_NO_DUCK
                }
            }
            mListener?.onAudioFocusChange()
        }

    interface AudioFocusChangeListener {
        fun onAudioFocusLossTransient()
        fun onAudioFocusChange()
    }

    companion object {
        const val VOLUME_DUCK = 0.2f
        const val VOLUME_NORMAL = 1.0f
        const val AUDIO_NO_FOCUS_NO_DUCK = 0
        const val AUDIO_NO_FOCUS_CAN_DUCK = 1
        const val AUDIO_FOCUSED = 2
    }
}