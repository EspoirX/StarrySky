package com.lzx.musiclib.example;

import android.content.Context;
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

    private OkHttpClient client;

    public MusicRequest() {
        OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
        builder.addInterceptor(chain -> {
            Request newRequest = chain.request().newBuilder()
                    .removeHeader("User-Agent")
                    .addHeader("User-Agent",
                            "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:0.9.4)")
                    .build();
            return chain.proceed(newRequest);
        });
        client = builder.build();
    }

    /**
     * 获取数据
     */
    public void getMusicList(Context context, RequestCallback callback) {
        Request request = new Request.Builder()
                .url("http://tingapi.ting.baidu.com/v1/restserver/ting?" +
                        "format=json" +
                        "&calback=" +
                        "&from=webapp_music" +
                        "&method=baidu.ting.billboard.billList" +
                        "&type=2" +
                        "&size=20" +
                        "&offset=0")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Toast.makeText(context, "接口请求失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response)
                    throws IOException {
                try {
                    String json = response.body().string();
                    JSONObject jsonObject = new JSONObject(json);
                    JSONArray jsonArray = jsonObject.getJSONArray("song_list");
                    List<SongInfo> list = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        SongInfo info = new SongInfo();
                        info.setSongId(object.getString("song_id"));
                        info.setSongCover(object.getString("pic_big"));
                        info.setSongName(object.getString("title"));
                        info.setArtist(object.getString("author"));
                        list.add(info);
                        callback.onSuccess(list);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 获取音频url
     */
    public void getSongInfoDetail(String songId, RequestInfoCallback callback) {
        Request request = new Request.Builder()
                .url("http://tingapi.ting.baidu.com/v1/restserver/ting?method=baidu.ting.song.play&songid=" +
                        songId)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response)
                    throws IOException {
                try {
                    JSONObject jsonObject =
                            new JSONObject(response.body().string()).getJSONObject("bitrate");
                    String url = jsonObject.getString("file_link");
                    callback.onSuccess(url);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    interface RequestCallback {
        void onSuccess(List<SongInfo> list);
    }

    public interface RequestInfoCallback {
        void onSuccess(String songUrl);
    }
}
