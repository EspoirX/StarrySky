package com.example.musiclib.service;

import android.os.RemoteException;

import com.example.musiclib.model.MusicInfo;

/**
 * 远程服务
 * Created by xian on 2017/12/16.
 */

public class MusicPlayServiceStub extends IMusicPlayService.Stub {

    private MusicPlayService mService;

    public MusicPlayServiceStub(MusicPlayService musicPlayService) {
        this.mService = musicPlayService;
    }

    @Override
    public void playPause() throws RemoteException {
        mService.playPause();
    }

    @Override
    public void start() throws RemoteException {
        mService.start();
    }

    @Override
    public void pause() throws RemoteException {
        mService.pause();
    }

    @Override
    public void stop() throws RemoteException {
        mService.stop();
    }

    @Override
    public void prev() throws RemoteException {
        mService.prev();
    }

    @Override
    public void seekTo(int msec) throws RemoteException {
        mService.seekTo(msec);
    }

    @Override
    public boolean isPlaying() throws RemoteException {
        return mService.isPlaying();
    }

    @Override
    public boolean isPausing() throws RemoteException {
        return mService.isPausing();
    }

    @Override
    public boolean isPreparing() throws RemoteException {
        return mService.isPreparing();
    }

    @Override
    public boolean isIdle() throws RemoteException {
        return mService.isIdle();
    }

    @Override
    public void playByPosition(int position) throws RemoteException {
        mService.play(position);
    }

    @Override
    public void playByMusicInfo(MusicInfo music) throws RemoteException {
        mService.play(music);
    }

    @Override
    public int getPlayingPosition() throws RemoteException {
        return mService.getPlayingPosition();
    }

    @Override
    public void setPlayingPosition(int playingPosition) throws RemoteException {
        mService.setPlayingPosition(playingPosition);
    }

    @Override
    public void next() throws RemoteException {
        mService.next();
    }

    @Override
    public long getCurrentPosition() throws RemoteException {
        return mService.getCurrentPosition();
    }

    @Override
    public MusicInfo getPlayingMusic() throws RemoteException {
        return mService.getPlayingMusic();
    }

    @Override
    public void quit() throws RemoteException {
        mService.quit();
    }

    @Override
    public String getPlayMode() throws RemoteException {
        return mService.getPlayMode();
    }

    @Override
    public void setPlayMode(String playMode) throws RemoteException {
        mService.setPlayMode(playMode);
    }
}
