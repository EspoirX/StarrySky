/*
 * Copyright 2018 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lzx.starrysky

import androidx.lifecycle.MutableLiveData
import android.content.ComponentName
import android.content.Context
import android.support.v4.media.MediaBrowserCompat
import androidx.media.MediaBrowserServiceCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat


/**
 * Class that manages a connection to a [MediaBrowserServiceCompat] instance.
 *
 * Typically it's best to construct/inject dependencies either using DI or, as UAMP does,
 * using [InjectorUtils]. There are a few difficulties for that here:
 * - [MediaBrowserCompat] is a final class, so mocking it directly is difficult.
 * - A [MediaBrowserConnectionCallback] is a parameter into the construction of
 *   a [MediaBrowserCompat], and provides callbacks to this class.
 * - [MediaBrowserCompat.ConnectionCallback.onConnected] is the best place to construct
 *   a [MediaControllerCompat] that will be used to control the [MediaSessionCompat].
 *
 *  Because of these reasons, rather than constructing additional classes, this is treated as
 *  a black box (which is why there's very little logic here).
 *
 *  This is also why the parameters to construct a [MediaSessionConnection] are simple
 *  parameters, rather than private properties. They're only required to build the
 *  [MediaBrowserConnectionCallback] and [MediaBrowserCompat] objects.
 */

class MediaSessionConnection(context: Context, serviceComponent: ComponentName) {
    val isConnected = MutableLiveData<Boolean>().apply {
        postValue(false)  //lifecycle中的方法，isConnected改变后通知变化
    }

    //获取 root id
    val rootMediaId: String get() = mediaBrowser.root

    //状态变化通知
    val playbackState = MutableLiveData<PlaybackStateCompat>()
            .apply { postValue(EMPTY_PLAYBACK_STATE) }

    val nowPlaying = MutableLiveData<MediaMetadataCompat>()
            .apply { postValue(NOTHING_PLAYING) }

    //获取此会话的{@link TransportControls}实例。
    val transportControls: MediaControllerCompat.TransportControls
        get() = mediaController.transportControls

    //连接回调
    private val mediaBrowserConnectionCallback = MediaBrowserConnectionCallback(context)

    //创建MediaBrowserCompat对象，并且连接
    private val mediaBrowser = MediaBrowserCompat(context,
            serviceComponent,
            mediaBrowserConnectionCallback, null)
            .apply {
                connect() //连接
            }

    private lateinit var mediaController: MediaControllerCompat

    /**
          *查询有关指定ID中包含的媒体项的信息，并订阅以在更改时接收更新。
          * <p>
          *即使未连接也会保留订阅列表，并在重新连接后恢复。 可以在未连接的情况下进行订阅，但在连接完成之前不会返回结果。
          * </ p>
          * <p>
          *如果id已经使用不同的回调进行订阅，则新回调将替换前一回调，并且将重新加载子数据。
          * </ p>
         *
          * @param parentId将订阅其子项列表的父媒体项的ID。
          * @param callback接收子列表的回调。
         */
    fun subscribe(parentId: String, callback: MediaBrowserCompat.SubscriptionCallback) {
        mediaBrowser.subscribe(parentId, callback)
    }

    /**
          *取消订阅指定媒体ID的子项的更改。
          * <p>
          *此方法返回后，将不再为与此id关联的结果调用查询回调。
          * </ p>
         *
          * @param parentId将取消订阅其子项列表的父媒体项的ID。
          * @param callback发送到媒体浏览服务的回调订阅。
         */
    fun unsubscribe(parentId: String, callback: MediaBrowserCompat.SubscriptionCallback) {
        mediaBrowser.unsubscribe(parentId, callback)
    }

    //连接回调
    private inner class MediaBrowserConnectionCallback(private val context: Context)
        : MediaBrowserCompat.ConnectionCallback() {

        //连接成功后，在[MediaBrowserCompat.connect]之后调用。
        override fun onConnected() {
            // Get a MediaController for the MediaSession.
            //连接成功后创建 mediaController 对象
            mediaController = MediaControllerCompat(context, mediaBrowser.sessionToken).apply {
                //添加回调以从Session接收更新。 更新将发布在调用者的主题上。
                registerCallback(MediaControllerCallback())
            }
            //通知更新
            isConnected.postValue(true)
        }

        //当客户端与媒体浏览器断开连接时调用。
        override fun onConnectionSuspended() {
            //通知更新
            isConnected.postValue(false)
        }

        //与媒体浏览器的连接失败时调用。
        override fun onConnectionFailed() {
            isConnected.postValue(false)
        }
    }

    private inner class MediaControllerCallback : MediaControllerCompat.Callback() {

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            playbackState.postValue(state ?: EMPTY_PLAYBACK_STATE)
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            nowPlaying.postValue(metadata ?: NOTHING_PLAYING)
        }

        override fun onQueueChanged(queue: MutableList<MediaSessionCompat.QueueItem>?) {
        }

        /**
         * Normally if a [MediaBrowserServiceCompat] drops its connection the callback comes via
         * [MediaControllerCompat.Callback] (here). But since other connection status events
         * are sent to [MediaBrowserCompat.ConnectionCallback], we catch the disconnect here and
         * send it on to the other callback.
         */
        /**
         * 通常，如果[MediaBrowserServiceCompat]断开连接，则回调来自[MediaControllerCompat.Callback]（此处）。
         * 但由于其他连接状态事件被发送到[MediaBrowserCompat.ConnectionCallback]，我们在这里捕获断开连接并将其发送到另一个回调。
         */
        override fun onSessionDestroyed() {
            mediaBrowserConnectionCallback.onConnectionSuspended()
        }
    }

    //单例
    companion object {
        // For Singleton instantiation.
        @Volatile
        private var instance: MediaSessionConnection? = null

        fun getInstance(context: Context, serviceComponent: ComponentName) =
                instance ?: synchronized(this) {
                    instance ?: MediaSessionConnection(context, serviceComponent)
                            .also { instance = it }
                }
    }
}

@Suppress("PropertyName")
val EMPTY_PLAYBACK_STATE: PlaybackStateCompat = PlaybackStateCompat.Builder()
        .setState(PlaybackStateCompat.STATE_NONE, 0, 0f)
        .build()

@Suppress("PropertyName")
val NOTHING_PLAYING: MediaMetadataCompat = MediaMetadataCompat.Builder()
        .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, "")
        .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, 0)
        .build()

