package com.lzx.starrysky.intercept

import com.lzx.starrysky.SongInfo

object InterceptorThread {
    const val UI = "UI"
    const val IO = "IO"
}

abstract class StarrySkyInterceptor {
    abstract fun getTag(): String
    open fun process(songInfo: SongInfo?, callback: InterceptCallback) {}
}

interface InterceptCallback {
    /**
     * 执行下一个，用于上传一个文件
     */
    fun onNext(songInfo: SongInfo?)

    /**
     * 中断
     * msg:可以添加 msg
     */
    fun onInterrupt(msg: String?)
}


