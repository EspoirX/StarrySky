package com.lzx.musiclib;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.lzx.musiclib.example.MusicRequest;
import com.lzx.musiclib.imageloader.GlideLoader;
import com.lzx.starrysky.StarrySky;
import com.lzx.starrysky.StarrySkyBuilder;
import com.lzx.starrysky.StarrySkyConfig;
import com.lzx.starrysky.notification.StarrySkyNotificationManager;
import com.lzx.starrysky.playback.offline.StarrySkyCacheManager;
import com.lzx.starrysky.provider.SongInfo;
import com.lzx.starrysky.registry.StarrySkyRegistry;
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
            builder.setOpenNotification(true);

        }

        @Override
        public void applyMediaValid(@NonNull Context context, StarrySkyRegistry registry) {
            super.applyMediaValid(context, registry);
            registry.appendValidRegistry(new RequestSongInfoValid());
            registry.registryImageLoader(new GlideLoader());
        }

        @Override
        public StarrySkyNotificationManager.NotificationFactory getNotificationFactory() {
            return StarrySkyNotificationManager.CUSTOM_NOTIFICATION_FACTORY;
        }

        @Override
        public StarrySkyCacheManager.CacheFactory getCacheFactory() {
            return super.getCacheFactory();
        }
    }

    public static class RequestSongInfoValid implements Valid {
        private MusicRequest mMusicRequest;

        RequestSongInfoValid() {
            mMusicRequest = new MusicRequest();
        }

        @Override
        public void doValid(SongInfo songInfo, ValidCallback callback) {
            if (TextUtils.isEmpty(songInfo.getSongUrl())) {
                mMusicRequest.getSongInfoDetail(songInfo.getSongId(), songUrl -> {
                    songInfo.setSongUrl(songUrl); //给songInfo设置Url
                    callback.finishValid();
                });
            } else {
                callback.doActionDirect();
            }
        }
    }


}
