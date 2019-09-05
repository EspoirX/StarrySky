package com.lzx.starrysky.control;

import com.lzx.starrysky.BaseMediaInfo;
import com.lzx.starrysky.StarrySky;
import com.lzx.starrysky.provider.MediaQueueProvider;
import com.lzx.starrysky.provider.SongInfo;

public class IdMediaLoader implements MediaLoader {
    private String songId;
    private MediaQueueProvider mediaQueueProvider;

    public IdMediaLoader(String songId) {
        this.songId = songId;
        StarrySky starrySky = StarrySky.get();
        mediaQueueProvider = starrySky.getMediaQueueProvider();
    }

    @Override
    public BaseMediaInfo getMediaInfo() {
        BaseMediaInfo baseMediaInfo = new BaseMediaInfo();
        SongInfo songInfo = null;
        if (mediaQueueProvider.hasSongInfo(songId)) {
            songInfo = mediaQueueProvider.getSongInfo(songId);
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
