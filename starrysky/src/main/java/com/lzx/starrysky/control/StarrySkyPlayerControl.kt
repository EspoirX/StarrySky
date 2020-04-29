package com.lzx.starrysky.control

import android.annotation.SuppressLint
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.text.TextUtils
import com.lzx.starrysky.StarrySky
import com.lzx.starrysky.common.IMediaConnection
import com.lzx.starrysky.common.PlaybackStage
import com.lzx.starrysky.ext.albumArtUrl
import com.lzx.starrysky.ext.data
import com.lzx.starrysky.ext.duration
import com.lzx.starrysky.ext.id
import com.lzx.starrysky.ext.mediaUrl
import com.lzx.starrysky.ext.title
import com.lzx.starrysky.playback.player.ExoPlayback
import com.lzx.starrysky.playback.player.Playback
import com.lzx.starrysky.provider.IMediaSourceProvider
import com.lzx.starrysky.provider.SongInfo
import com.lzx.starrysky.utils.MD5
import com.lzx.starrysky.utils.StarrySkyUtils

class StarrySkyPlayerControl constructor(private val context: Context) : PlayerControl {

    private val connection: IMediaConnection = StarrySky.get().mediaConnection()
    private val mediaQueueProvider: IMediaSourceProvider = StarrySky.get().mediaQueueProvider()
    private val mPlayback: Playback = StarrySky.get().playBack()
    private val mPlayerEventListeners = mutableListOf<OnPlayerEventListener>()

    override fun playMusicById(songId: String) {
        if (mediaQueueProvider.hasSongInfo(songId)) {
            playMusicImpl(songId)
        }
    }

    override fun playMusicByInfo(info: SongInfo) {
        mediaQueueProvider.addSongInfo(info)
        playMusicImpl(info.songId)
    }

    override fun playMusicByIndex(index: Int) {
        val info = mediaQueueProvider.getSongInfoByIndex(index)
        info?.let {
            playMusicImpl(it.songId)
        }
    }

    override fun playMusic(songInfos: MutableList<SongInfo>, index: Int) {
        updatePlayList(songInfos)
        playMusicByIndex(index)
    }

    private fun playMusicImpl(mediaId: String) {
        connection.getTransportControls()?.playFromMediaId(mediaId, null)
    }

    override fun pauseMusic() {
        connection.getTransportControls()?.pause()
    }

    override fun restoreMusic() {
        connection.getTransportControls()?.play()
    }

    override fun stopMusic() {
        connection.getTransportControls()?.stop()
    }

    override fun prepare() {
        connection.getTransportControls()?.prepare()
    }

    override fun prepareFromSongId(songId: String) {
        if (mediaQueueProvider.hasSongInfo(songId)) {
            connection.getTransportControls()?.prepareFromMediaId(songId, null)
        }
    }

    override fun skipToNext() {
        connection.getTransportControls()?.skipToNext()
    }

    override fun skipToPrevious() {
        connection.getTransportControls()?.skipToPrevious()
    }

    override fun fastForward() {
        connection.getTransportControls()?.fastForward()
    }

    override fun rewind() {
        connection.getTransportControls()?.rewind()
    }

    override fun onDerailleur(refer: Boolean, multiple: Float) {
        val bundle = Bundle()
        bundle.putBoolean("refer", refer)
        bundle.putFloat("multiple", multiple)
        connection.sendCommand(ExoPlayback.ACTION_DERAILLEUR, bundle)
    }

    override fun seekTo(pos: Long) {
        connection.getTransportControls()?.seekTo(pos)
    }

    override fun setRepeatMode(repeatMode: Int, isLoop: Boolean) {
        StarrySkyUtils.saveRepeatMode(repeatMode, isLoop)
        connection.sendCommand(RepeatMode.KEY_REPEAT_MODE, Bundle())
    }

    override fun getRepeatMode(): RepeatMode {
        return StarrySkyUtils.repeatMode
    }

    override fun getPlayList(): MutableList<SongInfo> {
        return mediaQueueProvider.songList
    }

    override fun updatePlayList(songInfos: MutableList<SongInfo>) {
        mediaQueueProvider.songList = songInfos
    }

    override fun addPlayList(infos: MutableList<SongInfo>) {
        mediaQueueProvider.addSongInfos(infos)
    }

    override fun addSongInfo(info: SongInfo) {
        mediaQueueProvider.addSongInfo(info)
    }

    override fun removeSongInfo(songId: String) {
        mediaQueueProvider.deleteSongInfoById(songId)
    }

    override fun getNowPlayingSongInfo(): SongInfo? {
        val metadataCompat = connection.getNowPlaying()
        return metadataCompat?.let {
            var songInfo = it.id?.let { songId -> mediaQueueProvider.getSongInfoById(songId) }
            //播放列表改变了或者清空了，如果还在播放歌曲，这时候 getSongInfo 就会获取不到，
            //此时需要从 metadataCompat 中获取
            if (songInfo == null) {
                songInfo = getSongInfoFromMediaMetadata(it)
            }
            return@let songInfo
        }
    }

    private fun getSongInfoFromMediaMetadata(metadata: MediaMetadataCompat): SongInfo {
        val songInfo = SongInfo()
        songInfo.songId = metadata.id.toString()
        songInfo.songUrl = metadata.mediaUrl.toString()
        songInfo.duration = metadata.duration
        songInfo.songCover = metadata.albumArtUrl.toString()
        songInfo.songName = metadata.title.toString()
        return songInfo
    }

    override fun getNowPlayingSongId(): String {
        var songId = ""
        val metadataCompat = connection.getNowPlaying()
        if (metadataCompat != null) {
            songId = metadataCompat.id.toString()
        }
        return songId
    }

