package com.lzx.starrysky.playback.offline;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.offline.ActionFile;
import com.google.android.exoplayer2.offline.DownloadAction;
import com.google.android.exoplayer2.offline.DownloadHelper;
import com.google.android.exoplayer2.offline.DownloadManager;
import com.google.android.exoplayer2.offline.DownloadService;
import com.google.android.exoplayer2.offline.DownloaderConstructorHelper;
import com.google.android.exoplayer2.offline.ProgressiveDownloadHelper;
import com.google.android.exoplayer2.offline.StreamKey;
import com.google.android.exoplayer2.offline.TrackKey;
import com.google.android.exoplayer2.source.TrackGroup;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Log;
import com.google.android.exoplayer2.util.Util;
import com.lzx.starrysky.playback.Utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ExoCache implements StarrySkyCache, DownloadHelper.Callback, DownloadManager.Listener {
    private static final String TAG = "DownloadTracker";
    private static final String DOWNLOAD_ACTION_FILE = "actions";
    private static final String DOWNLOAD_TRACKER_ACTION_FILE = "tracked_actions";
    private static final int MAX_SIMULTANEOUS_DOWNLOADS = 2;

    private Context context;
    private StarrySkyCacheManager cacheManager;

    private HashMap<Uri, DownloadAction> trackedDownloadStates;
    private ActionFile actionFile;
    private Handler actionFileWriteHandler;
    private DownloadHelper downloadHelper;
    private List<TrackKey> trackKeys;
    private String mediaId;

    private String userAgent;
    private DownloadManager downloadManager;

    public ExoCache(Context context, StarrySkyCacheManager manager) {
        this.context = context;
        this.cacheManager = manager;

        File file = new File(manager.getDownloadDirectory(context), DOWNLOAD_TRACKER_ACTION_FILE);
        this.actionFile = new ActionFile(file);
        trackedDownloadStates = new HashMap<>();
        trackKeys = new ArrayList<>();
        userAgent = Utils.getUserAgent(context, "ExoPlayback");

        HandlerThread actionFileWriteThread = new HandlerThread("DownloadTracker");
        actionFileWriteThread.start();
        actionFileWriteHandler = new Handler(actionFileWriteThread.getLooper());
        loadTrackedActions(DownloadAction.getDefaultDeserializers());
        initDownloadManager(context);
    }

    /**
     * 初始化 DownloadManager
     */
    private synchronized void initDownloadManager(Context context) {
        if (downloadManager == null) {
            DownloaderConstructorHelper downloaderConstructorHelper =
                    new DownloaderConstructorHelper(cacheManager.getDownloadCache(),
                            new DefaultHttpDataSourceFactory(userAgent));
            downloadManager =
                    new DownloadManager(
                            downloaderConstructorHelper,
                            MAX_SIMULTANEOUS_DOWNLOADS,
                            DownloadManager.DEFAULT_MIN_RETRY_COUNT,
                            new File(cacheManager.getDownloadDirectory(context), DOWNLOAD_ACTION_FILE));
            downloadManager.addListener(this);
        }
    }

    public DownloadManager getDownloadManager() {
        return downloadManager;
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

    @Override
    public boolean isCache(String url) {
        return trackedDownloadStates.containsKey(Uri.parse(url));
    }

    public List<StreamKey> getOfflineStreamKeys(Uri uri) {
        if (!trackedDownloadStates.containsKey(uri)) {
            return Collections.emptyList();
        } else {
            DownloadAction downloadAction = trackedDownloadStates.get(uri);
            return downloadAction != null ? downloadAction.getKeys() : Collections.emptyList();
        }
    }

    @Override
    public void startCache(String mediaId, String url, String extension) {
        if (!cacheManager.isOpenCache()) {
            return;
        }
        this.mediaId = mediaId;

        if (isCache(url)) {
            //--- 已经下载完了 ---
            //DownloadAction removeAction = getDownloadHelper(uri, extension).getRemoveAction(Util.getUtf8Bytes(name));
            //startServiceWithAction(removeAction);
        } else {
            // --- 新下载 ---
            downloadHelper = getDownloadHelper(Uri.parse(url), extension);
            if (downloadHelper != null) {
                downloadHelper.prepare(this);
            }
        }
    }

    @Override
    public void deleteCacheFileByUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        Uri uri = Uri.parse(url);
        if (isCache(url)) {
            if (downloadHelper == null) {
                downloadHelper = getDownloadHelper(uri, "");
            }
            if (downloadHelper != null) {
                DownloadAction removeAction = downloadHelper.getRemoveAction(Util.getUtf8Bytes(mediaId));
                startServiceWithAction(removeAction);
            }
        }
    }

    @Override
    public boolean deleteAllCacheFile() {
        File downloadDirectory = cacheManager.getDownloadDirectory(context);
        for (File file : downloadDirectory.listFiles()) {
            if (file.isFile()) {
                file.delete(); // 删除所有文件
            } else if (file.isDirectory()) {
                deleteAllCacheFile(); // 递规的方式删除文件夹
            }
        }
        return downloadDirectory.delete();// 删除目录本身
    }

    @Override
    public void onPrepared(DownloadHelper helper) {
        if (downloadHelper == null) {
            return;
        }
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

    /**
     * 开始下载
     */
    private void startDownload() {
        if (downloadHelper == null) {
            return;
        }
        DownloadAction downloadAction = downloadHelper.getDownloadAction(Util.getUtf8Bytes(mediaId), trackKeys);
        if (trackedDownloadStates.containsKey(downloadAction.uri)) {
            return;
        }
        trackedDownloadStates.put(downloadAction.uri, downloadAction);
        handleTrackedDownloadStatesChanged();
        startServiceWithAction(downloadAction);
    }

    private void handleTrackedDownloadStatesChanged() {
        final DownloadAction[] actions = trackedDownloadStates.values().toArray(new DownloadAction[0]);
        actionFileWriteHandler.post(() -> {
            try {
                actionFile.store(actions);
            } catch (IOException e) {
                Log.e(TAG, "Failed to store tracked actions", e);
            }
        });
    }

    /**
     * 执行DownloadAction
     */
    private void startServiceWithAction(DownloadAction action) {
        DownloadService.startWithAction(context, ExoDownloadService.class, action, false);
    }

    @Override
    public void onPrepareError(DownloadHelper helper, IOException e) {
        Log.e(TAG, "Failed to start download", e);
    }

    @Override
    public void onInitialized(DownloadManager downloadManager) {
        // Do nothing.
    }

    @Override
    public void onTaskStateChanged(DownloadManager downloadManager, DownloadManager.TaskState taskState) {
        DownloadAction action = taskState.action;
        Uri uri = action.uri;
        if ((action.isRemoveAction && taskState.state == DownloadManager.TaskState.STATE_COMPLETED)
                || (!action.isRemoveAction && taskState.state == DownloadManager.TaskState.STATE_FAILED)) {
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

    private DownloadHelper getDownloadHelper(Uri uri, String extension) {
        int type = Util.inferContentType(uri, extension);
        if (type == C.TYPE_OTHER) {
            return new ProgressiveDownloadHelper(uri); //只缓存非流式音频
        }
        return null;
    }

}
