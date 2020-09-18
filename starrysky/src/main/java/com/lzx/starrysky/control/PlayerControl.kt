package com.lzx.starrysky.control

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.lzx.starrysky.OnPlayerEventListener
import com.lzx.starrysky.SongInfo
import com.lzx.starrysky.playback.PlaybackManager
import com.lzx.starrysky.playback.PlaybackStage

data class RepeatMode(val repeatMode: Int, val isLoop: Boolean) {
    companion object {
        const val KEY_REPEAT_MODE = "StarrySky#KEY_REPEAT_MODE"
        const val REPEAT_MODE_NONE = 100     //顺序播放
        const val REPEAT_MODE_ONE = 200      //单曲播放
        const val REPEAT_MODE_SHUFFLE = 300  //随机播放
        const val REPEAT_MODE_REVERSE = 400  //倒序播放
    }
}

interface PlayerControl : PlaybackManager.PlaybackServiceCallback {

    /**
     * 根据songId播放,调用前请确保已经设置了播放列表
     */
    fun playMusicById(songId: String)

    /**
     * 根据 SongInfo 播放，实际也是根据 songId 播放
     */
    fun playMusicByInfo(info: SongInfo)

    /**
     * 只播放当前这首歌，播完就停止
     */
    fun playSingleMusicByInfo(info: SongInfo)

    /**
     * 根据要播放的歌曲在播放列表中的下标播放,调用前请确保已经设置了播放列表
     */
    fun playMusicByIndex(index: Int)

    /**
     * 播放
     *
     * @param mediaList 播放列表
     * @param index     要播放的歌曲在播放列表中的下标
     */
    fun playMusic(songInfos: MutableList<SongInfo>, index: Int)

    /**
     * 暂停
     */
    fun pauseMusic()

    /**
     * 恢复播放
     */
    fun restoreMusic()

    /**
     * 停止播放
     */
    fun stopMusic()

    /**
     * 准备播放
     */
    fun prepare()

    /**
     * 准备播放，根据songId
     */
    fun prepareFromSongId(songId: String)

    /**
     * 使用该方法前请在初始化时将 isCreateRefrainPlayer 标记设为 true
     * 将 isAutoManagerFocus 是否自动管理焦点设为 false，否则无法同时播放
     * 需求焦点管理的请实现 setOnAudioFocusChangeListener 方法监听焦点自己处理
     *
     * 允许同时播放另一个音频，该音频没有队列管理概念，没有通知栏功能
     * 会创建另一个播放实例去播放，有播放回调，有拦截器功能，该音频的 headData 里面
     * 统一都添加了 key = SongType,value = Refrain 的标记
     * 如果使用了该方法，请在回调监听和拦截器功能的时候做好区分
     * SongInfo 有扩展方法 isRefrain 可以方便判断
     */
    fun playRefrain(info: SongInfo)

    fun stopRefrain()

    fun setRefrainVolume(audioVolume: Float)

    fun getRefrainVolume(): Float

    fun getRefrainInfo(): SongInfo?

    fun isRefrainPlaying(): Boolean

    /**
     * 下一首
     */
    fun skipToNext()

    /**
     * 上一首
     */
    fun skipToPrevious()

    /**
     * 快进
     */
    fun fastForward()

    /**
     * 开始倒带 每调一次减 0.5 倍，最小为 0
     */
    fun rewind()

    /**
     * 指定语速,通过此方法可配置任意倍速，注意结果要大于0
     *
     * @param refer    refer 是否已当前速度为基数
     * @param multiple multiple 倍率
     */
    fun onDerailleur(refer: Boolean, multiple: Float)

    /**
     * 移动到媒体流中的新位置,以毫秒为单位。
     */
    fun seekTo(pos: Long)

    /**
     * 设置播放模式
     * 必须是以下之一：
     * REPEAT_MODE_NONE      顺序播放
     * REPEAT_MODE_ONE       单曲播放
     * REPEAT_MODE_SHUFFLE   随机播放
     * REPEAT_MODE_REVERSE   倒序播放
     *
     * isLoop 播放倒最后一首时是否从第一首开始循环播放,该参数对随机播放无效
     */
    fun setRepeatMode(repeatMode: Int, isLoop: Boolean)

