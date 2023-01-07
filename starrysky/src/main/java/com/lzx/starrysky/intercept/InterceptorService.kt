package com.lzx.starrysky.intercept

import android.os.AsyncTask
import com.lzx.starrysky.SongInfo
import com.lzx.starrysky.utils.MainLooper

class InterceptorService {

    /**
     * 拦截器+拦截线程的集合
     */
    private var interceptors = mutableListOf<Pair<StarrySkyInterceptor, String>>()

    /**
     * 配置拦截器
     * @param interceptors 拦截器列表
     */
    fun attachInterceptors(interceptors: MutableList<Pair<StarrySkyInterceptor, String>>) {
        this.interceptors.clear()
        this.interceptors.addAll(interceptors)
    }

    /**
     * 执行拦截流程
     * @param songInfo 音频信息
     * @param interceptorPlace 拦截位置
     * @param callback 拦截结果回调
     */
    fun handlerInterceptor(songInfo: SongInfo?,@InterceptorPlaceFlag interceptorPlace:Int, callback: InterceptCallback?) {
        if (interceptors.isNullOrEmpty()) {
            callback?.onNext(songInfo)
        } else {
            runCatching {
                doInterceptor(0, songInfo,interceptorPlace, callback)
            }.onFailure {
                callback?.onInterrupt(it.message)
            }
        }
    }

    /**
     * 执行拦截策略
     */
    private fun doInterceptor(index: Int, songInfo: SongInfo?,@InterceptorPlaceFlag place:Int, callback: InterceptCallback?) {
        if (index < interceptors.size) {
            val pair = interceptors[index]
            val interceptor = pair.first
            val interceptThread = pair.second
            if(interceptor.getInterceptorPosition() != place){
                doInterceptor(index + 1, songInfo,place, callback)
            }else{
                if (interceptThread == InterceptorThread.UI) {
                    MainLooper.instance.runOnUiThread {
                        doInterceptImpl(interceptor, index, songInfo,place, callback)
                    }
                } else {
                    AsyncTask.THREAD_POOL_EXECUTOR.execute {
                        doInterceptImpl(interceptor, index, songInfo,place, callback)
                    }
                }
            }

        } else {
            MainLooper.instance.runOnUiThread {
                callback?.onNext(songInfo)
            }
        }
    }

    /**
     * 拦截操作具体实现
     */
    private fun doInterceptImpl(
        interceptor: StarrySkyInterceptor,
        index: Int,
        songInfo: SongInfo?,
        @InterceptorPlaceFlag interceptorPlace:Int,
        callback: InterceptCallback?
    ) {
        interceptor.process(songInfo, object : InterceptCallback {
            override fun onNext(songInfo: SongInfo?) {
                doInterceptor(index + 1, songInfo,interceptorPlace, callback)
            }

            override fun onInterrupt(msg: String?) {
                MainLooper.instance.runOnUiThread {
                    callback?.onInterrupt(msg)
                }
            }
        })
    }
}
