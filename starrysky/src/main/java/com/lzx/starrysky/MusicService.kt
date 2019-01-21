/*
 * Copyright 2017 Google Inc. All rights reserved.
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

import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationManagerCompat
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem
import androidx.media.MediaBrowserServiceCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.lzx.starrysky.extensions.flag
import com.lzx.starrysky.library.BrowseTree
import com.lzx.starrysky.library.MusicSource
import com.lzx.starrysky.library.UAMP_BROWSABLE_ROOT
import com.lzx.starrysky.library.UAMP_EMPTY_ROOT
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Timeline
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.lzx.starrysky.model.MusicProvider


//媒体浏览器服务
class MusicService : androidx.media.MediaBrowserServiceCompat() {
    private lateinit var mediaSession: MediaSessionCompat //媒体会画
    private lateinit var mediaController: MediaControllerCompat //媒体控制器
    private lateinit var becomingNoisyReceiver: BecomingNoisyReceiver //线控相关广播
    private lateinit var notificationManager: NotificationManagerCompat //通知栏管理
    private lateinit var notificationBuilder: NotificationBuilder
    private lateinit var mediaSource: MusicProvider //
    private lateinit var mediaSessionConnector: MediaSessionConnector
    private lateinit var packageValidator: PackageValidator


    private val browseTree: BrowseTree by lazy {
        BrowseTree(mediaSource)
    }

    private var isForegroundService = false

    private val remoteJsonSource: Uri =
            Uri.parse("https://storage.googleapis.com/uamp/catalog.json")

    private val uAmpAudioAttributes = AudioAttributes.Builder()
            .setContentType(C.CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()


    private val exoPlayer: ExoPlayer by lazy {
        ExoPlayerFactory.newSimpleInstance(this).apply {
            setAudioAttributes(uAmpAudioAttributes, true)
        }
    }

    override fun onCreate() {
        super.onCreate()

        val sessionIntent = packageManager?.getLaunchIntentForPackage(packageName)
        val sessionActivityPendingIntent = PendingIntent.getActivity(this, 0, sessionIntent, 0)

        mediaSession = MediaSessionCompat(this, "MusicService")
                .apply {
                    setSessionActivity(sessionActivityPendingIntent)
                    isActive = true
                }

        sessionToken = mediaSession.sessionToken

        mediaController = MediaControllerCompat(this, mediaSession).also {
            it.registerCallback(MediaControllerCallback())
        }

        notificationBuilder = NotificationBuilder(this)
        notificationManager = NotificationManagerCompat.from(this)

        becomingNoisyReceiver = BecomingNoisyReceiver(context = this, sessionToken = mediaSession.sessionToken)

        mediaSource = MusicProvider(context = this)

        mediaSessionConnector = MediaSessionConnector(mediaSession).also {
            val dataSourceFactory = DefaultDataSourceFactory(
                    this, Util.getUserAgent(this, UAMP_USER_AGENT), null)

            val playbackPreparer = UampPlaybackPreparer(
                    mediaSource,
                    exoPlayer,
                    dataSourceFactory)

            it.setPlayer(exoPlayer, playbackPreparer)
            it.setQueueNavigator(UampQueueNavigator(mediaSession))
        }

        packageValidator = PackageValidator(this, R.xml.allowed_media_browser_callers)
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        super.onTaskRemoved(rootIntent)
        exoPlayer.stop(true)
    }

    override fun onDestroy() {
        mediaSession.run {
            isActive = false
            release()
        }
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot? {
        return if (packageValidator.isKnownCaller(clientPackageName, clientUid)) {
            BrowserRoot(UAMP_BROWSABLE_ROOT, null)
        } else {
            BrowserRoot(UAMP_EMPTY_ROOT, null)
        }
    }

    /**
     * 获取数据
     */
    override fun onLoadChildren(parentMediaId: String, result: androidx.media.MediaBrowserServiceCompat.Result<List<MediaItem>>) {
        mediaSource.loadSongInfoAnsy()

        val resultsSent = mediaSource.whenReady { successfullyInitialized ->
            if (successfullyInitialized) {
                val children = browseTree[parentMediaId]?.map { item ->
                    MediaItem(item.description, item.flag)
                }
                result.sendResult(children) //返回结果
            } else {
                result.sendError(null)
            }
        }
        if (!resultsSent) {
            result.detach()
        }
    }

    private fun removeNowPlayingNotification() {
        stopForeground(true)
    }

    private inner class MediaControllerCallback : MediaControllerCompat.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            mediaController.playbackState?.let { updateNotification(it) }
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            state?.let { updateNotification(it) }
        }

        private fun updateNotification(state: PlaybackStateCompat) {
            val updatedState = state.state
            if (mediaController.metadata == null) {
                return
            }

            // Skip building a notification when state is "none".
            val notification = if (updatedState != PlaybackStateCompat.STATE_NONE) {
                notificationBuilder.buildNotification(mediaSession.sessionToken)
            } else {
                null
            }

            when (updatedState) {
                PlaybackStateCompat.STATE_BUFFERING,
                PlaybackStateCompat.STATE_PLAYING -> {
                    becomingNoisyReceiver.register()

                    if (!isForegroundService) {
                        startService(Intent(applicationContext, this@MusicService.javaClass))
                        startForeground(NOW_PLAYING_NOTIFICATION, notification)
                        isForegroundService = true
                    } else if (notification != null) {
                        notificationManager.notify(NOW_PLAYING_NOTIFICATION, notification)
                    }
                }
                else -> {
                    becomingNoisyReceiver.unregister()

                    if (isForegroundService) {
                        stopForeground(false)
                        isForegroundService = false

                        // If playback has ended, also stop the service.
                        if (updatedState == PlaybackStateCompat.STATE_NONE) {
                            stopSelf()
                        }

                        if (notification != null) {
                            notificationManager.notify(NOW_PLAYING_NOTIFICATION, notification)
                        } else {
                            removeNowPlayingNotification()
                        }
                    }
                }
            }
        }
    }
}

private class UampQueueNavigator(
        mediaSession: MediaSessionCompat
) : TimelineQueueNavigator(mediaSession) {
    private val window = Timeline.Window()
    override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat =
            player.currentTimeline
                    .getWindow(windowIndex, window, true).tag as MediaDescriptionCompat
}


private class BecomingNoisyReceiver(private val context: Context, sessionToken: MediaSessionCompat.Token) : BroadcastReceiver() {

    private val noisyIntentFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
    private val controller = MediaControllerCompat(context, sessionToken)

    private var registered = false

    fun register() {
        if (!registered) {
            context.registerReceiver(this, noisyIntentFilter)
            registered = true
        }
    }

    fun unregister() {
        if (registered) {
            context.unregisterReceiver(this)
            registered = false
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
            controller.transportControls.pause()
        }
    }
}

private const val UAMP_USER_AGENT = "uamp.next"