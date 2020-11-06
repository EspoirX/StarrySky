package com.lzx.starrysky.intercept

import com.lzx.basecode.SongInfo
import com.lzx.basecode.MainLooper

/**
 * 拦截器
 */
interface StarrySkyInterceptor {

    /**
     * 这个方法运行在子线程，可通过 MainLooper 去切换到主线程
     */
    fun process(songInfo: SongInfo?, mainLooper: MainLooper, callback: InterceptorCallback)
}