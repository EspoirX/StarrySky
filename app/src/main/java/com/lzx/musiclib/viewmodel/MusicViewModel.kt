package com.lzx.musiclib.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lzx.musiclib.SpConstant
import com.lzx.musiclib.bean.MusicChannel
import com.lzx.musiclib.forEach
import com.lzx.musiclib.getArray
import com.lzx.musiclib.getObj
import com.lzx.musiclib.http.DoubanApi
import com.lzx.musiclib.http.RetrofitClient
import com.lzx.starrysky.SongInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

/**
 * 百度音乐
 * https://www.zhihu.com/question/348928857/answer/858421542
 * http://tingapi.ting.baidu.com/v1/restserver/ting?method=baidu.ting.billboard.billList&type=11&format=json
 */
class MusicViewModel : ViewModel() {

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
                SpConstant.KEY_TOKEN = obj.getString("access_token")
                SpConstant.KEY_EXPIRES = obj.getString("expires_in")
                getChannelList()
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    val musicChannels = MutableLiveData<MutableList<MusicChannel>>()
    private fun getChannelList() {
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
                val chls = mutableListOf<MusicChannel>()
                JSONObject(json).getArray("groups").forEach<JSONObject> { it ->
                    it?.getArray("chls")?.forEach<JSONObject> {
                        val channel = MusicChannel()
                        channel.bgColor = it?.getObj("style")?.getString("bg_color")
                        channel.name = it?.getString("name").orEmpty()
                        channel.cover = it?.getString("cover").orEmpty()
                        channel.intro = it?.getString("intro").orEmpty()
                        channel.songNum = it?.takeIf { it.has("song_num") }?.getInt("song_num") ?: 0
                        channel.id = it?.getInt("id") ?: 0
                        chls.add(channel)
                    }
                }
                musicChannels.postValue(chls)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    val songInfos = MutableLiveData<MutableList<SongInfo>>()
    fun getSongList(channel: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = RetrofitClient.getDoubanMusic().getSongList(
                "Bearer " + SpConstant.KEY_TOKEN,
                channel.toString(),
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
                val list = mutableListOf<SongInfo>()
                JSONObject(json).getArray("song").forEach<JSONObject> { it ->
                    val songInfo = SongInfo()
                    songInfo.songName = it?.getString("title").orEmpty()
                    songInfo.songId = it?.getString("aid").orEmpty()
                    songInfo.songCover = it?.getString("picture").orEmpty()
                    songInfo.songUrl = it?.getString("url").orEmpty()
                    songInfo.artist = it?.getArray("singers")
                        ?.takeIf { it.length() > 0 }?.getJSONObject(0)
                        ?.getString("name").orEmpty()
                    songInfo.duration = (it?.getInt("length")?.toLong() ?: 0L) * 1000
                    list.add(songInfo)
                }
                songInfos.postValue(list)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

//    //推荐，最新，热门，歌手
//    val musicChannelLiveData = MutableLiveData<MutableList<MusicChannel>>()
//    fun getQQMusicRecommend() {
//        viewModelScope.launch(Dispatchers.IO) {
//            val recommObj = async { RetrofitClient.getQQMusic().getQQMusicRecommend() }
//            val songListObj = async { RetrofitClient.getService(BaiduApi::class.java, BaiduApi.BASE_URL).getBaiduLeaderboard() }
//            val recommJson = recommObj.await().string()
//            val songJson = songListObj.await().string()
//
//            val channelList = mutableListOf<MusicChannel>()
//            try {
//                val array = JSONObject(recommJson).getJSONObject("data").getJSONArray("list")
//                array.forEach<JSONObject> {
//                    val channel = MusicChannel()
//                    channel.contentId = it?.getString("content_id")
//                    channel.cover = it?.getString("cover")
//                    channel.id = it?.getString("id")
//                    channel.rcmdtemplate = it?.getString("rcmdtemplate")
//                    channel.title = it?.getString("title")
//                    channel.username = it?.getString("username")
//                    channelList.add(channel)
//                }
//                val songArray = JSONObject(songJson).getJSONArray("song_list")
//                val songlist = mutableListOf<SongInfo>()
//                songArray.forEach<JSONObject> {
//                    val songInfo = SongInfo()
//                    songInfo.songId = it?.getString("song_id")
//                        ?: System.currentTimeMillis().toString()
//                    songInfo.songName = it?.getString("title") ?: ""
//                    songInfo.artist = it?.getString("author") ?: ""
//                    songInfo.songCover = it?.getString("pic_huge") ?: ""
//                    songlist.add(songInfo)
//                }
//                songlist.shuffle()
//                val channel = MusicChannel()
//                channel.songList = songlist
//                if (channelList.size > 3) {
//                    channelList.add(3, channel)
//                }
//                musicChannelLiveData.postValue(channelList)
//            } catch (ex: Exception) {
//                ex.printStackTrace()
//                TestApplication.context?.showToast("请求失败：" + ex.message)
//            }
//        }
//    }
//
//    val qqMusicsLiveData = MutableLiveData<MutableList<SongInfo>>()
//    fun getQQMusicSongList(id: String) {
//        viewModelScope.launch(Dispatchers.IO) {
//            try {
//                val result = RetrofitClient.getQQMusic().getQQMusicSongList(id)
//                val json = result.string()
//                val obj = JSONObject(json).getJSONObject("data")
//                val songArray = obj.getJSONArray("songlist")
//                val songlist = mutableListOf<SongInfo>()
//                songArray.forEach<JSONObject> { it ->
//                    val songInfo = SongInfo()
//                    songInfo.songId = it?.getString("songmid")
//                        ?: System.currentTimeMillis().toString()
//                    songInfo.songName = it?.getString("songname") ?: ""
//                    var singer = ""
//                    val singerArray = it?.getJSONArray("singer")
//                    singerArray?.forEach<JSONObject> {
//                        singer += it?.getString("name") + " "
//                    }
//                    songInfo.artist = singer
//                    songInfo.headData?.put("source", "qqMusic")
//                    songlist.add(songInfo)
//                }
//                qqMusicsLiveData.postValue(songlist)
//            } catch (ex: Exception) {
//                ex.printStackTrace()
//                TestApplication.context?.showToast("请求失败：" + ex.message)
//            }
//        }
//    }
//
//    fun getQQMusicSongCover(mid: String, callback: ((cover: String) -> Unit)? = null) {
//        viewModelScope.launch(Dispatchers.IO) {
//            try {
//                val coverResult = RetrofitClient.getQQMusic().getQQMusicSongCover(mid)
//                val coverJson = coverResult.string()
//                val obj = JSONObject(coverJson).getJSONObject("data")
//                val mid = obj.getObj("track_info").getObj("album").getString("mid")
//                val songCover = "https://y.gtimg.cn/music/photo_new/T002R300x300M000${mid}.jpg"
//                callback?.let { it(songCover) }
//            } catch (ex: Exception) {
//                ex.printStackTrace()
//                TestApplication.context?.showToast("请求失败：" + ex.message)
//            }
//        }
//    }
//
//    fun getQQMusicUrl(songId: String, callback: ((url: String) -> Unit)? = null) {
//        viewModelScope.launch(Dispatchers.IO) {
//            try {
//                val result = RetrofitClient.getQQMusic().getQQMusicSongUrl(songId)
//                val json = result.string()
//                val obj = JSONObject(json).getJSONObject("data")
//                val url = obj.getString(songId)
//                callback?.let { it(url) }
//            } catch (ex: Exception) {
//                ex.printStackTrace()
//                TestApplication.context?.showToast("请求失败：" + ex.message)
//            }
//        }
//    }
//
//
//    val songInfoLiveData = MutableLiveData<SongInfo>()
//    fun getBaiduMusicUrl(songId: String, callback: ((info: SongInfo) -> Unit)? = null) {
//        viewModelScope.launch(Dispatchers.IO) {
//            try {
//                val result = RetrofitClient.getService(BaiduApi::class.java, BaiduApi.BASE_URL).getSongDetail(songId)
//                val json = result.string()
//                val obj = JSONObject(json)
//                val bitrate = obj.getJSONObject("bitrate")
//                val detail = obj.getJSONObject("songinfo")
//                val songInfo = SongInfo()
//                songInfo.songId = detail.getString("song_id")
//                songInfo.songName = detail.getString("title")
//                songInfo.songCover = detail.getString("pic_huge")
//                songInfo.artist = detail.getString("author")
//                songInfo.songUrl = bitrate.getString("file_link")
//                songInfo.duration = bitrate.getLong("file_duration") * 1000
//                callback?.let { it(songInfo) }
//                songInfoLiveData.postValue(songInfo)
//            } catch (ex: Exception) {
//                ex.printStackTrace()
//                TestApplication.context?.showToast("请求失败：" + ex.message)
//            }
//        }
//    }
//
//    val hotListLiveData = MutableLiveData<Pair<MutableList<MusicBanner>, MutableList<HotSongInfo>>>()
//    fun getBaiduRankList() {
//        viewModelScope.launch(Dispatchers.IO) {
//            try {
//                val bannerArray = async { RetrofitClient.getQQMusic().getQQMusicBanner() }.await()
//                    .string().toJsonObj().getArray("data")
//                val oneArray = async { getBaiduMusicListById("1") }.await().string().toJsonObj()
//                    .getArray("song_list")
//                val twoArray = async { getBaiduMusicListById("2") }.await().string().toJsonObj()
//                    .getArray("song_list")
//                val threeArray = async { getBaiduMusicListById("21") }.await().string().toJsonObj()
//                    .getArray("song_list")
//                val fourArray = async { getBaiduMusicListById("23") }.await().string().toJsonObj()
//                    .getArray("song_list")
//                val bannerList = mutableListOf<MusicBanner>()
//                bannerArray.forEach<JSONObject> {
//                    val banner = MusicBanner()
//                    banner.type = it?.getString("type")
//                    banner.id = it?.getString("id")
//                    banner.picUrl = it?.getString("picUrl")
////                    banner.h5Url = it?.getString("h5Url")
////                    banner.typeStr = it?.getString("typeStr")
//                    bannerList.add(banner)
//                }
//                val list = mutableListOf<HotSongInfo>()
//                val hotSongInfo1 = HotSongInfo()
//                val songList1 = mutableListOf<SongInfo>()
//                hotSongInfo1.title = "寻找心动的故事🔥"
//                oneArray.forEach<JSONObject> {
//                    val songInfo = SongInfo()
//                    songInfo.songId = it?.getString("song_id")
//                        ?: System.currentTimeMillis().toString()
//                    songInfo.songName = it?.getString("title") ?: ""
//                    songInfo.artist = it?.getString("author") ?: ""
//                    songInfo.songCover = it?.getString("pic_huge") ?: ""
//                    songInfo.headData?.put("source", "baiduMusic")
//                    songList1.add(songInfo)
//                }
//                songList1.shuffle()
//                hotSongInfo1.infoList = songList1
//                list.add(hotSongInfo1)
//
//                val hotSongInfo2 = HotSongInfo()
//                val songList2 = mutableListOf<SongInfo>()
//                hotSongInfo2.title = "治愈小酒馆🍸"
//                twoArray.forEach<JSONObject> {
//                    val songInfo = SongInfo()
//                    songInfo.songId = it?.getString("song_id")
//                        ?: System.currentTimeMillis().toString()
//                    songInfo.songName = it?.getString("title") ?: ""
//                    songInfo.artist = it?.getString("author") ?: ""
//                    songInfo.songCover = it?.getString("pic_huge") ?: ""
//                    songInfo.headData?.put("source", "baiduMusic")
//                    songList2.add(songInfo)
//                }
//                songList2.shuffle()
//                hotSongInfo2.infoList = songList2
//                list.add(hotSongInfo2)
//
//                val hotSongInfo3 = HotSongInfo()
//                val songList3 = mutableListOf<SongInfo>()
//                hotSongInfo3.title = "陪你说晚安🌙"
//                threeArray.forEach<JSONObject> {
//                    val songInfo = SongInfo()
//                    songInfo.songId = it?.getString("song_id")
//                        ?: System.currentTimeMillis().toString()
//                    songInfo.songName = it?.getString("title") ?: ""
//                    songInfo.artist = it?.getString("author") ?: ""
//                    songInfo.songCover = it?.getString("pic_huge") ?: ""
//                    songInfo.headData?.put("source", "baiduMusic")
//                    songList3.add(songInfo)
//                }
//                songList3.shuffle()
//                hotSongInfo3.infoList = songList3
//                list.add(hotSongInfo3)
//
//                val hotSongInfo4 = HotSongInfo()
//                val songList4 = mutableListOf<SongInfo>()
//                fourArray.forEach<JSONObject> {
//                    val songInfo = SongInfo()
//                    songInfo.songId = it?.getString("song_id")
//                        ?: System.currentTimeMillis().toString()
//                    songInfo.songName = it?.getString("title") ?: ""
//                    songInfo.artist = it?.getString("author") ?: ""
//                    songInfo.songCover = it?.getString("pic_huge") ?: ""
//                    songInfo.headData?.put("source", "baiduMusic")
//                    songList4.add(songInfo)
//                }
//                songList4.shuffle()
//                hotSongInfo4.infoList = songList4
//                list.add(hotSongInfo4)
//
//                hotListLiveData.postValue(Pair(bannerList, list))
//            } catch (ex: Exception) {
//                ex.printStackTrace()
//                TestApplication.context?.showToast("请求失败：" + ex.message)
//            }
//        }
//    }
//
//    fun playWhenStartApp() {
//        viewModelScope.launch(Dispatchers.IO) {
//            try {
//                val list = mutableListOf<SongInfo>()
//                val array = getBaiduMusicListById("25").string().toJsonObj().getArray("song_list")
//                array.forEach<JSONObject> {
//                    val songInfo = SongInfo()
//                    songInfo.songId = it?.getString("song_id")
//                        ?: System.currentTimeMillis().toString()
//                    songInfo.songName = it?.getString("title") ?: ""
//                    songInfo.artist = it?.getString("author") ?: ""
//                    songInfo.songCover = it?.getString("pic_huge") ?: ""
//                    songInfo.headData?.put("source", "baiduMusic")
//                    list.add(songInfo)
//                }
//                list.shuffle()
//                withContext(Dispatchers.Main) {
//                    StarrySky.with().playMusic(list, 0)
//                }
//            } catch (ex: Exception) {
//                ex.printStackTrace()
//                TestApplication.context?.showToast("请求失败：" + ex.message)
//            }
//        }
//    }
//
//    private suspend fun getBaiduMusicListById(type: String): ResponseBody {
//        return RetrofitClient.getService(BaiduApi::class.java, BaiduApi.BASE_URL).getBaiduMusicList(type)
//    }


}