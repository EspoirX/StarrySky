package com.lzx.starrysky.notification;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.lzx.starrysky.MusicService;
import com.lzx.starrysky.StarrySky;

public class StarrySkyNotificationManager {

    private INotification notification;
    private NotificationFactory factory;

    public StarrySkyNotificationManager(@Nullable NotificationFactory factory) {
        this.factory = factory != null ? factory : SYSTEM_NOTIFICATION_FACTORY;
    }

    public INotification getNotification(MusicService musicService) {
        if (notification == null) {
            synchronized (this) {
                if (notification == null) {
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
