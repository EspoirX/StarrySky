package com.lzx.starrysky.notification.factory;

import android.os.RemoteException;

import com.lzx.starrysky.MusicService;
import com.lzx.starrysky.notification.CustomNotification;
import com.lzx.starrysky.notification.NotificationConstructor;
import com.lzx.starrysky.notification.SystemNotification;

public class NotificationFactory implements INotificationFactory {

    private MusicService mMusicService;
    private INotification mNotification;
    private NotificationConstructor mConstructor;

    public NotificationFactory(MusicService musicService, NotificationConstructor constructor) {
        mMusicService = musicService;
        mConstructor = constructor;
    }

    @Override
    public void createNotification() {
        if (mConstructor == null) {
            return;
        }
        try {
            if (mConstructor.isCreateSystemNotification()) {
                mNotification = new SystemNotification(mMusicService, mConstructor);
            } else {
                mNotification = new CustomNotification(mMusicService, mConstructor);
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

    /**
     * 更新喜欢或收藏按钮UI
     */
    public void updateFavoriteUI(boolean isFavorite) {
        if (mNotification != null) {
            mNotification.updateFavoriteUI(isFavorite);
        }
    }

    /**
     * 更新歌词按钮UI
     */
    public void updateLyricsUI(boolean isChecked) {
        if (mNotification != null) {
            mNotification.updateLyricsUI(isChecked);
        }
    }


}
