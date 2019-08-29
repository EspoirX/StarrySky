package com.lzx.starrysky.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.Process;

import java.util.Iterator;
import java.util.List;

import static android.content.Context.ACTIVITY_SERVICE;

public class StarrySkyUtils {
    /**
     * 判断Activity 是否可用
     *
     * @param activity 目标Activity
     * @return true of false
     */
    public static boolean isActivityAvailable(Activity activity) {
        if (null == activity) {
            return false;
        }
        if (activity.isFinishing()) {
            return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && activity.isDestroyed()) {
            return false;
        }
        return true;
    }

    public static boolean isPatchProcess(Context context) {
        ActivityManager am = (ActivityManager)context.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningApp = am.getRunningAppProcesses();
        if (runningApp == null) {
            return false;
        } else {
            Iterator var3 = runningApp.iterator();

            ActivityManager.RunningAppProcessInfo info;
            do {
                if (!var3.hasNext()) {
                    return false;
                }

                info = (ActivityManager.RunningAppProcessInfo)var3.next();
            } while(info.pid != Process.myPid());
            return info.processName.endsWith("patch");
        }
    }
}
