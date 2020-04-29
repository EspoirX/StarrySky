package com.lzx.musiclib.example

import android.widget.Toast
import com.lzx.musiclib.TestApplication
import com.lzx.starrysky.provider.SongInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject

open class MusicRequest : CoroutineScope by MainScope() {
    private val client: OkHttpClient

    init {
        client = OkHttpClient().newBuilder()
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
    }

    data class MediaInfo(
        val songid: String, val songmid: String, val songname: String, val singer: String
    )

    private fun getSongList(): MutableList<MediaInfo> {
        val list = mutableListOf<MediaInfo>()
        try {
            val url = "https://api.qq.jsososo.com/songlist?id=7382629476"
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val json = response.body!!.string()
                val jsonObject = JSONObject(json)
                val jsonArray = jsonObject.getJSONObject("data").getJSONArray("songlist")
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val singer = obj.getJSONArray("singer")
                    val artist = if (singer.length() > 0) {
                        singer.getJSONObject(0).getString("name")
                    } else {
                        "未知歌手"
                    }
                    val media = MediaInfo(
                        obj.getString("songid"),
                        obj.getString("songmid"),
                        obj.getString("songname"),
                        artist
                    )
                    list.add(media)
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return list
    }

    private fun getSongDetail(list: MutableList<MediaInfo>): MutableList<SongInfo> {
        val songList = mutableListOf<SongInfo>()
        try {
            val stringBuilder = StringBuilder()
            list.forEach {
                stringBuilder.append(it.songmid).append(",")
            }
            val result = stringBuilder.toString();
            val ids = result.substring(0, result.length - 1)
            val url = "https://api.qq.jsososo.com/song/batch?songmids=$ids"
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val json = response.body!!.string()
                val jsonObject = JSONObject(json)
                list.forEach {
                    val songInfo = SongInfo()
                    val data = jsonObject.getJSONObject("data").getJSONObject(it.songmid)
                    val mid =
                        data.getJSONObject("track_info").getJSONObject("album").getString("mid")
                    val songCover = "https://y.gtimg.cn/music/photo_new/T002R300x300M000${mid}.jpg"
                    songInfo.songId = it.songmid
                    songInfo.songName = it.songname
                    songInfo.artist = it.singer
                    songInfo.songCover = songCover
                    songList.add(songInfo)
                }
            }
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
        }
        return songList
    }

    fun requestSongList(callback: RequestCallback) {
        launch(Dispatchers.IO) {
            val list = async { getSongList() }
            val songList = async { getSongDetail(list.await()) }
            withContext(Dispatchers.Main) {
                val result = songList.await()
                if (result.isEmpty()) {
                    Toast.makeText(TestApplication.context, "请求失败", Toast.LENGTH_SHORT).show();
                }
                callback.onSuccess(songList.await())
            }
        }
    }

    fun requestSongUrl(songmid: String, callback: RequestInfoCallback) {
        launch(Dispatchers.IO) {
            val url = "https://api.qq.jsososo.com/song/urls?id=$songmid"
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val json = response.body!!.string()
                val jsonObject = JSONObject(json)
                val url = jsonObject.getJSONObject("data").getString(songmid)
                withContext(Dispatchers.Main) {
                    callback.onSuccess(url)
                }
            }
        }
    }

    interface RequestCallback {
        fun onSuccess(list: MutableList<SongInfo>)
    }

    interface RequestInfoCallback {
        fun onSuccess(songUrl: String)
    }
}