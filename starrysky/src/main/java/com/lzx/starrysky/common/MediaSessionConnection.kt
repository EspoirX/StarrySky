package com.lzx.starrysky.common

import android.arch.lifecycle.MutableLiveData
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.RemoteException
import android.os.ResultReceiver
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import com.lzx.starrysky.StarrySky
import com.lzx.starrysky.ext.id
import com.lzx.starrysky.control.OnPlayerEventListener
import com.lzx.starrysky.provider.SongInfo

class MediaSessionConnection constructor(context: Context, serviceComponent: ComponentName) :
    IMediaConnection {

    private val mContext: Context = context

    private val mediaBrowser: MediaBrowserCompat
    private var rootMediaId: String? = null
    private val isConnected = MutableLiveData<Boolean>()
    private val playbackState = MutableLiveData<PlaybackStage>()
    private val nowPlaying = MutableLiveData<MediaMetadataCompat>()
    private val playbackStateCompat = MutableLiveData<PlaybackStateCompat>()

    private var transportControls: MediaControllerCompat.TransportControls? = null
    private var mediaController: MediaControllerCompat? = null

    private val mediaBrowserConnectionCallback: MediaBrowserConnectionCallback by lazy {
        MediaBrowserConnectionCallback()
    }

    private val mMediaControllerCallback: MediaControllerCallback by lazy {
        MediaControllerCallback()
    }

    private var mConnectListener: IMediaConnection.OnConnectListener? = null

    init {
        mediaBrowser =
            MediaBrowserCompat(mContext, serviceComponent, mediaBrowserConnectionCallback,
                null)
        isConnected.value = false
        playbackState.value = PlaybackStage.buildNone()
        nowPlaying.value = NOTHING_PLAYING
        playbackStateCompat.value = EMPTY_PLAYBACK_STATE
    }

    override fun setOnConnectListener(listener: IMediaConnection.OnConnectListener?) {
        mConnectListener = listener
    }

    /**
     * 给服务发消息
     */
    override fun subscribe(parentId: String, callback: MediaBrowserCompat.SubscriptionCallback) {
        mediaBrowser.subscribe(parentId, callback)
    }

    override fun unsubscribe(
        parentId: String, callback: MediaBrowserCompat.SubscriptionCallback
    ) {
        mediaBrowser.unsubscribe(parentId, callback)
    }

    /**
     * 给服务发消息
     */
    override fun sendCommand(command: String, parameters: Bundle) {
        if (!mediaBrowser.isConnected) {
            return
        }
        mediaController?.sendCommand(command, parameters,
            object : ResultReceiver(Handler()) {
                //空实现
            })
    }

    /**
     * 是否已连接
     */
    fun isConnected(): MutableLiveData<Boolean> {
        return isConnected
    }

    /**
     * 获取rootMediaId
     */
    fun getRootMediaId(): String? {
        return rootMediaId
    }

    /**
     * 获取 MediaBrowserCompat
     */
    fun getMediaBrowser(): MediaBrowserCompat {
        return mediaBrowser
    }

    override fun getShuffleMode(): Int {
        return mediaController?.shuffleMode ?: PlaybackStateCompat.SHUFFLE_MODE_NONE
    }

    override fun getRepeatMode(): Int {
        return mediaController?.repeatMode ?: PlaybackStateCompat.REPEAT_MODE_NONE
    }

    /**
     * 获取当前播放的 MediaMetadataCompat
     */
    override fun getNowPlaying(): MediaMetadataCompat? {
        return nowPlaying.value
    }

    override fun getPlaybackStateCompat(): PlaybackStateCompat? {
        if (playbackStateCompat.value == null) {
            playbackStateCompat.value = EMPTY_PLAYBACK_STATE
        }
        return playbackStateCompat.value
    }

    override fun getPlaybackState(): MutableLiveData<PlaybackStage> {
        return playbackState
    }

    /**
     * 获取播放控制器
     */
    override fun getTransportControls(): MediaControllerCompat.TransportControls? {
        return transportControls
    }

    override fun getMediaController(): MediaControllerCompat? {
        return mediaController
    }

    override fun connect() {
        if (isConnected.value == null || !isConnected.value!!) {
            //进程被异常杀死时，App 被外部链接唤起时，connect 状态为 CONNECT_STATE_CONNECTING，
            //导致崩溃，所以要先执行 disconnect
            disconnectImpl()
            mediaBrowser.connect()
        }
    }

    /**
     * 断开连接
     */
    override fun disconnect() {
        if (isConnected.value != null && isConnected.value!!) {
            disconnectImpl()
            isConnected.value = false
        }
    }

    private fun disconnectImpl() {
        mediaController?.unregisterCallback(mMediaControllerCallback)
        mediaBrowser.disconnect()
    }

    /**
     * 连接回调
     */
    inner class MediaBrowserConnectionCallback : MediaBrowserCompat.ConnectionCallback() {
        /**
         * 已连接上
         */
        override fun onConnected() {
            super.onConnected()
            try {
                mediaController = MediaControllerCompat(mContext, mediaBrowser.sessionToken)
                mediaController?.registerCallback(mMediaControllerCallback)
                transportControls = mediaController?.transportControls
                rootMediaId = mediaBrowser.root
                isConnected.value = true
                mConnectListener?.onConnected()
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }

        override fun onConnectionSuspended() {
            super.onConnectionSuspended()
            isConnected.value = false
            disconnect()
        }

        override fun onConnectionFailed() {
            super.onConnectionFailed()
            isConnected.value = false
        }
    }

    inner class MediaControllerCallback : MediaControllerCompat.Callback() {

        private var playbackStage = PlaybackStage.buildNone()

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            super.onPlaybackStateChanged(state)
            if (state == null) {
                playbackState.value = playbackStage
                playbackStateCompat.value = EMPTY_PLAYBACK_STATE
                return
            }
            playbackStateCompat.value = state
            val songId = getNowPlaying()?.id
            if (songId.isNullOrEmpty()) {
                return
            }
            //状态监听
            val mPlayerEventListeners = StarrySky.with().getPlayerEventListeners()
            when (state.state) {
                PlaybackStateCompat.STATE_PLAYING -> {
                    for (listener in mPlayerEventListeners) {
                        listener.onPlayerStart()
                    }
                    playbackState.setValue(playbackStage.buildStart(songId))
                }
                PlaybackStateCompat.STATE_PAUSED -> {
                    for (listener in mPlayerEventListeners) {
                        listener.onPlayerPause()
                    }
                    playbackState.setValue(playbackStage.buildPause(songId))
                }
                PlaybackStateCompat.STATE_STOPPED -> {
                    for (listener in mPlayerEventListeners) {
                        listener.onPlayerStop()
                    }
                    playbackState.setValue(playbackStage.buildStop(songId))
                }
                PlaybackStateCompat.STATE_ERROR -> {
                    for (listener in mPlayerEventListeners) {
                        listener.onError(state.errorCode, state.errorMessage.toString())
                    }
                    playbackState.setValue(playbackStage.buildError(songId, state.errorCode,
                        state.errorMessage.toString()))
                }
                PlaybackStateCompat.STATE_NONE -> {
                    for (listener in mPlayerEventListeners) {
                        val songInfo = StarrySky.get().mediaQueueProvider.getSongInfo(songId)
                        listener.onPlayCompletion(songInfo!!)
                    }
                    playbackState.setValue(playbackStage.buildCompletion(songId))
                }
                PlaybackStateCompat.STATE_BUFFERING -> {
                    for (listener in mPlayerEventListeners) {
                        listener.onBuffering()
                    }
                    playbackState.setValue(playbackStage.buildBuffering(songId))
                }
            }
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            super.onMetadataChanged(metadata)
            nowPlaying.postValue(metadata ?: NOTHING_PLAYING)
            if (metadata == null) {
                return
            }
            val songId = metadata.id ?: return
            val songInfo = StarrySky.get().mediaQueueProvider.getSongInfo(songId)
            playbackState.postValue(playbackStage.buildSwitch(songId))
            //状态监听
            val mPlayerEventListeners = StarrySky.with().getPlayerEventListeners()
            for (listener in mPlayerEventListeners) {
                listener.onMusicSwitch(songInfo!!)
            }
        }

        override fun onSessionDestroyed() {
            super.onSessionDestroyed()
            mediaBrowserConnectionCallback.onConnectionSuspended()
        }
    }

    companion object {
        private val EMPTY_PLAYBACK_STATE = PlaybackStateCompat.Builder()
            .setState(PlaybackStateCompat.STATE_NONE, 0, 0f)
            .build()

        private val NOTHING_PLAYING = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, "")
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, 0)
            .build()
    }
}