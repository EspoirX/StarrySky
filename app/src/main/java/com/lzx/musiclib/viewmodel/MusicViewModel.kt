package com.lzx.musiclib.viewmodel

import androidx.lifecycle.ViewModel
import com.lzx.musiclib.TestApplication
import com.lzx.musiclib.forEach
import com.lzx.musiclib.getArray
import com.lzx.starrysky.SongInfo
import org.json.JSONObject
import java.io.BufferedInputStream

class MusicViewModel : ViewModel() {

    fun getHomeMusic() = getMusicList("home.json")

    private fun getMusicList(file: String): MutableList<SongInfo> {
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
        var name = 0
        arrayJson.forEach<JSONObject> {
            val info = SongInfo()
            info.songName = it?.getString("songname").orEmpty()
            info.songId = it?.getString("songmid").orEmpty()
            info.artist = it?.getArray("singer")?.getJSONObject(0)?.getString("name").orEmpty()
            val albumid = it?.getString("albummid").orEmpty()
            info.songCover = "https://y.gtimg.cn/music/photo_new/T002R300x300M000${albumid}.jpg"
            list.add(info)
            name++
        }
        return list
    }
}