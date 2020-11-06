package com.lzx.starrysky.playback

import android.content.Context
import android.os.Build
import android.os.Handler
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.lzx.basecode.SongInfo
import com.lzx.starrysky.control.PlayerControl

/**
 * 主要管理Android 5.0以后线控和蓝牙远程控制播放
 */
class MediaSessionManager(val context: Context, val playerControl: PlayerControl?) {
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
        mediaSession?.setCallback(MediaSessionCallback(playerControl), handler)
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

    private fun getCurrentPosition(): Long = playerControl?.getPlayingPosition() ?: 0

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
            metaDta.putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, getMusicCount().toLong())
        }
        mediaSession?.setMetadata(metaDta.build())
        updatePlaybackState()
    }

    fun getMediaSession(): MediaSessionCompat.Token? = mediaSession?.sessionToken

    fun isPlaying(): Boolean = playerControl?.isPlaying() ?: false

    fun getMusicCount(): Int = playerControl?.getPlayList()?.size ?: 0

    fun release() {
        mediaSession?.setCallback(null)
        mediaSession?.isActive = false
        mediaSession?.release()
    }

    private class MediaSessionCallback(val playerControl: PlayerControl?) : MediaSessionCompat.Callback() {

        override fun onPlay() {
            super.onPlay()
            playerControl?.restoreMusic()
        }

        override fun onPause() {
            super.onPause()
            playerControl?.pauseMusic()
        }

        override fun onSkipToNext() {
            super.onSkipToNext()
            playerControl?.skipToNext()
        }

        override fun onSkipToPrevious() {
            super.onSkipToPrevious()
            playerControl?.skipToPrevious()
        }

        override fun onStop() {
            super.onStop()
            playerControl?.stopMusic()
        }

        override fun onSeekTo(pos: Long) {
            super.onSeekTo(pos)
            playerControl?.seekTo(pos)
        }
    }
}