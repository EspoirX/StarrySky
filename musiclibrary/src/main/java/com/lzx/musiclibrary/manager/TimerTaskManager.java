package com.lzx.musiclibrary.manager;

import android.os.Handler;
import android.os.Looper;

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
    private Handler mTimerHandler;
    private Runnable mTimerRunnable;
    private final ScheduledExecutorService mExecutorService = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> mScheduleFuture;
    private Runnable mUpdateProgressTask;

    /**
     * 开始更新进度条
     */
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

    /**
     * 停止更新进度条
     */
    public void stopSeekBarUpdate() {
        if (mScheduleFuture != null) {
            mScheduleFuture.cancel(false);
        }
    }

    /**
     * 释放资源
     */
    public void onRemoveUpdateProgressTask() {
        stopSeekBarUpdate();
        mExecutorService.shutdown();
        mHandler.removeCallbacksAndMessages(null);
    }


    /**
     * 开始倒计时
     */
    private long time = 0;

    public void starCountDownTask(final long millisInFuture, final OnCountDownFinishListener listener) {
        if (mTimerHandler == null) {
            mTimerHandler = new Handler(Looper.getMainLooper());
        }
        if (millisInFuture != -1L && millisInFuture > 0L) {
            if (mTimerRunnable == null) {
                time = millisInFuture;
                mTimerRunnable = new Runnable() {
                    @Override
                    public void run() {
                        time = time - 1000L;
                        listener.onTick(time);
                        if (time <= 0L) {
                            listener.onFinish();
                            cancelCountDownTask();
                        } else {
                            mTimerHandler.postDelayed(mTimerRunnable, 1000L);
                        }
                    }
                };
            }
            mTimerHandler.postDelayed(mTimerRunnable, 1000L);
        }
    }

    public void cancelCountDownTask() {
        if (mTimerHandler != null) {
            mTimerHandler.removeCallbacksAndMessages(null);
            mTimerHandler = null;
        }
        if (mTimerRunnable != null) {
            mTimerRunnable = null;
        }
    }


    public interface OnCountDownFinishListener {
        void onFinish();

        void onTick(long millisUntilFinished);
    }


}
