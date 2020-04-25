package com.lzx.musiclib;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import com.lzx.musiclib.example.MusicRequest;
import com.lzx.starrysky.StarrySky;
import com.lzx.starrysky.StarrySkyConfig;
import com.lzx.starrysky.intercept.InterceptorCallback;
import com.lzx.starrysky.intercept.StarrySkyInterceptor;
import com.lzx.starrysky.provider.SongInfo;
import com.lzx.starrysky.utils.StarrySkyUtils;
import com.qw.soul.permission.SoulPermission;
import com.qw.soul.permission.bean.Permission;
import com.qw.soul.permission.bean.Permissions;
import com.qw.soul.permission.callbcak.CheckRequestPermissionsListener;
import com.tencent.bugly.crashreport.CrashReport;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * create by lzx
 * time:2018/11/9
 */
public class TestApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        StarrySkyConfig config = new StarrySkyConfig();
        config.addInterceptor(new RequestSongInfoInterceptor(this));
        StarrySky.Companion.init(this, config);
        StarrySkyUtils.isDebug = true;
        CrashReport.initCrashReport(getApplicationContext(), "9e447caa98", false);
    }

    public static class RequestSongInfoInterceptor implements StarrySkyInterceptor {
        private MusicRequest mMusicRequest;
        private Context mContext;

        RequestSongInfoInterceptor(Context context) {
            mContext = context;
            mMusicRequest = new MusicRequest();
        }

        @Override
        public void process(@Nullable SongInfo songInfo, @NotNull InterceptorCallback callback) {
            if (songInfo == null) {
                callback.onInterrupt(new RuntimeException("SongInfo is null"));
                return;
            }
            SoulPermission.getInstance().checkAndRequestPermissions(Permissions.build(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE), new CheckRequestPermissionsListener() {
                @Override
                public void onAllPermissionOk(Permission[] allPermissions) {
                    if (TextUtils.isEmpty(songInfo.getSongUrl())) {
                        mMusicRequest.getSongInfoDetail(songInfo.getSongId(), songUrl -> {
                            songInfo.setSongUrl(songUrl); //给songInfo设置Url
                            callback.onContinue(songInfo);
                        });
                    } else {
                        callback.onContinue(songInfo);
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
