package com.lzx.musiclib.http

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private val client: OkHttpClient
        get() {
            val builder = OkHttpClient.Builder()
            builder.connectTimeout(5L, TimeUnit.SECONDS)
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY
            builder.addInterceptor(interceptor)
            return builder.build()
        }

    fun <S> getService(serviceClass: Class<S>, baseUrl: String = ApiInterface.BASE_URL1): S {
        return Retrofit.Builder()
            .client(client)
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(serviceClass)
    }

    fun getMusicService(): ApiInterface {
        return getService(ApiInterface::class.java)
    }
}


data class MusicResponse<out T>(val data: T)
//
//suspend fun <T : Any> MusicResponse<T>.executeResponse(successBlock: (suspend CoroutineScope.() -> Unit)? = null,
//                                                       errorBlock: (suspend CoroutineScope.() -> Unit)? = null): Result<T> {
//    return coroutineScope {
//        if (errorCode == -1) {
//            errorBlock?.let { it() }
//            Result.Error(IOException(errorMsg))
//        } else {
//            successBlock?.let { it() }
//            Result.Success(data)
//        }
//    }
//}
//
//suspend fun <T : Any> MusicResponse<T>.doSuccess(successBlock: (suspend CoroutineScope.(T) -> Unit)? = null): MusicResponse<T> {
//    return coroutineScope {
//        if (errorCode != -1) successBlock?.invoke(this, this@doSuccess.data)
//        this@doSuccess
//    }
//
//}
//
//suspend fun <T : Any> MusicResponse<T>.doError(errorBlock: (suspend CoroutineScope.(String) -> Unit)? = null): MusicResponse<T> {
//    return coroutineScope {
//        if (errorCode == -1) errorBlock?.invoke(this, this@doError.errorMsg)
//        this@doError
//    }
//}