package com.lzx.starrysky.model;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.text.TextUtils;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.lzx.starrysky.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MusicProvider {

    private List<SongInfo> mSongInfos;
    private ConcurrentMap<String, List<MediaMetadataCompat>> mMusicListById;
    private List<MediaMetadataCompat> mMusicList;

    public static MusicProvider getInstance() {
        return SingletonHolder.sInstance;
    }

    private static class SingletonHolder {
        private static final MusicProvider sInstance = new MusicProvider();
    }

    private MusicProvider() {
        mSongInfos = Collections.synchronizedList(new ArrayList<>());
        mMusicList = Collections.synchronizedList(new ArrayList<>());
        mMusicListById = new ConcurrentHashMap<>();
    }

    public List<SongInfo> getSongInfos() {
        return mSongInfos;
    }

    public void setSongInfos(List<SongInfo> songInfos) {
        mSongInfos = songInfos;
    }

    public ConcurrentMap<String, List<MediaMetadataCompat>> getMusicListById() {
        return mMusicListById;
    }

    public List<MediaMetadataCompat> getMusicList() {
        return mMusicList;
    }

    public List<MediaBrowserCompat.MediaItem> getChildrenResult(String mediaId) {
        List<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<>();
        if (!TextUtils.isEmpty(mediaId)) {
            List<MediaMetadataCompat> mediaMetadataCompats = mMusicListById.get(mediaId);
            if (mediaMetadataCompats != null) {
                for (MediaMetadataCompat metadata : mediaMetadataCompats) {
                    MediaBrowserCompat.MediaItem mediaItem = new MediaBrowserCompat.MediaItem(
                            metadata.getDescription(),
                            MediaBrowserCompat.MediaItem.FLAG_PLAYABLE);
                    mediaItems.add(mediaItem);
                }
            }
        } else {
            for (MediaMetadataCompat metadata : mMusicList) {
                MediaBrowserCompat.MediaItem mediaItem = new MediaBrowserCompat.MediaItem(
                        metadata.getDescription(),
                        MediaBrowserCompat.MediaItem.FLAG_PLAYABLE);
                mediaItems.add(mediaItem);
            }
        }
        return mediaItems;
    }

    public void retrieveMediaAsync(Context context, Callback callback) {
        UpdateCatalogTask task = new UpdateCatalogTask(context, mSongInfos, (concurrentMap, mediaMetadataCompats) -> {
            mMusicList = mediaMetadataCompats;
            mMusicListById = concurrentMap;
            callback.onReady();
        });
        task.executeOnExecutor(Executors.newCachedThreadPool());
    }

    public static class UpdateCatalogTask extends AsyncTask<Void, Void, List<MediaMetadataCompat>> {

        @SuppressLint("StaticFieldLeak")
        private Context mContext;
        private List<SongInfo> songInfos;
        private AnsyncCallback callback;

        UpdateCatalogTask(Context context, List<SongInfo> songInfos, AnsyncCallback callback) {
            mContext = context;
            this.songInfos = songInfos;
            this.callback = callback;
        }

        @Override
        protected List<MediaMetadataCompat> doInBackground(Void... voids) {
            List<MediaMetadataCompat> list = new ArrayList<>();
            try {
                list = toMediaMetadata(mContext, songInfos);
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return list;
        }

        @Override
        protected void onPostExecute(List<MediaMetadataCompat> mediaMetadataCompats) {
            super.onPostExecute(mediaMetadataCompats);

            ConcurrentMap<String, List<MediaMetadataCompat>> newMusicList = new ConcurrentHashMap<>();
            for (MediaMetadataCompat m : mediaMetadataCompats) {
                String songId = m.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
                List<MediaMetadataCompat> list = newMusicList.get(songId);
                if (list == null) {
                    list = new ArrayList<>();
                    newMusicList.put(songId, list);
                }
                list.add(m);
            }

            callback.onMusicCatalogReady(newMusicList, mediaMetadataCompats);
        }
    }

    private static List<MediaMetadataCompat> toMediaMetadata(Context context, List<SongInfo> songInfos) throws ExecutionException, InterruptedException {
        List<MediaMetadataCompat> mediaMetadataCompats = new ArrayList<>();
        for (SongInfo info : songInfos) {

            String albumTitle = "";
            if (!TextUtils.isEmpty(info.getAlbumName())) {
                albumTitle = info.getAlbumName();
            } else if (!TextUtils.isEmpty(info.getSongName())) {
                albumTitle = info.getSongName();
            }
            String albumUrl = "";
            if (!TextUtils.isEmpty(info.getAlbumCover())) {
                albumUrl = info.getAlbumCover();
            } else if (!TextUtils.isEmpty(info.getSongCover())) {
                albumUrl = info.getSongCover();
            }
            MediaMetadataCompat.Builder builder = new MediaMetadataCompat.Builder();
            builder.putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, info.getSongId());
            builder.putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, info.getSongUrl());
            if (!TextUtils.isEmpty(albumTitle)) {
                builder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, albumTitle);
            }
            if (!TextUtils.isEmpty(info.getArtist())) {
                builder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, info.getArtist());
            }
            if (info.getDuration() != -1) {
                long durationMs = TimeUnit.SECONDS.toMillis(info.getDuration());
                builder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, durationMs);
            }
            if (!TextUtils.isEmpty(info.getGenre())) {
                builder.putString(MediaMetadataCompat.METADATA_KEY_GENRE, info.getGenre());
            }
            if (!TextUtils.isEmpty(albumUrl)) {
                builder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, albumUrl);
                Bitmap art = Glide.with(context).applyDefaultRequestOptions(
                        new RequestOptions()
                                .fallback(R.drawable.default_art)
                                .diskCacheStrategy(DiskCacheStrategy.RESOURCE))
                        .asBitmap()
                        .load(albumUrl)
                        .submit(144, 144)
                        .get();
                builder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, art);
                builder.putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, art);
            }
            if (!TextUtils.isEmpty(info.getSongName())) {
                builder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, info.getSongName());
            }
            if (info.getTrackNumber() != -1) {
                builder.putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, info.getTrackNumber());
            }
            builder.putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, info.getAlbumSongCount());
            mediaMetadataCompats.add(builder.build());

        }
        return mediaMetadataCompats;
    }

    public ConcatenatingMediaSource toMediaSource(DataSource.Factory dataSourceFactory) {
        ConcatenatingMediaSource concatenatingMediaSource = new ConcatenatingMediaSource();
        for (MediaMetadataCompat metadata : mMusicList) {
            MediaSource mediaSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                    .setTag(fullDescription(metadata))
                    .createMediaSource(Uri.parse(metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI)));
            concatenatingMediaSource.addMediaSource(mediaSource);
        }
        return concatenatingMediaSource;
    }

    private Object fullDescription(MediaMetadataCompat metadata) {
        Bundle bundle = metadata.getDescription().getExtras();
        if (bundle != null) {
            bundle.putAll(metadata.getBundle());
        }
        return bundle;
    }


    public interface AnsyncCallback {
        void onMusicCatalogReady(ConcurrentMap<String, List<MediaMetadataCompat>> concurrentMap, List<MediaMetadataCompat> mediaMetadataCompats);
    }

    public interface Callback {
        void onReady();
    }


}
