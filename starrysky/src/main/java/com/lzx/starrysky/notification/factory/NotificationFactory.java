package com.lzx.starrysky.notification.factory;

import android.os.RemoteException;

import com.lzx.starrysky.MusicService;
import com.lzx.starrysky.notification.SystemNotification;

public class NotificationFactory implements INotificationFactory {

    private MusicService mMusicService;
    private INotification mNotification;

    public NotificationFactory(MusicService musicService) {
        mMusicService = musicService;
    }

    @Override
    public void createNotification() {
        try {
            mNotification = new SystemNotification(mMusicService);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void startNotification() {
        if (mNotification != null) {
            mNotification.startNotification();
        }
    }

    @Override
    public void stopNotification() {
        if (mNotification != null) {
            mNotification.stopNotification();
        }
    }

    @Override
    public INotification buildSystemNotification() {
        try {
            return new SystemNotification(mMusicService);
        } catch (RemoteException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public INotification buildCustomNotification() {
        return null;
    }
}
