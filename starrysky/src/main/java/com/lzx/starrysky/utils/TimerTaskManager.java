package com.lzx.starrysky.utils;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 时间任务管理
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
    public void startToUpdateProgress() {
        stopToUpdateProgress();
        if (!mExecutorService.isShutdown()) {
            mScheduleFuture = mExecutorService.scheduleAtFixedRate(() -> {
                        if (mUpdateProgressTask != null) {
                            mHandler.post(mUpdateProgressTask);
                        }
                    },
                    PROGRESS_UPDATE_INITIAL_INTERVAL,
                    PROGRESS_UPDATE_INTERNAL,
                    TimeUnit.MILLISECONDS);
        }
    }

    /**
     * 设置定时Runnable
     */
    public void setUpdateProgressTask(Runnable task) {
        mUpdateProgressTask = task;
    }

    /**
     * 停止更新进度条
     */
    public void stopToUpdateProgress() {
        if (mScheduleFuture != null) {
            mScheduleFuture.cancel(false);
        }
    }

    /**
     * 释放资源
     */
    public void removeUpdateProgressTask() {
        stopToUpdateProgress();
        mExecutorService.shutdown();
        mHandler.removeCallbacksAndMessages(null);
    }


    /**
     * 开始倒计时
     */
    private long time = 0;

    public void startCountDownTask(final long millisInFuture, final OnCountDownFinishListener listener) {
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

    /**
     * 取消倒计时
     */
    public void cancelCountDownTask() {
        time = 0;
        if (mTimerHandler != null) {
            mTimerHandler.removeCallbacksAndMessages(null);
            mTimerHandler = null;
        }
        if (mTimerRunnable != null) {
            mTimerRunnable = null;
        }
    }

    /**
     * 倒计时监听
     */
    public interface OnCountDownFinishListener {
        void onFinish();

        void onTick(long millisUntilFinished);
    }
}
