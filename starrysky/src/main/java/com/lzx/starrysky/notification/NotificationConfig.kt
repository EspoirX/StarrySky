package com.lzx.starrysky.notification

import android.app.PendingIntent
import android.os.Bundle

/**
 * 通知栏构建者，可设置各种通知栏配置
 */
class NotificationConfig(
    var isNotificationCanClearBySystemBtn: Boolean = false, //是否让通知栏当暂停的时候可以滑动清除
    var targetClass: String? = null, //通知栏点击转跳界面
    var targetClassBundle: Bundle? = null, //点击通知栏传递的参数
    var contentTitle: String? = null, //通知栏标题
    var contentText: String? = null, //通知栏内容
    var nextIntent: PendingIntent? = null,  //下一首按钮 PendingIntent
    var preIntent: PendingIntent? = null, //上一首按钮 PendingIntent
    var closeIntent: PendingIntent? = null, //关闭按钮 PendingIntent
    var favoriteIntent: PendingIntent? = null, //喜欢或收藏按钮 PendingIntent
    var lyricsIntent: PendingIntent? = null, //桌面歌词按钮 PendingIntent
    var playIntent: PendingIntent? = null, //播放按钮 PendingIntent
    var pauseIntent: PendingIntent? = null, // 暂停按钮 PendingIntent
    var playOrPauseIntent: PendingIntent? = null, // 播放/暂停按钮 PendingIntent
    var stopIntent: PendingIntent? = null, //停止按钮 PendingIntent
    var downloadIntent: PendingIntent? = null,  //下载按钮 PendingIntent
    var pendingIntentMode: Int = -1, //通知栏点击模式
    var isSystemNotificationShowTime: Boolean = false,  //系统通知栏是否显示时间
    var skipPreviousDrawableRes: Int = -1,  //上一首的drawable res
    var skipPreviousTitle: String = "",  //上一首的 title
    var skipNextDrawableRes: Int = -1,  //下一首的drawable res
    var skipNextTitle: String = "",  //下一首的 title
    var labelPause: String = "",
    var pauseDrawableRes: Int = -1,
    var labelPlay: String = "",
    var playDrawableRes: Int = -1,
    var smallIconRes: Int = -1
) {

    companion object {
        const val MODE_ACTIVITY = 0
        const val MODE_BROADCAST = 1
        const val MODE_SERVICE = 2
    }
}
