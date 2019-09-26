/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lzx.starrysky.playback.offline

import android.app.Notification

import com.google.android.exoplayer2.offline.DownloadManager
import com.google.android.exoplayer2.offline.DownloadManager.TaskState
import com.google.android.exoplayer2.offline.DownloadService
import com.google.android.exoplayer2.scheduler.PlatformScheduler
import com.google.android.exoplayer2.ui.DownloadNotificationUtil
import com.google.android.exoplayer2.util.Util
import com.lzx.starrysky.R
import com.lzx.starrysky.StarrySky

/**
 * 媒体下载服务
 */
/**
 * 传入FOREGROUND_NOTIFICATION_ID，是因为这样服务位于前台需要通知，并且要求服务位于前台以确保进程不会被终止
 * 如果使用FOREGROUND_NOTIFICATION_ID_NONE，则服务可能会被后台杀死
 */
class ExoDownloadService : DownloadService(
    FOREGROUND_NOTIFICATION_ID_NONE,
    DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
    CHANNEL_ID,
    R.string.exo_download_notification_channel_name) {

    private val mStarrySkyCache: StarrySkyCache? by lazy {
        StarrySky.get().registry.starrySkyCacheManager.getStarrySkyCache(this)
    }

    override fun getDownloadManager(): DownloadManager? {
        if (mStarrySkyCache is ExoCache) {
            val cache = mStarrySkyCache as ExoCache
            return cache.getDownloadManager()
        }
        throw IllegalStateException("当前的缓存实现不是 ExoCache 或者 mStarrySkyCache 为 null")
    }

    override fun getScheduler(): PlatformScheduler? {
        return if (Util.SDK_INT >= 21) PlatformScheduler(this, JOB_ID) else null
    }

    override fun getForegroundNotification(taskStates: Array<TaskState>?): Notification {
        return DownloadNotificationUtil.buildProgressNotification(
            /* context= */ this,
            R.drawable.exo_controls_play,
            CHANNEL_ID, null, null,
            taskStates!!)/* contentIntent= */
    }

    override fun onTaskStateChanged(taskState: TaskState?) {
    }

    companion object {
        private const val CHANNEL_ID = "download_channel"
        private const val JOB_ID = 1
        private const val FOREGROUND_NOTIFICATION_ID = 1
    }
}
