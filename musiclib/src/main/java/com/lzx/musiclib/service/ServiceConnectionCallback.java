package com.lzx.musiclib.service;

/**
 * lzx
 */
public interface ServiceConnectionCallback {
    void onServiceConnected(MusicPlayService musicPlayService);
    void onServiceDisconnected();
}
