package com.lzx.starrysky

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle


class AppLifecycleCallback : ActivityLifecycleCallbacks {

    internal var currActivity: Activity? = null

    override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
        currActivity = activity
    }

    override fun onActivityStarted(activity: Activity?) {
    }

    override fun onActivityResumed(activity: Activity?) {
    }

    override fun onActivityPaused(activity: Activity?) {
    }

    override fun onActivityStopped(activity: Activity?) {
    }

    override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {
    }

    override fun onActivityDestroyed(activity: Activity?) {
        StarrySky.with().removeProgressListener(activity)
        currActivity = null
        StarrySky.with().resetVariable()
    }
}