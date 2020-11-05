package com.lzx.musiclib.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lzx.basecode.readAsBytes
import com.lzx.basecode.toSdcardPath
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
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

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

    fun isFinishDownloadMusic(): Boolean {
        val path = "StarrySkyRecord".toSdcardPath()
        val fileName = "/告白气球.mp3"
        val f = File("$path$fileName")
        return f.exists()
    }

    fun getDownloadPath(): String {
        val path = "StarrySkyRecord".toSdcardPath()
        val fileName = "/告白气球.mp3"
        return path + fileName
    }

    val downloadLiveData = MutableLiveData<String>()
    fun downloadMusic() {
        val path = "StarrySkyRecord".toSdcardPath()
        val fileName = "/告白气球.mp3"
        val f = File("$path$fileName")
        if (f.exists()) {
            downloadLiveData.postValue(f.absolutePath)
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val url = URL("https://github.com/EspoirX/lzxTreasureBox/raw/master/%E5%91%A8%E6%9D%B0%E4%BC%A6-%E5%91%8A%E7%99%BD%E6%B0%94%E7%90%83.mp3")
            (url.openConnection() as? HttpURLConnection)?.let { http ->
                http.connectTimeout = 20 * 1000
                http.requestMethod = "GET"
                http.connect()
                http.inputStream.use { inputStream ->
                    inputStream.readAsBytes()?.let { bytes ->
                        val fileDir = File(path).apply {
                            this.takeIf { !it.exists() }?.mkdirs()
                        }
                        val file = File(fileDir.absolutePath + fileName).apply {
                            this.takeIf { !it.exists() }?.createNewFile()
                        }
                        FileOutputStream(file).use {
                            it.write(bytes).also {
                                downloadLiveData.postValue(file.absolutePath)
                                http.disconnect()
                            }
                        }
                    }
                }
            }
        }
    }
}