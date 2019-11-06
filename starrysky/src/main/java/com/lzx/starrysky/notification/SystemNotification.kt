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
import android.os.RemoteException
import android.support.v4.app.NotificationCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.text.TextUtils
import com.lzx.starrysky.MusicService
import com.lzx.starrysky.R
import com.lzx.starrysky.StarrySky
import com.lzx.starrysky.ext.albumArt
import com.lzx.starrysky.ext.albumArtUrl
import com.lzx.starrysky.ext.artist
import com.lzx.starrysky.ext.id
import com.lzx.starrysky.imageloader.ImageLoaderCallBack
import com.lzx.starrysky.notification.utils.NotificationUtils

class SystemNotification constructor(service: MusicService, config: NotificationConfig?) :
    BroadcastReceiver(), INotification {


    private var mPlayIntent: PendingIntent? = null
    private var mPauseIntent: PendingIntent? = null
    private var mStopIntent: PendingIntent? = null
    private var mNextIntent: PendingIntent? = null
    private var mPreviousIntent: PendingIntent? = null

    private val mService: MusicService = service
    private var mSessionToken: MediaSessionCompat.Token? = null
    private var mController: MediaControllerCompat? = null
    private var mTransportControls: MediaControllerCompat.TransportControls? = null
    private var mPlaybackState: PlaybackStateCompat? = null
    private var mMetadata: MediaMetadataCompat? = null

    private val mNotificationManager: NotificationManager?
    private val packageName: String
    private var mStarted = false
    private var mConfig: NotificationConfig? = null
    private var lastClickTime: Long = 0

    init {
        mConfig = config
        if (mConfig == null) {
            mConfig = NotificationConfig()
        }
        try {
            updateSessionToken()
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
        mNotificationManager =
            mService.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
        packageName = mService.applicationContext.packageName

        setStopIntent(mConfig?.stopIntent)
        setNextPendingIntent(mConfig?.nextIntent)
        setPrePendingIntent(mConfig?.preIntent)
        setPlayPendingIntent(mConfig?.playIntent)
        setPausePendingIntent(mConfig?.pauseIntent)

        mNotificationManager.cancelAll()
    }

    @Throws(RemoteException::class)
    private fun updateSessionToken() {
        val freshToken = mService.sessionToken
        if (mSessionToken == null && freshToken != null || mSessionToken != null && mSessionToken != freshToken) {
            mController?.unregisterCallback(mCb)
            mSessionToken = freshToken
            if (mSessionToken != null) {
                mController = MediaControllerCompat(mService, mSessionToken!!)
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
            if (state!!.state == PlaybackStateCompat.STATE_STOPPED || state.state == PlaybackStateCompat.STATE_NONE) {
                stopNotification()
            } else {
                val notification = createNotification()
                if (notification != null && state.state != PlaybackStateCompat.STATE_BUFFERING) {
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

    override fun startNotification() {
        if (!mStarted) {
            mMetadata = mController?.metadata
            mPlaybackState = mController?.playbackState

            // The notification must be updated after setting started to true
            val notification = createNotification()
            if (notification != null) {
                mController?.registerCallback(mCb)
                val filter = IntentFilter()
                filter.addAction(INotification.ACTION_NEXT)
                filter.addAction(INotification.ACTION_PAUSE)
                filter.addAction(INotification.ACTION_PLAY)
                filter.addAction(INotification.ACTION_PREV)

                mService.registerReceiver(this, filter)

                mService.startForeground(INotification.NOTIFICATION_ID, notification)
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
                mService.unregisterReceiver(this)
            } catch (ex: IllegalArgumentException) {
                ex.printStackTrace()
            }

            mService.stopForeground(true)
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
                art = BitmapFactory.decodeResource(mService.resources,
                    R.drawable.default_art)
            }
        }

        //适配8.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationUtils.createNotificationChannel(mService, mNotificationManager!!)
        }

        val notificationBuilder = NotificationCompat.Builder(mService, INotification.CHANNEL_ID)

        val playPauseButtonPosition = addActions(notificationBuilder)

        val smallIcon = if (mConfig?.smallIconRes != -1)
            mConfig?.smallIconRes
        else
            R.drawable.ic_notification

        notificationBuilder
            .setStyle(android.support.v4.media.app.NotificationCompat.MediaStyle()
                // show only play/pause in compact view
                .setShowActionsInCompactView(playPauseButtonPosition)
                .setShowCancelButton(true)
                .setCancelButtonIntent(mStopIntent)
                .setMediaSession(mSessionToken))
            .setDeleteIntent(mStopIntent)
            //.setColor(mNotificationColor)
            .setColorized(true)
            .setSmallIcon(smallIcon!!)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .setContentTitle(description?.title) //歌名
            .setContentText(mMetadata?.artist) //艺术家
            .setLargeIcon(art)

        if (!mConfig?.targetClass.isNullOrEmpty()) {
            val clazz = NotificationUtils.getTargetClass(mConfig?.targetClass!!)
            if (clazz != null) {
                val songId = mMetadata?.id
                notificationBuilder.setContentIntent(NotificationUtils
                    .createContentIntent(mService, mConfig, songId, mConfig?.targetClassBundle, clazz))
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
            mService.stopForeground(true)
            return
        }
        builder.setOngoing(mPlaybackState?.state == PlaybackStateCompat.STATE_PLAYING)
    }

    /**
     * 封面加载
     */
    private fun fetchBitmapFromURLAsync(
        fetchArtUrl: String,
        notificationBuilder: NotificationCompat.Builder
    ) {
        val imageLoader = StarrySky.get().registry.imageLoader
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
                if (mConfig?.skipPreviousDrawableRes != -1)
                    mConfig?.skipPreviousDrawableRes ?: -1
                else
                    R.drawable.ic_skip_previous_white_24dp,
                if (!TextUtils.isEmpty(mConfig?.skipPreviousTitle))
                    mConfig?.skipPreviousTitle
                else
                    mService.getString(R.string.label_previous),
                mPreviousIntent)
            playPauseButtonPosition = 1
        }

        // 播放和暂停按钮
        val label: String
        val icon: Int
        val intent: PendingIntent?

        if (mPlaybackState?.state == PlaybackStateCompat.STATE_PLAYING) {
            label = if (!TextUtils.isEmpty(mConfig?.labelPlay))
                mConfig?.labelPlay ?: ""
            else
                mService.getString(R.string.label_pause)
            icon = if (mConfig?.pauseDrawableRes != -1)
                mConfig?.pauseDrawableRes ?: -1
            else
                R.drawable.ic_pause_white_24dp
            intent = mPauseIntent
        } else {
            label = if (!TextUtils.isEmpty(mConfig?.labelPause))
                mConfig?.labelPause ?: ""
            else
                mService.getString(R.string.label_play)
            icon = if (mConfig?.playDrawableRes != -1)
                mConfig?.playDrawableRes ?: -1
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
                if (mConfig?.skipNextDrawableRes != -1)
                    mConfig?.skipNextDrawableRes ?: -1
                else
                    R.drawable.ic_skip_next_white_24dp,
                if (!TextUtils.isEmpty(mConfig?.skipNextTitle))
                    mConfig?.skipNextTitle
                else
                    mService.getString(R.string.label_next),
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
        return PendingIntent
            .getBroadcast(mService, INotification.REQUEST_CODE, intent,
                PendingIntent.FLAG_CANCEL_CURRENT)
    }

    override fun updateFavoriteUI(isFavorite: Boolean) {
    }

    override fun updateLyricsUI(isChecked: Boolean) {
    }
}