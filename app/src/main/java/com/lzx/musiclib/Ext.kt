package com.lzx.musiclib

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.os.Parcelable
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import com.lzx.starrysky.utils.MainLooper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.Serializable
import kotlin.coroutines.CoroutineContext
import kotlin.math.roundToInt

val Float.dp
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this,
        Resources.getSystem().displayMetrics
    )

val Int.dp
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        Resources.getSystem().displayMetrics
    ).toInt()

val Float.sp
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP,
        this,
        Resources.getSystem().displayMetrics
    )

val Int.sp
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP,
        this.toFloat(),
        Resources.getSystem().displayMetrics
    )

fun String.parseColor(): Int = Color.parseColor(this)

fun String.toJsonObj(): JSONObject {
    return JSONObject(this)
}

fun JSONObject.getObj(value: String): JSONObject {
    return this.getJSONObject(value)
}

fun JSONObject.getArray(value: String): JSONArray {
    return this.getJSONArray(value)
}

inline fun <reified T> JSONArray.getOrNull(index: Int): T? {
    return if (index > 0 && index < this.length() - 1) {
        this.getJSONObject(index) as T
    } else {
        null
    }
}

fun JSONArray.iterator(): Iterator<JSONObject> =
    (0 until length()).asSequence().map { get(it) as JSONObject }.iterator()

inline fun <reified T> JSONArray.forEach(action: (T?) -> Unit) {
    (0 until length()).forEach { action(get(it) as? T) }
}

