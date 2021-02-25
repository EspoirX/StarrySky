package com.lzx.starrysky.service

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
import android.os.IBinder
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
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
    private var telephonyManager: TelephonyManager? = null


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

    private fun initPlayerService() {
        initTelephony()
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
                        startForeground(10000, notification)
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
     * 初始化电话监听服务，电话响了要暂停
     */

    private fun initTelephony() {
        if (telephonyManager == null) {
            telephonyManager = this.getSystemService(TELEPHONY_SERVICE) as TelephonyManager // 获取电话通讯服务
            telephonyManager?.listen(object : PhoneStateListener() {
                override fun onCallStateChanged(state: Int, phoneNumber: String?) {
                    super.onCallStateChanged(state, phoneNumber)
                    when (state) {
                        TelephonyManager.CALL_STATE_OFFHOOK,
                        TelephonyManager.CALL_STATE_RINGING -> {
                            binder?.player?.pause()
                        }
                    }
                }
            }, PhoneStateListener.LISTEN_CALL_STATE) // 创建一个监听对象，监听电话状态改变事件
        }
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
}