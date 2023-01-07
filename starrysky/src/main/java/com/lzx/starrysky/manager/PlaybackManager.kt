package com.lzx.starrysky.manager


import android.app.Activity
import android.graphics.BitmapFactory
import com.lzx.starrysky.SongInfo
import com.lzx.starrysky.StarrySky
import com.lzx.starrysky.StarrySkyInstall
import com.lzx.starrysky.control.PlayerControl
import com.lzx.starrysky.control.RepeatMode
import com.lzx.starrysky.control.isModeShuffle
import com.lzx.starrysky.intercept.InterceptCallback
import com.lzx.starrysky.intercept.InterceptorPlace
import com.lzx.starrysky.intercept.InterceptorService
import com.lzx.starrysky.intercept.StarrySkyInterceptor
import com.lzx.starrysky.playback.FocusInfo
import com.lzx.starrysky.playback.Playback
import com.lzx.starrysky.queue.MediaQueueManager
import com.lzx.starrysky.queue.MediaSourceProvider
import com.lzx.starrysky.service.MusicServiceBinder
import com.lzx.starrysky.utils.md5

class PlaybackManager(
    provider: MediaSourceProvider,
    private val appInterceptors: MutableList<Pair<StarrySkyInterceptor, String>>,
    private val playerControl: PlayerControl,
    private val binder: MusicServiceBinder?
) : Playback.Callback {

    private val interceptorService = InterceptorService()
    val mediaQueue = MediaQueueManager(provider)
    private var sessionManager = MediaSessionManager(StarrySkyInstall.globalContext!!, this)
    private var lastSongInfo: SongInfo? = null
    private var isActionStop = false
    var isSkipMediaQueue = false
    private var withOutCallback = true

    init {
        player()?.setCallback(this)
        binder?.setSessionToken(sessionManager.getMediaSession())
    }

    /**
     * 当前播放器
     */
    fun player() = binder?.player

    /**
     * 配置拦截器
     */
    fun attachInterceptors(interceptors: MutableList<Pair<StarrySkyInterceptor, String>>) = apply {
        val list = mutableListOf<Pair<StarrySkyInterceptor, String>>()
        list += interceptors
        list += appInterceptors
        interceptorService.attachInterceptors(list)
    }

    /**
     * 是否跳过播放队列
     */
    fun attachSkipMediaQueue(isSkipMediaQueue: Boolean) = apply {
        this.isSkipMediaQueue = isSkipMediaQueue
    }

    /**
     * 是否需要回调
     */
    fun attachWithOutCallback(withOutCallback: Boolean) = apply {
        this.withOutCallback = withOutCallback
    }

    /**
     * onDestroy 后重置变量
     */
    internal fun resetVariable(activity: Activity?) {
        isSkipMediaQueue = false
        withOutCallback = true
        interceptorService.attachInterceptors(appInterceptors)
    }

    /**
     * 播放
     */
    fun onPlayMusicImpl(songInfo: SongInfo?, isPlayWhenReady: Boolean) {
        if (songInfo == null) return
        isActionStop = false
        if (isSkipMediaQueue) {
            player()?.currentMediaId = ""
        } else {
            mediaQueue.updateIndexBySongId(songInfo.songId)
        }
        interceptorService.handlerInterceptor(songInfo, InterceptorPlace.BEFORE, object : InterceptCallback {
            override fun onNext(songInfo: SongInfo?) {
                if (songInfo == null || songInfo.songId.isEmpty() || songInfo.songUrl.isEmpty()) {
                    throw IllegalStateException("songId 或 songUrl 不能为空")
                }
                mediaQueue.updateMusicArt(songInfo)  //更新媒体封面信息
                player()?.play(songInfo, isPlayWhenReady)
            }

            override fun onInterrupt(msg: String?) {
                onPlaybackError(songInfo, msg.orEmpty())
            }
        })
    }

    /**
     * 暂停
     */
    fun onPause() {
        if (player()?.isPlaying() == true) {
            player()?.pause()
        }
    }

    /**
     * 停止
     */
    fun onStop() {
        isActionStop = true
        player()?.stop()
        lastSongInfo = null
    }

    /**
     * 下一首
     */
    fun onSkipToNext() {
        if (isSkipMediaQueue) {
            throw IllegalStateException("skipMediaQueue 模式下不能使用该方法")
        }
        if (mediaQueue.skipQueuePosition(1)) {
            val song = mediaQueue.getCurrentSongInfo(false)
            onPlayMusicImpl(song, true)
        }
    }

    /**
     * 上一首
     */
    fun onSkipToPrevious() {
        if (isSkipMediaQueue) {
            throw IllegalStateException("skipMediaQueue 模式下不能使用该方法")
        }
        if (mediaQueue.skipQueuePosition(-1)) {
            val song = mediaQueue.getCurrentSongInfo(false)
            onPlayMusicImpl(song, true)
        }
    }

    /**
     * 下一首(通知栏用)
     */
    override fun skipToNext() {
        if (!isSkipMediaQueue) {
            onSkipToNext()
        }
    }

    /**
     * 上一首(通知栏用)
     */
    override fun skipToPrevious() {
        if (!isSkipMediaQueue) {
            onSkipToPrevious()
        }
    }

    /**
     * 准备播放
     */
    fun onPrepare() {
        val song = mediaQueue.getCurrentSongInfo(true)
        onPlayMusicImpl(song, false)
    }

    /**
     * 准备播放
     */
    fun onPrepareById(songId: String) {
        var isMoreThenOne: Boolean
        val list = mediaQueue.getCurrSongList().filter { it.songId == songId }
            .also { isMoreThenOne = it.size > 1 }
        if (isMoreThenOne) {
            throw IllegalStateException("存在两条相同的音频信息")
        }
        onPlayMusicImpl(list.firstOrNull(), false)
    }

    /**
     * 准备播放
     */
    fun onPrepareByUrl(songUrl: String) {
        if (!isSkipMediaQueue) {
            var isMoreThenOne: Boolean
            val list = mediaQueue.getCurrSongList().filter { it.songUrl == songUrl }
                .also { isMoreThenOne = it.size > 1 }
            if (isMoreThenOne) {
                throw IllegalStateException("存在两条相同的音频信息")
            }
            onPlayMusicImpl(list.firstOrNull(), false)
        } else {
            val info = SongInfo(songUrl.md5(), songUrl)
            onPlayMusicImpl(info, false)
        }
    }

    /**
     * 准备播放
     */
    fun onPrepareByInfo(info: SongInfo) {
        if (!isSkipMediaQueue) {
            onPrepareById(info.songId)
        } else {
            onPlayMusicImpl(info, false)
        }
    }

    /**
     * 快进
     */
    fun onFastForward(speed: Float) {
        player()?.onFastForward(speed)
    }

    /**
     * 快退
     */
    fun onRewind(speed: Float) {
        player()?.onRewind(speed)
    }

    /**
     * 指定语速 refer 是否已当前速度为基数  multiple 倍率
     */
    fun onDerailleur(refer: Boolean, multiple: Float) {
        player()?.onDerailleur(refer, multiple)
    }

    /**
     * 转跳进度
     */
    fun onSeekTo(pos: Long, isPlayWhenPaused: Boolean = true) {
        player()?.seekTo(pos)
        if (isPlayWhenPaused && player()?.playbackState() == Playback.STATE_PAUSED) {
            onRestoreMusic()
        }
    }

    /**
     * 暂停后恢复播放
     */
    fun onRestoreMusic() {
        player()?.getCurrPlayInfo()?.let {
            player()?.play(it, true)
        }
    }

    /**
     * 设置播放模式
     */
    fun setRepeatMode(repeatMode: Int, loop: Boolean) {
        if (repeatMode == RepeatMode.REPEAT_MODE_SHUFFLE) {
            mediaQueue.provider.updateShuffleSongList()
        } else {
            mediaQueue.updateIndexByPlayingInfo(player()?.getCurrPlayInfo())
        }
    }

    /**
     * 是否可以下一首
     */
    fun isSkipToNextEnabled(): Boolean {
        if (isSkipMediaQueue) return false
        val repeatMode = RepeatMode.with
        if (repeatMode.repeatMode == RepeatMode.REPEAT_MODE_NONE ||
            repeatMode.repeatMode == RepeatMode.REPEAT_MODE_ONE ||
            repeatMode.repeatMode == RepeatMode.REPEAT_MODE_REVERSE
        ) {
            return if (repeatMode.isLoop) true else !mediaQueue.currSongIsLastSong()
        }
        return true
    }

    /**
     * 是否可以上一首
     */
    fun isSkipToPreviousEnabled(): Boolean {
        if (isSkipMediaQueue) return false
        val repeatMode = RepeatMode.with
        if (repeatMode.repeatMode == RepeatMode.REPEAT_MODE_NONE ||
            repeatMode.repeatMode == RepeatMode.REPEAT_MODE_ONE ||
            repeatMode.repeatMode == RepeatMode.REPEAT_MODE_REVERSE
        ) {
            return if (repeatMode.isLoop) true else !mediaQueue.currSongIsFirstSong()
        }
        return true
    }

    /**
     * 删除歌曲
     */
    fun removeSongInfo(songId: String) {
        val isSameInfo = songId == player()?.getCurrPlayInfo()?.songId
        val isPlaying = player()?.playbackState() == Playback.STATE_PLAYING && isSameInfo
        val isPaused = player()?.playbackState() == Playback.STATE_PAUSED && isSameInfo
        if (isPlaying || isPaused) {
            if (mediaQueue.skipQueuePosition(1)) {
                deleteAndUpdateInfo(songId, true)
            }
        } else {
            deleteAndUpdateInfo(songId, false)
        }
    }

    /**
     * 删除歌曲并更新下标
     */
    private fun deleteAndUpdateInfo(songId: String, isPlayNextSong: Boolean) {
        val repeatMode = RepeatMode.with.repeatMode
        val playIngInfo = mediaQueue.getCurrentSongInfo(!repeatMode.isModeShuffle())
        mediaQueue.provider.deleteSongInfoById(songId)
        mediaQueue.provider.updateShuffleSongList()
        mediaQueue.updateIndexByPlayingInfo(playIngInfo)
        if (mediaQueue.provider.getSourceSize() == 0) {
            onStop()
        } else {
            if (isPlayNextSong) {
                onPlayMusicImpl(playIngInfo, true)
            }
        }
    }

    /**
     * 根据当前播放信息更新下标
     */
    fun updateCurrIndex() {
        player()?.getCurrPlayInfo()?.let {
            mediaQueue.updateIndexByPlayingInfo(it)
        }
    }

    /**
     * 定时暂停
     */
    fun onStopByTimedOff(time: Long, isPause: Boolean, finishCurrSong: Boolean) {
        binder?.onStopByTimedOff(time, isPause, finishCurrSong)
    }

    /**
     * 重播当前音频
     */
    fun replayCurrMusic() {
        player()?.getCurrPlayInfo()?.let {
            player()?.currentMediaId = ""
            player()?.play(it, true)
        }
    }

    override fun onPlayerStateChanged(songInfo: SongInfo?, playWhenReady: Boolean, playbackState: Int) {
        if (lastSongInfo?.songId != songInfo?.songId && !isActionStop) {
            val state = PlaybackStage()
            state.lastSongInfo = lastSongInfo
            state.songInfo = songInfo
            state.stage = PlaybackStage.SWITCH
            if (withOutCallback && lastSongInfo != null) {
                playerControl.onPlaybackStateUpdated(state)
            }
            lastSongInfo = songInfo
        }
        updatePlaybackState(songInfo, null, playbackState)
        if (playbackState == Playback.STATE_IDLE && !isActionStop) { //stop 和 播放完成都会回调这个
            onPlaybackCompletion()
        }
    }


    private fun onPlaybackCompletion() {
        interceptorService.handlerInterceptor(player()?.getCurrPlayInfo(), InterceptorPlace.AFTER, object : InterceptCallback {
            override fun onNext(songInfo: SongInfo?) {
                val repeatMode = RepeatMode.with
                when (repeatMode.repeatMode) {
                    //顺序播放
                    RepeatMode.REPEAT_MODE_NONE -> {
                        if (repeatMode.isLoop) {
                            player()?.currentMediaId = ""
                            if (!isSkipMediaQueue) {
                                onSkipToNext()
                            }
                        } else if (!mediaQueue.currSongIsLastSong()) {
                            if (!isSkipMediaQueue) {
                                onSkipToNext()
                            }
                        } else {
                            player()?.currentMediaId = ""
                        }
                    }
                    //单曲播放
                    RepeatMode.REPEAT_MODE_ONE -> {
                        player()?.currentMediaId = ""
                        if (repeatMode.isLoop) {
                            onRestoreMusic()
                        }
                    }
                    //随机播放
                    RepeatMode.REPEAT_MODE_SHUFFLE -> {
                        player()?.currentMediaId = ""
                        if (!isSkipMediaQueue) {
                            onSkipToNext()
                        }
                    }
                    //倒序播放
                    RepeatMode.REPEAT_MODE_REVERSE -> {
                        if (repeatMode.isLoop) {
                            player()?.currentMediaId = ""
                            if (!isSkipMediaQueue) {
                                onSkipToPrevious()
                            }
                        } else if (!mediaQueue.currSongIsFirstSong()) {
                            if (!isSkipMediaQueue) {
                                onSkipToPrevious()
                            }
                        } else {
                            player()?.currentMediaId = ""
                        }
                    }
                }
            }

            override fun onInterrupt(msg: String?) {

            }
        })


    }

    override fun onPlaybackError(songInfo: SongInfo?, error: String) {
        updatePlaybackState(songInfo, error, Playback.STATE_ERROR)
    }

    override fun onFocusStateChange(info: FocusInfo) {
        playerControl.onFocusStateChange(info)
    }

    private fun updatePlaybackState(currPlayInfo: SongInfo?, errorMsg: String?, state: Int) {
        val newState = state.changePlaybackState()
        binder?.onChangedNotificationState(
            currPlayInfo, newState,
            isSkipToNextEnabled(), isSkipToPreviousEnabled()
        )
        when (newState) {
            PlaybackStage.BUFFERING,
            PlaybackStage.PAUSE -> {
                binder?.startNotification(currPlayInfo, newState)
            }
        }
        StarrySky.log("PlaybackStage = $newState")
        val playbackState = PlaybackStage()
        playbackState.errorMsg = errorMsg
        playbackState.songInfo = currPlayInfo
        playbackState.stage = newState
        playbackState.isStop = isActionStop

        sessionManager.updateMetaData(currPlayInfo)
        if (withOutCallback) {
            playerControl.onPlaybackStateUpdated(playbackState)
        }
    }
}