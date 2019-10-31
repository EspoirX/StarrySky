package com.lzx.starrysky.notification.utils

import android.app.Notification
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.support.v4.graphics.ColorUtils
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RemoteViews
import android.widget.TextView

import java.util.concurrent.CountDownLatch

/**
 * 通知栏颜色工具类，主要是判断通知栏主题是白色还是黑色
 */
class NotificationColorUtils {
    private var titleView: TextView? = null
    private var contentView: TextView? = null
    private var mNotificationColorModel: NotificationColorModel? = null

    fun setTitleTextColor(
        context: Context, remoteView: RemoteViews, viewId: Int, notification: Notification
    ) {
        if (mNotificationColorModel == null) {
            isDarkNotificationBar(context, notification)
        }

        if (mNotificationColorModel!!.titleColor == COLOR_UNDEF && Build.VERSION.SDK_INT >= 21) {
            if (mNotificationColorModel!!.isDarkNotificationBg) {
                mNotificationColorModel!!.titleColor = EVENTCONTENT_TITLE_COLOR
            } else {
                mNotificationColorModel!!.titleColor = NOTIFICATION_TITLE_COLOR
            }
        }

        remoteView.setTextColor(viewId, mNotificationColorModel!!.titleColor)
    }

    fun setContentTextColor(
        context: Context, remoteView: RemoteViews, viewId: Int, notification: Notification
    ) {
        if (mNotificationColorModel == null) {
            isDarkNotificationBar(context, notification)
        }

        if (mNotificationColorModel!!.contentColor == COLOR_UNDEF && Build.VERSION.SDK_INT >= 21) {
            if (mNotificationColorModel!!.isDarkNotificationBg) {
                mNotificationColorModel!!.contentColor = EVENTCONTENT_COLOR
            } else {
                mNotificationColorModel!!.contentColor = NOTIFICATION_LINE2_COLOR
            }
        }

        remoteView.setTextColor(viewId, mNotificationColorModel!!.contentColor)
    }

    @Synchronized
    fun isDarkNotificationBar(context: Context, notification: Notification?): Boolean {
        if (mNotificationColorModel == null) {
            mNotificationColorModel = NotificationColorModel()
            val isInMainThread = Looper.myLooper() == Looper.getMainLooper()
            var countDownLatch: CountDownLatch? = null
            if (!isInMainThread) {
                countDownLatch = CountDownLatch(1)
            }

            val finalCountDownLatch = countDownLatch
            val runnable = Runnable {
                try {
                    val notiTextColor = getNotificationColor(context, notification)
                    if (notiTextColor == COLOR_UNDEF) {
                        mNotificationColorModel!!.titleColor = COLOR_UNDEF
                        mNotificationColorModel!!.contentColor = COLOR_UNDEF
                        mNotificationColorModel!!.isDarkNotificationBg = true
                    } else {
                        //!isTextColorSimilar(-16777216, notiTextColor);
                        val isDark = ColorUtils.calculateLuminance(notiTextColor) > 0.5
                        mNotificationColorModel!!.isDarkNotificationBg = isDark
                    }
                } catch (var3: Exception) {
                    var3.printStackTrace()
                    mNotificationColorModel!!.titleColor = COLOR_UNDEF
                    mNotificationColorModel!!.contentColor = COLOR_UNDEF
                    mNotificationColorModel!!.isDarkNotificationBg = true
                }

                if (mNotificationColorModel!!.titleColor == COLOR_UNDEF && Build.VERSION.SDK_INT >= 21) {
                    if (mNotificationColorModel!!.isDarkNotificationBg) {
                        mNotificationColorModel!!.titleColor = EVENTCONTENT_TITLE_COLOR
                    } else {
                        mNotificationColorModel!!.titleColor = NOTIFICATION_TITLE_COLOR
                    }
                }

                if (mNotificationColorModel!!.contentColor == COLOR_UNDEF && Build.VERSION.SDK_INT >= 21) {
                    if (mNotificationColorModel!!.isDarkNotificationBg) {
                        mNotificationColorModel!!.contentColor = EVENTCONTENT_COLOR
                    } else {
                        mNotificationColorModel!!.contentColor = NOTIFICATION_LINE2_COLOR
                    }
                }

                finalCountDownLatch?.countDown()
            }
            if (isInMainThread) {
                runnable.run()
            } else {
                Handler(Looper.getMainLooper()).post(runnable)
                try {
                    countDownLatch!!.await()
                } catch (var6: InterruptedException) {
                    var6.printStackTrace()
                }
            }
        }

        return mNotificationColorModel!!.isDarkNotificationBg
    }

    private fun getNotificationColor(context: Context, notification: Notification?): Int {
        val layout = LinearLayout(context)
        layout.layoutParams = LinearLayout.LayoutParams(-2, -2)
        val viewGroup = notification?.contentView?.apply(context, layout) as ViewGroup
        getTextView(viewGroup, false)
        return if (titleView == null) {
            COLOR_UNDEF
        } else {
            val color = titleView!!.currentTextColor
            mNotificationColorModel!!.titleColor = color
            if (contentView != null) {
                val contentColor = contentView!!.currentTextColor
                mNotificationColorModel!!.contentColor = contentColor
            }

            color
        }
    }

    private fun getTextView(viewGroup: ViewGroup?, isSetTextColor: Boolean): TextView? {
        if (viewGroup == null) {
            return null
        } else {
            val count = viewGroup.childCount
            for (i in 0 until count) {
                val view = viewGroup.getChildAt(i)
                if (view is TextView) {
                    if (isSetTextColor) {
                        if (view.text == NOTIFICATION_TITLE) {
                            titleView = view
                        }
                        if (view.text == NOTIFICATION_CONTENT) {
                            contentView = view
                        }
                    } else {
                        titleView = view
                        contentView = view
                    }
                } else if (view is ViewGroup) {
                    getTextView(view, isSetTextColor)
                }
            }

            return null
        }
    }

    private fun isDark(notiTextColor: Int): Boolean {
        return ColorUtils.calculateLuminance(notiTextColor) < 0.5
    }

    private fun isTextColorSimilar(baseColor: Int, color: Int): Boolean {
        val simpleBaseColor = baseColor or -16777216
        val simpleColor = color or -16777216
        val baseRed = Color.red(simpleBaseColor) - Color.red(simpleColor)
        val baseGreen = Color.green(simpleBaseColor) - Color.green(simpleColor)
        val baseBlue = Color.blue(simpleBaseColor) - Color.blue(simpleColor)
        val value =
            Math.sqrt((baseRed * baseRed + baseGreen * baseGreen + baseBlue * baseBlue).toDouble())
        return value < 180.0
    }

    internal class NotificationColorModel {
        var titleColor: Int = 0
        var contentColor: Int = 0
        var isDarkNotificationBg: Boolean = false

        init {
            this.titleColor = COLOR_UNDEF
            this.contentColor = COLOR_UNDEF
            this.isDarkNotificationBg = true
        }
    }

    companion object {
        private const val NOTIFICATION_TITLE = "notification_music_title"
        private const val NOTIFICATION_CONTENT = "notification_music_content"

        private const val COLOR_UNDEF = 987654321
        private const val COLOR_THRESHOLD = 180.0
        private var NOTIFICATION_TITLE_COLOR = Color.parseColor("#de000000")
        private var NOTIFICATION_LINE2_COLOR = Color.parseColor("#8a000000")
        private const val EVENTCONTENT_TITLE_COLOR = -1
        private var EVENTCONTENT_COLOR = Color.parseColor("#b3ffffff")
    }
}
