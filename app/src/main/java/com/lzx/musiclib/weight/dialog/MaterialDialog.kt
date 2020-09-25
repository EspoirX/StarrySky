package com.lzx.musiclib.weight.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.lzx.musiclib.isContextValid
import com.lzx.musiclib.isMainThread

//类的别名
typealias DialogCallback = (MaterialDialog) -> Unit

/**
 * 通用dialog，不用每写一个就新建一个 xxxDialog，默认是 DialogToast
 */
class MaterialDialog(
    val windowContext: Context,
    val dialogBehavior: DialogBehavior = DefaultBehavior()
) : Dialog(windowContext, dialogBehavior.getThemeRes()) {

    var autoDismissEnabled: Boolean = true
        internal set
    var cancelOnTouchOutside: Boolean = true
        internal set
    var cancelable: Boolean = true
        internal set
    var view: DialogLayout

    init {
        val layoutInflater = LayoutInflater.from(windowContext)
        val rootView = dialogBehavior.createView(
            creatingContext = windowContext,
            dialogWindow = window!!,
            layoutInflater = layoutInflater,
            dialog = this
        )
        setContentView(rootView)
        this.view = dialogBehavior.getDialogLayout(rootView)
        invalidateBackgroundColor()
    }

    inline fun show(func: MaterialDialog.() -> Unit): MaterialDialog = apply {
        this.func()
        this.show()
    }

    inline fun create(func: MaterialDialog.() -> Unit): MaterialDialog = apply {
        this.func()
    }

    /**
     * 标题
     */
    fun title(text: String? = null, textColor: Int = Color.parseColor("#FF1D1D1D")) = apply {
        populateText(view.titleView, text = text, textColor = textColor)
    }

    /**
     * 内容
     */
    fun message(text: CharSequence? = null, textColor: Int = Color.parseColor("#FF1D1D1D")) = apply {
        populateText(view.messageView, text = text, textColor = textColor)
    }

    /**
     * 左边按钮
     */
    fun leftButton(
        text: CharSequence? = null,
        textColor: Int = Color.parseColor("#FF999999"),
        click: DialogCallback? = null
    ) = apply {
        val btn = view.leftButton
        if (text == null || btn == null) {
            return this
        }
        populateText(btn, text = text, textColor = textColor)
        btn.setOnClickListener {
            click?.invoke(this)
            if (autoDismissEnabled) {
                dismiss()
            }
        }
    }

    /**
     * 右边按钮
     */
    fun rightButton(
        text: CharSequence? = null,
        textColor: Int = Color.parseColor("#FF734FFF"),
        click: DialogCallback? = null
    ) = apply {
        val btn = view.rightButton
        if (text == null || btn == null) {
            return@apply
        }
        populateText(btn, text = text, textColor = textColor)
        btn.setOnClickListener {
            click?.invoke(this)
            if (autoDismissEnabled) {
                dismiss()
            }
        }
    }

    /**
     * 中间按钮
     */
    fun okButton(text: CharSequence? = null, textColor: Int = Color.BLACK, click: DialogCallback? = null) = apply {
        val btn = view.okBtn
        if (text == null || btn == null) {
            return@apply
        }
        view.leftButton?.visibility = View.GONE
        view.rightButton?.visibility = View.GONE
        populateText(btn, text = text, textColor = textColor)
        btn.setOnClickListener {
            click?.invoke(this)
            if (autoDismissEnabled) {
                dismiss()
            }
        }
    }

    /**
     * 点击按钮后是否关闭弹窗
     */
    fun noAutoDismiss() = apply { this.autoDismissEnabled = false }

    /**
     * 是否点击外部关闭弹窗
     */
    fun cancelOnTouchOutside(cancelable: Boolean): MaterialDialog = apply {
        cancelOnTouchOutside = cancelable
        setCanceledOnTouchOutside(cancelable)
    }

    fun cancelable(cancelable: Boolean): MaterialDialog = apply {
        this.cancelable = cancelable
        setCancelable(cancelable)
    }

    /**
     * 初始化背景
     */
    private fun invalidateBackgroundColor() {
        val backgroundColor = Color.TRANSPARENT
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        backgroundColor(backgroundColor)
    }

    /**
     * 设置背景
     */
    fun backgroundColor(backgroundColor: Int): MaterialDialog = apply {
        dialogBehavior.setBackgroundColor(view = view, color = backgroundColor)
    }

    /**
     * 添加自定义View
     */
    fun customView(@LayoutRes viewRes: Int? = null, view: View? = null, callback: DialogCallback? = null) = apply {
        this.view.addCustomView(res = viewRes, view = view)
        callback?.invoke(this)
    }

    /**
     * 配置window
     */
    private fun setWindowConstraints() {
        dialogBehavior.setWindowConstraints(context = windowContext, window = window!!, view = view)
    }

    override fun show() {
        try {
            if (windowContext.isMainThread()) {
                if (windowContext.isContextValid()) {
                    setWindowConstraints()
                    dialogBehavior.onPreShow(this)
                    super.show()
                    dialogBehavior.onPostShow(this)
                }
            } else {
                return
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    override fun dismiss() {
        try {
            if (windowContext.isMainThread()) {
                if (windowContext.isContextValid()) {
                    if (dialogBehavior.onDismiss()) return
                    hideKeyboard()
                    view.clearView()
                    super.dismiss()
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun populateText(textView: TextView?, text: CharSequence? = null, textColor: Int) {
        if (!text.isNullOrEmpty()) {
            (textView?.parent as View).visibility = View.VISIBLE
            textView.visibility = View.VISIBLE
            textView.text = text
            textView.setTextColor(textColor)
        } else {
            textView?.visibility = View.GONE
        }
    }
}

internal fun MaterialDialog.hideKeyboard() {
    val imm = windowContext.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
    val currentFocus = currentFocus
    if (currentFocus != null) {
        currentFocus.windowToken
    } else {
        view.windowToken
    }.let {
        imm.hideSoftInputFromWindow(it, 0)
    }
}

internal fun MaterialDialog.showKeyboard() {
    val imm = windowContext.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
    val currentFocus = currentFocus
    if (currentFocus != null) {
        currentFocus.windowToken
    } else {
        view.windowToken
    }.let {
        imm.showSoftInput(currentFocus, 0)
    }
}

fun MaterialDialog.getCustomView(): View {
    return this.view.customView ?: error("You have not setup this dialog as a customView dialog.")
}

fun MaterialDialog.onShow(callback: DialogCallback): MaterialDialog {
    if (this.isShowing) {
        callback.invoke(this)
    }
    setOnShowListener { callback.invoke(this) }
    return this
}

fun MaterialDialog.onDismiss(callback: DialogCallback): MaterialDialog {
    setOnDismissListener { callback.invoke(this) }
    return this
}

fun MaterialDialog.onCancel(callback: DialogCallback): MaterialDialog {
    setOnCancelListener { callback.invoke(this) }
    return this
}

/**
 * 生命周期绑定
 */
fun MaterialDialog.lifecycleOwner(owner: LifecycleOwner? = null): MaterialDialog {
    val observer = DialogLifecycleObserver(this)
    val lifecycleOwner = owner ?: (windowContext as? LifecycleOwner
        ?: throw IllegalStateException("$windowContext is not a LifecycleOwner."))
    lifecycleOwner.lifecycle.addObserver(observer)
    return this
}

fun MaterialDialog.lifecycleOwner(lifecycle: Lifecycle? = null): MaterialDialog {
    val observer = DialogLifecycleObserver(this)
    lifecycle?.addObserver(observer)
    return this
}

fun Context.createMaterialDialog(dialogBehavior: DialogBehavior = DefaultBehavior()): MaterialDialog {
    return MaterialDialog(this, dialogBehavior = dialogBehavior)
}

fun Activity.createMaterialDialog(dialogBehavior: DialogBehavior = DefaultBehavior()): MaterialDialog {
    return MaterialDialog(this, dialogBehavior = dialogBehavior)
}

fun FragmentActivity.createMaterialDialog(dialogBehavior: DialogBehavior = DefaultBehavior()): MaterialDialog {
    return MaterialDialog(this, dialogBehavior = dialogBehavior)
}

internal class DialogLifecycleObserver(private val dialog: MaterialDialog) : LifecycleObserver {
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        dialog.dismiss()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        dialog.dismiss()
    }
}

