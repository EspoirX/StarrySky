package com.lzx.musiclib;

import android.Manifest;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadSampleListener;
import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.util.FileDownloadLog;
import com.lzx.musiclib.example.MusicRequest;
import com.lzx.musiclib.imageloader.GlideLoader;
import com.lzx.starrysky.StarrySky;
import com.lzx.starrysky.StarrySkyBuilder;
import com.lzx.starrysky.StarrySkyConfig;
import com.lzx.starrysky.delayaction.Valid;
import com.lzx.starrysky.playback.offline.StarrySkyCache;
import com.lzx.starrysky.playback.offline.StarrySkyCacheManager;
import com.lzx.starrysky.provider.SongInfo;
import com.lzx.starrysky.registry.StarrySkyRegistry;
import com.lzx.starrysky.utils.StarrySkyUtils;
import com.qw.soul.permission.SoulPermission;
import com.qw.soul.permission.bean.Permission;
import com.qw.soul.permission.bean.Permissions;
import com.qw.soul.permission.callbcak.CheckRequestPermissionsListener;
import com.tencent.bugly.crashreport.CrashReport;

import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * create by lzx
 * time:2018/11/9
 */
public class TestApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        StarrySky.init(this, new TestConfig(this));
        StarrySkyUtils.isDebug = true;
        CrashReport.initCrashReport(getApplicationContext(), "9e447caa98", false);
    }

    private static class TestConfig extends StarrySkyConfig {

        private Context mContext;

        public TestConfig(Context context) {
            mContext = context;
        }

        @Override
        public void applyOptions(@NonNull Context context, @NonNull StarrySkyBuilder builder) {
            super.applyOptions(context, builder);
            builder.setOpenNotification(true);
            builder.setOpenCache(true);
            String destFileDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/000xian/";
            builder.setCacheDestFileDir(destFileDir);
        }

        @Override
        public void applyStarrySkyRegistry(@NonNull Context context, StarrySkyRegistry registry) {
            super.applyStarrySkyRegistry(context, registry);
            registry.appendValidRegistry(new RequestSongInfoValid(context));
            registry.registryImageLoader(new GlideLoader());
        }

        // @Override
        // public StarrySkyCacheManager.CacheFactory getCacheFactory() {
        //     return new StarrySkyCacheManager.CacheFactory() {
        //         @NotNull
        //         @Override
        //         public StarrySkyCache build(@NotNull Context context, @NotNull StarrySkyCacheManager manager) {
        //             return new StarrySkyCache() {
        //                 @Override
        //                 public boolean isCache(@NotNull String url) {
        //                     String path =
        //                             manager.getDownloadDirectory(context).getAbsolutePath() + "/" +
        //                                     StarrySkyCacheManager.DOWNLOAD_CONTENT_DIRECTORY + "/111.mp3";
        //                     File file = new File(path);
        //                     Log.i("TestApplication", "isCache = " + file.exists());
        //                     return file.exists();
        //                 }
        //
        //                 @Override
        //                 public void startCache(@NotNull String mediaId, @NotNull String url,
        //                                        @NotNull String extension) {
        //                     if (isCache(url)) {
        //                         return;
        //                     }
        //                     String path =
        //                             manager.getDownloadDirectory(context).getAbsolutePath() + "/" +
        //                                     StarrySkyCacheManager.DOWNLOAD_CONTENT_DIRECTORY + "/111.mp3";
        //                     FileDownloader.setup(context);
        //                     FileDownloadLog.NEED_LOG = true;
        //                     FileDownloader.getImpl().create(url)
        //                             .setPath(path)
        //                             .setListener(new FileDownloadSampleListener() {
        //                                 @Override
        //                                 protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
        //                                     Log.i("TestApplication", "progress = " + soFarBytes + "/" + totalBytes);
        //                                 }
        //
        //                                 @Override
        //                                 protected void completed(BaseDownloadTask task) {
        //                                     Log.i("TestApplication", "= completed = ");
        //                                 }
        //
        //                                 @Override
        //                                 protected void error(BaseDownloadTask task, Throwable e) {
        //                                     Log.i("TestApplication", "= error = " + e.getMessage());
        //                                 }
        //                             }).start();
        //                 }
        //
        //                 @Override
        //                 public void deleteCacheFileByUrl(@NotNull String url) {
        //
        //                 }
        //
        //                 @Override
        //                 public boolean deleteAllCacheFile() {
        //                     return false;
        //                 }
        //             };
        //         }
        //     };
        // }
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
