package com.lzx.musiclib

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.os.Parcelable
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.Serializable
import kotlin.coroutines.CoroutineContext

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
    )

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

fun Context.showToast(msg: String) {
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}

fun Int.getViewObj(context: Context, root: ViewGroup? = null, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(this, root, attachToRoot)
}

infix fun <T> Boolean.then(value: T?) = TernaryExpression(this, value)

class TernaryExpression<out T>(val flag: Boolean, private val truly: T?) {
    infix fun <T> or(falsy: T?) = if (flag) truly else falsy
}

fun View.setMargins( left: Int = 0, top: Int = 0, right: Int = 0, bottom: Int = 0,
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