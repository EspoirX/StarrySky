package com.lzx.starrysky.manager;


import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;

import com.lzx.starrysky.model.MusicProvider;
import com.lzx.starrysky.model.SongInfo;
import com.lzx.starrysky.notification.NotificationConstructor;
import com.lzx.starrysky.notification.factory.INotification;
import com.lzx.starrysky.playback.ExoPlayback;
import com.lzx.starrysky.playback.Playback;
import com.lzx.starrysky.playback.download.ExoDownload;
import com.lzx.starrysky.utils.MD5;
import com.lzx.starrysky.utils.imageloader.ILoaderStrategy;
import com.lzx.starrysky.utils.imageloader.ImageLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 用户操作管理类
 */
public class MusicManager {

    private static Context sContext;
    private NotificationConstructor mConstructor;
    private CopyOnWriteArrayList<OnPlayerEventListener> mPlayerEventListeners = new CopyOnWriteArrayList<>();
    private Playback mPlayback;

    public static MusicManager getInstance() {
        return SingletonHolder.sInstance;
    }

    private static class SingletonHolder {
        private static final MusicManager sInstance = new MusicManager();
    }

    /**
     * 在Application调用
     */
    public static void initMusicManager(Context context) {
        sContext = context;
        ExoDownload.initExoDownload(sContext);
        MediaSessionConnection.initConnection(sContext);
    }

    /**
     * 设置图片加载器
     */
    public static void setImageLoader(ILoaderStrategy loader) {
        ImageLoader.getInstance().setGlobalImageLoader(loader);
    }

    private MusicManager() {
    }

    /**
     * 释放资源，关闭程序时调用
     */
    public void onRelease() {
        clearPlayerEventListener();
        sContext = null;
        mPlayback = null;
        mConstructor = null;
    }

    /**
     * 设置通知栏配置,在Application创建并调用
     */
    public void setNotificationConstructor(NotificationConstructor constructor) {
        mConstructor = constructor;
    }

    public Playback getPlayback() {
        return mPlayback;
    }

    public void setPlayback(Playback playback) {
        mPlayback = playback;
    }

    /**
     * 获取通知栏配置，如果为 null ,则不创建通知栏
     *
     * @return
     */
    public NotificationConstructor getConstructor() {
        return mConstructor;
    }

    /**
     * 根据songId播放,调用前请确保已经设置了播放列表
     */
    public void playMusicById(String songId) {
        MediaSessionConnection connection = MediaSessionConnection.getInstance();
        if (connection.isConnected()) {
            if (MusicProvider.getInstance().hasSongInfo(songId)) {
                connection.getTransportControls().playFromMediaId(songId, null);
            }
        }
    }

    /**
     * 根据 SongInfo 播放，实际也是根据 songId 播放
     */
    public void playMusicByInfo(SongInfo info) {
        MediaSessionConnection connection = MediaSessionConnection.getInstance();
        if (connection.isConnected()) {
            MusicProvider.getInstance().addSongInfo(info);
            connection.getTransportControls().playFromMediaId(info.getSongId(), null);
        }
    }

    /**
     * 根据要播放的歌曲在播放列表中的下标播放,调用前请确保已经设置了播放列表
     */
    public void playMusicByIndex(int index) {
        MediaSessionConnection connection = MediaSessionConnection.getInstance();
        if (connection.isConnected()) {
            List<SongInfo> list = MusicProvider.getInstance().getSongInfos();
            if (list != null && index >= 0 && index < list.size()) {
                connection.getTransportControls().playFromMediaId(list.get(index).getSongId(), null);
            }
        }
    }

    /**
     * 播放
     *
     * @param songInfos 播放列表
     * @param index     要播放的歌曲在播放列表中的下标
     */
    public void playMusic(List<SongInfo> songInfos, int index) {
        MediaSessionConnection connection = MediaSessionConnection.getInstance();
        if (connection.isConnected()) {
            MusicProvider.getInstance().setSongInfos(songInfos);
            connection.getTransportControls().playFromMediaId(songInfos.get(index).getSongId(), null);
        }
    }

