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
import com.lzx.starrysky.ext.album
import com.lzx.starrysky.ext.albumArt
import com.lzx.starrysky.ext.albumArtUrl
import com.lzx.starrysky.ext.albumId
import com.lzx.starrysky.ext.albumKey
import com.lzx.starrysky.ext.artist
import com.lzx.starrysky.ext.artistKey
import com.lzx.starrysky.ext.data
import com.lzx.starrysky.ext.dateAdded
import com.lzx.starrysky.ext.dateModified
import com.lzx.starrysky.ext.displayName
import com.lzx.starrysky.ext.duration
import com.lzx.starrysky.ext.genre
import com.lzx.starrysky.ext.id
import com.lzx.starrysky.ext.mediaUrl
import com.lzx.starrysky.ext.mimeType
import com.lzx.starrysky.ext.size
import com.lzx.starrysky.ext.title
import com.lzx.starrysky.ext.titleKey
import com.lzx.starrysky.ext.trackCount
import com.lzx.starrysky.ext.trackNumber
import com.lzx.starrysky.ext.year
import com.lzx.starrysky.notification.INotification
import com.lzx.starrysky.playback.player.ExoPlayback
import com.lzx.starrysky.playback.player.Playback
import com.lzx.starrysky.provider.MediaQueueProvider
import com.lzx.starrysky.provider.SongInfo
import com.lzx.starrysky.utils.MD5

class StarrySkyPlayerControl constructor(private val context: Context) : PlayerControl {

    private val connection: IMediaConnection
    private val mMediaQueueProvider: MediaQueueProvider
    private val mPlayback: Playback?
    private val mPlayerEventListeners = mutableListOf<OnPlayerEventListener>()

    init {
        val starrySky = StarrySky.get()
        this.mMediaQueueProvider = starrySky.mediaQueueProvider
        this.connection = starrySky.connection
        this.mPlayback = starrySky.playback
        starrySky.registerPlayerControl(this)
    }

    override fun playMusicById(songId: String) {
        if (mMediaQueueProvider.hasMediaInfo(songId)) {
            playMusicImpl(songId)
        }
    }

    override fun playMusicByInfo(info: SongInfo) {
        if (mMediaQueueProvider.hasMediaInfo(info.songId)) {
            playMusicImpl(info.songId)
        } else {
            mMediaQueueProvider.addMediaBySongInfo(info)
            playMusicImpl(info.songId)
        }
    }

    override fun playMusicByInfoDirect(info: SongInfo) {
        mMediaQueueProvider.onlyOneMediaBySongInfo(info)
        playMusicImpl(info.songId)
    }

    override fun playMusicByIndex(index: Int) {
        val info = mMediaQueueProvider.getMediaInfo(index)
        if (info != null) {
            playMusicImpl(info.mediaId)
        }
    }

    override fun playMusic(songInfos: List<SongInfo>, index: Int) {
        mMediaQueueProvider.updateMediaListBySongInfo(songInfos)
        playMusicByIndex(index)
    }

    private fun playMusicImpl(mediaId: String) {
        connection.getTransportControls()?.playFromMediaId(mediaId, null)
    }

    override fun pauseMusic() {
        connection.getTransportControls()?.pause()
    }

    override fun playMusic() {
        connection.getTransportControls()?.play()
    }

    override fun stopMusic() {
        connection.getTransportControls()?.stop()
    }

    override fun prepare() {
        connection.getTransportControls()?.prepare()
    }

