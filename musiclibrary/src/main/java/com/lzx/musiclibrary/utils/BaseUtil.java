package com.lzx.musiclibrary.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Process;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Iterator;
import java.util.List;

/**
 * @author lzx
 * @date 2018/2/24
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
}
