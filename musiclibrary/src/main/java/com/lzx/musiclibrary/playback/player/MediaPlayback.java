package com.lzx.musiclibrary.playback.player;

import android.content.Context;
import android.media.MediaPlayer;

import com.lzx.musiclibrary.aidl.model.SongInfo;

/**
 * Created by xian on 2018/1/20.
 */

public class MediaPlayback implements Playback {

    private String mCurrentMediaId; //当前播放的媒体id

    private MediaPlayer mMediaPlayer;
    private Callback mCallback;
    private Context mContext;

    public MediaPlayback(Context context) {
        this.mContext = context;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop(boolean notifyListeners) {

    }

    @Override
    public void setState(int state) {

    }

    @Override
    public int getState() {
        return 0;
    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public boolean isPlaying() {
        return false;
    }

    @Override
    public long getCurrentStreamPosition() {
        return 0;
    }

    @Override
    public void updateLastKnownStreamPosition() {

    }

    @Override
    public void play(SongInfo info) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void seekTo(long position) {

    }

    @Override
    public void setCurrentMediaId(String mediaId) {
        mCurrentMediaId = mediaId;
    }

    @Override
    public String getCurrentMediaId() {
        return mCurrentMediaId;
    }

    @Override
    public void setCallback(Callback callback) {
        this.mCallback = callback;
    }
}
