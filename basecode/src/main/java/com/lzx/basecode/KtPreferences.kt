package com.lzx.basecode

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * 基于委托实现的sp简单封装
 */
abstract class KtPreferences {

    companion object {
        var context: Context? = null

        @JvmStatic
        fun init(context: Context?) {
            if (Companion.context == null) {
                Companion.context = context
            }
        }
    }

    private val preferences: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(context)
    }

    fun booleanPref(default: Boolean = false, synchronous: Boolean = false) = object : ReadWriteProperty<Any, Boolean> {
        override fun setValue(thisRef: Any, property: KProperty<*>, value: Boolean) {
            preferences.edit().putBoolean(property.name, value).execute(synchronous)
        }

        override fun getValue(thisRef: Any, property: KProperty<*>): Boolean {
            return preferences.getBoolean(property.name, default)
        }
    }

    fun intPref(default: Int = 0, synchronous: Boolean = false) = object : ReadWriteProperty<Any, Int> {
        override fun setValue(thisRef: Any, property: KProperty<*>, value: Int) {
            preferences.edit().putInt(property.name, value).execute(synchronous)
        }

        override fun getValue(thisRef: Any, property: KProperty<*>): Int {
            return preferences.getInt(property.name, default)
        }
    }

    fun stringPref(default: String = "", synchronous: Boolean = false) = object : ReadWriteProperty<Any, String?> {
        override fun setValue(thisRef: Any, property: KProperty<*>, value: String?) {
            preferences.edit().putString(property.name, value).execute(synchronous)
        }

        override fun getValue(thisRef: Any, property: KProperty<*>): String? {
            return preferences.getString(property.name, default)
        }
    }

    fun longPref(default: Long = 0L, synchronous: Boolean = false) = object : ReadWriteProperty<Any, Long> {
        override fun setValue(thisRef: Any, property: KProperty<*>, value: Long) {
            preferences.edit().putLong(property.name, value).execute(synchronous)
        }

        override fun getValue(thisRef: Any, property: KProperty<*>): Long {
            return preferences.getLong(property.name, default)
        }
    }

    fun floatPref(defaultValue: Float = 0.0f, synchronous: Boolean = false) = object : ReadWriteProperty<Any, Float> {
        override fun getValue(thisRef: Any, property: KProperty<*>): Float {
            return preferences.getFloat(property.name, defaultValue)
        }

        override fun setValue(thisRef: Any, property: KProperty<*>, value: Float) {
            preferences.edit().putFloat(property.name, value).execute(synchronous)
        }
    }

    @SuppressLint("CommitPrefEdits")
    fun clearAll(synchronous: Boolean = false) {
        preferences.edit().clear().execute(synchronous)
    }

    fun SharedPreferences.Editor.execute(synchronous: Boolean) {
        if (synchronous) commit() else apply()
    }

}