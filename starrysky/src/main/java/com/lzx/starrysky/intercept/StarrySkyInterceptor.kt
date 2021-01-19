package com.lzx.starrysky.intercept

import com.lzx.starrysky.SongInfo


/**
 * 拦截器
 */
interface ISyInterceptor {

    fun getTag(): String

    /**
     * 这个方法运行在子线程
     */
    fun process(songInfo: SongInfo?, callback: InterceptorCallback)

    /**
     * 这个方法运行在主线程
     */
    fun process(songInfo: SongInfo?): SongInfo?
}

abstract class AsyncInterceptor : ISyInterceptor {
    override fun process(songInfo: SongInfo?): SongInfo? {
        //do nothing
        return null
    }
}

interface SyncInterceptor : ISyInterceptor {
    override fun process(songInfo: SongInfo?, callback: InterceptorCallback) {
        //do nothing
    }
}


