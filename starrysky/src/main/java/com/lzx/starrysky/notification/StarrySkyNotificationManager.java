package com.lzx.starrysky.notification;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.lzx.starrysky.MusicService;
import com.lzx.starrysky.StarrySky;

public class StarrySkyNotificationManager {

    private INotification notification;
    private NotificationFactory factory;
    private boolean isOpenNotification;

    public StarrySkyNotificationManager(boolean isOpenNotification, @Nullable NotificationFactory factory) {
        this.isOpenNotification = isOpenNotification;
        if (isOpenNotification) {
            this.factory = factory == null ? SYSTEM_NOTIFICATION_FACTORY : factory;
        }
    }

    public boolean isOpenNotification() {
        return isOpenNotification;
    }

    public INotification getNotification(MusicService musicService) {
        if (notification == null) {
            synchronized (this) {
                if (notification == null && factory != null) {
                    NotificationConfig config = StarrySky.get().getRegistry().getNotificationConfig();
                    notification = factory.build(musicService, config);
                }
            }
        }
        return notification;
    }


    public interface NotificationFactory {
        @NonNull
        INotification build(MusicService musicService, NotificationConfig config);
    }

    private static final NotificationFactory SYSTEM_NOTIFICATION_FACTORY = SystemNotification::new;

    public static final NotificationFactory CUSTOM_NOTIFICATION_FACTORY = CustomNotification::new;

}
