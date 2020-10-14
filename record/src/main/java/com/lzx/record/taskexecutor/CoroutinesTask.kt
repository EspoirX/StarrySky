package com.lzx.record.taskexecutor

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * 必须调用[run]才会执行
 *
 * @param mHeavyFunction 需要异步执行的方法
 * 如果是耗时较长的方法，最好通过[CoroutineScope.isActive]判断任务是否还需要继续执行（可能被cancel），否则一旦任务已经开始无法中止
 *
 * Created by liaodongming on 2018/6/13.
 */

class CoroutinesTask<T>(private val mHeavyFunction: (scope: CoroutineScope) -> T) {

    companion object {
        @JvmField
        val UI: CoroutineContext = Dispatchers.Main

        @JvmField
        val BG: CoroutineContext = Dispatchers.Default
    }

    private var mOnError: ((error: Throwable?) -> Unit)? = null
    private var mOnResponse: ((response: T?) -> Unit)? = null

    private var mErrorContext: CoroutineContext = UI
    private var mResponseContext: CoroutineContext = UI
    private var mRunContext: CoroutineContext = Dispatchers.Default

    fun errorOn(contextType: CoroutineContext): CoroutinesTask<T> {
        mErrorContext = contextType
        return this
    }

    fun responseOn(contextType: CoroutineContext): CoroutinesTask<T> {
        mResponseContext = contextType
        return this
    }

    fun runOn(contextType: CoroutineContext): CoroutinesTask<T> {
        mRunContext = contextType
        return this
    }

    fun onError(onError: (error: Throwable?) -> Unit): CoroutinesTask<T> {
        mOnError = onError
        return this
    }

    fun onResponse(onResponse: (response: T?) -> Unit): CoroutinesTask<T> {
        mOnResponse = onResponse
        return this
    }

    fun run(): CoroutinesJob? {
        return runDelay(0)
    }

    fun runDelay(time: Long): CoroutinesJob? {
        var job: Job? = null
        try {
            job = GlobalScope.launch(mRunContext) {
                if (isActive) {
                    delay(time)
                    try {
                        val result: T = mHeavyFunction(this)
                        mOnResponse?.run { launch(mResponseContext) { invoke(result) } }
                    } catch (e: Exception) {
                        mOnError?.run { launch(mErrorContext) { invoke(e) } }
                            ?: launch(UI) { throw e }
                    }
                }
            }
        } catch (e: Exception) {
            mOnError?.run { GlobalScope.launch(mErrorContext) { invoke(e) } }
                ?: GlobalScope.launch(UI) { throw e }
        }
        return CoroutinesJob(job)
    }
}

/**
 * 兼容java不能调用默认参数的方法，给Job包一个类，使得java可以方便的调用cancel(), etc.
 *  job.cancel();
 *  job.cancel("我不想执行这个job了");
 *  job.cancel(throwable);
 */
class CoroutinesJob() {
    private var mJob: Job? = null

    constructor(job: Job?) : this() {
        mJob = job
    }

    fun cancel() {
        mJob?.cancel()
    }

    fun cancel(throwable: Throwable) {
        mJob?.cancel(throwable as? CancellationException)
    }

    fun cancel(reason: String) {
        mJob?.cancel(CancellationException(reason))
    }
}

fun <T> heavy(heavyFunction: (CoroutineScope) -> T): CoroutinesTask<T> {
    return CoroutinesTask(heavyFunction)
}

fun runOnMainThread(function: (scope: CoroutineScope) -> Unit): CoroutinesJob {
    var job: Job? = null
    try {
        job = GlobalScope.launch(Dispatchers.Main) {
            if (isActive) {
                function.invoke(this)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return CoroutinesJob(job)
}