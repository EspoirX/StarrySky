package com.lzx.starrysky.provider;

import android.graphics.Bitmap;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;

import com.lzx.starrysky.BaseMediaInfo;

import java.util.List;

/**
 * 数据提供类
 */
public class MediaQueueProviderSurface implements MediaQueueProvider {

    private MediaQueueProvider provider;

    public MediaQueueProviderSurface(MediaQueueProvider provider) {
        this.provider = provider;
    }

    @Override
    public List<BaseMediaInfo> getMediaList() {
        return provider.getMediaList();
    }

    @Override
    public List<SongInfo> getSongList() {
        return provider.getSongList();
    }

    @Override
    public void updateMediaList(List<BaseMediaInfo> mediaInfoList) {
        provider.updateMediaList(mediaInfoList);
    }

    @Override
    public void updateMediaListBySongInfo(List<SongInfo> songInfos) {
        provider.updateMediaListBySongInfo(songInfos);
    }

    @Override
    public void addMediaInfo(BaseMediaInfo mediaInfo) {
        provider.addMediaInfo(mediaInfo);
    }

    @Override
    public boolean hasMediaInfo(String songId) {
        return provider.hasMediaInfo(songId);
    }

    @Override
    public BaseMediaInfo getMediaInfo(String songId) {
        return provider.getMediaInfo(songId);
    }

    @Override
    public BaseMediaInfo getMediaInfo(int index) {
        return provider.getMediaInfo(index);
    }

    @Override
    public SongInfo getSongInfo(String songId) {
        return provider.getSongInfo(songId);
    }

    @Override
    public SongInfo getSongInfo(int index) {
        return provider.getSongInfo(index);
    }

    @Override
    public int getIndexByMediaId(String songId) {
        return provider.getIndexByMediaId(songId);
    }

    @Override
    public List<MediaMetadataCompat> getMediaMetadataCompatList() {
        return provider.getMediaMetadataCompatList();
    }

    @Override
    public List<MediaBrowserCompat.MediaItem> getChildrenResult() {
        return provider.getChildrenResult();
    }

    @Override
    public Iterable<MediaMetadataCompat> getShuffledMediaMetadataCompat() {
        return provider.getShuffledMediaMetadataCompat();
    }

    @Override
    public Iterable<BaseMediaInfo> getShuffledMediaInfo() {
        return provider.getShuffledMediaInfo();
    }

    @Override
    public MediaMetadataCompat getMediaMetadataCompatById(String songId) {
        return provider.getMediaMetadataCompatById(songId);
    }

    @Override
    public void updateMusicArt(String songId, MediaMetadataCompat changeData, Bitmap albumArt, Bitmap icon) {
        provider.updateMusicArt(songId, changeData, albumArt, icon);
    }

}
