package com.lzx.starrysky.imageloader

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.support.v4.util.LruCache
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class DefaultImageLoader : ImageLoaderStrategy {

    companion object {
        private const val MAX_READ_LIMIT_PER_IMG = 1024 * 1024
        private const val MAX_ALBUM_ART_CACHE_SIZE = 12 * 1024 * 1024  // 12 MB
        private const val MAX_ART_WIDTH = 800  // pixels
        private const val MAX_ART_HEIGHT = 480  // pixels
        private const val MAX_ART_WIDTH_ICON = 128  // pixels
        private const val MAX_ART_HEIGHT_ICON = 128  // pixels

        private const val BIG_BITMAP_INDEX = 0
        private const val ICON_BITMAP_INDEX = 1
    }

    private var mCache: LruCache<String, Array<Bitmap>>

    init {
        val maxSize = MAX_ALBUM_ART_CACHE_SIZE.coerceAtMost(
            Integer.MAX_VALUE.toLong().coerceAtMost(Runtime.getRuntime().maxMemory() / 4).toInt())
        mCache = object : LruCache<String, Array<Bitmap>>(maxSize) {
            override fun sizeOf(key: String, value: Array<Bitmap>): Int {
                return value[BIG_BITMAP_INDEX].byteCount + value[ICON_BITMAP_INDEX].byteCount
            }
        }
    }

    override fun loadImage(context: Context, url: String?, callBack: ImageLoaderCallBack) {
        if (url.isNullOrEmpty()) {
            return
        }
        val bitmap = mCache.get(url)
        if (bitmap != null) {
            callBack.onBitmapLoaded(bitmap[BIG_BITMAP_INDEX])
            return
        }
        BitmapAsyncTask(url, callBack, mCache)
            .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    private class BitmapAsyncTask internal constructor(
        private val artUrl: String, private val listener: ImageLoaderCallBack?,
        private val mCache: LruCache<String, Array<Bitmap>>
    ) : AsyncTask<Void, Void, Array<Bitmap>>() {

        override fun doInBackground(vararg voids: Void): Array<Bitmap>? {
            var bitmaps: Array<Bitmap>? = null
            try {
                val bitmap = fetchAndRescaleBitmap(artUrl, MAX_ART_WIDTH, MAX_ART_HEIGHT)
                val icon = scaleBitmap(bitmap, MAX_ART_WIDTH_ICON, MAX_ART_HEIGHT_ICON)
                if (icon != null && bitmap != null) {
                    bitmaps = arrayOf(bitmap, icon)
                    mCache.put(artUrl, bitmaps)
                }
            } catch (e: IOException) {
                return null
            }

            return bitmaps
        }

        override fun onPostExecute(bitmaps: Array<Bitmap>?) {
            if (listener == null) {
                return
            }
            if (bitmaps == null) {
                listener.onBitmapFailed(null)
            } else {
                listener.onBitmapLoaded(bitmaps[BIG_BITMAP_INDEX])
            }
        }

        private fun findScaleFactor(targetW: Int, targetH: Int, inputStream: InputStream): Int {
            // Get the dimensions of the bitmap
            val bmOptions = BitmapFactory.Options()
            bmOptions.inJustDecodeBounds = true
            BitmapFactory.decodeStream(inputStream, null, bmOptions)
            val actualW = bmOptions.outWidth
            val actualH = bmOptions.outHeight

            // Determine how much to scale down the image
            return (actualW / targetW).coerceAtMost(actualH / targetH)
        }

        @Throws(IOException::class)
        private fun fetchAndRescaleBitmap(uri: String, width: Int, height: Int): Bitmap? {
            val url = URL(uri)
            var inputStream: BufferedInputStream? = null
            try {
                val urlConnection = url.openConnection() as HttpURLConnection
                inputStream = BufferedInputStream(urlConnection.inputStream)
                inputStream.mark(MAX_READ_LIMIT_PER_IMG)
                val scaleFactor = findScaleFactor(width, height, inputStream)
                inputStream.reset()
                return scaleBitmap(scaleFactor, inputStream)
            } finally {
                inputStream?.close()
            }
        }

        private fun scaleBitmap(scaleFactor: Int, inputStream: InputStream): Bitmap? {
            // Get the dimensions of the bitmap
            val bmOptions = BitmapFactory.Options()

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false
            bmOptions.inSampleSize = scaleFactor

            return BitmapFactory.decodeStream(inputStream, null, bmOptions)
        }

        private fun scaleBitmap(src: Bitmap?, maxWidth: Int, maxHeight: Int): Bitmap? {
            return if (src != null && src.width > 0 && src.height > 0) {
                val scaleFactor =
                    (maxWidth.toDouble() / src.width).coerceAtMost(
                        maxHeight.toDouble() / src.height)
                Bitmap.createScaledBitmap(src, (src.width * scaleFactor).toInt(),
                    (src.height * scaleFactor).toInt(), false)
            } else {
                null
            }
        }
    }
}