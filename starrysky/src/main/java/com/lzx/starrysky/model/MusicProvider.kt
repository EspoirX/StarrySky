package com.lzx.starrysky.model

import android.content.Context
import android.os.AsyncTask
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.lzx.starrysky.R
import com.lzx.starrysky.extensions.*
import com.lzx.starrysky.library.*

import java.util.ArrayList
import java.util.Collections
import java.util.concurrent.TimeUnit

class MusicProvider(context: Context) : AbstractMusicSource() {
    private var songInfos: List<SongInfo>? = null
    private var context: Context
    private var catalog: List<MediaMetadataCompat> = emptyList()

    val shuffledMusic: Iterable<SongInfo>
        get() {
            val shuffled = Collections.synchronizedList(ArrayList<SongInfo>())
            shuffled.addAll(songInfos!!)
            shuffled.shuffle()
            return shuffled
        }

    init {
        songInfos = Collections.synchronizedList(ArrayList())
        this.context = context
    }


    fun loadSongInfoAnsy() {
        state = STATE_INITIALIZING

        UpdateCatalogTask(Glide.with(context)) { mediaItems ->
            catalog = mediaItems
            state = STATE_INITIALIZED
        }.execute(songInfos)
    }

    override fun iterator(): Iterator<MediaMetadataCompat> = catalog.iterator()


    class UpdateCatalogTask(var glide: RequestManager, var listener: (List<MediaMetadataCompat>) -> Unit) :
            AsyncTask<List<SongInfo>, Void, List<MediaMetadataCompat>>() {

        private val NOTIFICATION_LARGE_ICON_SIZE = 144 // px

        private val glideOptions = RequestOptions()
                .fallback(R.drawable.default_art)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)

        override fun doInBackground(vararg lists: List<SongInfo>): List<MediaMetadataCompat> {
            val mediaItems = java.util.ArrayList<MediaMetadataCompat>()
            for (info in lists[0]) {
                val art = glide.applyDefaultRequestOptions(glideOptions)
                        .asBitmap()
                        .load(info.songCover)
                        .submit(NOTIFICATION_LARGE_ICON_SIZE, NOTIFICATION_LARGE_ICON_SIZE)
                        .get()
                val compat = MediaMetadataCompat.Builder().from(info)
                        .apply {
                            albumArt = art
                        }
                        .build()
                mediaItems.add(compat)
            }
            return mediaItems
        }

        override fun onPostExecute(mediaItems: List<MediaMetadataCompat>) {
            super.onPostExecute(mediaItems)
            listener(mediaItems)
        }

        /**
         * MediaMetadataCompat.Builder 扩展
         */
        fun MediaMetadataCompat.Builder.from(song: SongInfo): MediaMetadataCompat.Builder {
            val durationMs = TimeUnit.SECONDS.toMillis(song.duration)

            id = song.songId
            title = song.songName
            artist = song.artist
            album = song.albumName
            duration = durationMs
            genre = song.genre
            mediaUri = song.songUrl
            albumArtUri = song.songCover
            trackNumber = song.trackNumber
            trackCount = song.albumSongCount
            flag = MediaBrowserCompat.MediaItem.FLAG_PLAYABLE


            displayTitle = song.songName
            displaySubtitle = song.artist
            displayDescription = song.description
            displayIconUri = song.songCover

            downloadStatus = MediaDescriptionCompat.STATUS_NOT_DOWNLOADED

            return this
        }

    }


}
