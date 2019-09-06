package com.lzx.starrysky.provider;

import android.graphics.Bitmap;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.text.TextUtils;

import com.lzx.starrysky.BaseMediaInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;


/**
 * 媒体信息提供类
 */
public class MediaQueueProviderImpl implements MediaQueueProvider {

    //使用Map在查找方面会效率高一点
    private LinkedHashMap<String, BaseMediaInfo> mediaListMap;
    private LinkedHashMap<String, MediaMetadataCompat> mMediaMetadataCompatMap;
    private List<BaseMediaInfo> mediaList;
    private LinkedHashMap<String, SongInfo> songListMap;
    private List<SongInfo> songList;

    public MediaQueueProviderImpl() {
        mediaListMap = new LinkedHashMap<>();
        mMediaMetadataCompatMap = new LinkedHashMap<>();
        songListMap = new LinkedHashMap<>();
        mediaList = new ArrayList<>();
        songList = new ArrayList<>();
    }

    @Override
    public List<BaseMediaInfo> getMediaList() {
        return mediaList;
    }

    @Override
    public List<SongInfo> getSongList() {
        return songList;
    }

    @Override
    public void updateMediaList(List<BaseMediaInfo> mediaInfoList) {
        mediaList.clear();
        mediaListMap.clear();
        mediaList.addAll(mediaInfoList);
        for (BaseMediaInfo info : mediaInfoList) {
            mediaListMap.put(info.getMediaId(), info);
        }
    }

    @Override
    public void updateMediaListBySongInfo(List<SongInfo> songInfos) {
        songListMap.clear();
        mMediaMetadataCompatMap.clear();
        songList.clear();
        songList.addAll(songInfos);
        List<BaseMediaInfo> mediaInfos = new ArrayList<>();
        for (SongInfo songInfo : songInfos) {
            BaseMediaInfo mediaInfo = new BaseMediaInfo();
            mediaInfo.setMediaId(songInfo.getSongId());
            mediaInfo.setMediaTitle(songInfo.getSongName());
            mediaInfo.setMediaCover(songInfo.getSongCover());
            mediaInfo.setMediaUrl(songInfo.getSongUrl());
            mediaInfo.setDuration(songInfo.getDuration());
            mediaInfos.add(mediaInfo);
            songListMap.put(songInfo.getSongId(), songInfo);
        }
        mMediaMetadataCompatMap = toMediaMetadata(songInfos);
        updateMediaList(mediaInfos);
    }

    @Override
    public void addMediaInfo(BaseMediaInfo mediaInfo) {
        if (mediaInfo == null) {
            return;
        }
        if (!mediaList.contains(mediaInfo)) {
            mediaList.add(mediaInfo);
        }
        mediaListMap.put(mediaInfo.getMediaId(), mediaInfo);
    }

    @Override
    public boolean hasMediaInfo(String songId) {
        return mediaListMap.containsKey(songId);
    }

    @Override
    public BaseMediaInfo getMediaInfo(String songId) {
        if (TextUtils.isEmpty(songId)) {
            return null;
        }
        if (hasMediaInfo(songId)) {
            return mediaListMap.get(songId);
        } else {
            return null;
        }
    }

    @Override
    public BaseMediaInfo getMediaInfo(int index) {
        if (index < 0 || index >= mediaList.size()) {
            return null;
        }
        return mediaList.get(index);
    }

    @Override
    public SongInfo getSongInfo(int index) {
        if (index < 0 || index >= songList.size()) {
            return null;
        }
        return songList.get(index);
    }

    @Override
    public SongInfo getSongInfo(String songId) {
        if (TextUtils.isEmpty(songId)) {
            return null;
        }
        if (songListMap.containsKey(songId)) {
            return songListMap.get(songId);
        } else {
            return null;
        }
    }

    @Override
    public int getIndexByMediaId(String songId) {
        BaseMediaInfo info = getMediaInfo(songId);
        return info != null ? getMediaList().indexOf(info) : -1;
    }

    /**
     * 获取List#MediaMetadataCompat
     */
    @Override
    public List<MediaMetadataCompat> getMediaMetadataCompatList() {
        return new ArrayList<>(mMediaMetadataCompatMap.values());
    }

    /**
     * 获取 List#MediaBrowserCompat.MediaItem 用于 onLoadChildren 回调
     */
    @Override
    public List<MediaBrowserCompat.MediaItem> getChildrenResult() {
        List<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<>();
        List<MediaMetadataCompat> list = getMediaMetadataCompatList();
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
    @Override
    public Iterable<MediaMetadataCompat> getShuffledMediaMetadataCompat() {
        List<MediaMetadataCompat> shuffled = new ArrayList<>(mMediaMetadataCompatMap.values());
        Collections.shuffle(shuffled);
        return shuffled;
    }

    @Override
    public Iterable<BaseMediaInfo> getShuffledMediaInfo() {
        Collections.shuffle(mediaList);
        return mediaList;
    }

    /**
     * 根据id获取对应的MediaMetadataCompat对象
     */
    @Override
    public MediaMetadataCompat getMediaMetadataCompatById(String songId) {
        return mMediaMetadataCompatMap.containsKey(songId) ? mMediaMetadataCompatMap.get(songId) : null;
    }

    /**
     * 更新封面art
     */
    @Override
    public synchronized void updateMusicArt(String songId, MediaMetadataCompat changeData, Bitmap albumArt, Bitmap icon) {
        MediaMetadataCompat metadata = new MediaMetadataCompat.Builder(changeData)
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt)
                .putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, icon)
                .build();
        mMediaMetadataCompatMap.put(songId, metadata);
    }

    /**
     * List<SongInfo> 转 ConcurrentMap<String, MediaMetadataCompat>
     */
    private synchronized static LinkedHashMap<String, MediaMetadataCompat> toMediaMetadata(List<SongInfo> songInfos) {
        LinkedHashMap<String, MediaMetadataCompat> map = new LinkedHashMap<>();
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
            builder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, info.getDuration());
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
