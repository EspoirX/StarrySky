package com.lzx.musiclib;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.lzx.musiclib.example.MusicRequest;
import com.lzx.starrysky.StarrySky;
import com.lzx.starrysky.StarrySkyBuilder;
import com.lzx.starrysky.StarrySkyConfig;
import com.lzx.starrysky.notification.StarrySkyNotificationManager;
import com.lzx.starrysky.playback.offline.StarrySkyCacheManager;
import com.lzx.starrysky.provider.SongInfo;
import com.lzx.starrysky.registry.StarrySkyRegistry;
import com.lzx.starrysky.utils.delayaction.PlayValidManager;
import com.lzx.starrysky.utils.delayaction.Valid;


/**
 * create by lzx
 * time:2018/11/9
 */
public class TestApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        StarrySky.init(this, new TestConfig());
    }

    private static class TestConfig extends StarrySkyConfig {
        @Override
        public void applyOptions(@NonNull Context context, @NonNull StarrySkyBuilder builder) {
            super.applyOptions(context, builder);
        }

        @Override
        public void applyMediaValid(@NonNull Context context, StarrySkyRegistry registry) {
            super.applyMediaValid(context, registry);
            registry.appendValidRegistry(new RequestSongInfoValid());
        }

        @Override
        public StarrySkyNotificationManager.NotificationFactory getNotificationFactory() {
            return super.getNotificationFactory();
        }

        @Override
        public StarrySkyCacheManager.CacheFactory getCacheFactory() {
            return super.getCacheFactory();
        }
    }

    public static class RequestSongInfoValid implements Valid {
        private boolean isRequest;
        private String mediaId;
        private MusicRequest mMusicRequest;

        RequestSongInfoValid() {
            mMusicRequest = new MusicRequest();
        }

        @Override
        public boolean preCheck() {
            return isRequest;  //是否需要执行 doValid
        }

        @Override
        public void doValid(SongInfo songInfo) {
            if (TextUtils.isEmpty(songInfo.getSongUrl())) {
                mMusicRequest.getSongInfoDetail(songInfo.getSongId(), songUrl -> {
                    songInfo.setSongUrl(songUrl); //给songInfo设置Url
                    Log.i("xian", "---getSongInfoDetail---");
                    //判断音频是否有改变
                    boolean mediaHasChanged = !TextUtils.equals(mediaId, songInfo.getSongId());
                    if (mediaHasChanged) {
                        mediaId = songInfo.getSongId();
                    }
                    isRequest = !mediaHasChanged;

                    PlayValidManager.get().doCall(songInfo);
                });
            } else {
                Log.i("xian", "---doCall---");
                isRequest = true;
                PlayValidManager.get().doCall(songInfo);
            }
        }
    }


}
