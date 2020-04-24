package com.lzx.starrysky.intercept

import com.lzx.starrysky.provider.SongInfo

class RealInterceptorChain(
    val interceptors: List<Interceptor>,
    var index: Int = 0,
    var songInfo: SongInfo?
) : Interceptor.Chain {

    override fun proceed(result: SongInfo?): SongInfo? {
        return if (index < interceptors.size) {
            val next: RealInterceptorChain = copy(index + 1, result!!)
            val interceptor = interceptors[index]
            interceptor.intercept(next)
        } else {
            result
        }
    }

    private fun copy(index: Int, songInfo: SongInfo): RealInterceptorChain {
        return RealInterceptorChain(interceptors, index, songInfo)
    }
}