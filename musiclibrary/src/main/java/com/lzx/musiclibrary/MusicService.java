package com.lzx.musiclibrary;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.Nullable;

import com.lzx.musiclibrary.control.PlayControl;

import java.lang.ref.WeakReference;

/**
 * Created by xian on 2018/1/20.
 */

public class MusicService extends Service {

    private static final int STOP_DELAY = 30000;
    //  private DelayedStopHandler mDelayedStopHandler;
    private PlayControl mBinder;
    private static MusicService mService;

    @Override
    public void onCreate() {
        super.onCreate();
        //  mDelayedStopHandler = new DelayedStopHandler(this);
        mService = this;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        boolean isUseMediaPlayer = intent.getBooleanExtra("isUseMediaPlayer", false);
        boolean isAutoPlayNext = intent.getBooleanExtra("isAutoPlayNext", true);
        boolean isCreateNotification = intent.getBooleanExtra("isCreateNotification", false);
        mBinder = new PlayControl
                .Builder(this)
                .setAutoPlayNext(isAutoPlayNext)
                .setUseMediaPlayer(isUseMediaPlayer)
                .setCreateNotification(isCreateNotification)
                .build();
        return mBinder;
    }

    public PlayControl getBinder() {
        return mBinder;
    }

    public static MusicService getService() {
        return mService;
    }
    //    @Override
//    public void onPlaybackStateUpdated(int state, PlaybackStateCompat newState) {
//
//        if (state == State.STATE_PLAYING) {
//
//            mDelayedStopHandler.removeCallbacksAndMessages(null);
//            startService(new Intent(getApplicationContext(), MusicService.class));
//        }
//        if (state == State.STATE_ERROR) {
//            mDelayedStopHandler.removeCallbacksAndMessages(null);
//            mDelayedStopHandler.sendEmptyMessageDelayed(0, STOP_DELAY);
//            stopForeground(true);
//        }
//    }

//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        mDelayedStopHandler.removeCallbacksAndMessages(null);
//        mDelayedStopHandler.sendEmptyMessageDelayed(0, STOP_DELAY);
//        return START_STICKY;
//    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            mBinder.stopMusic();
            mBinder.releaseMediaSession();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        //  mDelayedStopHandler.removeCallbacksAndMessages(null);
    }



    private static class DelayedStopHandler extends Handler {
        private final WeakReference<MusicService> mWeakReference;

        private DelayedStopHandler(MusicService service) {
            mWeakReference = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            MusicService service = mWeakReference.get();
            if (service != null && service.mBinder.getPlayback() != null) {
                if (service.mBinder.getPlayback().isPlaying()) {
                    return;
                }
                service.stopSelf();
            }
        }
    }


}
