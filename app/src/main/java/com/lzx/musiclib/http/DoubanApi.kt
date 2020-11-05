package com.lzx.musiclib.http

import okhttp3.ResponseBody
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Streaming
import retrofit2.http.Url

interface DoubanApi {
    companion object {
        const val BASE_URL = "https://www.douban.com/"
        const val BASE_URL1 = "https://api.douban.com/"
    }

    @FormUrlEncoded
    @POST("service/auth2/token")
    suspend fun login(
        @Header("Content-Type") Header: String,
        @Field("apikey") apikey: String,
        @Field("client_id") client_id: String,
        @Field("client_secret") client_secret: String,
        @Field("udid") udid: String,
        @Field("douban_udid") douban_udid: String,
        @Field("device_id") device_id: String,
        @Field("grant_type") grant_type: String,
        @Field("redirect_uri") redirect_uri: String,
        @Field("username") username: String,
        @Field("password") password: String
    ): ResponseBody

    /**
     * 频道列表
     */
    @GET("v2/fm/app_channels")
    suspend fun getChannelList(
        @Query("alt") alt: String,
        @Query("app_name") app_name: String,
        @Query("apikey") apikey: String,
        @Query("client") client: String,
        @Query("client_id") client_id: String,
        @Query("icon_cate") icon_cate: String,
        @Query("udid") udid: String,
        @Query("douban_udid") douban_udid: String,
        @Query("version") version: String
    ): ResponseBody

    /**
     * 歌曲列表
     */
    @GET("v2/fm/playlist")
    suspend fun getSongList(
        @Header("Authorization") Header: String,
        @Query("channel") channel: String,
        @Query("from") from: String,
        @Query("type") type: String,
        @Query("pt") pt: String,
        @Query("kbps") kbps: String,
        @Query("formats") formats: String,
        @Query("alt") alt: String,
        @Query("app_name") app_name: String,
        @Query("apikey") apikey: String,
        @Query("client") client: String,
        @Query("client_id") client_id: String,
        @Query("icon_cate") icon_cate: String,
        @Query("udid") udid: String,
        @Query("douban_udid") douban_udid: String,
        @Query("version") version: String
    ): ResponseBody

    @Streaming
    @GET
    suspend fun downloadFile(@Url url: String): ResponseBody
}