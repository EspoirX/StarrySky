package com.lzx.musiclibrary.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.net.Uri;
import android.os.Process;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Iterator;
import java.util.List;

/**
 *  lzx
 *  2018/2/24
 */

public class BaseUtil {
    public static String curProcessName;

    public static String getCurProcessName(Context context) {
        if(context == null) {
            return "";
        } else if(!TextUtils.isEmpty(curProcessName)) {
            return curProcessName;
        } else {
            int pid = Process.myPid();
            ActivityManager mActivityManager = (ActivityManager)context.getSystemService("activity");
            List runningAppProcesses = null;

            try {
                runningAppProcesses = mActivityManager.getRunningAppProcesses();
            } catch (Exception var6) {
                var6.printStackTrace();
            }

            if(runningAppProcesses != null) {
                Iterator var4 = runningAppProcesses.iterator();

                while(var4.hasNext()) {
                    ActivityManager.RunningAppProcessInfo appProcess = (ActivityManager.RunningAppProcessInfo)var4.next();
                    if(appProcess.pid == pid) {
                        curProcessName = appProcess.processName;
                        break;
                    }
                }
            }

            if(TextUtils.isEmpty(curProcessName)) {
                curProcessName = getProcessName();
            }

            if(TextUtils.isEmpty(curProcessName)) {
                curProcessName = context.getPackageName();
            }

            return curProcessName;
        }
    }

    private static String getProcessName() {
        try {
            File file = new File("/proc/" + Process.myPid() + "/cmdline");
            BufferedReader mBufferedReader = new BufferedReader(new FileReader(file));
            String processName = mBufferedReader.readLine().trim();
            mBufferedReader.close();
            return processName;
        } catch (Throwable var3) {
            var3.printStackTrace();
            return null;
        }
    }


    /**
     * 判断是网络地址还是本地地址（不知道这样准不准确）
     * @param source
     */
    public static boolean isOnLineSource(String source) {
        return (source.toLowerCase().startsWith("http://")
                || source.toLowerCase().startsWith("https://"))
                || source.toLowerCase().startsWith("rtmp://")
                && !source.toLowerCase().startsWith("file:///");
    }


    /**
     * 获取本地文件Uri
     */
    public static Uri getLocalSourceUri(String source) {
        Uri uri = null;
        try {
            File file = new File(source);
            if (file.exists()) {
                uri = Uri.fromFile(file);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (source.startsWith("file:///") && source.contains("android_asset/")) {
            uri = Uri.parse(source);
        }
        return uri;
    }
}
