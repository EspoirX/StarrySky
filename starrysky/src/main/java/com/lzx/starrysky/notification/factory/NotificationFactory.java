package com.lzx.starrysky.notification.factory;

import android.os.RemoteException;

import com.lzx.starrysky.MusicService;
import com.lzx.starrysky.notification.CustomNotification;
import com.lzx.starrysky.notification.NotificationBuilder;
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
            if (NotificationBuilder.getInstance().isCreateSystemNotification()) {
                mNotification = new SystemNotification(mMusicService);
            } else {
                mNotification = new CustomNotification(mMusicService);
            }
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
}
