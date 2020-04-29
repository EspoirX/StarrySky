package com.lzx.starrysky.intercept

import android.os.AsyncTask
import com.lzx.starrysky.StarrySky
import com.lzx.starrysky.provider.SongInfo
import com.lzx.starrysky.utils.MainLooper
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class InterceptorService {

    fun doInterceptions(songInfo: SongInfo?, callback: InterceptorCallback?) {
        if (StarrySky.get().interceptors().isNotEmpty()) {
            AsyncTask.THREAD_POOL_EXECUTOR.execute {
                val interceptorCounter =
                    CancelableCountDownLatch(StarrySky.get().interceptors().size)
                try {
                    doImpl(0, interceptorCounter, songInfo)
                    interceptorCounter.await(StarrySky.get().interceptorTimeOut(), TimeUnit.SECONDS)
                    when {
                        interceptorCounter.count > 0 -> {
                            callback?.onInterrupt(RuntimeException("拦截器超时啦，超时时间可通过 StarrySkyConfig 配置，默认 60 秒"))
                        }
                        null != songInfo?.tag -> {
                            callback?.onInterrupt(RuntimeException(songInfo.tag.toString()))
                        }
                        else -> {
                            callback?.onContinue(songInfo)
                        }
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    callback?.onInterrupt(ex)
                }
            }
        } else {
            callback?.onContinue(songInfo)
        }
    }

    private fun doImpl(
        index: Int, interceptorCounter: CancelableCountDownLatch, songInfo: SongInfo?
    ) {
        if (index < StarrySky.get().interceptors().size) {
            val interceptor = StarrySky.get().interceptors()[index]
            interceptor.process(songInfo, MainLooper.instance, object : InterceptorCallback {
                override fun onContinue(songInfo: SongInfo?) {
                    interceptorCounter.countDown()
                    doImpl(index + 1, interceptorCounter, songInfo) //执行下一个
                }

                override fun onInterrupt(exception: Throwable?) {
                    songInfo?.tag = if (null == exception) RuntimeException("No message.") else exception.message
                    interceptorCounter.cancel()
                }
            })
        }
    }
}

class CancelableCountDownLatch(count: Int) : CountDownLatch(count) {
    fun cancel() {
        while (count > 0) {
            countDown()
        }
    }
}