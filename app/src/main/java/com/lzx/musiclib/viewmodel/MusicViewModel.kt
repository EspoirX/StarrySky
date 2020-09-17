package com.lzx.musiclib.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lzx.musiclib.bean.HotSongInfo
import com.lzx.musiclib.bean.MusicBanner
import com.lzx.musiclib.bean.MusicChannel
import com.lzx.musiclib.forEach
import com.lzx.musiclib.getArray
import com.lzx.musiclib.getObj
import com.lzx.musiclib.http.BaiduApi
import com.lzx.musiclib.http.DoubanApi
import com.lzx.musiclib.http.RetrofitClient
import com.lzx.musiclib.toJsonObj
import com.lzx.starrysky.SongInfo
import com.lzx.starrysky.StarrySky
import com.lzx.starrysky.utils.SpUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
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
                SpUtil.instance?.putString(KEY_TOKEN, obj.getString("access_token"))
                SpUtil.instance?.putString(KEY_EXPIRES, obj.getString("expires_in"))
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
                "Bearer " + SpUtil.instance?.getString(KEY_TOKEN),
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
                qqMusicsLiveData.postValue(songlist)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    fun getQQMusicSongCover(mid: String, callback: ((cover: String) -> Unit)? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val coverResult = RetrofitClient.getQQMusic().getQQMusicSongCover(mid)
            val coverJson = coverResult.string()
            try {
                val obj = JSONObject(coverJson).getJSONObject("data")
                val mid = obj.getObj("track_info").getObj("album").getString("mid")
                val songCover = "https://y.gtimg.cn/music/photo_new/T002R300x300M000${mid}.jpg"
                callback?.let { it(songCover) }
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
    fun getBaiduMusicUrl(songId: String, callback: ((info: SongInfo) -> Unit)? = null) {
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
                callback?.let { it(songInfo) }
                songInfoLiveData.postValue(songInfo)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    val hotListLiveData = MutableLiveData<Pair<MutableList<MusicBanner>, MutableList<HotSongInfo>>>()
    fun getBaiduRankList() {
        viewModelScope.launch(Dispatchers.IO) {
            val bannerArray = async { RetrofitClient.getQQMusic().getQQMusicBanner() }.await()
                .string().toJsonObj().getArray("data")
            val oneArray = async { getBaiduMusicListById("1") }.await().string().toJsonObj()
                .getArray("song_list")
            val twoArray = async { getBaiduMusicListById("2") }.await().string().toJsonObj()
                .getArray("song_list")
            val threeArray = async { getBaiduMusicListById("21") }.await().string().toJsonObj()
                .getArray("song_list")
            val fourArray = async { getBaiduMusicListById("23") }.await().string().toJsonObj()
                .getArray("song_list")
            try {
                val bannerList = mutableListOf<MusicBanner>()
                bannerArray.forEach<JSONObject> {
                    val banner = MusicBanner()
                    banner.type = it?.getString("type")
                    banner.id = it?.getString("id")
                    banner.picUrl = it?.getString("picUrl")
                    banner.h5Url = it?.getString("h5Url")
                    banner.typeStr = it?.getString("typeStr")
                    bannerList.add(banner)
                }
                val list = mutableListOf<HotSongInfo>()
                val hotSongInfo1 = HotSongInfo()
                val songList1 = mutableListOf<SongInfo>()
                hotSongInfo1.title = "寻找心动的故事🔥"
                oneArray.forEach<JSONObject> {
                    val songInfo = SongInfo()
                    songInfo.songId = it?.getString("song_id")
                        ?: System.currentTimeMillis().toString()
                    songInfo.songName = it?.getString("title") ?: ""
                    songInfo.artist = it?.getString("author") ?: ""
                    songInfo.songCover = it?.getString("pic_huge") ?: ""
                    songInfo.headData?.put("source", "baiduMusic")
                    songList1.add(songInfo)
                }
                songList1.shuffle()
                hotSongInfo1.infoList = songList1
                list.add(hotSongInfo1)

                val hotSongInfo2 = HotSongInfo()
                val songList2 = mutableListOf<SongInfo>()
                hotSongInfo2.title = "治愈小酒馆🍸"
                twoArray.forEach<JSONObject> {
                    val songInfo = SongInfo()
                    songInfo.songId = it?.getString("song_id")
                        ?: System.currentTimeMillis().toString()
                    songInfo.songName = it?.getString("title") ?: ""
                    songInfo.artist = it?.getString("author") ?: ""
                    songInfo.songCover = it?.getString("pic_huge") ?: ""
                    songInfo.headData?.put("source", "baiduMusic")
                    songList2.add(songInfo)
                }
                songList2.shuffle()
                hotSongInfo2.infoList = songList2
                list.add(hotSongInfo2)

                val hotSongInfo3 = HotSongInfo()
                val songList3 = mutableListOf<SongInfo>()
                hotSongInfo3.title = "陪你说晚安🌙"
                threeArray.forEach<JSONObject> {
                    val songInfo = SongInfo()
                    songInfo.songId = it?.getString("song_id")
                        ?: System.currentTimeMillis().toString()
                    songInfo.songName = it?.getString("title") ?: ""
                    songInfo.artist = it?.getString("author") ?: ""
                    songInfo.songCover = it?.getString("pic_huge") ?: ""
                    songInfo.headData?.put("source", "baiduMusic")
                    songList3.add(songInfo)
                }
                songList3.shuffle()
                hotSongInfo3.infoList = songList3
                list.add(hotSongInfo3)

                val hotSongInfo4 = HotSongInfo()
                val songList4 = mutableListOf<SongInfo>()
                fourArray.forEach<JSONObject> {
                    val songInfo = SongInfo()
                    songInfo.songId = it?.getString("song_id")
                        ?: System.currentTimeMillis().toString()
                    songInfo.songName = it?.getString("title") ?: ""
                    songInfo.artist = it?.getString("author") ?: ""
                    songInfo.songCover = it?.getString("pic_huge") ?: ""
                    songInfo.headData?.put("source", "baiduMusic")
                    songList4.add(songInfo)
                }
                songList4.shuffle()
                hotSongInfo4.infoList = songList4
                list.add(hotSongInfo4)

                hotListLiveData.postValue(Pair(bannerList, list))
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    fun playWhenStartApp() {
        viewModelScope.launch(Dispatchers.IO) {
            val list = mutableListOf<SongInfo>()
            val array = getBaiduMusicListById("25").string().toJsonObj().getArray("song_list")
            array.forEach<JSONObject> {
                val songInfo = SongInfo()
                songInfo.songId = it?.getString("song_id") ?: System.currentTimeMillis().toString()
                songInfo.songName = it?.getString("title") ?: ""
                songInfo.artist = it?.getString("author") ?: ""
                songInfo.songCover = it?.getString("pic_huge") ?: ""
                songInfo.headData?.put("source", "baiduMusic")
                list.add(songInfo)
            }
            list.shuffle()
            withContext(Dispatchers.Main) {
                Log.i("XIAN", "list = " + list.size)
                StarrySky.with().playMusic(list, 0)
            }
        }
    }

    private suspend fun getBaiduMusicListById(type: String): ResponseBody {
        return RetrofitClient.getService(BaiduApi::class.java, BaiduApi.BASE_URL).getBaiduMusicList(type)
    }


}