package com.lzx.starrysky.manager

import android.content.Context
import android.os.Build
import android.os.Handler
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.lzx.starrysky.SongInfo
import com.lzx.starrysky.utils.orDef

/**
 * 主要管理Android 5.0以后线控和蓝牙远程控制播放
 */
class MediaSessionManager(val context: Context,
                          private val playbackManager: PlaybackManager) {
    companion object {
        //指定可以接收的来自锁屏页面的按键信息
        private const val MEDIA_SESSION_ACTIONS = (
            PlaybackStateCompat.ACTION_PLAY
                or PlaybackStateCompat.ACTION_PAUSE
                or PlaybackStateCompat.ACTION_PLAY_PAUSE
                or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                or PlaybackStateCompat.ACTION_STOP
                or PlaybackStateCompat.ACTION_SEEK_TO)
    }

    private var mediaSession: MediaSessionCompat? = null
    private val handler: Handler? = null

    init {
        mediaSession = MediaSessionCompat(context, "MediaSessionManager")
        //指明支持的按键信息类型
        mediaSession?.setFlags(
            MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
        )
        mediaSession?.setCallback(MediaSessionCallback(playbackManager), handler)
        mediaSession?.isActive = true
    }


    /**
     * 更新播放状态
     */
    private fun updatePlaybackState() {
        val state = if (isPlaying()) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED
        mediaSession?.setPlaybackState(PlaybackStateCompat.Builder()
            .setActions(MEDIA_SESSION_ACTIONS)
            .setState(state, getCurrentPosition(), 1f)
            .build())
    }

    private fun getCurrentPosition(): Long = playbackManager.player()?.currentStreamPosition().orDef()

    /**
     * 更新正在播放的音乐信息
     */
    fun updateMetaData(songInfo: SongInfo?) {
        if (songInfo == null) {
            mediaSession?.setMetadata(null)
            return
        }
        val metaDta = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, songInfo.songName)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, songInfo.artist)
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, songInfo.songName)
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ARTIST, songInfo.artist)
//            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, songInfo.duration)  //加上这个通知栏会有进度条
        if (songInfo.coverBitmap != null) {
            metaDta.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, songInfo.coverBitmap)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val count = if (playbackManager.isSkipMediaQueue()) 1 else getMusicCount().toLong()
            metaDta.putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, count)
        }
        mediaSession?.setMetadata(metaDta.build())
        updatePlaybackState()
    }

    fun getMediaSession(): MediaSessionCompat.Token? = mediaSession?.sessionToken

    fun isPlaying(): Boolean = playbackManager.player()?.isPlaying().orDef()

    private fun getMusicCount(): Int = playbackManager.mediaQueue.getCurrSongList().size

    fun release() {
        mediaSession?.setCallback(null)
        mediaSession?.isActive = false
        mediaSession?.release()
    }

    private class MediaSessionCallback(val playbackManager: PlaybackManager) : MediaSessionCompat.Callback() {

        override fun onPlay() {
            super.onPlay()
            playbackManager.onRestoreMusic()
        }

        override fun onPause() {
            super.onPause()
            playbackManager.onPause()
        }

        override fun onSkipToNext() {
            super.onSkipToNext()
            playbackManager.skipToNext()
        }

        override fun onSkipToPrevious() {
            super.onSkipToPrevious()
            playbackManager.skipToPrevious()
        }

        override fun onStop() {
            super.onStop()
            playbackManager.onStop()
        }

        override fun onSeekTo(pos: Long) {
            super.onSeekTo(pos)
            playbackManager.onSeekTo(pos, true)
        }
    }
}