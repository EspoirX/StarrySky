package com.lzx.musiclib

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lzx.musiclib.bean.MusicChannel
import com.lzx.musiclib.http.BaiduApi
import com.lzx.musiclib.http.DoubanApi
import com.lzx.musiclib.http.RetrofitClient
import com.lzx.starrysky.SongInfo
import com.lzx.starrysky.utils.SpUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.json.JSONObject

/**
 * 百度音乐
 * https://www.zhihu.com/question/348928857/answer/858421542
 * http://tingapi.ting.baidu.com/v1/restserver/ting?method=baidu.ting.billboard.billList&type=11&format=json
 */
class MusicViewModel : ViewModel() {

    companion object {
        const val KEY_TOKEN = "key_access_token"
        const val KEY_EXPIRES = "key_expires_in"
    }

    fun login() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = RetrofitClient.getService(DoubanApi::class.java, DoubanApi.BASE_URL).login(
                "application/x-www-form-urlencoded",
                "02646d3fb69a52ff072d47bf23cef8fd",
                "02646d3fb69a52ff072d47bf23cef8fd",
                "cde5d61429abcd7c",
                "b88146214e19b8a8244c9bc0e2789da68955234d",
                "b635779c65b816b13b330b68921c0f8edc049590",
                "b88146214e19b8a8244c9bc0e2789da68955234d",
                "password",
                "http://www.douban.com/mobile/fm",
                "13560357097",
                "lizixian18")
            val json = result.string()
            try {
                val obj = JSONObject(json)
                SpUtil.instance.putString(KEY_TOKEN, obj.getString("access_token"))
                SpUtil.instance.putString(KEY_EXPIRES, obj.getString("expires_in"))
                getChannelList()
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    fun getChannelList() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = RetrofitClient.getDoubanMusic().getChannelList(
                "json",
                "radio_iphone",
                "02646d3fb69a52ff072d47bf23cef8fd",
                "s:mobile|y:iOS 10.2|f:115|d:b88146214e19b8a8244c9bc0e2789da68955234d|e:iPhone7,1|m:appstore",
                "02646d3fb69a52ff072d47bf23cef8fd",
                "xlarge",
                "b88146214e19b8a8244c9bc0e2789da68955234d",
                "b635779c65b816b13b330b68921c0f8edc049590",
                "115")
            val json = result.string()
            try {
                val obj = JSONObject(json)

            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    fun getSongList(channel: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = RetrofitClient.getDoubanMusic().getSongList(
                "Bearer " + SpUtil.instance.getString(KEY_TOKEN),
                "10",
                "mainsite",
                "n",
                "0.0",
                "128",
                "aac",
                "json",
                "radio_iphone",
                "02646d3fb69a52ff072d47bf23cef8fd",
                "s:mobile|y:iOS 10.2|f:115|d:b88146214e19b8a8244c9bc0e2789da68955234d|e:iPhone7,1|m:appstore",
                "02646d3fb69a52ff072d47bf23cef8fd",
                "xlarge",
                "b88146214e19b8a8244c9bc0e2789da68955234d",
                "b635779c65b816b13b330b68921c0f8edc049590",
                "115")
            val json = result.string()
            try {
                val obj = JSONObject(json)

            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    //推荐，最新，热门，歌手
    val musicChannelLiveData = MutableLiveData<MutableList<MusicChannel>>()
    fun getQQMusicRecommend() {
        viewModelScope.launch(Dispatchers.IO) {
            val recommObj = async { RetrofitClient.getQQMusic().getQQMusicRecommend() }
            val songListObj = async { RetrofitClient.getService(BaiduApi::class.java, BaiduApi.BASE_URL).getBaiduLeaderboard() }
            val recommJson = recommObj.await().string()
            val songJson = songListObj.await().string()

            val channelList = mutableListOf<MusicChannel>()
            try {
                val array = JSONObject(recommJson).getJSONObject("data").getJSONArray("list")
                array.forEach<JSONObject> {
                    val channel = MusicChannel()
                    channel.contentId = it?.getString("content_id")
                    channel.cover = it?.getString("cover")
                    channel.id = it?.getString("id")
                    channel.rcmdtemplate = it?.getString("rcmdtemplate")
                    channel.title = it?.getString("title")
                    channel.username = it?.getString("username")
                    channelList.add(channel)
                }
                val songArray = JSONObject(songJson).getJSONArray("song_list")
                val songlist = mutableListOf<SongInfo>()
                songArray.forEach<JSONObject> {
                    val songInfo = SongInfo()
                    songInfo.songId = it?.getString("song_id")
                        ?: System.currentTimeMillis().toString()
                    songInfo.songName = it?.getString("title") ?: ""
                    songInfo.artist = it?.getString("author") ?: ""
                    songInfo.songCover = it?.getString("pic_huge") ?: ""
                    songlist.add(songInfo)
                }
                songlist.shuffle()
                val channel = MusicChannel()
                channel.songList = songlist
                if (channelList.size > 3) {
                    channelList.add(3, channel)
                }
                musicChannelLiveData.postValue(channelList)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    fun getQQMusicNewList() {

    }

    fun getQQMusicHotList() {

    }

    fun getQQMusicSinger() {

    }

    val qqMusicsLiveData = MutableLiveData<MutableList<SongInfo>>()
    fun getQQMusicSongList(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = RetrofitClient.getQQMusic().getQQMusicSongList(id)
            val json = result.string()
            try {
                val obj = JSONObject(json).getJSONObject("data")
                val songArray = obj.getJSONArray("songlist")
                val songlist = mutableListOf<SongInfo>()
                songArray.forEach<JSONObject> { it ->
                    val songInfo = SongInfo()
                    songInfo.songId = it?.getString("songmid")
                        ?: System.currentTimeMillis().toString()
                    songInfo.songName = it?.getString("songname") ?: ""
                    var singer = ""
                    val singerArray = it?.getJSONArray("singer")
                    singerArray?.forEach<JSONObject> {
                        singer += it?.getString("name") + " "
                    }
                    songInfo.artist = singer
                    songInfo.headData?.put("source", "qqMusic")
                    songlist.add(songInfo)
                }

                val stringBuilder = StringBuilder()
                songlist.forEach {
                    stringBuilder.append(it.songId).append(",")
                }
                val idsValue = stringBuilder.toString()
                val ids = idsValue.substring(0, idsValue.length - 1)
                val coverResult = RetrofitClient.getQQMusic().getQQMusicSongCover(ids)
                val coverJson = coverResult.string()
                songlist.forEach {
                    val data = JSONObject(coverJson).getObj("data").getObj(it.songId)
                    val mid = data.getObj("track_info").getObj("album").getString("mid")
                    it.songCover = "https://y.gtimg.cn/music/photo_new/T002R300x300M000${mid}.jpg"
                }
                qqMusicsLiveData.postValue(songlist)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    fun getQQMusicUrl(songId: String, callback: ((url: String) -> Unit)? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = RetrofitClient.getQQMusic().getQQMusicSongUrl(songId)
            val json = result.string()
            try {
                val obj = JSONObject(json).getJSONObject("data")
                val url = obj.getString(songId)
                callback?.let { it(url) }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }


    val songInfoLiveData = MutableLiveData<SongInfo>()
    fun getBaiduMusicUrl(songId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = RetrofitClient.getService(BaiduApi::class.java, BaiduApi.BASE_URL).getSongDetail(songId)
            val json = result.string()
            try {
                val obj = JSONObject(json)
                val bitrate = obj.getJSONObject("bitrate")
                val detail = obj.getJSONObject("songinfo")
                val songInfo = SongInfo()
                songInfo.songId = detail.getString("song_id")
                songInfo.songName = detail.getString("title")
                songInfo.songCover = detail.getString("pic_huge")
                songInfo.artist = detail.getString("author")
                songInfo.songUrl = bitrate.getString("file_link")
                songInfo.duration = bitrate.getLong("file_duration") * 1000
                songInfoLiveData.postValue(songInfo)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }
}