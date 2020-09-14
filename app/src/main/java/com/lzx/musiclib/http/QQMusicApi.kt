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

    @Headers("User-Agent: Mozilla/4.0 (compatible; MSIE 7.0; Windows 7)")
    @GET("songlist")
    suspend fun getQQMusicSongList(@Query("id") id: String): ResponseBody

    ///song
    @Headers("User-Agent: Mozilla/4.0 (compatible; MSIE 7.0; Windows 7)")
    @GET("song/batch")
    suspend fun getQQMusicSongCover(@Query("songmids") songmids: String): ResponseBody

    @Headers("User-Agent: Mozilla/4.0 (compatible; MSIE 7.0; Windows 7)")
    @GET("song/urls")
    suspend fun getQQMusicSongUrl(@Query("id") id: String): ResponseBody
}