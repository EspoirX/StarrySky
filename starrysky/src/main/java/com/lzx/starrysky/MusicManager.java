package com.lzx.starrysky;


import android.content.Context;

import com.lzx.starrysky.control.OnPlayerEventListener;
import com.lzx.starrysky.provider.SongInfo;
import com.lzx.starrysky.notification.NotificationConfig;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 用户操作管理类
 */
@Deprecated
public class MusicManager {

    private NotificationConfig mConstructor;

    public static MusicManager getInstance() {
        return SingletonHolder.sInstance;
    }

    private static class SingletonHolder {
        private static final MusicManager sInstance = new MusicManager();
    }


    /**
     * 在Application调用
     */
    @Deprecated
    public static void initMusicManager(Context context) {
        //Empty
    }

    private MusicManager() {
    }

    /**
     * 释放资源，关闭程序时调用
     */
    public void onRelease() {
        clearPlayerEventListener();
        mConstructor = null;
    }

    /**
     * 设置通知栏配置,在Application创建并调用
     */
    public void setNotificationConstructor(NotificationConfig constructor) {
        mConstructor = constructor;
    }

    /**
     * 获取通知栏配置，如果为 null ,则不创建通知栏
     *
     * @return
     */
    public NotificationConfig getConstructor() {
        return mConstructor;
    }

    /**
     * 根据songId播放,调用前请确保已经设置了播放列表
     */
    public void playMusicById(String songId) {
        StarrySky.with().playMusicById(songId);
    }

    /**
     * 根据 SongInfo 播放，实际也是根据 songId 播放
     */
    public void playMusicByInfo(SongInfo info) {
        StarrySky.with().playMusicByInfo(info);
    }

    /**
     * 根据要播放的歌曲在播放列表中的下标播放,调用前请确保已经设置了播放列表
     */
    public void playMusicByIndex(int index) {
        StarrySky.with().playMusicByIndex(index);
    }

    /**
     * 播放
     *
     * @param songInfos 播放列表
     * @param index     要播放的歌曲在播放列表中的下标
     */
    public void playMusic(List<SongInfo> songInfos, int index) {
        StarrySky.with().playMusic(songInfos, index);
    }

    /**
     * 暂停
     */
    public void pauseMusic() {
        StarrySky.with().pauseMusic();
    }

    /**
     * 恢复播放
     */
    public void playMusic() {
        StarrySky.with().playMusic();
    }

    /**
     * 停止播放
     */
    public void stopMusic() {
        StarrySky.with().stopMusic();
    }

    /**
     * 准备播放
     */
    public void prepare() {
        StarrySky.with().prepare();
    }

    /**
     * 准备播放，根据songId
     */
    public void prepareFromSongId(String songId) {
        StarrySky.with().prepareFromSongId(songId);
    }

    /**
     * 下一首
     */
    public void skipToNext() {
        StarrySky.with().skipToNext();
    }

    /**
     * 上一首
     */
    public void skipToPrevious() {
        StarrySky.with().skipToPrevious();
    }

    /**
     * 开始快进，每调一次加 0.5 倍
     */
    public void fastForward() {
        StarrySky.with().fastForward();
    }

    /**
     * 开始倒带 每调一次减 0.5 倍，最小为 0
     */
    public void rewind() {
        StarrySky.with().rewind();
    }

    /**
     * 指定语速,通过此方法可配置任意倍速，注意结果要大于0
     *
     * @param refer    refer 是否已当前速度为基数
     * @param multiple multiple 倍率
     */
    public void onDerailleur(boolean refer, float multiple) {
        StarrySky.with().onDerailleur(refer, multiple);
    }

    /**
     * 移动到媒体流中的新位置,以毫秒为单位。
     */
    public void seekTo(long pos) {
        StarrySky.with().seekTo(pos);
    }

    /**
     * 设置播放模式
     * 必须是以下之一：
     * PlaybackStateCompat.SHUFFLE_MODE_NONE 顺序播放
     * PlaybackStateCompat.SHUFFLE_MODE_ALL  随机播放
     */
    public void setShuffleMode(int shuffleMode) {
        StarrySky.with().setShuffleMode(shuffleMode);
    }

    /**
     * 获取播放模式
     */
    public int getShuffleMode() {
        return StarrySky.with().getShuffleMode();
    }

    /**
     * 设置播放模式
     * 必须是以下之一：
     * PlaybackStateCompat.REPEAT_MODE_NONE  顺序播放
     * PlaybackStateCompat.REPEAT_MODE_ONE   单曲循环
     * PlaybackStateCompat.REPEAT_MODE_ALL   列表循环
     * PlaybackStateCompatExt.SINGLE_MODE_ONE   单曲播放(播放当前就结束,不会自动播下一首)
     */
    public void setRepeatMode(int repeatMode) {
        StarrySky.with().setRepeatMode(repeatMode);
    }

    /**
     * 获取播放模式,默认顺序播放
     */
    public int getRepeatMode() {
        return StarrySky.with().getRepeatMode();
    }

