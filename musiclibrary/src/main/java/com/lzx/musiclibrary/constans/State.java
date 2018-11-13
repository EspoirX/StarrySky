package com.lzx.musiclibrary.constans;

/**
 * lzx
 * 2018/2/3
 */

public class State {
    public final static int STATE_IDLE = 1; //空闲
    public final static int STATE_ASYNC_LOADING = 2; //加载中
    public final static int STATE_PLAYING = 3; //播放中
    public final static int STATE_PAUSED = 4; //暂停
    public final static int STATE_ERROR = 5; //播放错误
    public final static int STATE_STOP = 6; //停止
}
