package com.lzx.starrysky.intercept

import com.lzx.starrysky.provider.SongInfo

/**
 * 拦截器
 */
interface StarrySkyInterceptor {

    fun process(songInfo: SongInfo?, callback: InterceptorCallback)
}