package com.lzx.starrysky.notification

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import com.lzx.starrysky.R
import com.lzx.starrysky.SongInfo
import com.lzx.starrysky.StarrySky
import com.lzx.starrysky.manager.PlaybackStage
import com.lzx.starrysky.notification.INotification.Companion.ACTION_NEXT
import com.lzx.starrysky.notification.INotification.Companion.ACTION_PAUSE
import com.lzx.starrysky.notification.INotification.Companion.ACTION_PLAY
import com.lzx.starrysky.notification.INotification.Companion.ACTION_PREV
import com.lzx.starrysky.notification.INotification.Companion.ACTION_STOP
import com.lzx.starrysky.notification.imageloader.ImageLoaderCallBack
import com.lzx.starrysky.notification.utils.NotificationUtils
import com.lzx.starrysky.playback.Playback
import com.lzx.starrysky.service.MusicService
import com.lzx.starrysky.utils.getPendingIntent
import com.lzx.starrysky.utils.getTargetClass

class SystemNotification constructor(
    val context: Context,
    var config: NotificationConfig = NotificationConfig.Builder().build()
) : BroadcastReceiver(), INotification {


    private var mPlayIntent: PendingIntent? = null
    private var mPauseIntent: PendingIntent? = null
    private var mStopIntent: PendingIntent? = null
    private var mNextIntent: PendingIntent? = null
    private var mPreviousIntent: PendingIntent? = null

    private var playbackState: String = PlaybackStage.IDLE
    private var songInfo: SongInfo? = null

    private var mediaSession: MediaSessionCompat.Token? = null
    private val mNotificationManager: NotificationManager?
    private val packageName: String
    private var mStarted = false
    private var lastClickTime: Long = 0
    private var hasNextSong = false
    private var hasPreSong = false

    init {
        mNotificationManager = context.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
        packageName = context.applicationContext.packageName

        mStopIntent = config.stopIntent ?: ACTION_STOP.getPendingIntent()
        mNextIntent = config.nextIntent ?: ACTION_NEXT.getPendingIntent()
        mPreviousIntent = config.preIntent ?: ACTION_PREV.getPendingIntent()
        mPlayIntent = config.playIntent ?: ACTION_PLAY.getPendingIntent()
        mPauseIntent = config.pauseIntent ?: ACTION_PAUSE.getPendingIntent()

        mNotificationManager.cancelAll()
    }

    override fun onPlaybackStateChanged(songInfo: SongInfo?, playbackState: String,
                                        hasNextSong: Boolean, hasPreSong: Boolean) {
        this.hasNextSong = hasNextSong
        this.hasPreSong = hasPreSong
        this.playbackState = playbackState
        this.songInfo = songInfo
        if (playbackState == PlaybackStage.IDLE) {
            stopNotification()
        } else {
            val notification = createNotification()
            if (notification != null && playbackState != PlaybackStage.BUFFERING) {
                mNotificationManager?.notify(INotification.NOTIFICATION_ID, notification)
            }
        }
    }

    override fun setSessionToken(mediaSession: MediaSessionCompat.Token?) {
        this.mediaSession = mediaSession
    }

    private fun pauseMusic(player: Playback?) {
        if (player?.isPlaying() == true) {
            player.pause()
        }
    }

    private fun restoreMusic(player: Playback?) {
        player?.getCurrPlayInfo()?.let {
            player.play(it, true)
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action ?: return
        val nowTime = System.currentTimeMillis()
        if (nowTime - lastClickTime <= INotification.TIME_INTERVAL) {
            return
        }
        val player = (context as MusicService).binder?.player
        when (action) {
            ACTION_PAUSE -> pauseMusic(player)
            ACTION_PLAY -> restoreMusic(player)
            ACTION_NEXT -> player?.skipToNext()
            ACTION_PREV -> player?.skipToPrevious()
        }
        lastClickTime = nowTime
    }

    override fun startNotification(songInfo: SongInfo?, playbackState: String) {
        this.playbackState = playbackState
        if (this.songInfo?.songId != songInfo?.songId) {
            this.songInfo = songInfo
            createNotification()
        }
        if (!mStarted) {
            // The notification must be updated after setting started to true
            val notification = createNotification()
            if (notification != null) {
                val filter = IntentFilter()
                filter.addAction(ACTION_NEXT)
                filter.addAction(ACTION_PAUSE)
                filter.addAction(ACTION_PLAY)
                filter.addAction(ACTION_PREV)
                context.registerReceiver(this, filter)
                (context as MusicService).customStartForeground(INotification.NOTIFICATION_ID, notification)
                mStarted = true
            }
        }
    }

    override fun stopNotification() {
        if (mStarted) {
            mStarted = false
            try {
                mNotificationManager?.cancel(INotification.NOTIFICATION_ID)
                context.unregisterReceiver(this)
            } catch (ex: IllegalArgumentException) {
                ex.printStackTrace()
            }
            (context as MusicService).stopForeground(true)
        }
    }

    private fun createNotification(): Notification? {
        if (songInfo == null) {
            return null
        }
        var art: Bitmap? = songInfo?.coverBitmap

        var fetchArtUrl: String? = null
        if (art == null) {
            fetchArtUrl = songInfo?.songCover
            if (fetchArtUrl.isNullOrEmpty()) {
                art = BitmapFactory.decodeResource(context.resources, R.drawable.default_art)
            }
        }
        //适配8.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationUtils.createNotificationChannel(context, mNotificationManager!!)
        }
        val notificationBuilder = NotificationCompat.Builder(context, INotification.CHANNEL_ID)

        val playPauseButtonPosition = addActions(notificationBuilder)

        val smallIcon = if (config.smallIconRes != -1) config.smallIconRes else
            R.drawable.ic_notification

        notificationBuilder
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                // show only play/pause in compact view
                .setShowActionsInCompactView(playPauseButtonPosition)
                .setShowCancelButton(true)
                .setCancelButtonIntent(mStopIntent)
                .setMediaSession(mediaSession)
            )
            .setDeleteIntent(mStopIntent)
            .setColorized(true)
            .setSmallIcon(smallIcon)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .setContentTitle(songInfo?.songName) //歌名
            .setContentText(songInfo?.artist) //艺术家
            .setLargeIcon(art)
        if (!config.targetClass.isNullOrEmpty()) {
            val clazz = config.targetClass.getTargetClass()
            clazz?.let {
                val intent = NotificationUtils.createContentIntent(context, config, songInfo, config.targetClassBundle, it)
                notificationBuilder.setContentIntent(intent)
            }
        }
        setNotificationPlaybackState(notificationBuilder)

        if (!fetchArtUrl.isNullOrEmpty()) {
            fetchBitmapFromURLAsync(fetchArtUrl, notificationBuilder)
        }
        return notificationBuilder.build()
    }

    private fun setNotificationPlaybackState(builder: NotificationCompat.Builder) {
        if (!mStarted) {
            (context as MusicService).stopForeground(true)
            return
        }
        builder.setOngoing(playbackState == PlaybackStage.PLAYING)
    }

    /**
     * 封面加载
     */
    private fun fetchBitmapFromURLAsync(
        fetchArtUrl: String,
        notificationBuilder: NotificationCompat.Builder
    ) {
        StarrySky.getImageLoader()?.load(fetchArtUrl, object : ImageLoaderCallBack {
            override fun onBitmapLoaded(bitmap: Bitmap?) {
                if (bitmap == null) {
                    return
                }
                notificationBuilder.setLargeIcon(bitmap)
                mNotificationManager?.notify(INotification.NOTIFICATION_ID,
                    notificationBuilder.build())
            }

            override fun onBitmapFailed(errorDrawable: Drawable?) {
            }
        })
    }

    /**
     * 添加上一首，下一首，播放，暂停按钮
     */
    private fun addActions(notificationBuilder: NotificationCompat.Builder): Int {
        var playPauseButtonPosition = 0

        // 如果有上一首
        if (hasPreSong) {
            val icon = if (config.skipPreviousDrawableRes != -1) config.skipPreviousDrawableRes else R.drawable.ic_skip_previous_white_24dp
            val title = if (config.skipPreviousTitle.isNotEmpty()) config.skipPreviousTitle else context.getString(R.string.label_previous)
            notificationBuilder.addAction(icon, title, mPreviousIntent)
            playPauseButtonPosition = 1
        }

        // 播放和暂停按钮
        val label: String
        val icon: Int
        val intent: PendingIntent?

        if (playbackState == PlaybackStage.PLAYING || playbackState == PlaybackStage.BUFFERING) {
            label = if (config.labelPlay.isNotEmpty()) config.labelPlay else context.getString(R.string.label_pause)
            icon = if (config.pauseDrawableRes != -1) config.pauseDrawableRes else R.drawable.ic_pause_white_24dp
            intent = mPauseIntent
        } else {
            label = if (config.labelPause.isNotEmpty()) config.labelPause else context.getString(R.string.label_play)
            icon = if (config.playDrawableRes != -1) config.playDrawableRes else R.drawable.ic_play_arrow_white_24dp
            intent = mPlayIntent
        }

        notificationBuilder.addAction(NotificationCompat.Action(icon, label, intent))

        // 如果有下一首
        if (hasNextSong) {
            val actionIcon = if (config.skipNextDrawableRes != -1) config.skipNextDrawableRes else R.drawable.ic_skip_next_white_24dp
            val title = if (config.skipNextTitle.isNotEmpty()) config.skipNextTitle else context.getString(R.string.label_next)
            notificationBuilder.addAction(actionIcon, title, mNextIntent)
        }

        return playPauseButtonPosition
    }

    override fun onCommand(command: String?, extras: Bundle?) {}

    private fun String.getPendingIntent(): PendingIntent {
        return context.getPendingIntent(INotification.REQUEST_CODE, this)
    }
}