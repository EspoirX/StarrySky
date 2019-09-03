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

package com.lzx.starrysky.playback;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaMetadataCompat;
import android.text.TextUtils;

import com.lzx.starrysky.R;
import com.lzx.starrysky.StarrySky;
import com.lzx.starrysky.provider.MediaQueueProvider;
import com.lzx.starrysky.provider.MediaQueueProviderSurface;
import com.lzx.starrysky.provider.MediaResource;
import com.lzx.starrysky.provider.SongInfo;
import com.lzx.starrysky.utils.imageloader.BitmapCallBack;
import com.lzx.starrysky.utils.imageloader.ImageLoader;

import java.util.List;

public class MediaQueueManager extends MediaQueueProviderSurface implements MediaQueue {

    //下标
    private int mCurrentIndex;
    private MediaResource mMediaResource;
    private MediaQueueProvider.MetadataUpdateListener mUpdateListener;
    private Context mContext;

    public MediaQueueManager(MediaQueueProvider provider, Context context) {
        super(provider);
        mCurrentIndex = 0;
        mContext = context;
    }

    @Override
    public void setMetadataUpdateListener(MetadataUpdateListener listener) {
        mUpdateListener = listener;
    }

    @Override
    public boolean isSameBrowsingCategory(@NonNull String mediaId) {
        MediaResource current = getCurrentMusic();
        if (current == null) {
            return false;
        }
        return mediaId.equals(current.getMediaId());
    }

    @Override
    public int getCurrentIndex() {
        return mCurrentIndex;
    }

    @Override
    public boolean skipQueuePosition(int amount) {
        List<SongInfo> mPlayingQueue = getSongInfos();
        if (mPlayingQueue.size() == 0) {
            return false;
        }
        int index = mCurrentIndex + amount;
        if (index < 0) {
            index = 0;
        } else {
            index %= mPlayingQueue.size();
        }
        if (!QueueHelper.isIndexPlayable(index, mPlayingQueue)) {
            return false;
        }
        mCurrentIndex = index;
        return true;
    }

    @Override
    public void setRandomQueue() {

    }

    @Override
    public MediaResource getCurrentMusic() {
        List<SongInfo> songInfos = getSongInfos();
        if (!QueueHelper.isIndexPlayable(mCurrentIndex, songInfos)) {
            return null;
        }
        SongInfo queueItem = songInfos.get(mCurrentIndex);
        //由于MediaQueueManager在构建Starry时初始化，所以这里不能放在构造函数中
        if (mMediaResource == null) {
            mMediaResource = StarrySky.get().getRegistry().get(MediaResource.class);
        }
        return mMediaResource.obtain(queueItem.getSongId(), queueItem.getTrackNumber());
    }

    @Override
    public boolean setCurrentQueueItem(String mediaId) {
        int index = QueueHelper.getMusicIndexOnSongInfos(getSongInfos(), mediaId);
        if (index >= 0 && index < getSongInfos().size()) {
            mCurrentIndex = index;
            if (mUpdateListener != null) {
                mUpdateListener.onCurrentQueueIndexUpdated(mCurrentIndex);
            }
        }
        return index >= 0;
    }

    @Override
    public void updateCurrPlayingMedia(String mediaId) {
        boolean canReuseQueue = false;
        if (isSameBrowsingCategory(mediaId)) {
            canReuseQueue = setCurrentQueueItem(mediaId);
        }
        if (!canReuseQueue) {
            mCurrentIndex = QueueHelper.getMusicIndexOnSongInfos(getSongInfos(), mediaId);
        }
        updateMetadata();
    }

