package com.lzx.starrysky.service

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.net.wifi.WifiManager.WifiLock
import android.os.Build
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import android.telephony.TelephonyManager


/**
 * @author Vincent
 * @date 2023/1/3
 * @description 唤醒锁与Wifi锁的辅助类
 *
 */
object LockHelper {

    private var mWifiLock: WifiLock? = null
    private var mCellularLock: WakeLock? = null

    /**
     * Locks the network and start using it. Later, the network must be unlocked using @release.
     * 锁定网络并开始使用它。之后，必须使用@release()解锁网络。
     * 这个函数就做两个事情，获取激活的网络，判断网络如果是Wifi，就获取WifiLock，如果是移动网络就获取PowerManager.WakeLock
     * @return true if succeed, false otherwise.
     * @sa release
     */
    @SuppressLint("InvalidWakeLockTag")
    fun acquire(context: Context){
       if(isWifiConnected(context) && mWifiLock == null){
           if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
               context.getSystemService(WifiManager::class.java)?.let {
                   mWifiLock = it.createWifiLock(WifiManager.WIFI_MODE_FULL,"LockHelper Lock_Wifi")
                   it.connectionInfo?.let { wifiInfo ->
                       val detailedState = WifiInfo.getDetailedStateOf(wifiInfo.supplicantState)
                       if (detailedState == NetworkInfo.DetailedState.CONNECTED 					// 此状态表示IP通信应该可用
                           || detailedState == NetworkInfo.DetailedState.CONNECTING			// 此状态表示当前正在建立数据连接
                           || detailedState == NetworkInfo.DetailedState.OBTAINING_IPADDR) {	// 此状态表示等待DHCP服务器的响应，以便分配IP地址信息。
                           mWifiLock?.acquire() // 获取Wifi锁，这样可以保证用户熄屏空闲时Wifi不会断开。
                       }

                   }
               }
           }
       }else if(mCellularLock == null && isMobileNetwork(context)){
           if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
               context.getSystemService(PowerManager::class.java)?.let {
                   mCellularLock = it.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "LockHelper Lock_Cellular")
                   mCellularLock?.acquire()


               }
           }
       }
    }

    /**
     * 判断wifi是否连接状态
     *
     * 需添加权限 android.permission.ACCESS_NETWORK_STATE
     *
     * @param context 上下文
     * @return true: 连接并可用<br></br>false: 未连接或者不可用
     */
    private fun isWifiConnected(context: Context): Boolean {
        val cm = context
            .getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        return cm != null
                && cm.activeNetworkInfo!!.type == ConnectivityManager.TYPE_WIFI
                && cm.activeNetworkInfo!!.isAvailable
    }

    private fun  isMobileNetwork(context: Context):Boolean {
        (context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager)?.activeNetworkInfo?.let {
            val netType = it.type
            /*
            ConnectivityManager.TYPE_WIMAX :即全球微波互联接入。是一项新兴的宽带无线接入技术，能提供面向互联网的高速连接，
            数据传输距离最远可达50km。WiMAX还具有QoS保障、传输速率高、业务丰富多样等优点。WiMAX的技术起点较高，
            采用了代表未来通信技术发展方向的OFDM/OFDMA、AAS、MIMO等先进技术，随着技术标准的发展，WiMAX逐步实现宽带业务的移动化，
            而3G则实现移动业务的宽带化，两种网络的融合程度会越来越高。
             */
            if ((netType == ConnectivityManager.TYPE_MOBILE || netType == ConnectivityManager.TYPE_WIMAX)) {
                val netSubType = it.subtype
                // 下面的判断写得像放屁一样，总结就是netSubType >= 1的都认为是移动网络，这就包含了所有的子网络类型了。所以这里可以直接返回true的，不需要判断子网络类型
                return ((netSubType >= TelephonyManager.NETWORK_TYPE_UMTS)		// 值是3
                        || // HACK
                        (netSubType == TelephonyManager.NETWORK_TYPE_GPRS)		// 值是1
                        || (netSubType == TelephonyManager.NETWORK_TYPE_EDGE)) // 值是2
            }else{
                return false
            }

        }?:return false
    }

    /**
     * Unlocks the network and stop using it. The network must be locked first using @ref acquire.
     * @return true is succeed, false otherwise.
     * @sa acquire
     */
     fun release() {
        if(mWifiLock?.isHeld == true){
            mWifiLock?.release()
        }
        mWifiLock = null

        if(mCellularLock?.isHeld == true){
            mCellularLock?.release()
        }
        mCellularLock = null

    }

}