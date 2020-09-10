package com.lzx.musiclib

import android.app.Application
import android.content.Context
import com.lzx.starrysky.StarrySky
import com.lzx.starrysky.StarrySkyConfig
import com.tencent.bugly.crashreport.CrashReport

/**
 * create by lzx
 * time:2018/11/9
 */
open class TestApplication : Application() {

    companion object {
        var context: Context? = null
    }

    override fun onCreate() {
        super.onCreate()
        context = this
        val config = StarrySkyConfig().newBuilder()
            .isOpenNotification(true)
            .isUserService(false)
            .build()
        StarrySky.init(this, config)
        CrashReport.initCrashReport(applicationContext, "9e447caa98", false)
    }
}