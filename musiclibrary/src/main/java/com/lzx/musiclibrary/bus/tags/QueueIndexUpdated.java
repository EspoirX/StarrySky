package com.lzx.musiclibrary.bus.tags;

/**
 * create by lzx
 * time:2018/11/2
 */
public class QueueIndexUpdated {
    public int index;
    public boolean isJustPlay;
    public boolean isSwitchMusic;

    public QueueIndexUpdated(int index, boolean isJustPlay, boolean isSwitchMusic) {
        this.index = index;
        this.isJustPlay = isJustPlay;
        this.isSwitchMusic = isSwitchMusic;
    }
}
