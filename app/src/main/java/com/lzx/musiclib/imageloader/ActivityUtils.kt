package com.lzx.musiclib.imageloader

import android.app.Activity
import android.content.Context
import android.os.Build
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager

/**
 * @author lzx
 * @date 2018/3/26
 */
object ActivityUtils {
    fun activityIsAlive(context: Context?): Boolean {
        if (context == null) {
            return false
        }
        return if (context is Activity) {
            activityIsAlive(context)
        } else {
            true
        }
    }

    fun activityIsAlive(activity: Activity?): Boolean {
        if (activity == null) {
            return false
        }
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            !(activity.isDestroyed || activity.isFinishing)
        } else {
            !activity.isFinishing
        }
    }

    fun addFragmentToActivity(
        fragmentManager: FragmentManager,
        fragment: Fragment, frameId: Int
    ) {
        checkNotNull(fragmentManager)
        checkNotNull(fragment)
        val transaction =
            fragmentManager.beginTransaction()
        transaction.add(frameId, fragment)
        transaction.commit()
    }

    fun <T> checkNotNull(reference: T?): T {
        return if (reference == null) {
            throw NullPointerException()
        } else {
            reference
        }
    }
}