    @Override
    public void updateMetadata() {
        MediaResource currentMusic = getCurrentMusic();
        if (currentMusic == null) {
            if (mUpdateListener != null) {
                mUpdateListener.onMetadataRetrieveError();
            }
            return;
        }
        final String musicId = currentMusic.getMediaId();
        MediaMetadataCompat metadata = getMusic(musicId);
        if (metadata == null) {
            throw new IllegalArgumentException("Invalid musicId " + musicId);
        }
        if (mUpdateListener != null) {
            mUpdateListener.onMetadataChanged(metadata);
        }
        //更新封面 bitmap
        String coverUrl = metadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI);
        if (!TextUtils.isEmpty(coverUrl)) {
            ImageLoader.getInstance()
                    .load(coverUrl)
                    .context(mContext)
                    .placeholder(R.drawable.default_art)
                    .resize(144, 144)
                    .bitmap(new BitmapCallBack.SimperCallback() {
                        @Override
                        public void onBitmapLoaded(Bitmap resource) {
                            super.onBitmapLoaded(resource);
                            updateMusicArt(musicId, metadata, resource, resource);
                            if (mUpdateListener != null) {
                                mUpdateListener.onMetadataChanged(metadata);
                            }
                        }
                    });
        }
    }

    //    private static final String TAG = "QueueManager";