fun ViewModel.safeLaunch(context: CoroutineContext, tryBlock: CoroutineScope.() -> Unit) {
    viewModelScope.launch(context) {
        try {
            tryBlock()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
}

fun ImageView.loadImage(url: String?) {
    url?.let {
        Glide.with(this.context).load(url).into(this)
    }
}

fun Context.showToast(msg: String?) {
    MainLooper.instance.post {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}

fun Int.getViewObj(context: Context, root: ViewGroup? = null, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(this, root, attachToRoot)
}

infix fun <T> Boolean.then(value: T?) = TernaryExpression(this, value)

class TernaryExpression<out T>(val flag: Boolean, private val truly: T?) {
    infix fun <T> or(falsy: T?) = if (flag) truly else falsy
}

fun View.setMargins(left: Int = 0, top: Int = 0, right: Int = 0, bottom: Int = 0,
                    requestLayout: Boolean = false
) {
    if (this.layoutParams is ViewGroup.MarginLayoutParams) {
        val p = this.layoutParams as ViewGroup.MarginLayoutParams
        p.setMargins(left.dp.toInt(), top.dp.toInt(), right.dp.toInt(), bottom.dp.toInt())
        if (requestLayout) {
            this.requestLayout()
        }
    }
}

inline fun <reified T : Activity> Context.navigationTo(vararg params: Pair<String, Any?>) =
    internalStartActivity(this, T::class.java, params)

inline fun <reified T : Activity> Activity.navigationTo(vararg params: Pair<String, Any?>) =
    internalStartActivity(this, T::class.java, params)

inline fun <reified T : Activity> androidx.fragment.app.Fragment.navigationTo(vararg params: Pair<String, Any?>) =
    context?.let { internalStartActivity(it, T::class.java, params) }

inline fun <reified T : Activity> Activity.navigationToForResult(requestCode: Int, vararg params: Pair<String, Any?>) =
    internalStartActivityForResult(this, T::class.java, requestCode, params)

fun Activity.finishForResult(vararg params: Pair<String, Any?>) {
    setResult(Activity.RESULT_OK, createIntent<Activity>(params = params))
    finish()
}

fun internalStartActivity(
    ctx: Context,
    activity: Class<out Activity>,
    params: Array<out Pair<String, Any?>>
) {
    ctx.startActivity(createIntent(ctx, activity, params))
}

fun internalStartActivityForResult(
    act: Activity,
    activity: Class<out Activity>,
    requestCode: Int,
    params: Array<out Pair<String, Any?>>
) {
    act.startActivityForResult(createIntent(act, activity, params), requestCode)
}

fun <T> createIntent(ctx: Context? = null, clazz: Class<out T>? = null, params: Array<out Pair<String, Any?>>): Intent {
    val intent = if (clazz == null) Intent() else Intent(ctx, clazz)
    if (params.isNotEmpty()) fillIntentArguments(intent, params)
    return intent
}

private fun fillIntentArguments(intent: Intent, params: Array<out Pair<String, Any?>>) {
    params.forEach {
        when (val value = it.second) {
            null -> intent.putExtra(it.first, null as Serializable?)
            is Int -> intent.putExtra(it.first, value)
            is Long -> intent.putExtra(it.first, value)
            is CharSequence -> intent.putExtra(it.first, value)
            is String -> intent.putExtra(it.first, value)
            is Float -> intent.putExtra(it.first, value)
            is Double -> intent.putExtra(it.first, value)
            is Char -> intent.putExtra(it.first, value)
            is Short -> intent.putExtra(it.first, value)
            is Boolean -> intent.putExtra(it.first, value)
            is Serializable -> intent.putExtra(it.first, value)
            is Bundle -> intent.putExtra(it.first, value)
            is Parcelable -> intent.putExtra(it.first, value)
            is Array<*> -> when {
                value.isArrayOf<CharSequence>() -> intent.putExtra(it.first, value)
                value.isArrayOf<String>() -> intent.putExtra(it.first, value)
                value.isArrayOf<Parcelable>() -> intent.putExtra(it.first, value)
                else -> throw RuntimeException("Intent extra ${it.first} has wrong type ${value.javaClass.name}")
            }
            is IntArray -> intent.putExtra(it.first, value)
            is LongArray -> intent.putExtra(it.first, value)
            is FloatArray -> intent.putExtra(it.first, value)
            is DoubleArray -> intent.putExtra(it.first, value)
            is CharArray -> intent.putExtra(it.first, value)
            is ShortArray -> intent.putExtra(it.first, value)
            is BooleanArray -> intent.putExtra(it.first, value)
            else -> throw RuntimeException("Intent extra ${it.first} has wrong type ${value.javaClass.name}")
        }
        return@forEach
    }
}

fun Long.formatTime(): String {
    var time = ""
    val minute = this / 60000
    val seconds = this % 60000
    val second = (seconds.toInt() / 1000.toFloat()).roundToInt().toLong()
    if (minute < 10) {
        time += "0"
    }
    time += "$minute:"
    if (second < 10) {
        time += "0"
    }
    time += second
    return time
}

fun View.setUpLayoutParams(
    width: Int = ViewGroup.LayoutParams.MATCH_PARENT,
    height: Int = ViewGroup.LayoutParams.MATCH_PARENT
) {
    if (this.parent == null) return
    when (this.parent) {
        is LinearLayout -> {
            this.layoutParams = LinearLayout.LayoutParams(width, height)
        }
        is RelativeLayout -> {
            this.layoutParams = RelativeLayout.LayoutParams(width, height)
        }
        is FrameLayout -> {
            this.layoutParams = FrameLayout.LayoutParams(width, height)
        }
        is ConstraintLayout -> {
            this.layoutParams = ConstraintLayout.LayoutParams(width, height)
        }
        else -> {
            this.layoutParams = ViewGroup.LayoutParams(width, height)
        }
    }
    requestLayout()
}

fun View.changeSize(width: Int = -1, height: Int = -1) {
    if (width != -1) {
        this.layoutParams?.width = width
    }
    if (height != -1) {
        this.layoutParams?.height = height
    }
}

fun Window.setUpHeightPercent(value: Any) {
    var height = when (value) {
        is Int -> value.toFloat()
        is Float -> value
        else -> return
    }
    val windowHeight = this.windowManager.defaultDisplay.height
    val params = this.attributes
    when (height) {
        -1F -> params.height = windowHeight
        -2F -> params.height = windowHeight / 2
        else -> {
            if (height > 100) height = 100F
            height /= 100F
            height *= windowHeight.toFloat()
            params.height = height.toInt()
        }
    }
}

fun Window.setUpWidthPercent(value: Any) {
    var width = when (value) {
        is Int -> value.toFloat()
        is Float -> value
        else -> return
    }
    val windowWidth = this.windowManager.defaultDisplay.width
    val params = this.attributes
    when (width) {
        -1F -> params.width = windowWidth
        -2F -> params.width = windowWidth / 2
        else -> {
            if (width > 100) width = 100F
            width /= 100F
            width *= windowWidth.toFloat()
            params.width = width.toInt()
        }
    }
}

internal fun <T> ViewGroup.inflate(
    @LayoutRes res: Int,
    root: ViewGroup? = this
) = LayoutInflater.from(context).inflate(res, root, false) as T

fun Context.isMainThread(): Boolean {
    return Looper.getMainLooper() == Looper.myLooper()
}

fun Context?.isContextValid(): Boolean {
    if (this == null) return false
    if (this !is Activity) {
        return false
    }
    return this.isActivityValid()
}

fun Activity.isActivityValid(): Boolean {
    if (this.isFinishing) {
        return false
    }
    if (Build.VERSION.SDK_INT >= 17) {
        return !this.isDestroyed
    }
    return true
}

fun Fragment.addFragmentToActivity(fragmentManager: FragmentManager, frameId: Int) {
    val transaction = fragmentManager.beginTransaction()
    transaction.add(frameId, this)
    transaction.commit()
}

fun Context.getPhoneWidth(): Int {
    return this.resources.displayMetrics.widthPixels
}

fun Context.getPhoneHeight(): Int {
    return this.resources.displayMetrics.heightPixels
}

//顶层函数版
inline fun <reified T : ViewModel> getViewModel(owner: ViewModelStoreOwner, configLiveData: T.() -> Unit = {}): T =
    ViewModelProvider(owner)[T::class.java].apply { configLiveData() }

//扩展函数版
inline fun <reified T : ViewModel> ViewModelStoreOwner.getSelfViewModel(configLiveData: T.() -> Unit = {}): T =
    getViewModel(this, configLiveData)