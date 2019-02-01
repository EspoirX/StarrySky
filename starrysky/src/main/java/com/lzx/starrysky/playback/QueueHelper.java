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

import android.app.Activity;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.text.TextUtils;

import com.lzx.starrysky.model.MusicProvider;

import java.util.ArrayList;
import java.util.List;


/**
 * 播放队列帮助类
 */
public class QueueHelper {

    private static final String TAG = "QueueHelper";

    /**
     * 获取正在播放的队列
     */
    public static List<MediaSessionCompat.QueueItem> getPlayingQueue(MusicProvider musicProvider) {
        List<MediaMetadataCompat> tracks = musicProvider.getMusicList();
        return convertToQueue(tracks);
    }

    /**
     * 获取 id 为 mediaId 的媒体在播放队列中的下标
     */
    public static int getMusicIndexOnQueue(Iterable<MediaSessionCompat.QueueItem> queue, String mediaId) {
        int index = 0;
        for (MediaSessionCompat.QueueItem item : queue) {
            if (mediaId.equals(item.getDescription().getMediaId())) {
                return index;
            }
            index++;
        }
        return -1;
    }

    public static int getMusicIndexOnQueue(Iterable<MediaSessionCompat.QueueItem> queue, long queueId) {
        int index = 0;
        for (MediaSessionCompat.QueueItem item : queue) {
            if (queueId == item.getQueueId()) {
                return index;
            }
            index++;
        }
        return -1;
    }

    /**
     * List<MediaMetadataCompat> 转 List<MediaSessionCompat.QueueItem>
     */
    private static List<MediaSessionCompat.QueueItem> convertToQueue(List<MediaMetadataCompat> tracks) {
        List<MediaSessionCompat.QueueItem> queue = new ArrayList<>();
        int count = 0;
        for (MediaMetadataCompat track : tracks) {
            MediaMetadataCompat trackCopy = new MediaMetadataCompat.Builder(track)
                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, track.getDescription().getMediaId())
                    .build();
            MediaSessionCompat.QueueItem item = new MediaSessionCompat.QueueItem(
                    trackCopy.getDescription(), count++);
            queue.add(item);
        }
        return queue;

    }

    /**
     * 获取乱序的 List#MediaSessionCompat.QueueItem
     */
    public static List<MediaSessionCompat.QueueItem> getRandomQueue(MusicProvider musicProvider) {
        List<MediaMetadataCompat> result = new ArrayList<>();
        Iterable<MediaMetadataCompat> shuffled = musicProvider.getShuffledMusic();
        for (MediaMetadataCompat metadata : shuffled) {
            result.add(metadata);
        }
        return convertToQueue(result);
    }

    /**
     * 检查下标有没有越界
     */
    public static boolean isIndexPlayable(int index, List<MediaSessionCompat.QueueItem> queue) {
        return (queue != null && index >= 0 && index < queue.size());
    }

    /**
     * 对比两个列表
     */
    public static boolean equals(List<MediaSessionCompat.QueueItem> list1,
                                 List<MediaSessionCompat.QueueItem> list2) {
        if (list1 == list2) {
            return true;
        }
        if (list1 == null || list2 == null) {
            return false;
        }
        if (list1.size() != list2.size()) {
            return false;
        }
        for (int i = 0; i < list1.size(); i++) {
            if (list1.get(i).getQueueId() != list2.get(i).getQueueId()) {
                return false;
            }
            if (!TextUtils.equals(list1.get(i).getDescription().getMediaId(),
                    list2.get(i).getDescription().getMediaId())) {
                return false;
            }
        }
        return true;
    }


    /**
     * 判断当前的媒体是否在播放
     */
    public static boolean isQueueItemPlaying(Activity context, MediaSessionCompat.QueueItem queueItem) {
        MediaControllerCompat controller = MediaControllerCompat.getMediaController(context);
        if (controller != null && controller.getPlaybackState() != null) {
            long currentPlayingQueueId = controller.getPlaybackState().getActiveQueueItemId();
            String currentPlayingMediaId = controller.getMetadata().getDescription().getMediaId();
            String itemMusicId = queueItem.getDescription().getMediaId();
            return queueItem.getQueueId() == currentPlayingQueueId
                    && currentPlayingMediaId != null
                    && TextUtils.equals(currentPlayingMediaId, itemMusicId);
        }
        return false;
    }
}
