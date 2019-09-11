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
package com.lzx.starrysky.playback.offline;

import android.app.Notification;

import com.google.android.exoplayer2.offline.DownloadManager;
import com.google.android.exoplayer2.offline.DownloadManager.TaskState;
import com.google.android.exoplayer2.offline.DownloadService;
import com.google.android.exoplayer2.scheduler.PlatformScheduler;
import com.google.android.exoplayer2.ui.DownloadNotificationUtil;
import com.google.android.exoplayer2.util.Util;
import com.lzx.starrysky.R;
import com.lzx.starrysky.StarrySky;

/**
 * 媒体下载服务
 */
public class ExoDownloadService extends DownloadService {


    private static final String CHANNEL_ID = "download_channel";
    private static final int JOB_ID = 1;
    private static final int FOREGROUND_NOTIFICATION_ID = 1;
    private StarrySkyCache mStarrySkyCache;

    /**
     * 传入FOREGROUND_NOTIFICATION_ID，是因为这样服务位于前台需要通知，并且要求服务位于前台以确保进程不会被终止
     * 如果使用FOREGROUND_NOTIFICATION_ID_NONE，则服务可能会被后台杀死
     */
    public ExoDownloadService() {
        super(
                //传入FOREGROUND_NOTIFICATION_ID_NONE，则下载时不会出现通知栏，如果想要通知栏，则传入FOREGROUND_NOTIFICATION_ID
                FOREGROUND_NOTIFICATION_ID_NONE,
                DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
                CHANNEL_ID,
                R.string.exo_download_notification_channel_name);
        mStarrySkyCache = StarrySky.get().getRegistry().getStarrySkyCacheManager().getStarrySkyCache(this);
    }

    @Override
    protected DownloadManager getDownloadManager() {
        if (mStarrySkyCache != null && mStarrySkyCache instanceof ExoCache) {
            ExoCache cache = (ExoCache) mStarrySkyCache;
            return cache.getDownloadManager();
        }
        throw new IllegalStateException("当前的缓存实现不是 ExoCache 或者 mStarrySkyCache 为 null");
    }

    @Override
    protected PlatformScheduler getScheduler() {
        return Util.SDK_INT >= 21 ? new PlatformScheduler(this, JOB_ID) : null;
    }

    @Override
    protected Notification getForegroundNotification(TaskState[] taskStates) {
        return DownloadNotificationUtil.buildProgressNotification(
                /* context= */ this,
                R.drawable.exo_controls_play,
                CHANNEL_ID,
                /* contentIntent= */ null,
                /* message= */ null,
                taskStates);
    }


    @Override
    protected void onTaskStateChanged(TaskState taskState) {
//        if (taskState.action.isRemoveAction) {
//            return;
//        }
//        Notification notification = null;
//        //下载完成时通知栏提示
//        if (taskState.state == TaskState.STATE_COMPLETED) {
//            notification =
//                    DownloadNotificationUtil.buildDownloadCompletedNotification(
//                            /* context= */ this,
//                            R.drawable.exo_controls_play,
//                            CHANNEL_ID,
//                            /* contentIntent= */ null,
//                            Util.fromUtf8Bytes(taskState.action.data));
//        } else if (taskState.state == TaskState.STATE_FAILED) {
//            //下载失败时通知栏提示
//            notification =
//                    DownloadNotificationUtil.buildDownloadFailedNotification(
//                            /* context= */ this,
//                            R.drawable.exo_controls_play,
//                            CHANNEL_ID,
//                            /* contentIntent= */ null,
//                            Util.fromUtf8Bytes(taskState.action.data));
//        }
//        int notificationId = FOREGROUND_NOTIFICATION_ID + 1 + taskState.taskId;
//        NotificationUtil.setNotification(this, notificationId, notification);
    }
}
