package com.lzx.starrysky;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.lzx.starrysky.utils.StarrySkyUtils;

import java.util.ArrayList;
import java.util.List;

public class StarrySkyActivityLifecycle implements Application.ActivityLifecycleCallbacks {

    private List<Activity> activities = new ArrayList<>();

    /**
     * 获取可用Activity
     */
   public Activity getActivity() {
        if (null == activities || activities.size() == 0) {
            throw new IllegalStateException("auto init failed ,you need invoke StarrySky.init() in your application");
        }
        for (int i = activities.size() - 1; i >= 0; i--) {
            Activity activity = activities.get(i);
            if (StarrySkyUtils.isActivityAvailable(activity)) {
                return activity;
            }
        }
        throw new IllegalStateException("activity did not existence, check your app status before use StarrySky");
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        activities.add(activity);
    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        activities.remove(activity);
    }
}
