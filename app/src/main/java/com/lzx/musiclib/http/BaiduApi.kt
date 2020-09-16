package com.lzx.musiclib.http

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface BaiduApi {

    companion object {
        const val BASE_URL = "http://tingapi.ting.baidu.com/"
    }

    @Headers("User-Agent: Mozilla/4.0 (compatible; MSIE 7.0; Windows 7)")
    @GET("v1/restserver/ting?method=baidu.ting.billboard.billList&type=11&format=json")
    suspend fun getBaiduLeaderboard(): ResponseBody

    @Headers("User-Agent: Mozilla/4.0 (compatible; MSIE 7.0; Windows 7)")
    @GET("v1/restserver/ting/song/playAAC")
    suspend fun getSongDetail(@Query("songid") songId: String): ResponseBody


    @Headers("User-Agent: Mozilla/4.0 (compatible; MSIE 7.0; Windows 7)")
    @GET("v1/restserver/ting/billboard/billList?size=20&offset=0")
    suspend fun getBaiduMusicList(@Query("type") type: String): ResponseBody

}