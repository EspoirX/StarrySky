package com.lzx.starrysky.control;

import com.lzx.starrysky.BaseMediaInfo;
import com.lzx.starrysky.StarrySky;
import com.lzx.starrysky.provider.MediaQueueProvider;
import com.lzx.starrysky.provider.SongInfo;

import java.util.List;

public class IndexMediaLoader implements MediaLoader {

    private MediaQueueProvider mediaQueueProvider;
    private int index;

    public IndexMediaLoader(int index) {
        StarrySky starrySky = StarrySky.get();
        mediaQueueProvider = starrySky.getMediaQueueProvider();
        this.index = index;
    }

    @Override
    public BaseMediaInfo getMediaInfo() {
        BaseMediaInfo baseMediaInfo = new BaseMediaInfo();
        List<SongInfo> list = mediaQueueProvider.getSongInfos();
        if (list.size() == 0) {
            throw new IllegalStateException("the song list is empty");
        }
        if (index < 0 || index >= list.size()) {
            throw new IndexOutOfBoundsException("index out of MediaInfoList");
        }
        SongInfo songInfo = list.get(index);
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
