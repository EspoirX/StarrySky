package com.lzx.starrysky

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle


class AppLifecycleCallback : ActivityLifecycleCallbacks {

    internal var currActivity: Activity? = null
    internal var activityList = mutableListOf<Activity?>()

    fun getVisibleActivity() = activityList.getOrNull(activityList.lastIndex)

    override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {

    }

    override fun onActivityStarted(activity: Activity?) {
        activityList.add(activity)
    }

    override fun onActivityResumed(activity: Activity?) {
        currActivity = activity
    }

    override fun onActivityPaused(activity: Activity?) {
        currActivity = null
    }

    override fun onActivityStopped(activity: Activity?) {
        activityList.remove(activity)
    }

    override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {
    }

    override fun onActivityDestroyed(activity: Activity?) {
        StarrySky.with().removeProgressListener(activity)
        StarrySky.with().resetVariable(activity)
    }
}