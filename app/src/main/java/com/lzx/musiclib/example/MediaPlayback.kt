package com.lzx.musiclib.example

import android.content.Context
import android.media.MediaPlayer
import com.lzx.musiclib.example.FocusAndLockManager.Companion.AUDIO_NO_FOCUS_CAN_DUCK
import com.lzx.musiclib.example.FocusAndLockManager.Companion.AUDIO_NO_FOCUS_NO_DUCK
import com.lzx.musiclib.example.FocusAndLockManager.Companion.VOLUME_DUCK
import com.lzx.musiclib.example.FocusAndLockManager.Companion.VOLUME_NORMAL
import com.lzx.starrysky.playback.offline.ICache
import com.lzx.starrysky.playback.player.Playback
import com.lzx.starrysky.provider.SongInfo
import com.lzx.starrysky.utils.StarrySkyUtils
import java.io.IOException

/**
 * MediaPlayer 简单demo
 */
class MediaPlayback(context: Context, private val playbackCache: ICache?) : Playback,
    MediaPlayer.OnPreparedListener,
    MediaPlayer.OnCompletionListener,
    MediaPlayer.OnErrorListener,
    MediaPlayer.OnBufferingUpdateListener,
    FocusAndLockManager.AudioFocusChangeListener {

    private var mediaPlayer: MediaPlayer? = null
    private var mCallback: Playback.Callback? = null
    private var mPlayState: Int = Playback.STATE_IDLE

    private var mMediaPlayerNullIsStopped = false
    private var mPlayOnFocusGain = false
    private var currBufferedPosition: Long = 0
    private var mCurrentMediaId: String = ""
    private var mCurrVolume: Float = 0F
    private var mErrorProgress: Long = 0
    private var currSongInfo: SongInfo? = null
    private var mFocusAndLockManager: FocusAndLockManager? = null

    init {
        mFocusAndLockManager = FocusAndLockManager(context, this)
    }

    override val playbackState: Int
        get() = if (mediaPlayer == null) Playback.STATE_IDLE else mPlayState

    override val isConnected: Boolean
        get() = true

    override val isPlaying: Boolean
        get() = mPlayOnFocusGain || (mediaPlayer != null && mPlayState == Playback.STATE_PLAYING)

    override val currentStreamPosition: Long
        get() = mediaPlayer?.currentPosition?.toLong() ?: 0L

    override val bufferedPosition: Long
        get() = currBufferedPosition

    override val duration: Long
        get() = mediaPlayer?.duration?.toLong() ?: 0

    override var currentMediaId: String
        get() = mCurrentMediaId
        set(value) {
            mCurrentMediaId = value
        }

    override var volume: Float
        get() = mCurrVolume
        set(value) {
            mCurrVolume = value
            mediaPlayer?.setVolume(value, value)
        }

    override val currPlayInfo: SongInfo?
        get() = currSongInfo

    override val audioSessionId: Int
        get() = mediaPlayer?.audioSessionId ?: 0

    private fun releaseResources(releasePlayer: Boolean) {
        // Stops and releases player (if requested and available).
        if (releasePlayer) {
            mediaPlayer?.stop()
            mediaPlayer?.reset()
            mediaPlayer?.release()
            mediaPlayer = null
            mMediaPlayerNullIsStopped = true
            mPlayOnFocusGain = false
        }
        mFocusAndLockManager?.releaseWifiLock()
    }

    override fun stop() {
        mFocusAndLockManager?.giveUpAudioFocus()
        releaseResources(true)
        mPlayState = Playback.STATE_STOPPED
        mCallback?.onPlaybackStatusChanged(currSongInfo, mPlayState)
    }

    override fun play(songInfo: SongInfo, isPlayWhenReady: Boolean) {
        mPlayOnFocusGain = true
        mFocusAndLockManager?.tryToGetAudioFocus()

        val mediaId = songInfo.songId
        if (mediaId.isEmpty()) {
            return
        }
        currSongInfo = songInfo
        val mediaHasChanged = mediaId != currentMediaId
        if (mediaHasChanged) {
            currentMediaId = mediaId
        }
        if (mediaHasChanged || mediaPlayer == null) {
            //url 处理
            var source = songInfo.songUrl
            if (source.isEmpty()) {
                mCallback?.onPlaybackError(currPlayInfo, "播放 url 为空")
                return
            }
            source = source.replace(" ".toRegex(), "%20") // Escape spaces for URL
            //代理url
            val proxyUrl = playbackCache?.getProxyUrl(source)
            source = if (proxyUrl.isNullOrEmpty()) source else proxyUrl
            playbackCache?.startCache(source)

            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer()
                //当装载流媒体完毕的时候回调
                mediaPlayer?.setOnPreparedListener(this)
                //当流媒体播放完毕的时候回调
                mediaPlayer?.setOnCompletionListener(this)
                //当播放中发生错误的时候回调
                mediaPlayer?.setOnErrorListener(this)
                mediaPlayer?.setOnBufferingUpdateListener(this)
//            changePlaybackParameters()
            }
            try {
                mediaPlayer?.reset()
                mediaPlayer?.setDataSource(source)
                mediaPlayer?.prepareAsync()
                mPlayState = Playback.STATE_BUFFERING
                mCallback?.onPlaybackStatusChanged(currSongInfo, mPlayState)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            mFocusAndLockManager?.acquireWifiLock()
        } else {
            configurePlayerState()
        }
    }

    override fun onPrepared(mp: MediaPlayer?) {
        configurePlayerState()
    }

    override fun onCompletion(mp: MediaPlayer?) {
        mPlayState = Playback.STATE_IDLE
        mCallback?.onPlaybackCompletion()
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        StarrySkyUtils.log("onError = $what")
        mCallback?.onPlaybackError(currSongInfo, "MediaPlayer error $what")
        mPlayState = Playback.STATE_ERROR
        mErrorProgress = currBufferedPosition
        mCurrentMediaId = ""
        return false
    }

    override fun onBufferingUpdate(mp: MediaPlayer?, percent: Int) {
        currBufferedPosition = percent * duration
    }

    override fun pause() {
        mediaPlayer?.pause()
        mPlayState = Playback.STATE_PAUSED
        mCallback?.onPlaybackStatusChanged(currSongInfo, mPlayState)
        releaseResources(false)
    }

    override fun seekTo(position: Long) {
        mediaPlayer?.seekTo(position.toInt())
    }

    override fun onFastForward() {
        //
    }

    override fun onRewind() {
        //
    }

    override fun onDerailleur(refer: Boolean, multiple: Float) {
        //
    }

    override fun setCallback(callback: Playback.Callback) {
        mCallback = callback
    }

    override fun onAudioFocusLossTransient() {
        mPlayOnFocusGain = mediaPlayer != null && mPlayState == Playback.STATE_PLAYING
    }

    override fun onAudioFocusChange() {
        mediaPlayer?.let {
            configurePlayerState()
        }
    }

    private fun configurePlayerState() {
        if (mFocusAndLockManager?.currentAudioFocusState == AUDIO_NO_FOCUS_NO_DUCK) {
            pause()
        } else {
            if (mFocusAndLockManager?.currentAudioFocusState == AUDIO_NO_FOCUS_CAN_DUCK) {
                mediaPlayer?.setVolume(VOLUME_DUCK, VOLUME_DUCK)
            } else {
                mediaPlayer?.setVolume(VOLUME_NORMAL, VOLUME_NORMAL)
            }
            if (mPlayOnFocusGain) {
                mediaPlayer?.start()
                mPlayState = Playback.STATE_PLAYING
                mPlayOnFocusGain = false
                mCallback?.onPlaybackStatusChanged(currSongInfo, mPlayState)
                if (mErrorProgress != 0L) {
                    seekTo(mErrorProgress)
                    mErrorProgress = 0
                }
            }
        }
    }
}