    /**
     * 暂停
     */
    public void pauseMusic() {
        MediaSessionConnection connection = MediaSessionConnection.getInstance();
        if (connection.isConnected()) {
            connection.getTransportControls().pause();
        }
    }

    /**
     * 恢复播放
     */
    public void playMusic() {
        MediaSessionConnection connection = MediaSessionConnection.getInstance();
        if (connection.isConnected()) {
            connection.getTransportControls().play();
        }
    }

    /**
     * 停止播放
     */
    public void stopMusic() {
        MediaSessionConnection connection = MediaSessionConnection.getInstance();
        if (connection.isConnected()) {
            connection.getTransportControls().stop();
        }
    }

    /**
     * 准备播放
     */
    public void prepare() {
        MediaSessionConnection connection = MediaSessionConnection.getInstance();
        if (connection.isConnected()) {
            connection.getTransportControls().prepare();
        }
    }

    /**
     * 准备播放，根据songId
     */
    public void prepareFromSongId(String songId) {
        MediaSessionConnection connection = MediaSessionConnection.getInstance();
        if (connection.isConnected()) {
            connection.getTransportControls().prepareFromMediaId(songId, null);
        }
    }

    /**
     * 下一首
     */
    public void skipToNext() {
        MediaSessionConnection connection = MediaSessionConnection.getInstance();
        if (connection.isConnected()) {
            connection.getTransportControls().skipToNext();
        }
    }

    /**
     * 上一首
     */
    public void skipToPrevious() {
        MediaSessionConnection connection = MediaSessionConnection.getInstance();
        if (connection.isConnected()) {
            connection.getTransportControls().skipToPrevious();
        }
    }

    /**
     * 开始快进，每调一次加 0.5 倍
     */
    public void fastForward() {
        MediaSessionConnection connection = MediaSessionConnection.getInstance();
        if (connection.isConnected()) {
            connection.getTransportControls().fastForward();
        }
    }

    /**
     * 开始倒带 每调一次减 0.5 倍，最小为 0
     */
    public void rewind() {
        MediaSessionConnection connection = MediaSessionConnection.getInstance();
        if (connection.isConnected()) {
            connection.getTransportControls().rewind();
        }
    }

    /**
     * 指定语速,通过此方法可配置任意倍速，注意结果要大于0
     *
     * @param refer    refer 是否已当前速度为基数
     * @param multiple multiple 倍率
     */
    public void onDerailleur(boolean refer, float multiple) {
        MediaSessionConnection connection = MediaSessionConnection.getInstance();
        if (connection.isConnected()) {
            Bundle bundle = new Bundle();
            bundle.putBoolean("refer", refer);
            bundle.putFloat("multiple", multiple);
            connection.getMediaController().sendCommand(ExoPlayback.ACTION_DERAILLEUR, bundle, null);
        }
    }

    /**
     * 移动到媒体流中的新位置,以毫秒为单位。
     */
    public void seekTo(long pos) {
        MediaSessionConnection connection = MediaSessionConnection.getInstance();
        if (connection.isConnected()) {
            connection.getTransportControls().seekTo(pos);
        }
    }

    /**
     * 设置播放模式
     * 必须是以下之一：
     * PlaybackStateCompat.SHUFFLE_MODE_NONE 顺序播放
     * PlaybackStateCompat.SHUFFLE_MODE_ALL  随机播放
     */
    public void setShuffleMode(int shuffleMode) {
        MediaSessionConnection connection = MediaSessionConnection.getInstance();
        if (connection.isConnected()) {
            connection.getTransportControls().setShuffleMode(shuffleMode);
        }
    }

    /**
     * 获取播放模式
     */
    public int getShuffleMode() {
        MediaSessionConnection connection = MediaSessionConnection.getInstance();
        if (connection.isConnected()) {
            return connection.getMediaController().getShuffleMode();
        }
        return -1;
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
        MediaSessionConnection connection = MediaSessionConnection.getInstance();
        if (connection.isConnected()) {
            connection.getTransportControls().setRepeatMode(repeatMode);
        }
    }

    /**
     * 获取播放模式,默认顺序播放
     */
    public int getRepeatMode() {
        MediaSessionConnection connection = MediaSessionConnection.getInstance();
        if (connection.isConnected()) {
            return connection.getMediaController().getRepeatMode();
        }
        return -1;
    }

