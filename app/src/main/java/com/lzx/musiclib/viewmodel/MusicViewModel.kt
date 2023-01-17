package com.lzx.musiclib.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lzx.musiclib.TestApplication
import com.lzx.musiclib.forEach
import com.lzx.musiclib.getArray
import com.lzx.starrysky.SongInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedInputStream

class MusicViewModel : ViewModel() {

    companion object {
        val baseUrl = "http://espoir.aifield.biz/"
    }

    fun getHomeMusic() = getMusicList("home.json", "home")

    private fun getMusicList(file: String, tag: String): MutableList<SongInfo> {
        var json: String
        val asset = TestApplication.context?.assets ?: return mutableListOf()
        asset.open(file).use { it ->
            BufferedInputStream(it).use {
                json = it.reader().readText()
            }
        }
        val arrayJson = JSONObject(json).getJSONObject("data")
            .getJSONArray("songlist")
        val list = mutableListOf<SongInfo>()
        arrayJson.forEach<JSONObject> {
            val info = SongInfo()
            info.tag = tag
            info.songName = it?.getString("songname").orEmpty()
            info.songId = it?.getString("songmid").orEmpty()
            info.artist = it?.getArray("singer")?.getJSONObject(0)?.getString("name").orEmpty()
            val albumid = it?.getString("albummid").orEmpty()
            info.songCover = "https://y.gtimg.cn/music/photo_new/T002R300x300M000${albumid}.jpg"
            info.songUrl = if (it?.has("songUrl") == true) {
                it.getString("songUrl").orEmpty()
            } else {
                ""
            }
            list.add(info)
        }
        return list
    }


    var cardLiveData = MutableLiveData<MutableList<SongInfo>>()
    fun getCardMusicList(type: String?) {
        if (type.isNullOrEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            var json: String?
            val asset = TestApplication.context?.assets
            asset?.open("card.json").use { it ->
                BufferedInputStream(it).use {
                    json = it.reader().readText()
                }
            }
            if (json.isNullOrEmpty()) {
                cardLiveData.postValue(mutableListOf())
            } else {
                val arrayJson = JSONObject(json).getJSONObject(type)
                    .getJSONArray("songlist")
                val list = mutableListOf<SongInfo>()
                arrayJson.forEach<JSONObject> {
                    val info = SongInfo()
                    info.songName = it?.getString("songname").orEmpty()
                    info.songId = it?.getString("songmid").orEmpty()
                    info.artist = it?.getArray("singer")?.getJSONObject(0)?.getString("name").orEmpty()
                    val albumid = it?.getString("albummid").orEmpty()
                    info.songCover = "https://y.gtimg.cn/music/photo_new/T002R300x300M000${albumid}.jpg"
                    list.add(info)
                }
                cardLiveData.postValue(list)
            }
        }
    }

    var dynamicLiveData = MutableLiveData<MutableList<SongInfo>>()
    fun getDynamicMusicList(typeText: String?) {
        var type = ""
        if (typeText == "推荐") {
            type = "recom"
        } else if (typeText == "最新") {
            type = "news"
        }
        if (type.isEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            var json: String?
            val asset = TestApplication.context?.assets
            asset?.open("dynamic.json").use { it ->
                BufferedInputStream(it).use {
                    json = it.reader().readText()
                }
            }
            if (json.isNullOrEmpty()) {
                dynamicLiveData.postValue(mutableListOf())
            } else {
                val arrayJson = JSONObject(json).getJSONObject(type)
                    .getJSONArray("songlist")
                val list = mutableListOf<SongInfo>()
                arrayJson.forEach<JSONObject> {
                    val info = SongInfo()
                    info.songName = it?.getString("songname").orEmpty()
                    info.songId = it?.getString("songmid").orEmpty()
                    info.artist = it?.getArray("singer")?.getJSONObject(0)?.getString("name").orEmpty()
                    val albumid = it?.getString("albummid").orEmpty()
                    info.songCover = "https://y.gtimg.cn/music/photo_new/T002R300x300M000${albumid}.jpg"
                    list.add(info)
                }
                dynamicLiveData.postValue(list)
            }
        }
    }

    fun getUserMusicList() = getMusicList("user.json", "user")
}