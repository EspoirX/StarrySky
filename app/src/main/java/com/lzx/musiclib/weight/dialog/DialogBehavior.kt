package com.lzx.musiclib.weight.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.StyleRes
import com.lzx.musiclib.R
import com.lzx.musiclib.setUpHeightPercent
import com.lzx.musiclib.setUpWidthPercent

/**
 * 弹窗行为
 */
interface DialogBehavior {
    @StyleRes
    fun getThemeRes(): Int

    fun createView(
        creatingContext: Context, dialogWindow: Window, layoutInflater: LayoutInflater, dialog: Dialog
    ): ViewGroup

    fun getDialogLayout(root: ViewGroup): DialogLayout

    fun setWindowConstraints(context: Context, window: Window, view: DialogLayout)

    fun setBackgroundColor(view: DialogLayout, @ColorInt color: Int)

    fun onPreShow(dialog: Dialog)

    fun onPostShow(dialog: Dialog)

    fun onDismiss(): Boolean
}

/**
 * 默认实现，参照 DialogToast
 */
class DefaultBehavior : CommonBehavior() {

    private var mTitle: TextView? = null
    private var mContent: TextView? = null
    private var btnPositive: Button? = null
    private var btnNegative: Button? = null
    private var btnOk: Button? = null

    override fun createView(
        creatingContext: Context, dialogWindow: Window, layoutInflater: LayoutInflater, dialog: Dialog
    ): ViewGroup {
        val layout = super.createView(creatingContext, dialogWindow, layoutInflater, dialog)
        val view = layoutInflater.inflate(R.layout.base_dialog, null, false) as ViewGroup
        mTitle = view.findViewById(R.id.dialog_toast_title) as TextView
        mContent = view.findViewById(R.id.dialog_toast_content) as TextView
        btnPositive = view.findViewById(R.id.dialog_toast_button_positive) as Button
        btnNegative = view.findViewById(R.id.dialog_toast_button_negative) as Button
        btnOk = view.findViewById(R.id.dialog_toast_button_ok) as Button
        layout.removeAllViews()
        layout.addView(view)
        return layout
    }

    override fun setBackgroundColor(view: DialogLayout, color: Int) {
        super.setBackgroundColor(view, Color.TRANSPARENT)
    }

    override fun getDialogLayout(root: ViewGroup): DialogLayout {
        val layout = super.getDialogLayout(root)
        layout.titleView = mTitle
        layout.messageView = mContent
        layout.leftButton = btnNegative
        layout.rightButton = btnPositive
        layout.okBtn = btnOk
        return layout
    }
}

open class CommonBehavior(var style: Int = -1, vararg configs: Pair<String, Any>) : DialogBehavior {
    private var windowConfig: Array<out Pair<String, Any>> = configs

    override fun getThemeRes(): Int = if (style == -1) R.style.dialog_base_style else style

    override fun createView(
        creatingContext: Context, dialogWindow: Window, layoutInflater: LayoutInflater, dialog: Dialog
    ): ViewGroup {
        return layoutInflater.inflate(R.layout.layout_base_dialog, null, false) as ViewGroup
    }

    override fun getDialogLayout(root: ViewGroup): DialogLayout = root as DialogLayout

    override fun setWindowConstraints(context: Context, window: Window, view: DialogLayout) {
        val params = window.attributes
        if (windowConfig.isNullOrEmpty()) {
            params.width = window.windowManager.defaultDisplay.width
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT
            params.gravity = Gravity.CENTER
        } else {
            windowConfig.forEach {
                val key = it.first
                val value = it.second
                when (key) {
                    "width" -> window.setUpWidthPercent(value)
                    "height" -> window.setUpHeightPercent(value)
                    "realHeight" -> params.height = value as Int
                    "realWidth" -> params.width = value as Int
                    "gravity" -> params.gravity = value as Int
                    "alpha" -> params.alpha = value as Float
                    "softInputMode" -> params.softInputMode = value as Int
                    "windowAnimations" -> params.windowAnimations = value as Int
                    "bgDrawableRes" -> window.setBackgroundDrawableResource(value as Int)
                    "xOffset" -> {
                        val xOffset = value as Int
                        if (xOffset != 0) params.x = xOffset
                    }
                    "yOffset" -> {
                        val yOffset = value as Int
                        if (yOffset != 0) params.y = yOffset
                    }
                }
            }
        }
        window.setBackgroundDrawableResource(android.R.color.transparent)
        window.attributes = params
    }

    override fun setBackgroundColor(view: DialogLayout, color: Int) {
        view.background = GradientDrawable().apply { setColor(color) }
    }

    override fun onPreShow(dialog: Dialog) = Unit

    override fun onPostShow(dialog: Dialog) = Unit

    override fun onDismiss(): Boolean = false
}
