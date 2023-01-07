package com.lzx.starrysky.intercept

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.IntDef
import com.google.common.primitives.UnsignedBytes.toInt
import com.lzx.starrysky.SongInfo
import java.util.*


object InterceptorThread {
    const val UI = "UI"
    const val IO = "IO"
}

object InterceptorPlace {
    const val BEFORE = 1
    const val AFTER = 2
}



@IntDef(InterceptorPlace.AFTER, InterceptorPlace.BEFORE)
@Retention(AnnotationRetention.SOURCE)
annotation class InterceptorPlaceFlag

abstract class StarrySkyInterceptor {
    abstract fun getTag(): String
    open fun process(songInfo: SongInfo?, callback: InterceptCallback) {}
    @InterceptorPlaceFlag
    open fun getInterceptorPosition() = InterceptorPlace.BEFORE


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


