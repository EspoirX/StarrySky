package com.lzx.starrysky.utils

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

class SpUtil private constructor() {


    fun putString(key: String?, value: String?): Boolean {
        val editor = mPref!!.edit()
        editor.putString(key, value)
        return editor.commit()
    }

    fun putLong(key: String?, value: Long): Boolean {
        val editor = mPref!!.edit()
        editor.putLong(key, value)
        return editor.commit()
    }

    fun putInt(key: String?, value: Int): Boolean {
        val editor = mPref!!.edit()
        editor.putInt(key, value)
        return editor.commit()
    }

    fun putBoolean(key: String?, value: Boolean): Boolean {
        val editor = mPref!!.edit()
        editor.putBoolean(key, value)
        return editor.commit()
    }

    fun getBoolean(key: String?): Boolean {
        return mPref!!.getBoolean(key, false)
    }

    fun getBoolean(key: String?, def: Boolean): Boolean {
        return mPref!!.getBoolean(key, def)
    }

    fun getString(key: String?): String {
        return mPref!!.getString(key, "")
    }

    fun getString(key: String?, def: String?): String {
        return mPref!!.getString(key, def)
    }

    fun getLong(key: String?): Long {
        return mPref!!.getLong(key, 0)
    }

    fun getLong(key: String?, defInt: Int): Long {
        return mPref!!.getLong(key, defInt.toLong())
    }

    fun getInt(key: String?): Int {
        return mPref!!.getInt(key, 0)
    }

    fun getInt(key: String?, defInt: Int): Long {
        return mPref!!.getInt(key, defInt).toLong()
    }

    operator fun contains(key: String?): Boolean {
        return mPref!!.contains(key)
    }

    fun remove(key: String?): Boolean {
        val editor = mPref!!.edit()
        editor.remove(key)
        return editor.commit()
    }

    fun clear(): Boolean {
        val editor = mPref!!.edit()
        editor.clear()
        return editor.commit()
    }

    companion object {

        @Volatile
        private var mInstance: SpUtil? = null
        private var mContext: Context? = null
        private var mPref: SharedPreferences? = null

        fun init(context: Context?) {
            if (mContext == null) {
                mContext = context
            }
            if (mPref == null) {
                mPref = PreferenceManager.getDefaultSharedPreferences(mContext)
            }
        }

        val instance: SpUtil
            get() {
                if (null == mInstance) {
                    synchronized(SpUtil::class.java) {
                        if (null == mInstance) {
                            mInstance = SpUtil()
                        }
                    }
                }
                return mInstance!!
            }
    }
}