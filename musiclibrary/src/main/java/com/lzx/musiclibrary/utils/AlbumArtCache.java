/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lzx.musiclibrary.utils;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.LruCache;

import com.lzx.musiclibrary.helper.BitmapHelper;

import java.io.IOException;

/**
 * Implements a basic cache of album arts, with async loading support.
 */
public final class AlbumArtCache {

    private static final int MAX_ALBUM_ART_CACHE_SIZE = 12 * 1024 * 1024;  // 12 MB
    private static final int MAX_ART_WIDTH = 800;  // pixels
    private static final int MAX_ART_HEIGHT = 480;  // pixels

    // Resolution reasonable for carrying around as an icon (generally in
    // MediaDescription.getIconBitmap). This should not be bigger than necessary, because
    // the MediaDescription object should be lightweight. If you set it too high and try to
    // serialize the MediaDescription, you may get FAILED BINDER TRANSACTION errors.
    private static final int MAX_ART_WIDTH_ICON = 128;  // pixels
    private static final int MAX_ART_HEIGHT_ICON = 128;  // pixels

    private static final int BIG_BITMAP_INDEX = 0;
    private static final int ICON_BITMAP_INDEX = 1;

    private final LruCache<String, Bitmap[]> mCache;

    private static final AlbumArtCache sInstance = new AlbumArtCache();

    public static AlbumArtCache getInstance() {
        return sInstance;
    }

    private AlbumArtCache() {
        int maxSize = Math.min(MAX_ALBUM_ART_CACHE_SIZE,
                (int) (Math.min(Integer.MAX_VALUE, Runtime.getRuntime().maxMemory() / 4)));
        mCache = new LruCache<String, Bitmap[]>(maxSize) {
            @Override
            protected int sizeOf(String key, Bitmap[] value) {
                return value[BIG_BITMAP_INDEX].getByteCount()
                        + value[ICON_BITMAP_INDEX].getByteCount();
            }
        };
    }

    public Bitmap getBigImage(String artUrl) {
        Bitmap[] result = mCache.get(artUrl);
        return result == null ? null : result[BIG_BITMAP_INDEX];
    }

    public Bitmap getIconImage(String artUrl) {
        Bitmap[] result = mCache.get(artUrl);
        return result == null ? null : result[ICON_BITMAP_INDEX];
    }

    public void fetch(final String artUrl, final FetchListener listener) {
        Bitmap[] bitmap = mCache.get(artUrl);
        if (bitmap != null) {
            listener.onFetched(artUrl, bitmap[BIG_BITMAP_INDEX], bitmap[ICON_BITMAP_INDEX]);
            return;
        }
        new BitmapAsyncTask(artUrl, listener, mCache).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private static class BitmapAsyncTask extends AsyncTask<Void, Void, Bitmap[]> {
        private String artUrl;
        private FetchListener listener;
        private LruCache<String, Bitmap[]> mCache;

        BitmapAsyncTask(String artUrl, FetchListener listener, LruCache<String, Bitmap[]> cache) {
            this.artUrl = artUrl;
            this.listener = listener;
            mCache = cache;
        }

        @Override
        protected Bitmap[] doInBackground(Void... voids) {
            Bitmap[] bitmaps;
            try {
                Bitmap bitmap = BitmapHelper.fetchAndRescaleBitmap(artUrl, MAX_ART_WIDTH, MAX_ART_HEIGHT);
                Bitmap icon = BitmapHelper.scaleBitmap(bitmap, MAX_ART_WIDTH_ICON, MAX_ART_HEIGHT_ICON);
                bitmaps = new Bitmap[]{bitmap, icon};
                mCache.put(artUrl, bitmaps);
            } catch (IOException e) {
                return null;
            }
            return bitmaps;
        }

        @Override
        protected void onPostExecute(Bitmap[] bitmaps) {
            if (bitmaps == null) {
                listener.onError(artUrl, new IllegalArgumentException("got null bitmaps"));
            } else {
                listener.onFetched(artUrl, bitmaps[BIG_BITMAP_INDEX], bitmaps[ICON_BITMAP_INDEX]);
            }
        }
    }

    public static abstract class FetchListener {
        public abstract void onFetched(String artUrl, Bitmap bigImage, Bitmap iconImage);

        public void onError(String artUrl, Exception e) {
        }
    }
}
