package com.lzx.starrysky.utils

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

/**
 * 定时器
 */
class TimerTaskManager : LifecycleObserver {

    companion object {
        private const val PROGRESS_UPDATE_INTERNAL: Long = 1000
        private const val PROGRESS_UPDATE_INITIAL_INTERVAL: Long = 100
    }

    private val mExecutorService: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    private var mScheduleFuture: ScheduledFuture<*>? = null
    private var mUpdateProgressTask: Runnable? = null

    /**
     * 开始更新进度条
     */
    fun startToUpdateProgress(timeInternal: Long = PROGRESS_UPDATE_INTERNAL) {
        stopToUpdateProgress()
        if (!mExecutorService.isShutdown) {
            mScheduleFuture = mExecutorService.scheduleAtFixedRate({
                mUpdateProgressTask?.let { MainLooper.instance.post(it) }
            }, PROGRESS_UPDATE_INITIAL_INTERVAL,
                timeInternal,
                TimeUnit.MILLISECONDS)
        }
    }

    /**
     * 设置定时Runnable
     */
    fun setUpdateProgressTask(task: Runnable?) {
        mUpdateProgressTask = task
    }

    /**
     * 停止更新进度条
     */
    fun stopToUpdateProgress() {
        mScheduleFuture?.cancel(false)
    }

    /**
     * 释放资源
     */
    fun removeUpdateProgressTask() {
        stopToUpdateProgress()
        mExecutorService.shutdown()
        MainLooper.instance.removeCallbacksAndMessages(null)
    }

    /**
     * 绑定生命周期，onDestroy时自动释放
     */
    fun bindLifecycle(lifecycle: Lifecycle?) = apply {
        lifecycle?.removeObserver(this)
        lifecycle?.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private fun onDestroy() {
        removeUpdateProgressTask()
    }
}