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
package com.lzx.starrysky.playback.download;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.offline.ActionFile;
import com.google.android.exoplayer2.offline.DownloadAction;
import com.google.android.exoplayer2.offline.DownloadHelper;
import com.google.android.exoplayer2.offline.DownloadManager;
import com.google.android.exoplayer2.offline.DownloadManager.TaskState;
import com.google.android.exoplayer2.offline.DownloadService;
import com.google.android.exoplayer2.offline.ProgressiveDownloadHelper;
import com.google.android.exoplayer2.offline.StreamKey;
import com.google.android.exoplayer2.offline.TrackKey;
import com.google.android.exoplayer2.source.TrackGroup;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.offline.DashDownloadHelper;
import com.google.android.exoplayer2.source.hls.offline.HlsDownloadHelper;
import com.google.android.exoplayer2.source.smoothstreaming.offline.SsDownloadHelper;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.util.Log;
import com.google.android.exoplayer2.util.Util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;


public class DownloadTracker implements DownloadManager.Listener, DownloadHelper.Callback {

    public interface Listener {
        void onDownloadsChanged();
    }

    private static final String TAG = "DownloadTracker";

    private final Context context;
    private final DataSource.Factory dataSourceFactory;
    private final CopyOnWriteArraySet<Listener> listeners;
    private final HashMap<Uri, DownloadAction> trackedDownloadStates;
    private final ActionFile actionFile;
    private final Handler actionFileWriteHandler;
    private DownloadHelper downloadHelper;
    private List<TrackKey> trackKeys;
    private String name;

    public DownloadTracker(Context context, DataSource.Factory dataSourceFactory, File actionFile,
                           DownloadAction.Deserializer... deserializers) {
        this.context = context.getApplicationContext();
        this.dataSourceFactory = dataSourceFactory;
        this.actionFile = new ActionFile(actionFile);
        listeners = new CopyOnWriteArraySet<>();
        trackedDownloadStates = new HashMap<>();
        trackKeys = new ArrayList<>();

        HandlerThread actionFileWriteThread = new HandlerThread("DownloadTracker");
        actionFileWriteThread.start();
        actionFileWriteHandler = new Handler(actionFileWriteThread.getLooper());
        loadTrackedActions(deserializers.length > 0 ? deserializers : DownloadAction.getDefaultDeserializers());
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    /**
     * 是否已经下载
     */
    public boolean isDownloaded(Uri uri) {
        return trackedDownloadStates.containsKey(uri);
    }

    @SuppressWarnings("unchecked")
    public List<StreamKey> getOfflineStreamKeys(Uri uri) {
        if (!trackedDownloadStates.containsKey(uri)) {
            return Collections.emptyList();
        } else {
            DownloadAction downloadAction = trackedDownloadStates.get(uri);
            return downloadAction != null ? downloadAction.getKeys() : Collections.emptyList();
        }
    }

    /**
     * 下载
     */
    public void toggleDownload(String name, Uri uri, String extension) {
        this.name = name;
        if (isDownloaded(uri)) {
            Log.i("xian", "--- 已经下载完了 ---");
            //DownloadAction removeAction = getDownloadHelper(uri, extension).getRemoveAction(Util.getUtf8Bytes(name));
            //startServiceWithAction(removeAction);
        } else {
            Log.i("xian", "--- 新下载 ---");
            downloadHelper = getDownloadHelper(uri, extension);
            downloadHelper.prepare(this);
        }
    }

    /**
     * 开始下载
     */
    private void startDownload() {
        DownloadAction downloadAction = downloadHelper.getDownloadAction(Util.getUtf8Bytes(name), trackKeys);
        if (trackedDownloadStates.containsKey(downloadAction.uri)) {
            return;
        }
        trackedDownloadStates.put(downloadAction.uri, downloadAction);
        handleTrackedDownloadStatesChanged();
        startServiceWithAction(downloadAction);
    }

    @Override
    public void onPrepared(DownloadHelper helper) {
        for (int i = 0; i < downloadHelper.getPeriodCount(); i++) {
            TrackGroupArray trackGroups = downloadHelper.getTrackGroups(i);
            for (int j = 0; j < trackGroups.length; j++) {
                TrackGroup trackGroup = trackGroups.get(j);
                for (int k = 0; k < trackGroup.length; k++) {
                    trackKeys.add(new TrackKey(i, j, k));
                }
            }
        }
        startDownload();
    }

    @Override
    public void onPrepareError(DownloadHelper helper, IOException e) {
        Log.e(TAG, "Failed to start download", e);
    }

    // ExoDownload.Listener
    @Override
    public void onInitialized(DownloadManager downloadManager) {
        // Do nothing.
    }

    @Override
    public void onTaskStateChanged(DownloadManager downloadManager, TaskState taskState) {
        DownloadAction action = taskState.action;
        Uri uri = action.uri;
        if ((action.isRemoveAction && taskState.state == TaskState.STATE_COMPLETED)
                || (!action.isRemoveAction && taskState.state == TaskState.STATE_FAILED)) {
            // A download has been removed, or has failed. Stop tracking it.
            if (trackedDownloadStates.remove(uri) != null) {
                handleTrackedDownloadStatesChanged();
            }
        }
    }

    @Override
    public void onIdle(DownloadManager downloadManager) {
        // Do nothing.
    }

    // Internal methods
    private void loadTrackedActions(DownloadAction.Deserializer[] deserializers) {
        try {
            DownloadAction[] allActions = actionFile.load(deserializers);
            for (DownloadAction action : allActions) {
                trackedDownloadStates.put(action.uri, action);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to load tracked actions", e);
        }
    }

    private void handleTrackedDownloadStatesChanged() {
        for (Listener listener : listeners) {
            listener.onDownloadsChanged();
        }
        final DownloadAction[] actions = trackedDownloadStates.values().toArray(new DownloadAction[0]);
        actionFileWriteHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    actionFile.store(actions);
                } catch (IOException e) {
                    Log.e(TAG, "Failed to store tracked actions", e);
                }
            }
        });
    }


    /**
     * 执行DownloadAction
     */
    private void startServiceWithAction(DownloadAction action) {
        DownloadService.startWithAction(context, ExoDownloadService.class, action, false);
    }

    private DownloadHelper getDownloadHelper(Uri uri, String extension) {
        int type = Util.inferContentType(uri, extension);
        switch (type) {
            case C.TYPE_DASH:
                return new DashDownloadHelper(uri, dataSourceFactory);
            case C.TYPE_SS:
                return new SsDownloadHelper(uri, dataSourceFactory);
            case C.TYPE_HLS:
                return new HlsDownloadHelper(uri, dataSourceFactory);
            case C.TYPE_OTHER:
                return new ProgressiveDownloadHelper(uri);
            default:
                throw new IllegalStateException("Unsupported type: " + type);
        }
    }
}
