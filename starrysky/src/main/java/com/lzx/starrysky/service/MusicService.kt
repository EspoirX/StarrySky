package com.lzx.starrysky.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Build
import android.os.IBinder
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.impl.utils.futures.SettableFuture
import androidx.work.workDataOf
import com.google.common.util.concurrent.ListenableFuture
import com.lzx.starrysky.StarrySky
import com.lzx.starrysky.notification.utils.NotificationUtils
import com.lzx.starrysky.utils.MainLooper
import com.lzx.starrysky.utils.TimerTaskManager
import com.lzx.starrysky.utils.orDef


class MusicService : Service() {

    var binder: MusicServiceBinder? = null
    private var noisyReceiver: BecomingNoisyReceiver? = null
    private var timerTaskManager: TimerTaskManager? = null
    private var timedOffDuration = -1L
    private var isPauseByTimedOff = true
    private var timedOffFinishCurrSong = false
    private var mustShowNotification = false

    override fun onCreate() {
        super.onCreate()
        initPlayerService()
    }

    override fun onBind(intent: Intent?): IBinder? {
        binder = MusicServiceBinder(this)
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            this.mustShowNotification = intent.getBooleanExtra("flag_must_to_show_notification", false)
        } else {
            this.mustShowNotification = false
        }
        initPlayerService()
        return START_STICKY
    }

    /**
     * 自定义启动前台服务
     * If the app targeting API is
     * {@link android.os.Build.VERSION_CODES#S} or later, and the service is restricted from
     * becoming foreground service due to background restriction.
     * {@link android.app.service#startForeground}
     *
     */
    fun customStartForeground(id: Int, notification: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            startForegroundByWorkManager(id)
        } else {
            startForeground(id, notification)
        }
    }

    /**
     * 通过WordManager来实现前台服务启动，避免崩溃
     */
    private fun startForegroundByWorkManager(id: Int) {
        val uploadWorkRequest: WorkRequest = OneTimeWorkRequestBuilder<UploadWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setInputData(workDataOf("id" to id))
            .build()
        WorkManager
            .getInstance(this)
            .enqueue(uploadWorkRequest)
    }


    private fun initPlayerService() {
        if (noisyReceiver == null) {
            noisyReceiver = BecomingNoisyReceiver(this)
            noisyReceiver?.register()
        }
        if (timerTaskManager == null) {
            timerTaskManager = TimerTaskManager()
            //计时回调
            timerTaskManager?.setUpdateProgressTask {
                timedOffDuration -= 1000
                if (timedOffDuration <= 0) { //时间到了
                    timerTaskManager?.stopToUpdateProgress()
                    if (!timedOffFinishCurrSong) {
                        if (isPauseByTimedOff) {
                            binder?.player?.pause()
                        } else {
                            binder?.player?.stop()
                        }
                        timedOffDuration = -1
                        timedOffFinishCurrSong = false
                    }
                }
            }
        }

        // https://developer.android.com/about/versions/oreo/background?hl=zh-cn#services 防止后台启动service后导致崩溃问题
        val notification: Notification = NotificationUtils.createNoCrashNotification(this)
        if (this.applicationInfo.targetSdkVersion >= 26 && this.mustShowNotification) {
            MainLooper.instance.postDelayed({
                if (binder?.notification == null) {
                    try {
                        customStartForeground(10000, notification)
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }
                }
            }, 3500L)
        }
    }


    /**
     * 定时关闭功能实现
     */
    fun onStopByTimedOffImpl(time: Long, isPause: Boolean, finishCurrSong: Boolean) {
        if (time == 0L) {
            timerTaskManager?.stopToUpdateProgress()
            timedOffDuration = -1
            timedOffFinishCurrSong = false
            return
        }
        timedOffDuration = time
        isPauseByTimedOff = isPause
        timedOffFinishCurrSong = finishCurrSong
        timerTaskManager?.startToUpdateProgress()
    }


    /**
     * 耳机拔出广播接收器
     */
    private inner class BecomingNoisyReceiver(private val context: Context) : BroadcastReceiver() {
        var bluetoothAdapter: BluetoothAdapter? = null
        private var intentFilter: IntentFilter? = null

        init {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            intentFilter = IntentFilter()
            intentFilter?.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY)  //有线耳机拔出变化
            intentFilter?.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED) //蓝牙耳机连接变化
        }

        private var registered = false

        fun register() {
            if (!registered) {
                context.registerReceiver(this, intentFilter)
                registered = true
            }
        }

        fun unregister() {
            if (registered) {
                context.unregisterReceiver(this)
                registered = false
            }
        }

        override fun onReceive(context: Context?, intent: Intent?) {
            //当前是正在运行的时候才能通过媒体按键来操作音频
            val isPlaying = binder?.player?.isPlaying().orDef()
            when (intent?.action) {
                BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED -> {
                    StarrySky.log("蓝牙耳机插拔状态改变")
                    val state = bluetoothAdapter?.getProfileConnectionState(BluetoothProfile.HEADSET)
                    if (BluetoothProfile.STATE_DISCONNECTED == state && isPlaying) {
                        //蓝牙耳机断开连接 同时当前音乐正在播放 则将其暂停
                        binder?.player?.pause()
                    }
                }
                AudioManager.ACTION_AUDIO_BECOMING_NOISY -> {
                    StarrySky.log("有线耳机插拔状态改变")
                    if (isPlaying) {
                        //有线耳机断开连接 同时当前音乐正在播放 则将其暂停
                        binder?.player?.pause()
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timerTaskManager?.removeUpdateProgressTask()
        noisyReceiver?.unregister()
        binder?.player?.stop()
        binder?.player?.setCallback(null)
        binder?.notification?.stopNotification()
    }

    // 后台任务实例
    class UploadWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {
        override fun doWork(): Result {
            val id = inputData.getInt("id", 1000)
            kotlin.runCatching { setForegroundAsync(getForegroundInfo(id)) }
            return Result.success()
        }

        @SuppressLint("RestrictedApi")
        override fun getForegroundInfoAsync(): ListenableFuture<ForegroundInfo> {
            val future = SettableFuture.create<ForegroundInfo>()
            val id = inputData.getInt("id", 1000)
            future.set(getForegroundInfo(id))
            return future

        }

        private fun getForegroundInfo(id: Int): ForegroundInfo {
            val notification: Notification = NotificationUtils.createNoCrashNotification(applicationContext)
            return ForegroundInfo(id, notification)
        }
    }
}