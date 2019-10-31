package com.lzx.starrysky.control

import android.arch.lifecycle.MutableLiveData
import com.lzx.starrysky.common.PlaybackStage
import com.lzx.starrysky.provider.SongInfo

interface PlayerControl {

    /**
     * 根据songId播放,调用前请确保已经设置了播放列表
     */
    fun playMusicById(songId: String)

    /**
     * 根据 SongInfo 播放，实际也是根据 songId 播放
     */
    fun playMusicByInfo(info: SongInfo)

    /**
     * 保持播放列表里面只有一个音频
     */
    fun playMusicByInfoDirect(info: SongInfo)

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
    fun playMusic(songInfos: List<SongInfo>, index: Int)

    /**
     * 暂停
     */
    fun pauseMusic()

    /**
     * 恢复播放
     */
    fun playMusic()

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
     * 下一首
     */
    fun skipToNext()

    /**
     * 上一首
     */
    fun skipToPrevious()

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
     * PlaybackStateCompat.SHUFFLE_MODE_NONE 顺序播放
     * PlaybackStateCompat.SHUFFLE_MODE_ALL  随机播放
     */
    fun setShuffleMode(shuffleMode: Int)

    /**
     * 获取播放模式
     */
    fun getShuffleMode(): Int

    /**
     * 设置播放模式
     * 必须是以下之一：
     * PlaybackStateCompat.REPEAT_MODE_NONE  顺序播放
     * PlaybackStateCompat.REPEAT_MODE_ONE   单曲循环
     * PlaybackStateCompat.REPEAT_MODE_ALL   列表循环
     * PlaybackStateCompatExt.SINGLE_MODE_ONE   单曲播放(播放当前就结束,不会自动播下一首)
     */
    fun setRepeatMode(repeatMode: Int)

    /**
     * 获取播放模式,默认顺序播放
     */
    fun getRepeatMode(): Int

    /**
     * 获取播放列表
     */
    fun getPlayList(): List<SongInfo>

    /**
     * 更新播放列表
     */
    fun updatePlayList(songInfos: List<SongInfo>)

    /**
     * 添加一首歌
     */
    fun addSongInfo(info: SongInfo)

    /**
     * 删除
     */
    fun removeSongInfo(songId: String)

    /**
     * 获取当前播放的歌曲信息
     */
    fun getNowPlayingSongInfo(): SongInfo?

    /**
     * 获取当前播放的歌曲songId
     */
    fun getNowPlayingSongId(): String

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
     * 获取底层框架[android.media.session.PlaybackState]对象。
     * 此方法仅在API 21+上受支持。
     */
    fun getPlaybackState(): Any?

    /**
     * 获取发送错误时的错误信息
     */
    fun getErrorMessage(): CharSequence

    /**
     * 获取发送错误时的错误码
     * 0 : 这是默认的错误代码
     * 1 : 当应用程序状态无效以满足请求时的错误代码。
     * 2 : 应用程序不支持请求时的错误代码。
     * 3 : 由于身份验证已过期而无法执行请求时出现错误代码。
     * 4 : 成功请求需要高级帐户时的错误代码。
     * 5 : 检测到太多并发流时的错误代码。
     * 6 : 由于家长控制而阻止内容时出现错误代码。
     * 7 : 内容因区域不可用而被阻止时的错误代码。
     * 8 : 请求的内容已在播放时出现错误代码。
     * 9 : 当应用程序无法跳过任何更多歌曲时出现错误代码，因为已达到跳过限制。
     * 10: 由于某些外部事件而导致操作中断时的错误代码。
     * 11: 由于队列耗尽而无法播放导航（上一个，下一个）时出现错误代码。
     */
    fun getErrorCode(): Int

    /**
     * 获取当前的播放状态。 以下之一：
     * PlaybackStateCompat.STATE_NONE                   默认播放状态，表示尚未添加媒体，或者表示已重置且无内容可播放。
     * PlaybackStateCompat.STATE_STOPPED                当前已停止。
     * PlaybackStateCompat.STATE_PLAYING                正在播放
     * PlaybackStateCompat.STATE_PAUSED                 已暂停
     * PlaybackStateCompat.STATE_FAST_FORWARDING        当前正在快进
     * PlaybackStateCompat.STATE_REWINDING              当前正在倒带
     * PlaybackStateCompat.STATE_BUFFERING              当前正在缓冲
     * PlaybackStateCompat.STATE_ERROR                  当前处于错误状态
     * PlaybackStateCompat.STATE_CONNECTING             正在连接中
     * PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS   正在转跳到上一首
     * PlaybackStateCompat.STATE_SKIPPING_TO_NEXT       正在转跳到下一首
     * PlaybackStateCompat.STATE_SKIPPING_TO_QUEUE_ITEM 正在切歌
     */
    fun getState(): Int

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
    fun getAudioSessionId():Int

    /**
     * 更新通知栏喜欢或收藏按钮UI
     */
    fun updateFavoriteUI(isFavorite: Boolean)

    /**
     * 更新通知栏歌词按钮UI
     */
    fun updateLyricsUI(isChecked: Boolean)

    /**
     * 扫描本地媒体信息
     */
    fun querySongInfoInLocal(): List<SongInfo>

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

    fun getPlayerEventListeners(): MutableList<OnPlayerEventListener>

    fun playbackState(): MutableLiveData<PlaybackStage>
}