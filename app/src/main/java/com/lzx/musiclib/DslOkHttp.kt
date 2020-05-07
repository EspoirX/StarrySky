package com.lzx.musiclib

import okhttp3.Call
import okhttp3.Callback
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class DslOkHttp(var async: Boolean) {

    private val client: OkHttpClient = OkHttpClient().newBuilder()
        .addInterceptor(object : Interceptor {
            override fun intercept(chain: Interceptor.Chain): Response {
                val newRequest = chain.request().newBuilder()
                    .removeHeader("User-Agent")
                    .addHeader("User-Agent",
                        "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:0.9.4)")
                    .build()
                return chain.proceed(newRequest)
            }
        }).build()

    private var url: String = ""
    private var callSuccess: ((String) -> Unit)? = null
    private var callFailure: ((e: Throwable?, errorMsg: String) -> Unit)? = null

    fun url(init: DslOkHttp.() -> String) {
        url = init()
        if (async) {
            doRequest()
        }
    }

    fun onSuccess(onSuccess: (String) -> Unit) {
        callSuccess = onSuccess
    }

    fun onFailure(onFailure: (e: Throwable?, errorMsg: String) -> Unit) {
        callFailure = onFailure
    }

    fun doRequestSync(): String {
        var json = ""
        val request = Request.Builder().url(url).build()
        try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                json = response.body!!.string()
                callSuccess?.invoke(json)
            } else {
                callFailure?.invoke(null, "response is Fail")
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            callFailure?.invoke(ex, ex.message.toString())
        }
        return json
    }

    private fun doRequest() {
        val request = Request.Builder().url(url).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callFailure?.invoke(e, e.message.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                callSuccess?.invoke(response.body!!.string())
            }
        })
    }
}

fun dslOkHttpAsync(init: DslOkHttp.() -> Unit): DslOkHttp {
    val http = DslOkHttp(true)
    http.init()
    return http
}

fun dslOkHttpSync(init: DslOkHttp.() -> Unit): String {
    val http = DslOkHttp(false)
    http.init()
    return http.doRequestSync()
}