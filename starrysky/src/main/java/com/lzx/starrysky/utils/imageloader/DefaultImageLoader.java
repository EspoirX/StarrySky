package com.lzx.starrysky.utils.imageloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import android.text.TextUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 默认图片加载器
 */
public class DefaultImageLoader implements ImageLoaderStrategy {

    private static final int MAX_READ_LIMIT_PER_IMG = 1024 * 1024;
    private static final int MAX_ALBUM_ART_CACHE_SIZE = 12 * 1024 * 1024;  // 12 MB
    private static final int MAX_ART_WIDTH = 800;  // pixels
    private static final int MAX_ART_HEIGHT = 480;  // pixels
    private static final int MAX_ART_WIDTH_ICON = 128;  // pixels
    private static final int MAX_ART_HEIGHT_ICON = 128;  // pixels

    private static final int BIG_BITMAP_INDEX = 0;
    private static final int ICON_BITMAP_INDEX = 1;

    private final LruCache<String, Bitmap[]> mCache;

    public DefaultImageLoader() {
        int maxSize = Math.min(MAX_ALBUM_ART_CACHE_SIZE,
                (int) (Math.min(Integer.MAX_VALUE, Runtime.getRuntime().maxMemory() / 4)));
        mCache = new LruCache<String, Bitmap[]>(maxSize) {
            @Override
            protected int sizeOf(String key, Bitmap[] value) {
                if (value[BIG_BITMAP_INDEX] != null && value[ICON_BITMAP_INDEX] != null) {
                    return value[BIG_BITMAP_INDEX].getByteCount()
                            + value[ICON_BITMAP_INDEX].getByteCount();
                } else {
                    return 0;
                }
            }
        };
    }

    @Override
    public void loadImage(Context context, String url, ImageLoaderCallBack callBack) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        Bitmap[] bitmap = mCache.get(url);
        if (bitmap != null) {
            if (callBack != null) {
                callBack.onBitmapLoaded(bitmap[BIG_BITMAP_INDEX]);
            }
            return;
        }
        new BitmapAsyncTask(url, callBack, mCache)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private static class BitmapAsyncTask extends AsyncTask<Void, Void, Bitmap[]> {
        private String artUrl;
        private ImageLoaderCallBack listener;
        private LruCache<String, Bitmap[]> mCache;

        BitmapAsyncTask(String artUrl, ImageLoaderCallBack listener, LruCache<String, Bitmap[]> cache) {
            this.artUrl = artUrl;
            this.listener = listener;
            mCache = cache;
        }

        @Override
        protected Bitmap[] doInBackground(Void... voids) {
            Bitmap[] bitmaps = null;
            try {
                Bitmap bitmap = fetchAndRescaleBitmap(artUrl, MAX_ART_WIDTH, MAX_ART_HEIGHT);
                Bitmap icon = scaleBitmap(bitmap, MAX_ART_WIDTH_ICON, MAX_ART_HEIGHT_ICON);
                if (icon != null) {
                    bitmaps = new Bitmap[]{bitmap, icon};
                    mCache.put(artUrl, bitmaps);
                }
            } catch (IOException e) {
                return null;
            }
            return bitmaps;
        }

        @Override
        protected void onPostExecute(Bitmap[] bitmaps) {
            if (listener == null) {
                return;
            }
            if (bitmaps == null) {
                listener.onBitmapFailed(null);
            } else {
                listener.onBitmapLoaded(bitmaps[BIG_BITMAP_INDEX]);
            }
        }
    }

    public Bitmap getBigImage(String artUrl) {
        Bitmap[] result = mCache.get(artUrl);
        return result == null ? null : result[BIG_BITMAP_INDEX];
    }

    public Bitmap getIconImage(String artUrl) {
        Bitmap[] result = mCache.get(artUrl);
        return result == null ? null : result[ICON_BITMAP_INDEX];
    }

    private static Bitmap scaleBitmap(Bitmap src, int maxWidth, int maxHeight) {
        if (src != null && src.getWidth() > 0 && src.getHeight() > 0) {
            double scaleFactor = Math.min(((double) maxWidth) / src.getWidth(), ((double) maxHeight) / src.getHeight());
            return Bitmap.createScaledBitmap(src, (int) (src.getWidth() * scaleFactor), (int) (src.getHeight() * scaleFactor), false);
        } else {
            return null;
        }
    }

    private static Bitmap scaleBitmap(int scaleFactor, InputStream is) {
        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        return BitmapFactory.decodeStream(is, null, bmOptions);
    }

    private static int findScaleFactor(int targetW, int targetH, InputStream is) {
        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(is, null, bmOptions);
        int actualW = bmOptions.outWidth;
        int actualH = bmOptions.outHeight;

        // Determine how much to scale down the image
        return Math.min(actualW / targetW, actualH / targetH);
    }

    @SuppressWarnings("SameParameterValue")
    private static Bitmap fetchAndRescaleBitmap(String uri, int width, int height) throws IOException {
        URL url = new URL(uri);
        BufferedInputStream is = null;
        try {
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            is = new BufferedInputStream(urlConnection.getInputStream());
            is.mark(MAX_READ_LIMIT_PER_IMG);
            int scaleFactor = findScaleFactor(width, height, is);
            is.reset();
            return scaleBitmap(scaleFactor, is);
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }
}
