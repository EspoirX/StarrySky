package com.lzx.musiclib.http

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface QQMusicApi {
    companion object {
        const val BASE_URL = "https://api.qq.jsososo.com/"
    }

    @Headers("User-Agent: Mozilla/4.0 (compatible; MSIE 7.0; Windows 7)")
    @GET("recommend/playlist/u")
    suspend fun getQQMusicRecommend(): ResponseBody

    @GET("songlist")
    suspend fun getQQMusicSongList(@Query("id") id: String)
}