package com.lzx.starrysky.playback.offline

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.HandlerThread
import android.text.TextUtils
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.offline.ActionFile
import com.google.android.exoplayer2.offline.DownloadAction
import com.google.android.exoplayer2.offline.DownloadHelper
import com.google.android.exoplayer2.offline.DownloadManager
import com.google.android.exoplayer2.offline.DownloadService
import com.google.android.exoplayer2.offline.DownloaderConstructorHelper
import com.google.android.exoplayer2.offline.ProgressiveDownloadHelper
import com.google.android.exoplayer2.offline.TrackKey
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.Log
import com.google.android.exoplayer2.util.Util
import com.lzx.starrysky.utils.StarrySkyUtils
import java.io.File
import java.io.IOException

class ExoCache constructor(
    private val context: Context, private val cacheManager: StarrySkyCacheManager
) :
    StarrySkyCache,
    DownloadHelper.Callback, DownloadManager.Listener {

    companion object {
        const val TAG = "DownloadTracker"
        const val DOWNLOAD_ACTION_FILE = "actions"
        const val DOWNLOAD_TRACKER_ACTION_FILE = "tracked_actions"
        const val MAX_SIMULTANEOUS_DOWNLOADS = 2
    }

    private val trackedDownloadStates: HashMap<Uri, DownloadAction>
    private val actionFile: ActionFile
    private val actionFileWriteHandler: Handler
    private var downloadHelper: DownloadHelper? = null
    private val trackKeys: MutableList<TrackKey>
    private var mediaId: String? = null

    private val userAgent: String
    private var downloadManager: DownloadManager? = null

    init {
        val file = File(cacheManager.getDownloadDirectory(context), DOWNLOAD_TRACKER_ACTION_FILE)
        this.actionFile = ActionFile(file)
        trackedDownloadStates = hashMapOf()
        trackKeys = mutableListOf()
        userAgent = StarrySkyUtils.getUserAgent(context, "ExoPlaybackJava")

        val actionFileWriteThread = HandlerThread("DownloadTracker")
        actionFileWriteThread.start()
        actionFileWriteHandler = Handler(actionFileWriteThread.looper)
        loadTrackedActions(DownloadAction.getDefaultDeserializers())
        initDownloadManager(context)
    }

    // Internal methods
    private fun loadTrackedActions(deserializers: Array<DownloadAction.Deserializer>) {
        try {
            val allActions = actionFile.load(*deserializers)
            for (action in allActions) {
                trackedDownloadStates[action.uri] = action
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e(TAG, "Failed to load tracked actions", e)
        }
    }

    /**
     * 初始化 DownloadManager
     */
    @Synchronized
    private fun initDownloadManager(context: Context) {
        if (downloadManager == null) {
            val downloaderConstructorHelper =
                DownloaderConstructorHelper(cacheManager.getDownloadCache(),
                    DefaultHttpDataSourceFactory(userAgent))
            downloadManager = DownloadManager(
                downloaderConstructorHelper,
                MAX_SIMULTANEOUS_DOWNLOADS,
                DownloadManager.DEFAULT_MIN_RETRY_COUNT,
                File(cacheManager.getDownloadDirectory(context), DOWNLOAD_ACTION_FILE))
            downloadManager!!.addListener(this)
        }
    }

    fun getDownloadManager(): DownloadManager? {
        return downloadManager
    }

    override fun isCache(url: String): Boolean {
        return trackedDownloadStates.containsKey(Uri.parse(url))
    }

    override fun startCache(mediaId: String, url: String, extension: String) {
        if (!cacheManager.isOpenCache()) {
            return
        }
        this.mediaId = mediaId

        if (isCache(url)) {
            //--- 已经下载完了 ---
            //DownloadAction removeAction = getDownloadHelper(uri, extension).getRemoveAction(Util.getUtf8Bytes(name));
            //startServiceWithAction(removeAction);
        } else {
            // --- 新下载 ---
            downloadHelper = getDownloadHelper(Uri.parse(url), extension)
            downloadHelper?.prepare(this)
        }
    }

    override fun deleteCacheFileByUrl(url: String) {
        if (TextUtils.isEmpty(url)) {
            return
        }
        val uri = Uri.parse(url)
        if (isCache(url)) {
            if (downloadHelper == null) {
                downloadHelper = getDownloadHelper(uri, "")
            }
            val removeAction = downloadHelper!!.getRemoveAction(Util.getUtf8Bytes(mediaId))
            startServiceWithAction(removeAction)
        }
    }

    /**
     * 执行DownloadAction
     */
    private fun startServiceWithAction(action: DownloadAction) {
        DownloadService.startWithAction(context, ExoDownloadService::class.java, action, false)
    }

    override fun deleteAllCacheFile(): Boolean {
        val downloadDirectory = cacheManager.getDownloadDirectory(context)
        for (file in downloadDirectory.listFiles()) {
            if (file.isFile) {
                file.delete() // 删除所有文件
            } else if (file.isDirectory) {
                deleteAllCacheFile() // 递规的方式删除文件夹
            }
        }
        return downloadDirectory.delete()// 删除目录本身
    }

    override fun onPrepared(helper: DownloadHelper?) {
        if (downloadHelper == null) {
            return
        }
        for (i in 0 until downloadHelper!!.periodCount) {
            val trackGroups = downloadHelper!!.getTrackGroups(i)
            for (j in 0 until trackGroups.length) {
                val trackGroup = trackGroups.get(j)
                for (k in 0 until trackGroup.length) {
                    trackKeys.add(TrackKey(i, j, k))
                }
            }
        }
        startDownload()
    }

    /**
     * 开始下载
     */
    private fun startDownload() {
        if (downloadHelper == null) {
            return
        }
        val downloadAction = downloadHelper!!.getDownloadAction(Util.getUtf8Bytes(mediaId),
            trackKeys)
        if (trackedDownloadStates.containsKey(downloadAction.uri)) {
            return
        }
        trackedDownloadStates[downloadAction.uri] = downloadAction
        handleTrackedDownloadStatesChanged()
        startServiceWithAction(downloadAction)
    }

    private fun handleTrackedDownloadStatesChanged() {
        val actions = trackedDownloadStates.values.toTypedArray()
        actionFileWriteHandler.post {
            try {
                actionFile.store(*actions)
            } catch (e: IOException) {
                Log.e(TAG, "Failed to store tracked actions", e)
            }
        }
    }

    override fun onPrepareError(helper: DownloadHelper?, e: IOException?) {
        Log.e(TAG, "Failed to start download", e)
    }

    override fun onTaskStateChanged(
        downloadManager: DownloadManager?, taskState: DownloadManager.TaskState?
    ) {
        if (taskState == null) {
            return
        }
        val action = taskState.action
        val uri = action.uri
        if (action.isRemoveAction
            && taskState.state == DownloadManager.TaskState.STATE_COMPLETED
            || !action.isRemoveAction
            && taskState.state == DownloadManager.TaskState.STATE_FAILED) {
            // A download has been removed, or has failed. Stop tracking it.
            if (trackedDownloadStates.remove(uri) != null) {
                handleTrackedDownloadStatesChanged()
            }
        }
    }

    override fun onIdle(downloadManager: DownloadManager?) {
        // Do nothing.
    }

    override fun onInitialized(downloadManager: DownloadManager?) {
        // Do nothing.
    }

    private fun getDownloadHelper(uri: Uri, extension: String): DownloadHelper? {
        val type = Util.inferContentType(uri, extension)
        return if (type == C.TYPE_OTHER) {
            ProgressiveDownloadHelper(uri) //只缓存非流式音频
        } else null
    }
}