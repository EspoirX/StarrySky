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

    fun <S> getService(serviceClass: Class<S>, baseUrl: String = DoubanApi.BASE_URL1): S {
        return Retrofit.Builder()
                .client(client)
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(serviceClass)
    }

    fun getDoubanMusic(): DoubanApi {
        return getService(DoubanApi::class.java)
    }

    fun getQQMusic(): QQMusicApi {
        return getService(QQMusicApi::class.java, QQMusicApi.BASE_URL)
    }
}

