package com.lzx.musiclibrary.manager;

import android.os.Handler;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 定时任务
 *
 * @author lzx
 * @date 2018/2/1
 */
public class TimerTaskManager {

    private static final long PROGRESS_UPDATE_INTERNAL = 1000;
    private static final long PROGRESS_UPDATE_INITIAL_INTERVAL = 100;
    private final Handler mHandler = new Handler();
    private final ScheduledExecutorService mExecutorService = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> mScheduleFuture;
    private Runnable mUpdateProgressTask;

    public void scheduleSeekBarUpdate() {
        stopSeekBarUpdate();
        if (!mExecutorService.isShutdown()) {
            mScheduleFuture = mExecutorService.scheduleAtFixedRate(
                    new Runnable() {
                        @Override
                        public void run() {
                            if (mUpdateProgressTask != null) {
                                mHandler.post(mUpdateProgressTask);
                            }
                        }
                    },
                    PROGRESS_UPDATE_INITIAL_INTERVAL,
                    PROGRESS_UPDATE_INTERNAL,
                    TimeUnit.MILLISECONDS);
        }
    }

    public void setUpdateProgressTask(Runnable task) {
        mUpdateProgressTask = task;
    }


//    private final  Runnable mUpdateTimerTask = new Runnable() {
//        @Override
//        public void run() {
//
//        }
//    };
//
//    private CountDownTimer mCountDownTimer = new CountDownTimer() {
//        @Override
//        public void onTick(long millisUntilFinished) {
//
//        }
//
//        @Override
//        public void onFinish() {
//
//        }
//    }

    public void stopSeekBarUpdate() {
        if (mScheduleFuture != null) {
            mScheduleFuture.cancel(false);
        }
    }


    public void onRemoveUpdateProgressTask() {
        stopSeekBarUpdate();
        mExecutorService.shutdown();
        mHandler.removeCallbacksAndMessages(null);
    }


}
