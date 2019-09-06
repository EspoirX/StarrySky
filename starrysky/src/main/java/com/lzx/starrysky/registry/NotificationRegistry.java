package com.lzx.starrysky.registry;

import com.lzx.starrysky.notification.NotificationConfig;
import com.lzx.starrysky.notification.StarrySkyNotificationManager;

public class NotificationRegistry {
    private NotificationConfig config;
    private StarrySkyNotificationManager notificationManager;


    NotificationRegistry() {

    }

    NotificationConfig getConfig() {
        return config;
    }

    void setConfig(NotificationConfig config) {
        this.config = config;
    }

    void setNotificationManager(StarrySkyNotificationManager notificationManager) {
        this.notificationManager = notificationManager;
    }

    StarrySkyNotificationManager getNotificationManager() {
        return notificationManager;
    }
}
