package com.lzx.starrysky.intercept

import android.os.AsyncTask
import com.lzx.starrysky.SongInfo
import com.lzx.starrysky.utils.MainLooper
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class InterceptorService {

    private var interceptors = mutableListOf<ISyInterceptor>()

    fun attachInterceptors(interceptors: MutableList<ISyInterceptor>) {
        this.interceptors.clear()
        this.interceptors.addAll(interceptors)
    }

    fun doInterceptions(songInfo: SongInfo?, callback: InterceptorCallback?) {
        if (interceptors.isNotEmpty()) {
            AsyncTask.THREAD_POOL_EXECUTOR.execute {
                val interceptorCounter = CancelableCountDownLatch(interceptors.size)
                try {
                    doImpl(0, interceptorCounter, songInfo)
                    interceptorCounter.await(60L, TimeUnit.SECONDS)
                    when {
                        interceptorCounter.count > 0 -> {
                            callback?.onInterrupt(RuntimeException("拦截器超时啦，超时时间可通过 StarrySkyConfig 配置，默认 60 秒"))
                        }
                        null != songInfo?.tag -> {
                            callback?.onInterrupt(RuntimeException(songInfo.tag.toString()))
                        }
                        else -> {
                            MainLooper.instance.runOnUiThread {
                                callback?.onContinue(songInfo)
                            }
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
        if (index < interceptors.size) {
            val interceptor = interceptors[index]
            if (interceptor is SyncInterceptor) {
                val info = interceptor.process(songInfo)
                if (info != null) {
                    interceptorCounter.countDown()
                    doImpl(index + 1, interceptorCounter, info) //执行下一个
                } else {
                    interceptorCounter.cancel()
                }
            } else if (interceptor is AsyncInterceptor) {
                interceptor.process(songInfo, object : InterceptorCallback {
                    override fun onContinue(songInfo: SongInfo?) {
                        interceptorCounter.countDown()
                        doImpl(index + 1, interceptorCounter, songInfo) //执行下一个
                    }

                    override fun onInterrupt(exception: Throwable?) {
                        interceptorCounter.cancel()
                    }
                })
            }
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