//
//    private Context mContext;
//    private MediaQueueProvider mMusicProvider;
//    private MetadataUpdateListener mListener;
//    private MediaResource mMediaResource;
//
//    //正在播放的队列
//    private List<MediaSessionCompat.QueueItem> mPlayingQueue;
//
//    //下标
//    private int mCurrentIndex;
//
//    public MediaQueueManager(Context context, @NonNull MediaQueueProvider musicProvider,
//                             @NonNull MetadataUpdateListener listener) {
//        mContext = context;
//        this.mMusicProvider = musicProvider;
//        this.mListener = listener;
//
//        mPlayingQueue = Collections.synchronizedList(new ArrayList<>());
//        mCurrentIndex = 0;
//        mMediaResource = StarrySky.get().getRegistry().getMediaResource();
//    }
//
//    /**
//     * 判断传入的媒体跟正在播放的媒体是否一样
//     */
//    public boolean isSameBrowsingCategory(@NonNull String mediaId) {
//        //MediaSessionCompat.QueueItem current = getCurrentMusic();
//        MediaResource current = getCurrentMusic();
//        if (current == null) {
//            return false;
//        }
//        //return mediaId.equals(current.getDescription().getMediaId());
//        return mediaId.equals(current.getMediaId());
//    }
//
//    /**
//     * 更新当前下标并通知
//     */
//    private void setCurrentQueueIndex(int index) {
//        if (index >= 0 && index < mPlayingQueue.size()) {
//            mCurrentIndex = index;
//            mListener.onCurrentQueueIndexUpdated(mCurrentIndex);
//        }
//    }
//
//    /**
//     * 获取当前下标
//     */
//    public int getCurrentIndex() {
//        return mCurrentIndex;
//    }
//
//    /**
//     * 根据传入的媒体id来更新此媒体的下标并通知
//     */
//    public boolean setCurrentQueueItem(String mediaId) {
//        int index = QueueHelper.getMusicIndexOnQueue(mPlayingQueue, mediaId);
//        setCurrentQueueIndex(index);
//        return index >= 0;
//    }
//
//    /**
//     * 转跳下一首或上一首
//     *
//     * @param amount 正为下一首，负为上一首
//     */
//    public boolean skipQueuePosition(int amount) {
//        if (mPlayingQueue.size() == 0) {
//            return false;
//        }
//        int index = mCurrentIndex + amount;
//        if (index < 0) {
//            index = 0;
//        } else {
//            index %= mPlayingQueue.size();
//        }
//        if (!QueueHelper.isIndexPlayable(index, mPlayingQueue)) {
//            return false;
//        }
//        mCurrentIndex = index;
//        return true;
//    }
//
//    /**
//     * 打乱当前的列表顺序
//     */
//    public void setRandomQueue() {
//        setCurrentQueue(QueueHelper.getRandomQueue(mMusicProvider));
//        updateMetadata();
//    }
//
//    /**
//     * 如果当前模式是随机，则打乱顺序，否则恢复正常顺序
//     */
//    public void setQueueByShuffleMode(int shuffleMode) {
//        if (shuffleMode == PlaybackStateCompat.SHUFFLE_MODE_NONE) {
//            setCurrentQueue(QueueHelper.getPlayingQueue(mMusicProvider));
//        } else if (shuffleMode == PlaybackStateCompat.SHUFFLE_MODE_ALL) {
//            setCurrentQueue(QueueHelper.getRandomQueue(mMusicProvider));
//        }
//    }
//
//    public void setQueueFromMusic(String mediaId) {
//        boolean canReuseQueue = false;
//        if (isSameBrowsingCategory(mediaId)) {
//            canReuseQueue = setCurrentQueueItem(mediaId);
//        }
//        if (!canReuseQueue) {
//            setCurrentQueue(QueueHelper.getPlayingQueue(mMusicProvider), mediaId);
//        }
//        updateMetadata();
//    }
//
//    /**
//     * 获取当前播放的媒体
//     */
////    public MediaSessionCompat.QueueItem getCurrentMusic() {
////        if (!QueueHelper.isIndexPlayable(mCurrentIndex, mPlayingQueue)) {
////            return null;
////        }
////        return mPlayingQueue.get(mCurrentIndex);
////    }
//    public MediaResource getCurrentMusic() {
//        if (!QueueHelper.isIndexPlayable(mCurrentIndex, mPlayingQueue)) {
//            return null;
//        }
//        MediaSessionCompat.QueueItem queueItem = mPlayingQueue.get(mCurrentIndex);
//        return mMediaResource.obtain(queueItem.getDescription().getMediaId(), queueItem.getQueueId());
//    }
//
//
//    /**
//     * 获取队列大小
//     */
//    public int getCurrentQueueSize() {
//        if (mPlayingQueue == null) {
//            return 0;
//        }
//        return mPlayingQueue.size();
//    }
//
//    /**
//     * 更新队列,下标为 0
//     */
//    protected void setCurrentQueue(List<MediaSessionCompat.QueueItem> newQueue) {
//        setCurrentQueue(newQueue, null);
//    }
//
//    /**
//     * 更新队列和下标
//     */
//    protected void setCurrentQueue(List<MediaSessionCompat.QueueItem> newQueue, String initialMediaId) {
//        mPlayingQueue = newQueue;
//        int index = 0;
//        if (initialMediaId != null) {
//            index = QueueHelper.getMusicIndexOnQueue(mPlayingQueue, initialMediaId);
//        }
//        mCurrentIndex = Math.max(index, 0);
//        mListener.onQueueUpdated(newQueue);
//    }
//
//    /**
//     * 更新媒体信息
//     */
//    public void updateMetadata() {
//        // MediaSessionCompat.QueueItem currentMusic = getCurrentMusic();
//        MediaResource currentMusic = getCurrentMusic();
//        if (currentMusic == null) {
//            mListener.onMetadataRetrieveError();
//            return;
//        }
//        //final String musicId = currentMusic.getDescription().getMediaId();
//        final String musicId = currentMusic.getMediaId();
//        MediaMetadataCompat metadata = mMusicProvider.getMusic(musicId);
//        if (metadata == null) {
//            throw new IllegalArgumentException("Invalid musicId " + musicId);
//        }
//        mListener.onMetadataChanged(metadata);
//        //更新封面 bitmap
//        String coverUrl = metadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI);
//        if (!TextUtils.isEmpty(coverUrl)) {
//            ImageLoader.getInstance()
//                    .load(coverUrl)
//                    .context(mContext)
//                    .placeholder(R.drawable.default_art)
//                    .resize(144, 144)
//                    .bitmap(new BitmapCallBack.SimperCallback() {
//                        @Override
//                        public void onBitmapLoaded(Bitmap resource) {
//                            super.onBitmapLoaded(resource);
//                            mMusicProvider.updateMusicArt(musicId, metadata, resource, resource);
//                            mListener.onMetadataChanged(metadata);
//                        }
//                    });
//        }
//    }
//
//    public interface MetadataUpdateListener {
//        void onMetadataChanged(MediaMetadataCompat metadata);
//
//        void onMetadataRetrieveError();
//
//        void onCurrentQueueIndexUpdated(int queueIndex);
//
//        void onQueueUpdated(List<MediaSessionCompat.QueueItem> newQueue);
//    }
}
