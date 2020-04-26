package com.lzx.starrysky.intercept

import com.lzx.starrysky.provider.SongInfo
import com.lzx.starrysky.utils.MainLooper

/**
 * 拦截器
 */
interface StarrySkyInterceptor {

    fun process(songInfo: SongInfo?, mainLooper: MainLooper, callback: InterceptorCallback)
}