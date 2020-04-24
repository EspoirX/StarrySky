package com.lzx.starrysky.intercept

import com.lzx.starrysky.provider.SongInfo

/**
 * 拦截器
 */
interface Interceptor {

    fun intercept(chain: Chain): SongInfo?

    interface Chain {
        fun proceed(result: SongInfo?): SongInfo?
    }
}