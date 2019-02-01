package com.lzx.starrysky.model;

import android.graphics.Bitmap;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;


/**
 * 媒体信息提供类
 */
public class MusicProvider {

    //使用Map在查找方面会效率高一点
    private ConcurrentMap<String, SongInfo> mSongInfoListById;
    private ConcurrentMap<String, MediaMetadataCompat> mMusicListById;

    public static MusicProvider getInstance() {
        return SingletonHolder.sInstance;
    }

    private static class SingletonHolder {
        private static final MusicProvider sInstance = new MusicProvider();
    }

    private MusicProvider() {
        mSongInfoListById = new ConcurrentHashMap<>();
        mMusicListById = new ConcurrentHashMap<>();
    }

    /**
     * 获取List#SongInfo
     */
    public List<SongInfo> getSongInfos() {
        return new ArrayList<>(mSongInfoListById.values());
    }

    /**
     * 设置播放列表
     */
    public synchronized void setSongInfos(List<SongInfo> songInfos) {
        for (SongInfo info : songInfos) {
            mSongInfoListById.put(info.getSongId(), info);
        }
        mMusicListById = toMediaMetadata(songInfos);
    }

    /**
     * 添加一首歌
     */
    public synchronized void addSongInfo(SongInfo songInfo) {
        mSongInfoListById.put(songInfo.getSongId(), songInfo);
        mMusicListById.put(songInfo.getSongId(), toMediaMetadata(songInfo));
    }

    /**
     * 根据检查是否有某首音频
     */
    public boolean hasSongInfo(String songId) {
        return mSongInfoListById.containsKey(songId);
    }

    /**
     * 根据songId获取SongInfo
     */
    public SongInfo getSongInfo(String songId) {
        if (mSongInfoListById.containsKey(songId)) {
            return mSongInfoListById.get(songId);
        } else {
            return null;
        }
    }

    /**
     * 根据songId获取索引
     */
    public int getIndexBySongInfo(String songId) {
        SongInfo songInfo = getSongInfo(songId);
        return songInfo != null ? getSongInfos().indexOf(songInfo) : -1;
    }

    /**
     * 获取List#MediaMetadataCompat
     */
    public List<MediaMetadataCompat> getMusicList() {
        return new ArrayList<>(mMusicListById.values());
    }

    /**
     * 获取 List#MediaBrowserCompat.MediaItem 用于 onLoadChildren 回调
     */
    public List<MediaBrowserCompat.MediaItem> getChildrenResult() {
        List<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<>();
        List<MediaMetadataCompat> list = new ArrayList<>(mMusicListById.values());
        for (MediaMetadataCompat metadata : list) {
            MediaBrowserCompat.MediaItem mediaItem = new MediaBrowserCompat.MediaItem(
                    metadata.getDescription(),
                    MediaBrowserCompat.MediaItem.FLAG_PLAYABLE);
            mediaItems.add(mediaItem);
        }
        return mediaItems;
    }

    /**
     * 获取乱序列表
     */
    public Iterable<MediaMetadataCompat> getShuffledMusic() {
        List<MediaMetadataCompat> shuffled = new ArrayList<>(mMusicListById.size());
        shuffled.addAll(mMusicListById.values());
        Collections.shuffle(shuffled);
        return shuffled;
    }

    /**
     * 根据id获取对应的MediaMetadataCompat对象
     */
    public MediaMetadataCompat getMusic(String songId) {
        return mMusicListById.containsKey(songId) ? mMusicListById.get(songId) : null;
    }

    /**
     * 更新封面art
     */
    public synchronized void updateMusicArt(String songId, MediaMetadataCompat changeData, Bitmap albumArt, Bitmap icon) {
        MediaMetadataCompat metadata = new MediaMetadataCompat.Builder(changeData)
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt)
                .putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, icon)
                .build();
        mMusicListById.put(songId, metadata);
    }

    /**
     * List<SongInfo> 转 ConcurrentMap<String, MediaMetadataCompat>
     */
    private synchronized static ConcurrentMap<String, MediaMetadataCompat> toMediaMetadata(List<SongInfo> songInfos) {
        ConcurrentMap<String, MediaMetadataCompat> map = new ConcurrentHashMap<>();
        for (SongInfo info : songInfos) {
            MediaMetadataCompat metadataCompat = toMediaMetadata(info);
            map.put(info.getSongId(), metadataCompat);
        }
        return map;
    }

    /**
     * SongInfo 转 MediaMetadataCompat
     */
    private synchronized static MediaMetadataCompat toMediaMetadata(SongInfo info) {
        String albumTitle = "";
        if (!TextUtils.isEmpty(info.getAlbumName())) {
            albumTitle = info.getAlbumName();
        } else if (!TextUtils.isEmpty(info.getSongName())) {
            albumTitle = info.getSongName();
        }
        String songCover = "";
        if (!TextUtils.isEmpty(info.getSongCover())) {
            songCover = info.getSongCover();
        } else if (!TextUtils.isEmpty(info.getAlbumCover())) {
            songCover = info.getAlbumCover();
        }
        MediaMetadataCompat.Builder builder = new MediaMetadataCompat.Builder();
        builder.putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, info.getSongId());
        builder.putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, info.getSongUrl());
        if (!TextUtils.isEmpty(albumTitle)) {
            builder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, albumTitle);
        }
        if (!TextUtils.isEmpty(info.getArtist())) {
            builder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, info.getArtist());
        }
        if (info.getDuration() != -1) {
            long durationMs = TimeUnit.SECONDS.toMillis(info.getDuration());
            builder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, durationMs);
        }
        if (!TextUtils.isEmpty(info.getGenre())) {
            builder.putString(MediaMetadataCompat.METADATA_KEY_GENRE, info.getGenre());
        }
        if (!TextUtils.isEmpty(songCover)) {
            builder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, songCover);
        }
        if (!TextUtils.isEmpty(info.getSongName())) {
            builder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, info.getSongName());
        }
        if (info.getTrackNumber() != -1) {
            builder.putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, info.getTrackNumber());
        }
        builder.putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, info.getAlbumSongCount());
        return builder.build();
    }
}
