package com.lzx.starrysky.intercept

import android.os.AsyncTask
import com.lzx.starrysky.SongInfo
import com.lzx.starrysky.utils.MainLooper

class InterceptorService {

    private var interceptors = mutableListOf<Pair<StarrySkyInterceptor, String>>()

    fun attachInterceptors(interceptors: MutableList<Pair<StarrySkyInterceptor, String>>) {
        this.interceptors.clear()
        this.interceptors.addAll(interceptors)
    }

    fun handlerInterceptor(songInfo: SongInfo?, callback: InterceptCallback?) {
        if (interceptors.isNullOrEmpty()) {
            callback?.onNext(songInfo)
        } else {
            runCatching {
                doInterceptor(0, songInfo, callback)
            }.onFailure {
                callback?.onInterrupt(it.message)
            }
        }
    }

    private fun doInterceptor(index: Int, songInfo: SongInfo?, callback: InterceptCallback?) {
        if (index < interceptors.size) {
            val pair = interceptors[index]
            val interceptor = pair.first
            val interceptThread = pair.second
            if (interceptThread == InterceptorThread.UI) {
                MainLooper.instance.runOnUiThread {
                    doInterceptImpl(interceptor, index, songInfo, callback)
                }
            } else {
                AsyncTask.THREAD_POOL_EXECUTOR.execute {
                    doInterceptImpl(interceptor, index, songInfo, callback)
                }
            }
        } else {
            MainLooper.instance.runOnUiThread {
                callback?.onNext(songInfo)
            }
        }
    }

    private fun doInterceptImpl(
        interceptor: StarrySkyInterceptor,
        index: Int,
        songInfo: SongInfo?,
        callback: InterceptCallback?
    ) {
        interceptor.process(songInfo, object : InterceptCallback {
            override fun onNext(songInfo: SongInfo?) {
                doInterceptor(index + 1, songInfo, callback)
            }

            override fun onInterrupt(msg: String?) {
                MainLooper.instance.runOnUiThread {
                    callback?.onInterrupt(msg)
                }
            }
        })
    }
}
