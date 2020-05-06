package com.lzx.starrysky

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import com.lzx.starrysky.utils.StarrySkyUtils
import java.util.ArrayList

class StarrySkyActivityLifecycle : ActivityLifecycleCallbacks {
    val activities: MutableList<Activity> = ArrayList()

    /**
     * 获取可用Activity
     */
    val activity: Activity?
        get() {
            if (activities.size == 0) {
                return null
            }
            for (i in activities.indices.reversed()) {
                val activity = activities[i]
                if (StarrySkyUtils.isActivityAvailable(activity)) {
                    return activity
                }
            }
            return null
        }

    override fun onActivityCreated(
        activity: Activity, savedInstanceState: Bundle?
    ) {
        activities.add(activity)
    }

    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(
        activity: Activity, outState: Bundle
    ) {
    }

    override fun onActivityDestroyed(activity: Activity) {
        activities.remove(activity)
    }
}