package com.lzx.musiclib.base

import android.util.Log
import android.widget.Toast
import com.lzx.musiclib.TestApplication
import com.lzx.musiclib.dslOkHttpSync
import com.lzx.musiclib.forEach
import com.lzx.musiclib.getArray
import com.lzx.musiclib.getObj
import com.lzx.musiclib.getOrNull
import com.lzx.musiclib.toJsonObj
import com.lzx.starrysky.SongInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject



open class MusicRequest : CoroutineScope by MainScope() {

    data class MediaInfo(
        val songid: String, val songmid: String, val songname: String, val singer: String
    )

    /**
     * 获取列表
     */
    private fun getSongList(): MutableList<MediaInfo> {
        val list = mutableListOf<MediaInfo>()
        val json = dslOkHttpSync {
            url { "https://api.qq.jsososo.com/songlist?id=7382629476" }
            onFailure { e, errorMsg ->
                Log.i("MusicRequest", "onFailure = $errorMsg")
            }
        }
        json.toJsonObj().getObj("data").getArray("songlist").forEach<JSONObject> {
            val singer = it?.getArray("singer")
            val artist = singer?.getOrNull<JSONObject>(0)?.getString("name")
            val media = MediaInfo(
                it?.getString("songid") ?: "",
                it?.getString("songmid") ?: "",
                it?.getString("songname") ?: "",
                artist ?: "未知歌手"
            )
            list.add(media)
        }
        return list
    }


    /**
     * 获取信息
     */
    private fun getSongDetail(list: MutableList<MediaInfo>): MutableList<SongInfo> {
        val songList = mutableListOf<SongInfo>()
        val json = dslOkHttpSync {
            url {
                val stringBuilder = StringBuilder()
                list.forEach {
                    stringBuilder.append(it.songmid).append(",")
                }
                val result = stringBuilder.toString()
                val ids = result.substring(0, result.length - 1)
                return@url "https://api.qq.jsososo.com/song/batch?songmids=$ids"
            }
            onFailure { e, errorMsg ->
                Log.i("MusicRequest", "onFailure = $errorMsg")
            }
        }
        val jsonObject = json.toJsonObj()
        list.forEach {
            val songInfo = SongInfo()
            val data = jsonObject.getObj("data").getObj(it.songmid)
            val mid = data.getObj("track_info").getObj("album").getString("mid")
            val songCover = "https://y.gtimg.cn/music/photo_new/T002R300x300M000${mid}.jpg"
            songInfo.songId = it.songmid
            songInfo.songName = it.songname
            songInfo.artist = it.singer
            songInfo.songCover = songCover
            songList.add(songInfo)
        }
        return songList
    }

    /**
     * 获取歌单列表信息
     */
    fun requestSongList(callback: RequestCallback) {
        launch(Dispatchers.IO) {
            val list = async { getSongList() }
            val songList = async { getSongDetail(list.await()) }
            val result = songList.await()
            withContext(Dispatchers.Main) {
                if (result.isEmpty()) {
                    Toast.makeText(TestApplication.context, "请求失败", Toast.LENGTH_SHORT).show()
                }
                callback.onSuccess(songList.await())
            }
        }
    }

    /**
     * 获取播放url
     */
    fun requestSongUrl(songmid: String, callback: RequestInfoCallback) {
        launch(Dispatchers.IO) {
            val json = dslOkHttpSync {
                url { "https://api.qq.jsososo.com/song/urls?id=$songmid" }
                onFailure { e, errorMsg ->
                    Log.i("MusicRequest", "onFailure = $errorMsg")
                }
            }
            val url = json.toJsonObj().getObj("data").getString(songmid)
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

    fun clear() {
        cancel()
    }
}