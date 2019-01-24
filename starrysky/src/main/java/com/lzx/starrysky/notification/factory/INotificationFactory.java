package com.lzx.starrysky.notification.factory;

import android.os.RemoteException;

public interface INotificationFactory {

    void createNotification();

    void startNotification();

    void stopNotification();

    INotification buildSystemNotification() throws RemoteException;

    INotification buildCustomNotification();
}