    /**
     * 获取播放列表
     */
    public List<SongInfo> getPlayList() {
        return StarrySky.with().getPlayList();
    }

    /**
     * 更新播放列表
     */
    public void updatePlayList(List<SongInfo> songInfos) {
        StarrySky.with().updatePlayList(songInfos);
    }

    /**
     * 获取当前播放的歌曲信息
     */
    public SongInfo getNowPlayingSongInfo() {
        return StarrySky.with().getNowPlayingSongInfo();
    }

    /**
     * 获取当前播放的歌曲songId
     */
    public String getNowPlayingSongId() {
        return StarrySky.with().getNowPlayingSongId();
    }

    /**
     * 获取当前播放歌曲的下标
     */
    public int getNowPlayingIndex() {
        return StarrySky.with().getNowPlayingIndex();
    }

    /**
     * 以ms为单位获取当前缓冲的位置。
     */
    public long getBufferedPosition() {
        return StarrySky.with().getBufferedPosition();
    }

    /**
     * 获取播放位置 毫秒为单位。
     */
    public long getPlayingPosition() {
        return StarrySky.with().getPlayingPosition();
    }

    /**
     * 是否有下一首
     */
    public boolean isSkipToNextEnabled() {
        return StarrySky.with().isSkipToNextEnabled();
    }

    /**
     * 是否有上一首
     */
    public boolean isSkipToPreviousEnabled() {
        return StarrySky.with().isSkipToPreviousEnabled();
    }

    /**
     * 将当前播放速度作为正常播放的倍数。 倒带时这应该是负数， 值为1表示正常播放，0表示暂停。
     */
    public float getPlaybackSpeed() {
        return StarrySky.with().getPlaybackSpeed();
    }

    /**
     * 获取底层框架{@link android.media.session.PlaybackState}对象。
     * 此方法仅在API 21+上受支持。
     */
    public Object getPlaybackState() {
        return StarrySky.with().getPlaybackState();
    }

    /**
     * 获取发送错误时的错误信息
     */
    public CharSequence getErrorMessage() {
        return StarrySky.with().getErrorMessage();
    }

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
    public int getErrorCode() {
        return StarrySky.with().getErrorCode();
    }

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
    public int getState() {
        return StarrySky.with().getState();
    }

    /**
     * 比较方便的判断当前媒体是否在播放
     */
    public boolean isPlaying() {
        return StarrySky.with().isPlaying();
    }

    /**
     * 比较方便的判断当前媒体是否暂停中
     */
    public boolean isPaused() {
        return StarrySky.with().isPaused();
    }

    /**
     * 比较方便的判断当前媒体是否空闲
     */
    public boolean isIdea() {
        return StarrySky.with().isIdea();
    }

    /**
     * 判断传入的音乐是不是正在播放的音乐
     */
    public boolean isCurrMusicIsPlayingMusic(String songId) {
        return StarrySky.with().isCurrMusicIsPlayingMusic(songId);
    }

    /**
     * 判断传入的音乐是否正在播放
     */
    public boolean isCurrMusicIsPlaying(String songId) {
        return StarrySky.with().isCurrMusicIsPlaying(songId);
    }

    /**
     * 判断传入的音乐是否正在暂停
     */
    public boolean isCurrMusicIsPaused(String songId) {
        return StarrySky.with().isCurrMusicIsPaused(songId);
    }

    /**
     * 设置音量
     */
    public void setVolume(float audioVolume) {
        StarrySky.with().setVolume(audioVolume);
    }

    /**
     * 获取音量
     */
    public float getVolume() {
        return StarrySky.with().getVolume();
    }

    /**
     * 获取媒体时长，单位毫秒
     */
    public long getDuration() {
        return StarrySky.with().getDuration();
    }

    /**
     * 更新通知栏喜欢或收藏按钮UI
     */
    public void updateFavoriteUI(boolean isFavorite) {
        StarrySky.with().updateFavoriteUI(isFavorite);
    }

    /**
     * 更新通知栏歌词按钮UI
     */
    public void updateLyricsUI(boolean isChecked) {
        StarrySky.with().updateLyricsUI(isChecked);
    }

    /**
     * 扫描本地媒体信息
     */
    public List<SongInfo> querySongInfoInLocal() {
        return StarrySky.with().querySongInfoInLocal();
    }

    /**
     * 添加一个状态监听
     */
    public void addPlayerEventListener(OnPlayerEventListener listener) {
        StarrySky.with().addPlayerEventListener(listener);
    }

    /**
     * 删除一个状态监听
     */
    public void removePlayerEventListener(OnPlayerEventListener listener) {
        StarrySky.with().removePlayerEventListener(listener);
    }

    /**
     * 删除所有状态监听
     */
    public void clearPlayerEventListener() {
        StarrySky.with().clearPlayerEventListener();
    }

    public  List<OnPlayerEventListener> getPlayerEventListeners() {
        return StarrySky.with().getPlayerEventListeners();
    }
}
