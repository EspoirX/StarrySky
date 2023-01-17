package com.lzx.starrysky

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle


class AppLifecycleCallback : ActivityLifecycleCallbacks {

    var currActivity: Activity? = null
    var activityList = mutableListOf<Activity?>()
    var activityStack = mutableListOf<Activity?>()

    fun getStackTopActivity() = activityStack.getOrNull(activityStack.lastIndex)

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        activityStack.add(activity)
    }

    override fun onActivityStarted(activity: Activity) {
    }

    override fun onActivityResumed(activity: Activity) {
        currActivity = activity
    }

    override fun onActivityPaused(activity: Activity) {
        currActivity = null
    }

    override fun onActivityStopped(activity: Activity) {
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityDestroyed(activity: Activity) {
        activityStack.remove(activity)
        StarrySky.with().removeProgressListener(activity.toString())
        StarrySky.with().resetVariable(activity)
    }
}