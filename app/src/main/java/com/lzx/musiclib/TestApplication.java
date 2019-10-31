package com.lzx.musiclib;

import android.Manifest;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.widget.Toast;

import com.lzx.musiclib.example.MusicRequest;
import com.lzx.musiclib.imageloader.GlideLoader;
import com.lzx.starrysky.StarrySky;
import com.lzx.starrysky.StarrySkyBuilder;
import com.lzx.starrysky.StarrySkyConfig;
import com.lzx.starrysky.delayaction.Valid;
import com.lzx.starrysky.provider.SongInfo;
import com.lzx.starrysky.registry.StarrySkyRegistry;
import com.lzx.starrysky.utils.StarrySkyUtils;
import com.qw.soul.permission.SoulPermission;
import com.qw.soul.permission.bean.Permission;
import com.qw.soul.permission.bean.Permissions;
import com.qw.soul.permission.callbcak.CheckRequestPermissionsListener;

import leakcanary.LeakCanary;

/**
 * create by lzx
 * time:2018/11/9
 */
public class TestApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        StarrySky.init(this, new TestConfig());
        StarrySkyUtils.isDebug = true;

        LeakCanary.INSTANCE.dumpHeap();
    }

    private static class TestConfig extends StarrySkyConfig {

        public static String ACTION_PLAY_OR_PAUSE = "ACTION_PLAY_OR_PAUSE";
        public static String ACTION_NEXT = "ACTION_NEXT";
        public static String ACTION_PRE = "ACTION_PRE";
        public static String ACTION_FAVORITE = "ACTION_FAVORITE";
        public static String ACTION_LYRICS = "ACTION_LYRICS";

        private PendingIntent getPendingIntent(Context context, String action) {
            Intent intent = new Intent(action);
            intent.setClass(context, NotificationReceiver.class);
            return PendingIntent.getBroadcast(context, 0, intent, 0);
        }

        @Override
        public void applyOptions(@NonNull Context context, @NonNull StarrySkyBuilder builder) {
            super.applyOptions(context, builder);
            builder.setOpenNotification(true);
        }

        @Override
        public void applyStarrySkyRegistry(@NonNull Context context, StarrySkyRegistry registry) {
            super.applyStarrySkyRegistry(context, registry);
            registry.appendValidRegistry(new RequestSongInfoValid(context));
            registry.registryImageLoader(new GlideLoader());
        }
    }

    public static class RequestSongInfoValid implements Valid {
        private MusicRequest mMusicRequest;
        private Context mContext;

        RequestSongInfoValid(Context context) {
            mContext = context;
            mMusicRequest = new MusicRequest();
        }

        @Override
        public void doValid(SongInfo songInfo, ValidCallback callback) {
            SoulPermission.getInstance().checkAndRequestPermissions(
                    Permissions.build(
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    new CheckRequestPermissionsListener() {
                        @Override
                        public void onAllPermissionOk(Permission[] allPermissions) {
                            if (TextUtils.isEmpty(songInfo.getSongUrl())) {
                                mMusicRequest.getSongInfoDetail(songInfo.getSongId(), songUrl -> {
                                    songInfo.setSongUrl(songUrl); //给songInfo设置Url
                                    callback.finishValid();
                                });
                            } else {
                                callback.doActionDirect();
                            }
                        }

                        @Override
                        public void onPermissionDenied(Permission[] refusedPermissions) {
                            Toast.makeText(mContext, "没有权限，播放失败", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }


}
