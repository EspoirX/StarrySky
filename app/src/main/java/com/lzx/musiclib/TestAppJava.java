package com.lzx.musiclib;

import android.Manifest;
import android.app.Application;
import android.text.TextUtils;

import com.lzx.musiclib.example.MusicRequest;
import com.lzx.starrysky.StarrySky;
import com.lzx.starrysky.StarrySkyConfig;
import com.lzx.starrysky.intercept.InterceptorCallback;
import com.lzx.starrysky.intercept.StarrySkyInterceptor;
import com.lzx.starrysky.notification.NotificationConfig;
import com.lzx.starrysky.provider.SongInfo;
import com.lzx.starrysky.utils.MainLooper;
import com.lzx.starrysky.utils.SpUtil;
import com.qw.soul.permission.SoulPermission;
import com.qw.soul.permission.bean.Permission;
import com.qw.soul.permission.bean.Permissions;
import com.qw.soul.permission.callbcak.CheckRequestPermissionsListener;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TestAppJava extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        NotificationConfig notificationConfig =
                NotificationConfig.create(builder -> {
                    builder.setTargetClass("com.lzx.musiclib.example.PlayDetailActivity");
                    return builder;
                });

        StarrySkyConfig config = new StarrySkyConfig().newBuilder()
                .addInterceptor(new PermissionInterceptor())
                .addInterceptor(new RequestSongInfoInterceptor())
                .isOpenNotification(true)
                .setNotificationConfig(notificationConfig)
                .build();

        StarrySky.init(this, config, null);

    }

    private static class PermissionInterceptor implements StarrySkyInterceptor {

        @Override
        public void process(@Nullable SongInfo songInfo, @NotNull MainLooper mainLooper,
                            @NotNull InterceptorCallback callback) {
            if (songInfo == null) {
                callback.onInterrupt(new RuntimeException("SongInfo is null"));
                return;
            }
            boolean hasPermission =
                    SpUtil.getInstance().getBoolean("HAS_PERMISSION", false);
            if (hasPermission) {
                callback.onContinue(songInfo);
                return;
            }
            SoulPermission.getInstance()
                    .checkAndRequestPermissions(
                            Permissions.build(Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            new CheckRequestPermissionsListener() {
                                @Override
                                public void onAllPermissionOk(Permission[] allPermissions) {
                                    SpUtil.getInstance()
                                            .putBoolean("HAS_PERMISSION", true);
                                    callback.onContinue(songInfo);
                                }

                                @Override
                                public void onPermissionDenied(Permission[] refusedPermissions) {
                                    SpUtil.getInstance()
                                            .putBoolean("HAS_PERMISSION", false);
                                    callback.onInterrupt(new RuntimeException("没有权限，播放失败"));
                                }
                            });
        }
    }

    private static class RequestSongInfoInterceptor implements StarrySkyInterceptor {

        private MusicRequest mMusicRequest = new MusicRequest();

        @Override
        public void process(@Nullable SongInfo songInfo, @NotNull MainLooper mainLooper,
                            @NotNull InterceptorCallback callback) {
            if (songInfo == null) {
                callback.onInterrupt(new RuntimeException("SongInfo is null"));
                return;
            }
            if (TextUtils.isEmpty(songInfo.getSongUrl())) {
                mMusicRequest.requestSongUrl(songInfo.getSongId(), songUrl -> {
                    songInfo.setSongUrl(songUrl);
                    callback.onContinue(songInfo);
                });
            } else {
                callback.onContinue(songInfo);
            }
        }
    }
}