    override fun prepareFromSongId(songId: String) {
        if (mMediaQueueProvider.hasMediaInfo(songId)) {
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

    override fun setShuffleMode(shuffleMode: Int) {
        connection.getTransportControls()?.setShuffleMode(shuffleMode)
    }

    override fun getShuffleMode(): Int {
        return connection.getShuffleMode()
    }

    override fun setRepeatMode(repeatMode: Int) {
        connection.getTransportControls()?.setRepeatMode(repeatMode)
    }

    override fun getRepeatMode(): Int {
        return connection.getRepeatMode()
    }

    override fun getPlayList(): List<SongInfo> {
        return mMediaQueueProvider.getSongList()
    }

    override fun updatePlayList(songInfos: List<SongInfo>) {
        mMediaQueueProvider.updateMediaListBySongInfo(songInfos)
    }

    override fun addSongInfo(info: SongInfo) {
        mMediaQueueProvider.addMediaBySongInfo(info)
    }

    override fun removeSongInfo(songId: String) {
        mMediaQueueProvider.deleteMediaById(songId)
    }

    override fun getNowPlayingSongInfo(): SongInfo? {
        var songInfo: SongInfo? = null
        val metadataCompat = connection.getNowPlaying()
        if (metadataCompat != null) {
            val songId = metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)
            songInfo = mMediaQueueProvider.getSongInfo(songId)
            //播放列表改变了或者清空了，如果还在播放歌曲，这时候 getSongInfo 就会获取不到，
            //此时需要从 metadataCompat 中获取
            if (songInfo == null && !TextUtils.isEmpty(songId)) {
                songInfo = getSongInfoFromMediaMetadata(metadataCompat)
            }
        }
        return songInfo
    }

    private fun getSongInfoFromMediaMetadata(metadata: MediaMetadataCompat): SongInfo {
        val songInfo = SongInfo()
        songInfo.songId = metadata.id.toString()
        songInfo.songUrl = metadata.mediaUrl.toString()
        songInfo.albumName = metadata.album.toString()
        songInfo.artist = metadata.artist.toString()
        songInfo.duration = metadata.duration
        songInfo.genre = metadata.genre.toString()
        songInfo.songCover = metadata.albumArtUrl.toString()
        songInfo.albumCover = metadata.albumArtUrl.toString()
        songInfo.songName = metadata.title.toString()
        songInfo.trackNumber = metadata.trackNumber.toInt()
        songInfo.albumSongCount = metadata.trackCount.toInt()
        songInfo.songCoverBitmap = metadata.albumArt
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
        if (!TextUtils.isEmpty(songId)) {
            index = mMediaQueueProvider.getIndexByMediaId(songId)
        }
        return index
    }

    override fun getBufferedPosition(): Long {
        return mPlayback?.bufferedPosition ?: 0
    }

    override fun getPlayingPosition(): Long {
        return mPlayback?.currentStreamPosition ?: 0
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
        return getState() == PlaybackStateCompat.STATE_PLAYING
    }

    override fun isPaused(): Boolean {
        return getState() == PlaybackStateCompat.STATE_PAUSED
    }

    override fun isIdea(): Boolean {
        return getState() == PlaybackStateCompat.STATE_NONE
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
        return mPlayback?.volume ?: -1F
    }

    override fun getDuration(): Long {
        var duration = connection.getNowPlaying()?.duration
        //如果没设置duration
        if (duration == null || duration == 0L) {
            duration = mPlayback?.duration ?: 0
        }
        //当切换歌曲的时候偶尔回调为 -9223372036854775807  Long.MIN_VALUE
        return if (duration < -1) {
            -1
        } else duration
    }

    override fun getAudioSessionId(): Int {
        return mPlayback?.getAudioSessionId() ?: 0
    }

    override fun updateFavoriteUI(isFavorite: Boolean) {
        val bundle = Bundle()
        bundle.putBoolean("isFavorite", isFavorite)
        connection.sendCommand(INotification.ACTION_UPDATE_FAVORITE_UI, bundle)
    }

    override fun updateLyricsUI(isChecked: Boolean) {
        val bundle = Bundle()
        bundle.putBoolean("isChecked", isChecked)
        connection.sendCommand(INotification.ACTION_UPDATE_LYRICS_UI, bundle)
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
            song.albumId = cursor.albumId
            song.albumCover = getAlbumArtPicPath(context, song.albumId).toString()
            song.songNameKey = cursor.titleKey
            song.artistKey = cursor.artistKey
            song.albumNameKey = cursor.albumKey
            song.artist = cursor.artist
            song.albumName = cursor.album
            song.songUrl = cursor.data
            song.description = cursor.displayName
            song.songName = cursor.title
            song.mimeType = cursor.mimeType
            song.year = cursor.year
            song.duration = cursor.duration
            song.size = cursor.size
            song.publishTime = cursor.dateAdded
            song.modifiedTime = cursor.dateModified
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
        if (listener != null) {
            if (!mPlayerEventListeners.contains(listener)) {
                mPlayerEventListeners.add(listener)
            }
        }
    }

    override fun removePlayerEventListener(listener: OnPlayerEventListener?) {
        if (listener != null) {
            mPlayerEventListeners.remove(listener)
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