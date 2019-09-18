package com.lzx.starrysky.delayaction;

import com.lzx.starrysky.provider.SongInfo;

public interface Action {
    void call(SongInfo songInfo);
}
