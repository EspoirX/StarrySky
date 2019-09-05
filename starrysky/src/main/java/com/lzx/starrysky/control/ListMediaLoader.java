package com.lzx.starrysky.control;

import com.lzx.starrysky.BaseMediaInfo;
import com.lzx.starrysky.StarrySky;
import com.lzx.starrysky.provider.MediaQueueProvider;
import com.lzx.starrysky.provider.SongInfo;

import java.util.List;

public class ListMediaLoader implements MediaLoader {

    private MediaQueueProvider mediaQueueProvider;
    private List<SongInfo> mediaList;
    private int index;


    public ListMediaLoader(List<SongInfo> mediaList, int index) {
        this.mediaList = mediaList;
        this.index = index;
        StarrySky starrySky = StarrySky.get();
        mediaQueueProvider = starrySky.getMediaQueueProvider();
    }

    @Override
    public BaseMediaInfo getMediaInfo() {
        BaseMediaInfo baseMediaInfo = new BaseMediaInfo();
        if (mediaList.size() == 0) {
            throw new IllegalStateException("the song list is empty");
        }
        if (index < 0 || index >= mediaList.size()) {
            throw new IndexOutOfBoundsException("index out of MediaInfoList");
        }
        mediaQueueProvider.setSongInfos(mediaList);
        SongInfo songInfo = mediaList.get(index);
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
