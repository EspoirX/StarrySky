package com.lzx.starrysky.notification

import android.app.PendingIntent
import android.os.Bundle

class NotificationConfig private constructor(
    val isNotificationCanClearBySystemBtn: Boolean, //是否让通知栏当暂停的时候可以滑动清除
    val targetClass: String?, //通知栏点击转跳界面
    val targetClassBundle: Bundle?, //点击通知栏传递的参数
    val contentTitle: String?, //通知栏标题
    val contentText: String?, //通知栏内容
    val nextIntent: PendingIntent?,  //下一首按钮 PendingIntent
    val preIntent: PendingIntent?, //上一首按钮 PendingIntent
    val closeIntent: PendingIntent?, //关闭按钮 PendingIntent
    val favoriteIntent: PendingIntent?, //喜欢或收藏按钮 PendingIntent
    val lyricsIntent: PendingIntent?, //桌面歌词按钮 PendingIntent
    val playIntent: PendingIntent?, //播放按钮 PendingIntent
    val pauseIntent: PendingIntent?, // 暂停按钮 PendingIntent
    val playOrPauseIntent: PendingIntent?, // 播放/暂停按钮 PendingIntent
    val stopIntent: PendingIntent?, //停止按钮 PendingIntent
    val downloadIntent: PendingIntent?,  //下载按钮 PendingIntent
    val pendingIntentMode: Int, //通知栏点击模式
    val isSystemNotificationShowTime: Boolean,  //系统通知栏是否显示时间
    val skipPreviousDrawableRes: Int,  //上一首的drawable res
    val skipPreviousTitle: String,  //上一首的 title
    val skipNextDrawableRes: Int,  //下一首的drawable res
    val skipNextTitle: String,  //下一首的 title
    val labelPause: String,
    val pauseDrawableRes: Int,
    val labelPlay: String,
    val playDrawableRes: Int,
    val smallIconRes: Int
) {

    companion object {
        const val MODE_ACTIVITY = 0
        const val MODE_BROADCAST = 1
        const val MODE_SERVICE = 2

        @JvmStatic
        fun create(init: Builder.() -> Builder): NotificationConfig {
            val builder = Builder()
            builder.init()
            return builder.build()
        }
    }

    constructor(builder: Builder) : this(
        builder.isNotificationCanClearBySystemBtn,
        builder.targetClass,
        builder.targetClassBundle,
        builder.contentTitle,
        builder.contentText,
        builder.nextIntent,
        builder.preIntent,
        builder.closeIntent,
        builder.favoriteIntent,
        builder.lyricsIntent,
        builder.playIntent,
        builder.pauseIntent,
        builder.playOrPauseIntent,
        builder.stopIntent,
        builder.downloadIntent,
        builder.pendingIntentMode,
        builder.isSystemNotificationShowTime,
        builder.skipPreviousDrawableRes,
        builder.skipPreviousTitle,
        builder.skipNextDrawableRes,
        builder.skipNextTitle,
        builder.labelPause,
        builder.pauseDrawableRes,
        builder.labelPlay,
        builder.playDrawableRes,
        builder.smallIconRes
    )

    class Builder constructor() {
        constructor(init: Builder.() -> Unit) : this() {
            init()
        }

        var isNotificationCanClearBySystemBtn: Boolean = false //是否让通知栏当暂停的时候可以滑动清除
        var targetClass: String? = null //通知栏点击转跳界面
        var targetClassBundle: Bundle? = null //点击通知栏传递的参数
        var contentTitle: String? = null //通知栏标题
        var contentText: String? = null //通知栏内容
        var nextIntent: PendingIntent? = null  //下一首按钮 PendingIntent
        var preIntent: PendingIntent? = null //上一首按钮 PendingIntent
        var closeIntent: PendingIntent? = null //关闭按钮 PendingIntent
        var favoriteIntent: PendingIntent? = null //喜欢或收藏按钮 PendingIntent
        var lyricsIntent: PendingIntent? = null //桌面歌词按钮 PendingIntent
        var playIntent: PendingIntent? = null //播放按钮 PendingIntent
        var pauseIntent: PendingIntent? = null // 暂停按钮 PendingIntent
        var playOrPauseIntent: PendingIntent? = null // 播放/暂停按钮 PendingIntent
        var stopIntent: PendingIntent? = null //停止按钮 PendingIntent
        var downloadIntent: PendingIntent? = null  //下载按钮 PendingIntent
        var pendingIntentMode: Int = -1 //通知栏点击模式
        var isSystemNotificationShowTime: Boolean = false  //系统通知栏是否显示时间
        var skipPreviousDrawableRes: Int = -1  //上一首的drawable res
        var skipPreviousTitle: String = ""  //上一首的 title
        var skipNextDrawableRes: Int = -1  //下一首的drawable res
        var skipNextTitle: String = ""
        var labelPause: String = ""
        var pauseDrawableRes: Int = -1
        var labelPlay: String = ""
        var playDrawableRes: Int = -1
        var smallIconRes: Int = -1

        fun isNotificationCanClearBySystemBtn(init: Builder.() -> Boolean) = apply {
            isNotificationCanClearBySystemBtn = init()
        }

        fun targetClass(init: Builder.() -> String?) = apply {
            targetClass = init()
        }

        fun targetClassBundle(init: Builder.() -> Bundle?) = apply {
            targetClassBundle = init()
        }

        fun contentTitle(init: Builder.() -> String?) = apply {
            contentTitle = init()
        }

        fun contentText(init: Builder.() -> String?) = apply {
            contentText = init()
        }

        fun nextIntent(init: Builder.() -> PendingIntent?) = apply {
            nextIntent = init()
        }

        fun preIntent(init: Builder.() -> PendingIntent?) = apply {
            preIntent = init()
        }

        fun closeIntent(init: Builder.() -> PendingIntent?) = apply {
            closeIntent = init()
        }

        fun favoriteIntent(init: Builder.() -> PendingIntent?) = apply {
            favoriteIntent = init()
        }

        fun lyricsIntent(init: Builder.() -> PendingIntent?) = apply {
            lyricsIntent = init()
        }

        fun playIntent(init: Builder.() -> PendingIntent?) = apply {
            playIntent = init()
        }

        fun pauseIntent(init: Builder.() -> PendingIntent?) = apply {
            pauseIntent = init()
        }

        fun playOrPauseIntent(init: Builder.() -> PendingIntent?) = apply {
            playOrPauseIntent = init()
        }

        fun stopIntent(init: Builder.() -> PendingIntent?) = apply {
            stopIntent = init()
        }

        fun downloadIntent(init: Builder.() -> PendingIntent?) = apply {
            downloadIntent = init()
        }

        fun pendingIntentMode(init: Builder.() -> Int) = apply {
            pendingIntentMode = init()
        }

        fun isSystemNotificationShowTime(init: Builder.() -> Boolean) = apply {
            isSystemNotificationShowTime = init()
        }

        fun skipPreviousDrawableRes(init: Builder.() -> Int) = apply {
            skipPreviousDrawableRes = init()
        }

        fun skipPreviousTitle(init: Builder.() -> String) = apply {
            skipPreviousTitle = init()
        }

        fun skipNextDrawableRes(init: Builder.() -> Int) = apply {
            skipNextDrawableRes = init()
        }

        fun skipNextTitle(init: Builder.() -> String) = apply {
            skipNextTitle = init()
        }

        fun labelPause(init: Builder.() -> String) = apply {
            labelPause = init()
        }

        fun pauseDrawableRes(init: Builder.() -> Int) = apply {
            pauseDrawableRes = init()
        }

        fun labelPlay(init: Builder.() -> String) = apply {
            labelPlay = init()
        }

        fun playDrawableRes(init: Builder.() -> Int) = apply {
            playDrawableRes = init()
        }

        fun smallIconRes(init: Builder.() -> Int) = apply {
            smallIconRes = init()
        }

        fun build() = NotificationConfig(this)
    }
}