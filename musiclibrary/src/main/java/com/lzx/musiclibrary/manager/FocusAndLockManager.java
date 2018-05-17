package com.lzx.musiclibrary.manager;

import android.content.Context;
import android.media.AudioManager;
import android.net.wifi.WifiManager;

/**
 * Created by xian on 2018/1/20.
 */

public class FocusAndLockManager {
    private AudioManager mAudioManager;
    private WifiManager.WifiLock mWifiLock;

    public static final float VOLUME_DUCK = 0.2f;
    public static final float VOLUME_NORMAL = 1.0f;

    public static final int AUDIO_NO_FOCUS_NO_DUCK = 0;
    public static final int AUDIO_NO_FOCUS_CAN_DUCK = 1;
    public static final int AUDIO_FOCUSED = 2;

    private int mCurrentAudioFocusState = AUDIO_NO_FOCUS_NO_DUCK;

    private AudioFocusChangeListener mListener;

    public FocusAndLockManager(Context context, AudioFocusChangeListener listener) {
        Context applicationContext = context.getApplicationContext();

        this.mAudioManager = (AudioManager) applicationContext.getSystemService(Context.AUDIO_SERVICE);
        this.mWifiLock = ((WifiManager) applicationContext.getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "uAmp_lock");
        this.mListener = listener;
    }

    public FocusAndLockManager(Context context) {
        new FocusAndLockManager(context, null);
    }

    public void giveUpAudioFocus() {
        if (mAudioManager.abandonAudioFocus(mOnAudioFocusChangeListener)
                == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            mCurrentAudioFocusState = AUDIO_NO_FOCUS_NO_DUCK;
        }
    }

    public void tryToGetAudioFocus() {
        int result =
                mAudioManager.requestAudioFocus(
                        mOnAudioFocusChangeListener,
                        AudioManager.STREAM_MUSIC,
                        AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            mCurrentAudioFocusState = AUDIO_FOCUSED;
        } else {
            mCurrentAudioFocusState = AUDIO_NO_FOCUS_NO_DUCK;
        }
    }

    public int getCurrentAudioFocusState() {
        return mCurrentAudioFocusState;
    }

    public void releaseWifiLock() {
        if (mWifiLock.isHeld()) {
            mWifiLock.release();
        }
    }

    public void acquireWifiLock() {
        mWifiLock.acquire();
    }

    private final AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    mCurrentAudioFocusState = AUDIO_FOCUSED;
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    mCurrentAudioFocusState = AUDIO_NO_FOCUS_CAN_DUCK;
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    mCurrentAudioFocusState = AUDIO_NO_FOCUS_NO_DUCK;

                    if (mListener != null) {
                        mListener.onAudioFocusLossTransient();
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    mCurrentAudioFocusState = AUDIO_NO_FOCUS_NO_DUCK;
                    break;
            }

            if (mListener != null) {
                mListener.onAudioFocusChange();
            }
        }
    };

    public interface AudioFocusChangeListener {
        void onAudioFocusLossTransient();

        void onAudioFocusChange();
    }
}