    /**
     * 获取播放模式,默认顺序播放
     * REPEAT_MODE_NONE      顺序播放
     * REPEAT_MODE_ONE       单曲播放
     * REPEAT_MODE_SHUFFLE   随机播放
     * REPEAT_MODE_REVERSE   倒序播放
     */
    fun getRepeatMode(): RepeatMode

    /**
     * 获取播放列表
     */
    fun getPlayList(): MutableList<SongInfo>

    /**
     * 更新播放列表
     */
    fun updatePlayList(songInfos: MutableList<SongInfo>)

    /**
     * 添加更多播放列表
     */
    fun addPlayList(infos: MutableList<SongInfo>)

    /**
     * 添加一首歌
     */
    fun addSongInfo(info: SongInfo)

    /**
     * 删除
     */
    fun removeSongInfo(songId: String)

    /**
     * 清除播放列表
     */
    fun clearPlayList()

    /**
     * 获取当前播放的歌曲信息
     */
    fun getNowPlayingSongInfo(): SongInfo?

    /**
     * 获取当前播放的歌曲songId
     */
    fun getNowPlayingSongId(): String

    /**
     *  获取当前播放的歌曲url
     */
    fun getNowPlayingSongUrl(): String

    /**
     * 获取当前播放歌曲的下标
     */
    fun getNowPlayingIndex(): Int

    /**
     * 以ms为单位获取当前缓冲的位置。
     */
    fun getBufferedPosition(): Long

    /**
     * 获取播放位置 毫秒为单位。
     */
    fun getPlayingPosition(): Long

    /**
     * 是否有下一首
     */
    fun isSkipToNextEnabled(): Boolean

    /**
     * 是否有上一首
     */
    fun isSkipToPreviousEnabled(): Boolean

    /**
     * 将当前播放速度作为正常播放的倍数。 倒带时这应该是负数， 值为1表示正常播放，0表示暂停。
     */
    fun getPlaybackSpeed(): Float

    /**
     * 比较方便的判断当前媒体是否在播放
     */
    fun isPlaying(): Boolean

    /**
     * 比较方便的判断当前媒体是否暂停中
     */
    fun isPaused(): Boolean

    /**
     * 比较方便的判断当前媒体是否空闲
     */
    fun isIdea(): Boolean

    /**
     * 比较方便的判断当前媒体是否缓冲
     */
    fun isBuffering(): Boolean

    /**
     * 判断传入的音乐是不是正在播放的音乐
     */
    fun isCurrMusicIsPlayingMusic(songId: String): Boolean

    /**
     * 判断传入的音乐是否正在播放
     */
    fun isCurrMusicIsPlaying(songId: String): Boolean

    /**
     * 判断传入的音乐是否正在暂停
     */
    fun isCurrMusicIsPaused(songId: String): Boolean

    /**
     * 判断传入的音乐是否空闲
     */
    fun isCurrMusicIsIdea(songId: String): Boolean

    /**
     * 判断传入的音乐是否缓冲
     */
    fun isCurrMusicIsBuffering(songId: String): Boolean

    /**
     * 设置音量
     */
    fun setVolume(audioVolume: Float)

    /**
     * 获取音量
     */
    fun getVolume(): Float

    /**
     * 获取媒体时长，单位毫秒
     */
    fun getDuration(): Long

    /**
     * 获取 AudioSessionId
     */
    fun getAudioSessionId(): Int

    /**
     * 扫描本地媒体信息
     */
    fun querySongInfoInLocal(context: Context): List<SongInfo>

    /**
     * 添加一个状态监听
     */
    fun addPlayerEventListener(listener: OnPlayerEventListener?)

    /**
     * 删除一个状态监听
     */
    fun removePlayerEventListener(listener: OnPlayerEventListener?)

    /**
     * 删除所有状态监听
     */
    fun clearPlayerEventListener()

    /**
     * 焦点变化监听
     */
    fun focusStateChange(): MutableLiveData<Int>

    fun getPlayerEventListeners(): MutableList<OnPlayerEventListener>

    fun playbackState(): MutableLiveData<PlaybackStage>
}