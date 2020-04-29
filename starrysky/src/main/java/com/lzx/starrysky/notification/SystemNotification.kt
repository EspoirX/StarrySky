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
import android.os.RemoteException
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.text.TextUtils
import androidx.core.app.NotificationCompat
import com.lzx.starrysky.MusicService
import com.lzx.starrysky.R
import com.lzx.starrysky.StarrySky
import com.lzx.starrysky.ext.albumArt
import com.lzx.starrysky.ext.albumArtUrl
import com.lzx.starrysky.ext.artist
import com.lzx.starrysky.ext.id
import com.lzx.starrysky.imageloader.ImageLoaderCallBack
import com.lzx.starrysky.notification.utils.NotificationUtils
import com.lzx.starrysky.playback.player.Playback
import com.lzx.starrysky.provider.SongInfo
import com.lzx.starrysky.utils.StarrySkyUtils

class SystemNotification constructor(
    val context: Context,
    var config: NotificationConfig = NotificationConfig()
) : BroadcastReceiver(), INotification {


    private var mPlayIntent: PendingIntent? = null
    private var mPauseIntent: PendingIntent? = null
    private var mStopIntent: PendingIntent? = null
    private var mNextIntent: PendingIntent? = null
    private var mPreviousIntent: PendingIntent? = null

    private var mSessionToken: MediaSessionCompat.Token? = null
    private var mController: MediaControllerCompat? = null
    private var mTransportControls: MediaControllerCompat.TransportControls? = null
    private var mPlaybackState: PlaybackStateCompat? = null
    private var mMetadata: MediaMetadataCompat? = null

    private val mNotificationManager: NotificationManager?
    private val packageName: String
    private var mStarted = false
    private var lastClickTime: Long = 0

    init {
        try {
            updateSessionToken()
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
        mNotificationManager =
            context.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
        packageName = context.applicationContext.packageName

        setStopIntent(config.stopIntent)
        setNextPendingIntent(config.nextIntent)
        setPrePendingIntent(config.preIntent)
        setPlayPendingIntent(config.playIntent)
        setPausePendingIntent(config.pauseIntent)

        mNotificationManager.cancelAll()
    }

    @Throws(RemoteException::class)
    private fun updateSessionToken() {
        val freshToken = (context as MusicService).sessionToken
        if (mSessionToken == null && freshToken != null || mSessionToken != null && mSessionToken != freshToken) {
            mController?.unregisterCallback(mCb)
            mSessionToken = freshToken
            if (mSessionToken != null) {
                mController = MediaControllerCompat(context, mSessionToken!!)
                mTransportControls = mController?.transportControls
                if (mStarted) {
                    mController?.registerCallback(mCb)
                }
            }
        }
    }

    /**
     * 通知栏点击监听
     */
    private val mCb = object : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            super.onPlaybackStateChanged(state)
            mPlaybackState = state
            if (state?.state == Playback.STATE_STOPPED || state?.state == Playback.STATE_NONE) {
                stopNotification()
            } else {
                val notification = createNotification()
                if (notification != null && state?.state != Playback.STATE_BUFFERING) {
                    mNotificationManager?.notify(INotification.NOTIFICATION_ID, notification)
                }
            }
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            super.onMetadataChanged(metadata)
            mMetadata = metadata
        }

        override fun onSessionDestroyed() {
            super.onSessionDestroyed()
            try {
                updateSessionToken()
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action ?: return
        val nowTime = System.currentTimeMillis()
        if (nowTime - lastClickTime <= INotification.TIME_INTERVAL) {
            return
        }
        when (action) {
            INotification.ACTION_PAUSE -> mTransportControls?.pause()
            INotification.ACTION_PLAY -> mTransportControls?.play()
            INotification.ACTION_NEXT -> mTransportControls?.skipToNext()
            INotification.ACTION_PREV -> mTransportControls?.skipToPrevious()
            else -> {
            }
        }
        lastClickTime = nowTime
    }

    override fun startNotification(songInfo: SongInfo?, playbackState: PlaybackStateCompat?) {
        mPlaybackState = mController?.playbackState
        if (mPlaybackState?.state != playbackState?.state) {
            mPlaybackState = playbackState
        }
        if (mMetadata?.id != songInfo?.songId) {
            mMetadata = songInfo?.let { StarrySkyUtils.toMediaMetadata(it) }
            createNotification()
        } else {
            mMetadata = mController?.metadata
        }
        if (!mStarted) {
            // The notification must be updated after setting started to true
            val notification = createNotification()
            if (notification != null) {
                mController?.registerCallback(mCb)
                val filter = IntentFilter()
                filter.addAction(INotification.ACTION_NEXT)
                filter.addAction(INotification.ACTION_PAUSE)
                filter.addAction(INotification.ACTION_PLAY)
                filter.addAction(INotification.ACTION_PREV)

                context.registerReceiver(this, filter)

                (context as MusicService).startForeground(INotification.NOTIFICATION_ID, notification)
                mStarted = true
            }
        }
    }

    override fun stopNotification() {
        if (mStarted) {
            mStarted = false
            mController?.unregisterCallback(mCb)
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
        if (mMetadata == null || mPlaybackState == null) {
            return null
        }
        val description = mMetadata?.description

        var art: Bitmap? = mMetadata?.albumArt

        var fetchArtUrl: String? = null
        if (art == null) {
            fetchArtUrl = mMetadata?.albumArtUrl
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

        val smallIcon = if (config.smallIconRes != -1)
            config.smallIconRes
        else
            R.drawable.ic_notification

        notificationBuilder
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                // show only play/pause in compact view
                .setShowActionsInCompactView(playPauseButtonPosition)
                .setShowCancelButton(true)
                .setCancelButtonIntent(mStopIntent)
                .setMediaSession(mSessionToken))
            .setDeleteIntent(mStopIntent)
            .setColorized(true)
            .setSmallIcon(smallIcon)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .setContentTitle(description?.title) //歌名
            .setContentText(mMetadata?.artist) //艺术家
            .setLargeIcon(art)
        if (!config.targetClass.isNullOrEmpty()) {
            val clazz = NotificationUtils.getTargetClass(config.targetClass!!)
            if (clazz != null) {
                val songId = mMetadata?.id
                notificationBuilder.setContentIntent(NotificationUtils
                    .createContentIntent(context, config, songId, config.targetClassBundle, clazz))
            }
        }
        setNotificationPlaybackState(notificationBuilder)

        if (!fetchArtUrl.isNullOrEmpty()) {
            fetchBitmapFromURLAsync(fetchArtUrl, notificationBuilder)
        }
        return notificationBuilder.build()
    }

    private fun setNotificationPlaybackState(builder: NotificationCompat.Builder) {
        if (mPlaybackState == null || !mStarted) {
            (context as MusicService).stopForeground(true)
            return
        }
        builder.setOngoing(mPlaybackState?.state == Playback.STATE_PLAYING)
    }

    /**
     * 封面加载
     */
    private fun fetchBitmapFromURLAsync(
        fetchArtUrl: String,
        notificationBuilder: NotificationCompat.Builder
    ) {
        val imageLoader = StarrySky.get().imageLoader()
        imageLoader.load(fetchArtUrl, object : ImageLoaderCallBack {
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
        val hasPrevious = if (mPlaybackState == null) false else mPlaybackState!!.actions and
            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS != 0L
        if (hasPrevious) {
            notificationBuilder.addAction(
                if (config.skipPreviousDrawableRes != -1)
                    config.skipPreviousDrawableRes ?: -1
                else
                    R.drawable.ic_skip_previous_white_24dp,
                if (!TextUtils.isEmpty(config.skipPreviousTitle))
                    config.skipPreviousTitle
                else
                    context.getString(R.string.label_previous),
                mPreviousIntent)
            playPauseButtonPosition = 1
        }

        // 播放和暂停按钮
        val label: String
        val icon: Int
        val intent: PendingIntent?

        if (mPlaybackState?.state == Playback.STATE_PLAYING) {
            label = if (!TextUtils.isEmpty(config.labelPlay))
                config.labelPlay ?: ""
            else
                context.getString(R.string.label_pause)
            icon = if (config.pauseDrawableRes != -1)
                config.pauseDrawableRes ?: -1
            else
                R.drawable.ic_pause_white_24dp
            intent = mPauseIntent
        } else {
            label = if (!TextUtils.isEmpty(config.labelPause))
                config.labelPause ?: ""
            else
                context.getString(R.string.label_play)
            icon = if (config.playDrawableRes != -1)
                config.playDrawableRes ?: -1
            else
                R.drawable.ic_play_arrow_white_24dp
            intent = mPlayIntent
        }

        notificationBuilder.addAction(NotificationCompat.Action(icon, label, intent))

        // 如果有下一首
        val hasNext = if (mPlaybackState == null) false else mPlaybackState!!.actions and
            PlaybackStateCompat.ACTION_SKIP_TO_NEXT != 0L
        if (hasNext) {
            notificationBuilder.addAction(
                if (config.skipNextDrawableRes != -1)
                    config.skipNextDrawableRes ?: -1
                else
                    R.drawable.ic_skip_next_white_24dp,
                if (!TextUtils.isEmpty(config.skipNextTitle))
                    config.skipNextTitle
                else
                    context.getString(R.string.label_next),
                mNextIntent)
        }

        return playPauseButtonPosition
    }

    private fun setStopIntent(pendingIntent: PendingIntent?) {
        mStopIntent = pendingIntent ?: getPendingIntent(INotification.ACTION_STOP)
    }

    private fun setNextPendingIntent(pendingIntent: PendingIntent?) {
        mNextIntent = pendingIntent ?: getPendingIntent(INotification.ACTION_NEXT)
    }

    private fun setPrePendingIntent(pendingIntent: PendingIntent?) {
        mPreviousIntent = pendingIntent ?: getPendingIntent(INotification.ACTION_PREV)
    }

    private fun setPlayPendingIntent(pendingIntent: PendingIntent?) {
        mPlayIntent = pendingIntent ?: getPendingIntent(INotification.ACTION_PLAY)
    }

    private fun setPausePendingIntent(pendingIntent: PendingIntent?) {
        mPauseIntent = pendingIntent ?: getPendingIntent(INotification.ACTION_PAUSE)
    }

    private fun getPendingIntent(action: String): PendingIntent {
        val intent = Intent(action)
        intent.setPackage(packageName)
        return PendingIntent.getBroadcast(context, INotification.REQUEST_CODE, intent,
            PendingIntent.FLAG_CANCEL_CURRENT)
    }

    override fun onCommand(command: String?, extras: Bundle?) {}
}