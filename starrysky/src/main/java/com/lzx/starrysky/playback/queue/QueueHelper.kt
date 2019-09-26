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

package com.lzx.starrysky.playback.queue

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.text.TextUtils

import java.util.ArrayList

/**
 * 播放队列帮助类
 */
object QueueHelper {

    private val TAG = "QueueHelper"

    /**
     * List<MediaMetadataCompat> 转 List<MediaSessionCompat.QueueItem>
    </MediaSessionCompat.QueueItem></MediaMetadataCompat> */
    private fun convertToQueue(
        tracks: List<MediaMetadataCompat>
    ): List<MediaSessionCompat.QueueItem> {
        val queue = ArrayList<MediaSessionCompat.QueueItem>()
        for ((count, track) in tracks.withIndex()) {
            val trackCopy = MediaMetadataCompat.Builder(track)
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, track.description.mediaId)
                .build()
            val item = MediaSessionCompat.QueueItem(
                trackCopy.description, count.toLong())
            queue.add(item)
        }
        return queue
    }

    /**
     * 检查下标有没有越界
     */
    fun <T> isIndexPlayable(index: Int, queue: List<T>?): Boolean {
        return queue != null && index >= 0 && index < queue.size
    }

    /**
     * 对比两个列表
     */
    fun equals(
        list1: List<MediaSessionCompat.QueueItem>?,
        list2: List<MediaSessionCompat.QueueItem>?
    ): Boolean {
        if (list1 === list2) {
            return true
        }
        if (list1 == null || list2 == null) {
            return false
        }
        if (list1.size != list2.size) {
            return false
        }
        for (i in list1.indices) {
            if (list1[i].queueId != list2[i].queueId) {
                return false
            }
            if (!TextUtils.equals(list1[i].description.mediaId,
                    list2[i].description.mediaId)) {
                return false
            }
        }
        return true
    }
}
