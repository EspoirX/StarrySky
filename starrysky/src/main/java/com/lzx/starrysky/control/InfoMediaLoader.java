package com.lzx.starrysky.control;

import com.lzx.starrysky.BaseMediaInfo;
import com.lzx.starrysky.StarrySky;
import com.lzx.starrysky.provider.MediaQueueProvider;
import com.lzx.starrysky.provider.SongInfo;

import java.util.List;

public class InfoMediaLoader implements MediaLoader {

    private MediaQueueProvider mediaQueueProvider;
    private SongInfo info;

    public InfoMediaLoader(SongInfo info) {
        StarrySky starrySky = StarrySky.get();
        mediaQueueProvider = starrySky.getMediaQueueProvider();
        this.info = info;
    }

    @Override
    public BaseMediaInfo getMediaInfo() {
        BaseMediaInfo baseMediaInfo = new BaseMediaInfo();
        List<SongInfo> list = mediaQueueProvider.getSongInfos();
        if (list.size() == 0) {
            throw new IllegalStateException("the song list is empty");
        }
        if (info == null) {
            throw new IllegalStateException("media info is null");
        }
        SongInfo songInfo;
        if (mediaQueueProvider.hasSongInfo(info.getSongId())) {
            songInfo = mediaQueueProvider.getSongInfo(info.getSongId());
        } else {
            mediaQueueProvider.addSongInfo(info);
            songInfo = info;
        }
        if (songInfo != null) {
            baseMediaInfo.setMediaId(songInfo.getSongId());
            baseMediaInfo.setMediaUrl(songInfo.getSongUrl());
            baseMediaInfo.setMediaTitle(songInfo.getSongName());
            baseMediaInfo.setMediaCover(songInfo.getSongCover());
            baseMediaInfo.setDuration(songInfo.getDuration());
        }
        return baseMediaInfo;
    }
}
