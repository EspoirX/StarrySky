package com.lzx.musiclib.example;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.widget.Toast;

import com.lzx.musiclib.R;
import com.lzx.starrysky.StarrySky;
import com.lzx.starrysky.StarrySkyConfig;
import com.lzx.starrysky.delayaction.Valid;
import com.lzx.starrysky.provider.SongInfo;
import com.lzx.starrysky.registry.StarrySkyRegistry;

/**
 * 这里演示播放音频前需要先请求接口获取url这类需求的解决方案，只需要按照下面代码那样实现
 * 自己的 StarrySkyConfig 然后在初始化时设置进去即可。有关于 StarrySkyConfig 的相关
 * 信息请看文档
 */
public class RequestBeforePlayActivity extends AppCompatActivity {

    private ListPlayAdapter mListPlayAdapter;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_before_play);

        RecyclerView recyclerView = findViewById(R.id.recycle_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mListPlayAdapter = new ListPlayAdapter(this);
        recyclerView.setAdapter(mListPlayAdapter);

        //获取数据
        MusicRequest musicRequest = new MusicRequest();
        musicRequest.getMusicList(this, list -> {
            for (SongInfo songInfo : list) {
                songInfo.setSongUrl("");  //先把url设为空，模拟没有url的情况
            }
            runOnUiThread(() -> {
                mListPlayAdapter.setSongInfos(list);
                StarrySky.with().updatePlayList(list);
            });
        });
    }

    /**
     * 将这个在初始化的时候传到第二个参数即可，例子：
     * //     * public class TestApplication extends Application {
     * //     *
     * //     *     @Override
     * //     *     public void onCreate() {
     * //     *         super.onCreate();
     * //     *         StarrySky.init(this , new RequestUrlConfig());
     * //     *     }
     * //     *}
     */
    public static class RequestUrlConfig extends StarrySkyConfig {
        /**
         * 重写 applyStarrySkyRegistry 方法
         */
        @Override
        public void applyStarrySkyRegistry(@NonNull Context context, StarrySkyRegistry registry) {
            super.applyStarrySkyRegistry(context, registry);
            //调用 appendValidRegistry 方法添加播放前验证操作，可添加多个
            registry.appendValidRegistry(new RequestUrlValid(context));
        }

        private static class RequestUrlValid implements Valid {
            Context context;

            RequestUrlValid(Context context) {
                this.context = context;
            }

            @Override
            public void doValid(SongInfo songInfo, ValidCallback callback) {
                //TODO 这里执行验证，该方法在播放前执行，例如这里可以请求接口拿到 url
                if (TextUtils.isEmpty(songInfo.getSongUrl())) {
                    //模拟接口请求成功
                    Toast.makeText(context, "请求接口成功", Toast.LENGTH_SHORT).show();
                    //请求完后做自己的操作，这里举例把请求到的url设置给songInfo更新信息
                    songInfo.setSongUrl("http://music.163.com/song/media/outer/url?id=" + songInfo.getSongId() + ".mp3");
                    //调用一下 doCall ，继续执行，才会执行后续的播放操作
                    callback.finishValid();
                } else {
                    //如果有 url，直接执行 action
                    callback.doActionDirect();
                }
            }
        }
    }
}
