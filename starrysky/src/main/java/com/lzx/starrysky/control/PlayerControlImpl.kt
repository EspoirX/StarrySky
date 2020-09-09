package com.lzx.starrysky.control

import androidx.lifecycle.MutableLiveData
import android.content.Context
import android.os.Bundle
import android.provider.MediaStore
import com.lzx.starrysky.playback.MediaSourceProvider
import com.lzx.starrysky.OnPlayerEventListener
import com.lzx.starrysky.SongInfo
import com.lzx.starrysky.playback.Playback
import com.lzx.starrysky.playback.PlaybackManager
import com.lzx.starrysky.playback.PlaybackStage
import com.lzx.starrysky.utils.MD5
import com.lzx.starrysky.utils.StarrySkyUtils
import com.lzx.starrysky.utils.data
import com.lzx.starrysky.utils.duration
import com.lzx.starrysky.utils.title

class PlayerControlImpl(
    private val provider: MediaSourceProvider,
    private val playbackManager: PlaybackManager
) : PlayerControl{

    override fun playMusicById(songId: String) {
        if (provider.hasSongInfo(songId)) {
            playMusicImpl(songId)
        }
    }

    override fun playMusicByInfo(info: SongInfo) {
        provider.addSongInfo(info)
        playMusicImpl(info.songId)
    }

    override fun playSingleMusicByInfo(info: SongInfo) {
        provider.clearSongInfos()
        provider.addSongInfo(info)
        val bundle = Bundle()
        bundle.putInt("clearSongId", 1)
        playMusicImpl(info.songId, bundle)
    }

    override fun playMusicByIndex(index: Int) {
        val info = provider.getSongInfoByIndex(index)
        info?.let {
            playMusicImpl(it.songId)
        }
    }

    override fun playMusic(songInfos: MutableList<SongInfo>, index: Int) {
        updatePlayList(songInfos)
        playMusicByIndex(index)
    }

    private fun playMusicImpl(mediaId: String, extras: Bundle? = null) {
        playbackManager.onPlayFromMediaId(mediaId, extras)
    }

    override fun pauseMusic() {
        playbackManager.onPause()
    }

    override fun restoreMusic() {
        playbackManager.onPlay()
    }

    override fun stopMusic() {
        playbackManager.onStop()
    }

    override fun prepare() {
        playbackManager.onPrepare()
    }

    override fun prepareFromSongId(songId: String) {
        if (provider.hasSongInfo(songId)) {
            playbackManager.onPrepareFromSongId(songId)
        }
    }

    override fun skipToNext() {
        playbackManager.onSkipToNext()
    }

    override fun skipToPrevious() {
        playbackManager.onSkipToPrevious()
    }

    override fun fastForward() {
        playbackManager.onFastForward()
    }

    override fun rewind() {
        playbackManager.onRewind()
    }

    override fun onDerailleur(refer: Boolean, multiple: Float) {
        playbackManager.onDerailleur(refer, multiple)
    }

    override fun seekTo(pos: Long) {
        playbackManager.seekTo(pos)
    }

    override fun setRepeatMode(repeatMode: Int, isLoop: Boolean) {
        StarrySkyUtils.saveRepeatMode(repeatMode, isLoop)
        playbackManager.setRepeatMode(repeatMode, isLoop)
    }

    override fun getRepeatMode(): RepeatMode {
        return StarrySkyUtils.repeatMode
    }

    override fun getPlayList(): MutableList<SongInfo> {
        return provider.songList
    }

    override fun updatePlayList(songInfos: MutableList<SongInfo>) {
        provider.songList = songInfos
    }

    override fun addPlayList(infos: MutableList<SongInfo>) {
        provider.addSongInfos(infos)
    }

    override fun addSongInfo(info: SongInfo) {
        provider.addSongInfo(info)
    }

    override fun removeSongInfo(songId: String) {
        provider.deleteSongInfoById(songId)
    }

    override fun clearPlayList() {
        provider.clearSongInfos()
    }

    override fun getNowPlayingSongInfo(): SongInfo? {
        return playbackManager.playback.currPlayInfo
    }

    override fun getNowPlayingSongId(): String {
        return getNowPlayingSongInfo()?.songId ?: ""
    }

    override fun getNowPlayingSongUrl(): String {
        return getNowPlayingSongInfo()?.songUrl ?: ""
    }

    override fun getNowPlayingIndex(): Int {
        val songId = getNowPlayingSongId()
        return provider.getIndexById(songId)
    }

    override fun getBufferedPosition(): Long {
        return playbackManager.playback.bufferedPosition
    }

    override fun getPlayingPosition(): Long {
        return playbackManager.playback.currentStreamPosition
    }

    override fun isSkipToNextEnabled(): Boolean {
        return playbackManager.isSkipToNextEnabled()
    }

    override fun isSkipToPreviousEnabled(): Boolean {
        return playbackManager.isSkipToPreviousEnabled()
    }

    override fun getPlaybackSpeed(): Float {
        return playbackManager.playback.getPlaybackSpeed()
    }

    override fun getPlaybackState(): PlaybackStage? {
        return playbackManager.getPlaybackState()
    }

    override fun isPlaying(): Boolean {
        return playbackManager.playback.playbackState == Playback.STATE_PLAYING
    }

    override fun isPaused(): Boolean {
        return playbackManager.playback.playbackState == Playback.STATE_PAUSED
    }

    override fun isIdea(): Boolean {
        return playbackManager.playback.playbackState == Playback.STATE_IDLE
    }

    override fun isBuffering(): Boolean {
        return playbackManager.playback.playbackState == Playback.STATE_BUFFERING
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

    override fun isCurrMusicIsIdea(songId: String): Boolean {
        return isCurrMusicIsPlayingMusic(songId) && isIdea()
    }

    override fun isCurrMusicIsBuffering(songId: String): Boolean {
        return isCurrMusicIsPlayingMusic(songId) && isBuffering()
    }

    override fun setVolume(audioVolume: Float) {
        var volume = audioVolume
        if (volume < 0) {
            volume = 0f
        }
        if (volume > 1) {
            volume = 1f
        }
        playbackManager.playback.volume = volume
    }

    override fun getVolume(): Float {
        return playbackManager.playback.volume
    }

    override fun getDuration(): Long {
        return playbackManager.playback.duration
    }

    override fun getAudioSessionId(): Int {
        return playbackManager.playback.audioSessionId
    }

    override fun querySongInfoInLocal(context: Context): List<SongInfo> {
        val songInfos = mutableListOf<SongInfo>()
        val cursor = context.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            null, null, null, null)
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

    override fun addPlayerEventListener(listener: OnPlayerEventListener?) {
        TODO("Not yet implemented")
    }

    override fun removePlayerEventListener(listener: OnPlayerEventListener?) {
        TODO("Not yet implemented")
    }

    override fun clearPlayerEventListener() {
        TODO("Not yet implemented")
    }

    override fun getPlayerEventListeners(): MutableList<OnPlayerEventListener> {
        TODO("Not yet implemented")
    }

    override fun playbackState(): MutableLiveData<PlaybackStage> {
        TODO("Not yet implemented")
    }

    override fun onPlaybackStateUpdated(playbackStage: PlaybackStage) {
        TODO("Not yet implemented")
    }
}