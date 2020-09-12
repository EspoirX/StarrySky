package com.lzx.musiclib

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.util.TypedValue
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

fun ViewModel.safeLaunchOnMain(tryBlock: CoroutineScope.() -> Unit) {
    viewModelScope.launch(Dispatchers.Main) {
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