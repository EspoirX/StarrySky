package com.lzx.starrysky.provider;

import android.graphics.Bitmap;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;

import java.util.List;

public class MediaQueueProviderSurface implements MediaQueueProvider {

    private MediaQueueProvider provider;

    public MediaQueueProviderSurface(MediaQueueProvider provider) {
        this.provider = provider;
    }

    @Override
    public List<SongInfo> getSongInfos() {
        return provider.getSongInfos();
    }

    @Override
    public void setSongInfos(List<SongInfo> songInfos) {
        provider.setSongInfos(songInfos);
    }

    @Override
    public void addSongInfo(SongInfo songInfo) {
        provider.addSongInfo(songInfo);
    }

    @Override
    public boolean hasSongInfo(String songId) {
        return provider.hasSongInfo(songId);
    }

    @Override
    public SongInfo getSongInfo(String songId) {
        return provider.getSongInfo(songId);
    }

    @Override
    public int getIndexBySongInfo(String songId) {
        return provider.getIndexBySongInfo(songId);
    }

    @Override
    public List<MediaMetadataCompat> getMusicList() {
        return provider.getMusicList();
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
    public Iterable<SongInfo> getShuffledSongInfo() {
        return provider.getSongInfos();
    }

    @Override
    public MediaMetadataCompat getMusic(String songId) {
        return provider.getMusic(songId);
    }

    @Override
    public void updateMusicArt(String songId, MediaMetadataCompat changeData, Bitmap albumArt, Bitmap icon) {
        provider.updateMusicArt(songId, changeData, albumArt, icon);
    }
}
