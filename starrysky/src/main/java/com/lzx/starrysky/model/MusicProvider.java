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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 媒体信息提供类
 */
public class MusicProvider {

    private List<SongInfo> mSongInfos;
    private List<MediaMetadataCompat> metadatas;

    enum State {
        NON_INITIALIZED, INITIALIZING, INITIALIZED
    }

    private volatile static State mCurrentState = State.NON_INITIALIZED;

    public static MusicProvider getInstance() {
        return SingletonHolder.sInstance;
    }

    private static class SingletonHolder {
        private static final MusicProvider sInstance = new MusicProvider();
    }

    private MusicProvider() {
        mSongInfos = Collections.synchronizedList(new ArrayList<>());
        metadatas = Collections.synchronizedList(new ArrayList<>());
    }

    /**
     * 获取原始的List<SongInfo>
     */
    public List<SongInfo> getSongInfos() {
        return mSongInfos;
    }

    /**
     * 设置播放列表
     */
    public void setSongInfos(List<SongInfo> songInfos) {
        mSongInfos = songInfos;
    }

    public List<MediaMetadataCompat> getMetadatas() {
        return metadatas;
    }

    /**
     * 获取 List<MediaBrowserCompat.MediaItem> 用于 onLoadChildren 回调
     */
    public List<MediaBrowserCompat.MediaItem> getChildrenResult(String mediaId) {
        List<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<>();
        for (MediaMetadataCompat metadata : metadatas) {
            MediaBrowserCompat.MediaItem mediaItem = new MediaBrowserCompat.MediaItem(
                    metadata.getDescription(),
                    MediaBrowserCompat.MediaItem.FLAG_PLAYABLE);
            mediaItems.add(mediaItem);
        }
        return mediaItems;
    }

    /**
     * 获取乱序列表
     */
    public Iterable<MediaMetadataCompat> getShuffledMusic() {
        if (mCurrentState != State.INITIALIZED) {
            return Collections.emptyList();
        }
        List<MediaMetadataCompat> shuffled = new ArrayList<>(metadatas.size());
        shuffled.addAll(metadatas);
        Collections.shuffle(shuffled);
        return shuffled;
    }

    /**
     * 根据id获取对应的MediaMetadataCompat对象
     */
    public MediaMetadataCompat getMusic(String songId) {
        MediaMetadataCompat music = null;
        for (MediaMetadataCompat data : metadatas) {
            if (data != null && data.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID).equals(songId)) {
                music = data;
                break;
            }
        }
        return music;
    }

    /**
     * 异步加载给metadatasById和metadatas赋值
     */
    public void retrieveMediaAsync(Context context, Callback callback) {
        if (isInitialized()) {
            callback.onReady();
            return;
        }
        UpdateCatalogTask task = new UpdateCatalogTask(context, mSongInfos, (mediaMetadataCompats) -> {
            metadatas = mediaMetadataCompats;
            // metadatasById = concurrentMap;
            callback.onReady();
        });
        task.executeOnExecutor(Executors.newCachedThreadPool());
    }

    /**
     * 是否已经加载完
     */
    public boolean isInitialized() {
        return mCurrentState == State.INITIALIZED;
    }

    public void nonInitialized() {
        mCurrentState = State.NON_INITIALIZED;
    }

    /**
     * 加载List<MediaMetadataCompat>的异步任务类
     */
    public static class UpdateCatalogTask extends AsyncTask<Void, Void, List<MediaMetadataCompat>> {

        @SuppressLint("StaticFieldLeak")
        private Context mContext;
        private List<SongInfo> songInfos;
        private AsyncCallback callback;

        UpdateCatalogTask(Context context, List<SongInfo> songInfos, AsyncCallback callback) {
            mContext = context;
            this.songInfos = songInfos;
            this.callback = callback;
        }

        @Override
        protected List<MediaMetadataCompat> doInBackground(Void... voids) {
            List<MediaMetadataCompat> list = new ArrayList<>();
            try {
                list = toMediaMetadata(mContext, songInfos);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return list;
        }

        @Override
        protected void onPostExecute(List<MediaMetadataCompat> mediaMetadataCompats) {
            super.onPostExecute(mediaMetadataCompats);
            callback.onMusicCatalogReady(mediaMetadataCompats);
        }
    }

    /**
     * List<SongInfo> 转 List<MediaMetadataCompat>
     */
    private synchronized static List<MediaMetadataCompat> toMediaMetadata(Context context, List<SongInfo> songInfos) throws ExecutionException, InterruptedException {
        List<MediaMetadataCompat> mediaMetadataCompats = new ArrayList<>();
        try {
            if (mCurrentState == State.NON_INITIALIZED) {
                mCurrentState = State.INITIALIZING;
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
                mCurrentState = State.INITIALIZED;
            }
        } finally {
            if (mCurrentState != State.INITIALIZED) {
                mCurrentState = State.NON_INITIALIZED;
            }
        }
        return mediaMetadataCompats;
    }

    /**
     * List<MediaMetadataCompat> 转 ConcatenatingMediaSource
     */
    public ConcatenatingMediaSource toMediaSource(DataSource.Factory dataSourceFactory) {
        ConcatenatingMediaSource concatenatingMediaSource = new ConcatenatingMediaSource();
        for (MediaMetadataCompat metadata : metadatas) {
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


    public interface AsyncCallback {
        void onMusicCatalogReady(List<MediaMetadataCompat> mediaMetadataCompats);
    }

    public interface Callback {
        void onReady();
    }


}
