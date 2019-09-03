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

package com.lzx.starrysky.playback.queue;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
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
    private List<SongInfo> mSongInfosCopy;

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
        return getCurrentMusic(null);
    }

    @Override
    public MediaResource getCurrentMusic(SongInfo songInfo) {
        List<SongInfo> songInfos = getSongInfos();
        if (!QueueHelper.isIndexPlayable(mCurrentIndex, songInfos)) {
            return null;
        }
        SongInfo queueItem;
        if (songInfo != null) {
            songInfos.set(mCurrentIndex, songInfo);
            queueItem = songInfo;
        } else {
            queueItem = songInfos.get(mCurrentIndex);
        }
        //由于MediaQueueManager在构建Starry时初始化，所以这里不能放在构造函数中
        if (mMediaResource == null) {
            mMediaResource = StarrySky.get().getRegistry().get(MediaResource.class);
        }
        return mMediaResource.obtain(queueItem.getSongId(), queueItem.getSongUrl(), queueItem.getTrackNumber());
    }

    @Override
    public SongInfo getCurrSongInfo() {
        List<SongInfo> songInfos = getSongInfos();
        if (!QueueHelper.isIndexPlayable(mCurrentIndex, songInfos)) {
            return songInfos.get(mCurrentIndex);
        }
        return null;
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
        String coverUrl = currentMusic.getMediaUrl();// metadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI);
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

    @Override
    public int getCurrentQueueSize() {
        return getSongInfos().size();
    }

    @Override
    public void setQueueByShuffleMode(int shuffleMode) {
        if (shuffleMode == PlaybackStateCompat.SHUFFLE_MODE_NONE) {
            if (mSongInfosCopy != null) {
                setSongInfos(mSongInfosCopy);
            }
        } else if (shuffleMode == PlaybackStateCompat.SHUFFLE_MODE_ALL) {
            mSongInfosCopy = getSongInfos();
            getShuffledSongInfo();
        }
    }
}