    /**
     * 获取播放列表
     */
    public List<SongInfo> getPlayList() {
        return MusicProvider.getInstance().getSongInfos();
    }

    /**
     * 更新播放列表
     */
    public void updatePlayList(List<SongInfo> songInfos) {
        MediaSessionConnection connection = MediaSessionConnection.getInstance();
        if (connection.isConnected()) {
            MusicProvider.getInstance().setSongInfos(songInfos);
        }
    }

    /**
     * 获取当前播放的歌曲信息
     */
    public SongInfo getNowPlayingSongInfo() {
        SongInfo songInfo = null;
        MediaSessionConnection connection = MediaSessionConnection.getInstance();
        if (connection.isConnected()) {
            MediaMetadataCompat metadataCompat = connection.getNowPlaying();
            if (metadataCompat != null) {
                String songId = metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
                songInfo = MusicProvider.getInstance().getSongInfo(songId);
                //播放列表改变了或者清空了，如果还在播放歌曲，这时候 getSongInfo 就会获取不到，
                //此时需要从 metadataCompat 中获取
                if (songInfo == null && !TextUtils.isEmpty(songId)) {
                    songInfo = getSongInfoFromMediaMetadata(metadataCompat);
                }
            }
        }
        return songInfo;
    }

    private SongInfo getSongInfoFromMediaMetadata(MediaMetadataCompat metadata) {
        SongInfo songInfo = new SongInfo();
        songInfo.setSongId(metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID));
        songInfo.setSongUrl(metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI));
        songInfo.setAlbumName(metadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM));
        songInfo.setArtist(metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST));
        songInfo.setDuration(metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION));
        songInfo.setGenre(metadata.getString(MediaMetadataCompat.METADATA_KEY_GENRE));
        songInfo.setSongCover(metadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI));
        songInfo.setAlbumCover(metadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI));
        songInfo.setSongName(metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE));
        songInfo.setTrackNumber((int) metadata.getLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER));
        songInfo.setAlbumSongCount((int) metadata.getLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS));
        songInfo.setSongCoverBitmap(metadata.getBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART));
        return songInfo;
    }

    /**
     * 获取当前播放的歌曲songId
     */
    public String getNowPlayingSongId() {
        String songId = "";
        MediaSessionConnection connection = MediaSessionConnection.getInstance();
        if (connection.isConnected()) {
            MediaMetadataCompat metadataCompat = connection.getNowPlaying();
            if (metadataCompat != null) {
                songId = metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
            }
        }
        return songId;
    }

    /**
     * 获取当前播放歌曲的下标
     */
    public int getNowPlayingIndex() {
        int index = -1;
        String songId = getNowPlayingSongId();
        if (!TextUtils.isEmpty(songId)) {
            index = MusicProvider.getInstance().getIndexBySongInfo(songId);
        }
        return index;
    }

    /**
     * 以ms为单位获取当前缓冲的位置。
     */
    public long getBufferedPosition() {
        MediaSessionConnection connection = MediaSessionConnection.getInstance();
        if (connection.isConnected()) {
            return mPlayback != null ? mPlayback.getBufferedPosition() : 0;
        } else {
            return 0;
        }
    }

    /**
     * 获取播放位置 毫秒为单位。
     */
    public long getPlayingPosition() {
        MediaSessionConnection connection = MediaSessionConnection.getInstance();
        if (connection.isConnected()) {
            return mPlayback != null ? mPlayback.getCurrentStreamPosition() : 0;
        } else {
            return 0;
        }
    }

    /**
     * 是否有下一首
     */
    public boolean isSkipToNextEnabled() {
        MediaSessionConnection connection = MediaSessionConnection.getInstance();
        if (connection.isConnected()) {
            return (connection.getPlaybackState().getActions() & PlaybackStateCompat.ACTION_SKIP_TO_NEXT) != 0;
        } else {
            return false;
        }
    }

    /**
     * 是否有上一首
     */
    public boolean isSkipToPreviousEnabled() {
        MediaSessionConnection connection = MediaSessionConnection.getInstance();
        if (connection.isConnected()) {
            return (connection.getPlaybackState().getActions() & PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS) != 0;
        } else {
            return false;
        }
    }

    /**
     * 将当前播放速度作为正常播放的倍数。 倒带时这应该是负数， 值为1表示正常播放，0表示暂停。
     */
    public float getPlaybackSpeed() {
        MediaSessionConnection connection = MediaSessionConnection.getInstance();
        if (connection.isConnected()) {
            return connection.getPlaybackState().getPlaybackSpeed();
        } else {
            return -1;
        }
    }

    /**
     * 获取底层框架{@link android.media.session.PlaybackState}对象。
     * 此方法仅在API 21+上受支持。
     */
    public Object getPlaybackState() {
        MediaSessionConnection connection = MediaSessionConnection.getInstance();
        if (connection.isConnected()) {
            return connection.getPlaybackState().getPlaybackState();
        } else {
            return null;
        }
    }

    /**
     * 获取发送错误时的错误信息
     */
    public CharSequence getErrorMessage() {
        MediaSessionConnection connection = MediaSessionConnection.getInstance();
        if (connection.isConnected()) {
            return connection.getPlaybackState().getErrorMessage();
        } else {
            return "connection is not connect";
        }
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
        MediaSessionConnection connection = MediaSessionConnection.getInstance();
        if (connection.isConnected()) {
            return connection.getPlaybackState().getErrorCode();
        } else {
            return -1;
        }
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
        MediaSessionConnection connection = MediaSessionConnection.getInstance();
        if (connection.isConnected()) {
            return connection.getPlaybackState().getState();
        } else {
            return -1;
        }
    }

    /**
     * 比较方便的判断当前媒体是否在播放
     */
    public boolean isPlaying() {
        return getState() == PlaybackStateCompat.STATE_PLAYING;
    }

    /**
     * 比较方便的判断当前媒体是否暂停中
     */
    public boolean isPaused() {
        return getState() == PlaybackStateCompat.STATE_PAUSED;
    }

    /**
     * 比较方便的判断当前媒体是否空闲
     */
    public boolean isIdea() {
        return getState() == PlaybackStateCompat.STATE_NONE;
    }

    /**
     * 判断传入的音乐是不是正在播放的音乐
     */
    public boolean isCurrMusicIsPlayingMusic(String songId) {
        if (TextUtils.isEmpty(songId)) {
            return false;
        } else {
            SongInfo playingMusic = getNowPlayingSongInfo();
            return playingMusic != null && songId.equals(playingMusic.getSongId());
        }
    }

    /**
     * 判断传入的音乐是否正在播放
     */
    public boolean isCurrMusicIsPlaying(String songId) {
        return isCurrMusicIsPlayingMusic(songId) && isPlaying();
    }

    /**
     * 判断传入的音乐是否正在暂停
     */
    public boolean isCurrMusicIsPaused(String songId) {
        return isCurrMusicIsPlayingMusic(songId) && isPaused();
    }

    /**
     * 设置音量
     */
    public void setVolume(float audioVolume) {
        MediaSessionConnection connection = MediaSessionConnection.getInstance();
        if (connection.isConnected()) {
            if (audioVolume < 0) {
                audioVolume = 0;
            }
            if (audioVolume > 1) {
                audioVolume = 1;
            }
            Bundle bundle = new Bundle();
            bundle.putFloat("AudioVolume", audioVolume);
            connection.getMediaController().sendCommand(ExoPlayback.ACTION_CHANGE_VOLUME, bundle, null);
        }
    }

    /**
     * 获取音量
     */
    public float getVolume() {
        MediaSessionConnection connection = MediaSessionConnection.getInstance();
        if (connection.isConnected()) {
            return mPlayback != null ? mPlayback.getVolume() : -1;
        } else {
            return -1;
        }
    }

    /**
     * 获取媒体时长，单位毫秒
     */
    public long getDuration() {
        long duration = -1;
        MediaSessionConnection connection = MediaSessionConnection.getInstance();
        if (connection.isConnected()) {
            duration = connection.getNowPlaying().getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
            //如果没设置duration
            if (duration == 0) {
                if (mPlayback != null) {
                    duration = mPlayback.getDuration();
                }
            }
        }
        return duration;
    }

    /**
     * 更新通知栏喜欢或收藏按钮UI
     */
    public void updateFavoriteUI(boolean isFavorite) {
        MediaSessionConnection connection = MediaSessionConnection.getInstance();
        if (connection.isConnected()) {
            Bundle bundle = new Bundle();
            bundle.putBoolean("isFavorite", isFavorite);
            connection.getMediaController().sendCommand(INotification.ACTION_UPDATE_FAVORITE_UI, bundle, null);
        }
    }

    /**
     * 更新通知栏歌词按钮UI
     */
    public void updateLyricsUI(boolean isChecked) {
        MediaSessionConnection connection = MediaSessionConnection.getInstance();
        if (connection.isConnected()) {
            Bundle bundle = new Bundle();
            bundle.putBoolean("isChecked", isChecked);
            connection.getMediaController().sendCommand(INotification.ACTION_UPDATE_LYRICS_UI, bundle, null);
        }
    }

    /**
     * 扫描本地媒体信息
     */
    public List<SongInfo> querySongInfoInLocal() {
        List<SongInfo> songInfos = new ArrayList<>();
        Cursor cursor = sContext.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null,
                null, null);
        if (cursor == null) {
            return songInfos;
        }
        while (cursor.moveToNext()) {
            SongInfo song = new SongInfo();
            song.setAlbumId(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM_ID)));
            song.setAlbumCover(getAlbumArtPicPath(sContext, song.getAlbumId()));
            song.setSongNameKey(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.TITLE_KEY)));
            song.setArtistKey(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ARTIST_KEY)));
            song.setAlbumNameKey(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM_KEY)));
            song.setArtist(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ARTIST)));
            song.setAlbumName(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM)));
            song.setSongUrl(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATA)));
            song.setDescription(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DISPLAY_NAME)));
            song.setSongName(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.TITLE)));
            song.setMimeType(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.MIME_TYPE)));
            song.setYear(String.valueOf(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.YEAR))));
            song.setDuration(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DURATION)));
            song.setSize(String.valueOf(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.SIZE))));
            song.setPublishTime(String.valueOf(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATE_ADDED))));
            song.setModifiedTime(String.valueOf(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATE_MODIFIED))));
            String songId = !TextUtils.isEmpty(song.getSongUrl()) ? MD5.hexdigest(song.getSongUrl())
                    : MD5.hexdigest(String.valueOf(System.currentTimeMillis()));
            song.setSongId(songId);
            songInfos.add(song);
        }
        cursor.close();
        return songInfos;
    }

    private synchronized String getAlbumArtPicPath(Context context, String albumId) {
        // 小米应用商店检测crash ，错误信息：[31188,0,com.duan.musicoco,13155908,java.lang.IllegalStateException,Unknown URL: content://media/external/audio/albums/null,Parcel.java,1548]
        if (TextUtils.isEmpty(albumId)) {
            return null;
        }
        String[] projection = {MediaStore.Audio.Albums.ALBUM_ART};
        String imagePath = null;
        Uri uri = Uri.parse("content://media" + MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI.getPath() + "/" + albumId);
        Cursor cur = context.getContentResolver().query(uri, projection, null, null, null);
        if (cur == null) {
            return null;
        }
        if (cur.getCount() > 0 && cur.getColumnCount() > 0) {
            cur.moveToNext();
            imagePath = cur.getString(0);
        }
        cur.close();
        return imagePath;
    }

    /**
     * 添加一个状态监听
     */
    public void addPlayerEventListener(OnPlayerEventListener listener) {
        if (listener != null) {
            if (!mPlayerEventListeners.contains(listener)) {
                mPlayerEventListeners.add(listener);
            }
        }
    }

    /**
     * 删除一个状态监听
     */
    public void removePlayerEventListener(OnPlayerEventListener listener) {
        if (listener != null) {
            mPlayerEventListeners.remove(listener);
        }
    }

    /**
     * 删除所有状态监听
     */
    public void clearPlayerEventListener() {
        mPlayerEventListeners.clear();
    }

    public CopyOnWriteArrayList<OnPlayerEventListener> getPlayerEventListeners() {
        return mPlayerEventListeners;
    }
}
