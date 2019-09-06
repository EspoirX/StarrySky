package com.lzx.musiclib.example;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.lzx.starrysky.provider.SongInfo;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MusicRequest {

    private OkHttpClient client = new OkHttpClient();

    /**
     * 获取数据
     */
    public void getMusicList(Context context, RequestCallback callback) {
        Request request = new Request.Builder()
                .url("https://music.163.com/api/playlist/highquality/list?limit=50")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Toast.makeText(context, "网易云接口请求失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try {
                    JSONObject object = new JSONObject(response.body().string());
                    JSONArray array = object.getJSONArray("playlists");
                    JSONObject jsonObject = array.getJSONObject(1);
                    if (jsonObject == null) {
                        return;
                    }
                    String playlistId = jsonObject.getString("id");
                    getPlayList(context, playlistId, callback);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void getPlayList(Context context, String playlistId, RequestCallback callback) {
        Request request = new Request.Builder()
                .url("https://music.163.com/api/playlist/detail?id=" + playlistId)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Toast.makeText(context, "网易云接口请求失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string()).getJSONObject("result");
                    JSONObject creator = jsonObject.getJSONObject("creator");
                    JSONArray tracks = jsonObject.getJSONArray("tracks");
                    List<SongInfo> list = new ArrayList<>();
                    for (int i = 0; i < tracks.length(); i++) {
                        JSONObject object = tracks.getJSONObject(i);
                        SongInfo info = new SongInfo();
                        info.setAlbumName(object.optString("name"));
                        info.setAlbumArtist(object.optString("nickname"));
                        info.setSongCover(object.optJSONObject("album").optString("picUrl"));
                        info.setSongId(object.optString("id"));
                        info.setSongName(object.optString("name"));
                        info.setDuration(object.optLong("duration"));
                        info.setSongUrl("http://music.163.com/song/media/outer/url?id=" + info.getSongId() + ".mp3");
                        list.add(info);
                    }
                    Log.i("xian", "list = " + list.size());
                    callback.onSuccess(list);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    interface RequestCallback {
        void onSuccess(List<SongInfo> list);
    }

}
