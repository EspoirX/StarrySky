package com.lzx.starrysky.service

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
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import com.lzx.starrysky.utils.StarrySkyUtils

class MusicService : Service() {

    var bridge: ServiceBridge? = null
    private var noisyReceiver: BecomingNoisyReceiver? = null

    companion object {
        var isRunningForeground = false
    }

    override fun onBind(intent: Intent?): IBinder? {
        bridge = ServiceBridge(this)
        return bridge
    }

    override fun onUnbind(intent: Intent?): Boolean {
        if (bridge?.playerControl?.isPlaying() == false) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                bridge?.sessionManager?.release()
            }
        }
        return super.onUnbind(intent)
    }

    override fun onCreate() {
        super.onCreate()
        if (StarrySkyUtils.isLollipop()) {
            isRunningForeground = true
        }
        initTelephony()
        noisyReceiver = BecomingNoisyReceiver(this)
        noisyReceiver?.register()
    }

    /**
     *  bridge 里面的东西已经创建好
     */
    fun onCreateServiceBridgeSuccess() {
    }

    /**
     * 初始化电话监听服务，电话响了要暂停
     */
    private fun initTelephony() {
        val telephonyManager = this.getSystemService(TELEPHONY_SERVICE) as TelephonyManager // 获取电话通讯服务
        telephonyManager.listen(object : PhoneStateListener() {
            override fun onCallStateChanged(state: Int, phoneNumber: String?) {
                super.onCallStateChanged(state, phoneNumber)
                when (state) {
                    TelephonyManager.CALL_STATE_OFFHOOK, TelephonyManager.CALL_STATE_RINGING -> {
                        bridge?.playerControl?.pauseMusic()
                    }
                }
            }
        }, PhoneStateListener.LISTEN_CALL_STATE) // 创建一个监听对象，监听电话状态改变事件
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
            val isPlaying = bridge?.playerControl?.isPlaying() ?: false
            when (intent?.action) {
                BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED -> {
                    StarrySkyUtils.log("蓝牙耳机插拔状态改变")
                    val state = bluetoothAdapter?.getProfileConnectionState(BluetoothProfile.HEADSET)
                    if (BluetoothProfile.STATE_DISCONNECTED == state && isPlaying) {
                        //蓝牙耳机断开连接 同时当前音乐正在播放 则将其暂停
                        bridge?.playerControl?.pauseMusic()
                    }
                }
                AudioManager.ACTION_AUDIO_BECOMING_NOISY -> {
                    StarrySkyUtils.log("有线耳机插拔状态改变")
                    if (isPlaying) {
                        //有线耳机断开连接 同时当前音乐正在播放 则将其暂停
                        bridge?.playerControl?.pauseMusic()
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        noisyReceiver?.unregister()
        bridge?.playerControl?.stopMusic()
        bridge?.notification?.stopNotification()
    }
}