package com.lzx.musiclib

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lzx.musiclib.http.ApiInterface
import com.lzx.musiclib.http.RetrofitClient
import com.lzx.starrysky.utils.SpUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

class MusicViewModel : ViewModel() {

    companion object {
        const val KEY_TOKEN = "key_access_token"
        const val KEY_EXPIRES = "key_expires_in"
    }

    fun login() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = RetrofitClient.getService(ApiInterface::class.java, ApiInterface.BASE_URL).login(
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
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    fun getChannelList() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = RetrofitClient.getMusicService().getChannelList(
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
            Log.i("XIAN", "json = " + json)
            try {
                val obj = JSONObject(json)

            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }


    fun getSongList(channel: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = RetrofitClient.getMusicService().getSongList(
                "Bearer " + SpUtil.instance.getString(KEY_TOKEN),
                "10",
                "mainsite",
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
            Log.i("XIAN", "json = " + json)
            try {
                val obj = JSONObject(json)

            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    fun a(){
        val a = "https://api.douban.com/v2/fm/playlist?&&&&&&&&&type=n&&version=115"
    }

    fun b(){
        val b = "https://api.douban.com/v2/fm/playlist?&from=mainsite&&&&&&&&client_id=02646d3fb69a52ff072d47bf23cef8fd&icon_cate=xlarge&&&version=115"
    }

}