    override fun getNowPlayingIndex(): Int {
        var index = -1
        val songId = getNowPlayingSongId()
        index = mediaQueueProvider.getIndexById(songId)
        return index
    }

    override fun getBufferedPosition(): Long {
        return mPlayback.bufferedPosition
    }

    override fun getPlayingPosition(): Long {
        return mPlayback.currentStreamPosition
    }

    override fun isSkipToNextEnabled(): Boolean {
        val stateCompat = connection.getPlaybackStateCompat()
        if (stateCompat != null) {
            return stateCompat.actions and PlaybackStateCompat.ACTION_SKIP_TO_NEXT != 0L
        }
        return false
    }

    override fun isSkipToPreviousEnabled(): Boolean {
        val stateCompat = connection.getPlaybackStateCompat()
        if (stateCompat != null) {
            return stateCompat.actions and PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS != 0L
        }
        return false
    }

    override fun getPlaybackSpeed(): Float {
        val stateCompat = connection.getPlaybackStateCompat()
        return stateCompat?.playbackSpeed ?: 0F
    }

    override fun getPlaybackState(): Any? {
        val stateCompat = connection.getPlaybackStateCompat()
        return stateCompat?.playbackState
    }

    override fun getErrorMessage(): CharSequence {
        val stateCompat = connection.getPlaybackStateCompat()
        return stateCompat?.errorMessage ?: ""
    }

    override fun getErrorCode(): Int {
        val stateCompat = connection.getPlaybackStateCompat()
        return stateCompat?.errorCode ?: -1
    }

    override fun getState(): Int {
        val stateCompat = connection.getPlaybackStateCompat()
        return stateCompat?.state ?: -1
    }

    override fun isPlaying(): Boolean {
        return getState() == Playback.STATE_PLAYING // PlaybackStateCompat.STATE_PLAYING
    }

    override fun isPaused(): Boolean {
        return getState() == Playback.STATE_PAUSED // PlaybackStateCompat.STATE_PAUSED
    }

    override fun isIdea(): Boolean {
        return getState() == Playback.STATE_NONE // PlaybackStateCompat.STATE_NONE
    }

    override fun isCurrMusicIsPlayingMusic(songId: String): Boolean {
        return if (songId.isEmpty()) {
            false
        } else {
            val playingMusic = getNowPlayingSongInfo()
            playingMusic != null && songId == playingMusic.songId
        }
    }

    override fun isCurrMusicIsPlaying(songId: String): Boolean {
        return isCurrMusicIsPlayingMusic(songId) && isPlaying()
    }

    override fun isCurrMusicIsPaused(songId: String): Boolean {
        return isCurrMusicIsPlayingMusic(songId) && isPaused()
    }

    override fun setVolume(audioVolume: Float) {
        var volume = audioVolume
        if (volume < 0) {
            volume = 0f
        }
        if (volume > 1) {
            volume = 1f
        }
        val bundle = Bundle()
        bundle.putFloat("AudioVolume", volume)
        connection.sendCommand(ExoPlayback.ACTION_CHANGE_VOLUME, bundle)
    }

    override fun getVolume(): Float {
        return mPlayback.volume
    }

    override fun getDuration(): Long {
        var duration = connection.getNowPlaying()?.duration
        //如果没设置duration
        if (duration == null || duration == 0L) {
            duration = mPlayback.duration
        }
        //当切换歌曲的时候偶尔回调为 -9223372036854775807  Long.MIN_VALUE
        return if (duration < -1) {
            -1
        } else duration
    }

    override fun getAudioSessionId(): Int {
        return mPlayback.audioSessionId
    }

    override fun sendCommand(command: String, parameters: Bundle) {
        connection.sendCommand(command, parameters)
    }

    @SuppressLint("Recycle")
    override fun querySongInfoInLocal(): List<SongInfo> {
        val songInfos = mutableListOf<SongInfo>()
        val cursor =
            context.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,
                null, null, null)
                ?: return songInfos
        while (cursor.moveToNext()) {
            val song = SongInfo()
            song.songUrl = cursor.data
            song.songName = cursor.title
            song.duration = cursor.duration
            val songId = if (song.songUrl.isNotEmpty())
                MD5.hexdigest(song.songUrl)
            else
                MD5.hexdigest(System.currentTimeMillis().toString())
            song.songId = songId
            songInfos.add(song)
        }
        cursor.close()
        return songInfos
    }

    @Synchronized
    private fun getAlbumArtPicPath(context: Context, albumId: String): String? {
        if (TextUtils.isEmpty(albumId)) {
            return null
        }
        val projection = arrayOf(MediaStore.Audio.Albums.ALBUM_ART)
        var imagePath: String? = null
        val uri = Uri.parse(
            "content://media" + MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI.path + "/" + albumId)
        val cur = context.contentResolver.query(uri, projection, null, null, null) ?: return null
        if (cur.count > 0 && cur.columnCount > 0) {
            cur.moveToNext()
            imagePath = cur.getString(0)
        }
        cur.close()
        return imagePath
    }

    override fun addPlayerEventListener(listener: OnPlayerEventListener?) {
        listener?.let {
            if (!mPlayerEventListeners.contains(it)) {
                mPlayerEventListeners.add(it)
            }
        }
    }

    override fun removePlayerEventListener(listener: OnPlayerEventListener?) {
        listener?.let {
            mPlayerEventListeners.remove(it)
        }
    }

    override fun clearPlayerEventListener() {
        mPlayerEventListeners.clear()
    }

    override fun getPlayerEventListeners(): MutableList<OnPlayerEventListener> {
        return mPlayerEventListeners
    }

    override fun playbackState(): MutableLiveData<PlaybackStage> {
        return connection.getPlaybackState()
    }
}