package com.example.musiclib.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.util.LruCache;
import android.text.TextUtils;

import com.example.musiclib.R;
import com.example.musiclib.model.MusicInfo;


/**
 * 专辑封面图片加载器
 * Created by wcy on 2015/11/27.
 */
public class CoverLoader {

    private static final String KEY_IMAGE_CACHE = "KEY_IMAGE_CACHE";

    // 封面缓存
    private LruCache<String, Bitmap> mCoverCache;
    private Context mContext;


    public static CoverLoader getInstance() {
        return SingletonHolder.instance;
    }

    private static class SingletonHolder {
        private static CoverLoader instance = new CoverLoader();
    }

    private CoverLoader() {
        // 获取当前进程的可用内存（单位KB）
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        // 缓存大小为当前进程可用内存的1/8
        int cacheSize = maxMemory / 8;
        mCoverCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    return bitmap.getAllocationByteCount() / 1024;
                } else {
                    return bitmap.getByteCount() / 1024;
                }
            }
        };
    }

    public void init(Context context) {
        mContext = context.getApplicationContext();
    }

    public Bitmap loadCover(MusicInfo music) {
        Bitmap bitmap;
        String key = getKey(music);
        if (TextUtils.isEmpty(key)) {
            bitmap = mCoverCache.get(KEY_IMAGE_CACHE);
            if (bitmap != null) {
                return bitmap;
            }

            bitmap = getDefaultCover();
            mCoverCache.put(KEY_IMAGE_CACHE, bitmap);
            return bitmap;
        }

        bitmap = mCoverCache.get(key);
        if (bitmap != null) {
            return bitmap;
        }

        bitmap = loadCoverByType(music);
        if (bitmap != null) {
            mCoverCache.put(key, bitmap);
            return bitmap;
        }

        return loadCover(null);
    }

    private String getKey(MusicInfo music) {
        if (music == null) {
            return null;
        }
        if (!TextUtils.isEmpty(music.getMusicCover())) {
            return music.getMusicCover();
        } else {
            return null;
        }
    }

    public Bitmap getDefaultCover() {
        return BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_launcher_background);
    }

    private Bitmap loadCoverByType(MusicInfo music) {
        Bitmap bitmap = loadCoverFromFile(music.getMusicCover());
        return ImageUtils.blur(bitmap);
    }

    /**
     * 从下载的图片加载封面<br>
     * 网络音乐
     */
    private Bitmap loadCoverFromFile(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        return BitmapFactory.decodeFile(path, options);
    }
}
