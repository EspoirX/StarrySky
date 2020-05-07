package com.lzx.musiclib.example

import android.util.Log
import android.widget.Toast
import com.lzx.musiclib.TestApplication
import com.lzx.musiclib.dslOkHttpSync
import com.lzx.starrysky.provider.SongInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

open class MusicRequest : CoroutineScope by MainScope() {

    data class MediaInfo(
        val songid: String, val songmid: String, val songname: String, val singer: String
    )

    private fun getSongList(): MutableList<MediaInfo> {
        val list = mutableListOf<MediaInfo>()
        val json = dslOkHttpSync {
            url { "https://api.qq.jsososo.com/songlist?id=7382629476" }
            onFailure { e, errorMsg ->
                Log.i("MusicRequest", "onFailure = $errorMsg")
            }
        }
        val jsonArray = JSONObject(json).getJSONObject("data").getJSONArray("songlist")
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
        return list
    }

    private fun getSongDetail(list: MutableList<MediaInfo>): MutableList<SongInfo> {
        val songList = mutableListOf<SongInfo>()
        val json = dslOkHttpSync {
            url {
                val stringBuilder = StringBuilder()
                list.forEach {
                    stringBuilder.append(it.songmid).append(",")
                }
                val result = stringBuilder.toString();
                val ids = result.substring(0, result.length - 1)
                return@url "https://api.qq.jsososo.com/song/batch?songmids=$ids"
            }
            onFailure { e, errorMsg ->
                Log.i("MusicRequest", "onFailure = $errorMsg")
            }
        }
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
            val json = dslOkHttpSync {
                url { "https://api.qq.jsososo.com/song/urls?id=$songmid" }
                onFailure { e, errorMsg ->
                    Log.i("MusicRequest", "onFailure = $errorMsg")
                }
            }
            val url = JSONObject(json).getJSONObject("data").getString(songmid)
            withContext(Dispatchers.Main) {
                callback.onSuccess(url)
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