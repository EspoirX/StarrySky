package com.lzx.starrysky.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.exoplayer2.ExoPlayerLibraryInfo;
import com.lzx.starrysky.control.PlayerControl;
import com.lzx.starrysky.control.RepeatMode;

import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

import static android.content.Context.ACTIVITY_SERVICE;

public class StarrySkyUtils {

    public static boolean isDebug = true;


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
        ActivityManager am = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
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

                info = (ActivityManager.RunningAppProcessInfo) var3.next();
            } while (info.pid != Process.myPid());
            return info.processName.endsWith("patch");
        }
    }

    public static String getUserAgent(Context context, String applicationName) {
        String versionName;
        try {
            String packageName = context.getPackageName();
            PackageInfo info = context.getPackageManager().getPackageInfo(packageName, 0);
            versionName = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            versionName = "?";
        }
        return applicationName + "/" + versionName + " (Linux;Android " + Build.VERSION.RELEASE
                + ") " + ExoPlayerLibraryInfo.VERSION_SLASHY;
    }

    /**
     * 反射一下主线程获取一下上下文
     */
    public static Application getContextReflex() {
        try {
            @SuppressLint("PrivateApi")
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            @SuppressLint("DiscouragedPrivateApi")
            Method currentApplicationMethod = activityThreadClass.getDeclaredMethod("currentApplication");
            currentApplicationMethod.setAccessible(true);
            return (Application) currentApplicationMethod.invoke(null);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static void saveRepeatMode(int repeatMode, boolean isLoop) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("repeatMode", repeatMode);
            jsonObject.put("isLoop", isLoop);
            SpUtil.Companion.getInstance().putString(RepeatMode.KEY_REPEAT_MODE, jsonObject.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static RepeatMode getRepeatMode() {
        String json = SpUtil.Companion.getInstance().getString(RepeatMode.KEY_REPEAT_MODE);
        RepeatMode defaultMode = new RepeatMode(RepeatMode.REPEAT_MODE_NONE, true);
        if (TextUtils.isEmpty(json)) {
            return defaultMode;
        } else {
            try {
                JSONObject jsonObject = new JSONObject(json);
                return new RepeatMode(jsonObject.getInt("repeatMode"), jsonObject.getBoolean("isLoop"));
            } catch (Exception ex) {
                ex.printStackTrace();
                return defaultMode;
            }
        }
    }

    public static String formatStackTrace(StackTraceElement[] stackTrace) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : stackTrace) {
            sb.append("    at ").append(element.toString());
            sb.append("\n");
        }
        return sb.toString();
    }

    public static void log(String msg) {
        if (isDebug) {
            Log.i("StarrySky", msg);
        }
    }
}
