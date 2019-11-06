package com.lzx.starrysky.notification

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Resources
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
import android.widget.RemoteViews
import com.lzx.starrysky.MusicService
import com.lzx.starrysky.R
import com.lzx.starrysky.StarrySky
import com.lzx.starrysky.ext.albumArt
import com.lzx.starrysky.ext.albumArtUrl
import com.lzx.starrysky.ext.id
import com.lzx.starrysky.imageloader.ImageLoaderCallBack
import com.lzx.starrysky.notification.utils.NotificationColorUtils
import com.lzx.starrysky.notification.utils.NotificationUtils
import com.lzx.starrysky.provider.SongInfo

class CustomNotification constructor(service: MusicService, config: NotificationConfig?) :
    BroadcastReceiver(), INotification {


    private var mRemoteView: RemoteViews? = null
    private var mBigRemoteView: RemoteViews? = null

    private var mPlayOrPauseIntent: PendingIntent? = null
    private var mPlayIntent: PendingIntent? = null
    private var mPauseIntent: PendingIntent? = null
    private var mStopIntent: PendingIntent? = null
    private var mNextIntent: PendingIntent? = null
    private var mPreviousIntent: PendingIntent? = null
    private var mFavoriteIntent: PendingIntent? = null
    private var mLyricsIntent: PendingIntent? = null
    private var mDownloadIntent: PendingIntent? = null
    private var mCloseIntent: PendingIntent? = null

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
    private var mNotification: Notification? = null

    private val res: Resources
    private val mColorUtils: NotificationColorUtils

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
        res = mService.applicationContext.resources
        mColorUtils = NotificationColorUtils()
        setStopIntent(mConfig?.stopIntent)
        setNextPendingIntent(mConfig?.nextIntent)
        setPrePendingIntent(mConfig?.preIntent)
        setPlayPendingIntent(mConfig?.playIntent)
        setPausePendingIntent(mConfig?.pauseIntent)
        setFavoritePendingIntent(mConfig?.favoriteIntent)
        setLyricsPendingIntent(mConfig?.lyricsIntent)
        setDownloadPendingIntent(mConfig?.downloadIntent)
        setClosePendingIntent(mConfig?.closeIntent)
        setPlayOrPauseIntent(mConfig?.playOrPauseIntent)

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
            INotification.ACTION_PLAY_OR_PAUSE -> if (mPlaybackState?.state ==
                PlaybackStateCompat.STATE_PLAYING) {
                mTransportControls?.pause()
            } else {
                mTransportControls?.play()
            }
            INotification.ACTION_NEXT -> mTransportControls?.skipToNext()
            INotification.ACTION_PREV -> mTransportControls?.skipToPrevious()
            INotification.ACTION_CLOSE -> stopNotification()
            else -> {
            }
        }
        lastClickTime = nowTime
    }

    private fun createNotification(): Notification? {
        if (mMetadata == null || mPlaybackState == null) {
            return null
        }
        val description = mMetadata?.description

        val songId = mMetadata?.id
        val smallIcon = if (mConfig?.smallIconRes != -1)
            mConfig?.smallIconRes
        else
            R.drawable.ic_notification
        //适配8.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationUtils.createNotificationChannel(mService, mNotificationManager!!)
        }
        val notificationBuilder =
            NotificationCompat.Builder(mService, INotification.CHANNEL_ID)
        notificationBuilder
            .setOnlyAlertOnce(true)
            .setSmallIcon(smallIcon!!)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentTitle(description?.title) //歌名
            .setContentText(description?.subtitle) //艺术家
        //setContentIntent
        if (!mConfig?.targetClass.isNullOrEmpty()) {
            val clazz = NotificationUtils.getTargetClass(mConfig?.targetClass!!)
            if (clazz != null) {
                notificationBuilder.setContentIntent(NotificationUtils
                    .createContentIntent(mService, mConfig, songId, mConfig?.targetClassBundle, clazz))
            }
        }
        //这里不能复用，会导致内存泄漏，需要每次都创建
        mRemoteView = createRemoteViews(false)
        mBigRemoteView = createRemoteViews(true)

        //setCustomContentView and setCustomBigContentView
        if (Build.VERSION.SDK_INT >= 24) {
            notificationBuilder.setCustomContentView(mRemoteView)
            notificationBuilder.setCustomBigContentView(mBigRemoteView)
        }

        setNotificationPlaybackState(notificationBuilder)

        //create Notification
        mNotification = notificationBuilder.build()
        mNotification?.contentView = mRemoteView
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mNotification?.bigContentView = mBigRemoteView
        }
        val songInfos = StarrySky.get().mediaQueueProvider.getSongList().filter {
            it.songId == songId
        }
        val songInfo: SongInfo? = if (songInfos.isNotEmpty()) songInfos[0] else null
        updateRemoteViewUI(mNotification, songInfo, smallIcon)

        return mNotification
    }

    private fun setNotificationPlaybackState(builder: NotificationCompat.Builder) {
        if (mPlaybackState == null || !mStarted) {
            mService.stopForeground(true)
            return
        }
        builder.setOngoing(mPlaybackState?.state == PlaybackStateCompat.STATE_PLAYING)
    }

    /**
     * 创建RemoteViews
     */
    private fun createRemoteViews(isBigRemoteViews: Boolean): RemoteViews {
        val remoteView: RemoteViews
        if (isBigRemoteViews) {
            remoteView = RemoteViews(packageName, getResourceId(
                INotification.LAYOUT_NOTIFY_BIG_PLAY, "layout"))
        } else {
            remoteView = RemoteViews(packageName, getResourceId(
                INotification.LAYOUT_NOTIFY_PLAY, "layout"))
        }
        if (mPlayIntent != null) {
            remoteView
                .setOnClickPendingIntent(
                    getResourceId(INotification.ID_IMG_NOTIFY_PLAY, "id"),
                    mPlayIntent)
        }
        if (mPauseIntent != null) {
            remoteView.setOnClickPendingIntent(
                getResourceId(INotification.ID_IMG_NOTIFY_PAUSE, "id"),
                mPauseIntent)
        }
        if (mStopIntent != null) {
            remoteView
                .setOnClickPendingIntent(
                    getResourceId(INotification.ID_IMG_NOTIFY_STOP, "id"),
                    mStopIntent)
        }
        if (mFavoriteIntent != null) {
            remoteView.setOnClickPendingIntent(
                getResourceId(INotification.ID_IMG_NOTIFY_FAVORITE, "id"),
                mFavoriteIntent)
        }
        if (mLyricsIntent != null) {
            remoteView.setOnClickPendingIntent(
                getResourceId(INotification.ID_IMG_NOTIFY_LYRICS, "id"),
                mLyricsIntent)
        }
        if (mDownloadIntent != null) {
            remoteView.setOnClickPendingIntent(
                getResourceId(INotification.ID_IMG_NOTIFY_DOWNLOAD, "id"),
                mDownloadIntent)
        }
        if (mNextIntent != null) {
            remoteView
                .setOnClickPendingIntent(
                    getResourceId(INotification.ID_IMG_NOTIFY_NEXT, "id"),
                    mNextIntent)
        }
        if (mPreviousIntent != null) {
            remoteView
                .setOnClickPendingIntent(
                    getResourceId(INotification.ID_IMG_NOTIFY_PRE, "id"),
                    mPreviousIntent)
        }
        if (mCloseIntent != null) {
            remoteView.setOnClickPendingIntent(
                getResourceId(INotification.ID_IMG_NOTIFY_CLOSE, "id"),
                mCloseIntent)
        }
        if (mPlayOrPauseIntent != null) {
            remoteView.setOnClickPendingIntent(getResourceId(
                INotification.ID_IMG_NOTIFY_PLAY_OR_PAUSE, "id"),
                mPlayOrPauseIntent)
        }
        return remoteView
    }

    /**
     * 更新RemoteViews
     */
    private fun updateRemoteViewUI(
        notification: Notification?, songInfo: SongInfo?, smallIcon: Int
    ) {
        val isDark = mColorUtils.isDarkNotificationBar(mService, notification)
        var art: Bitmap? = mMetadata?.albumArt
        val artistName = songInfo?.artist ?: ""
        val songName = songInfo?.songName ?: ""
        //设置文字内容
        mRemoteView?.setTextViewText(
            getResourceId(INotification.ID_TXT_NOTIFY_SONGNAME, "id"),
            songName)
        mRemoteView?.setTextViewText(
            getResourceId(INotification.ID_TXT_NOTIFY_ARTISTNAME, "id"),
            artistName)
        //设置播放暂停按钮
        if (mPlaybackState?.state == PlaybackStateCompat.STATE_PLAYING) {
            mRemoteView?.setImageViewResource(getResourceId(
                INotification.ID_IMG_NOTIFY_PLAY_OR_PAUSE, "id"),
                getResourceId(if (isDark)
                    INotification.DRAWABLE_NOTIFY_BTN_DARK_PAUSE_SELECTOR
                else
                    INotification.DRAWABLE_NOTIFY_BTN_LIGHT_PAUSE_SELECTOR,
                    "drawable"))
        } else {
            mRemoteView?.setImageViewResource(getResourceId(
                INotification.ID_IMG_NOTIFY_PLAY_OR_PAUSE, "id"),
                getResourceId(if (isDark)
                    INotification.DRAWABLE_NOTIFY_BTN_DARK_PLAY_SELECTOR
                else
                    INotification.DRAWABLE_NOTIFY_BTN_LIGHT_PLAY_SELECTOR,
                    "drawable"))
        }

        //大布局
        //设置文字内容
        mBigRemoteView?.setTextViewText(
            getResourceId(INotification.ID_TXT_NOTIFY_SONGNAME, "id"),
            songName)
        mBigRemoteView?.setTextViewText(
            getResourceId(INotification.ID_TXT_NOTIFY_ARTISTNAME, "id"),
            artistName)
        //设置播放暂停按钮
        if (mPlaybackState?.state == PlaybackStateCompat.STATE_PLAYING) {
            mBigRemoteView?.setImageViewResource(getResourceId(
                INotification.ID_IMG_NOTIFY_PLAY_OR_PAUSE, "id"),
                getResourceId(if (isDark)
                    INotification.DRAWABLE_NOTIFY_BTN_DARK_PAUSE_SELECTOR
                else
                    INotification.DRAWABLE_NOTIFY_BTN_LIGHT_PAUSE_SELECTOR,
                    "drawable"))
        } else {
            mBigRemoteView?.setImageViewResource(getResourceId(
                INotification.ID_IMG_NOTIFY_PLAY_OR_PAUSE, "id"),
                getResourceId(if (isDark)
                    INotification.DRAWABLE_NOTIFY_BTN_DARK_PLAY_SELECTOR
                else
                    INotification.DRAWABLE_NOTIFY_BTN_LIGHT_PLAY_SELECTOR,
                    "drawable"))
        }
        //设置喜欢或收藏按钮
        mBigRemoteView
            ?.setImageViewResource(
                getResourceId(INotification.ID_IMG_NOTIFY_FAVORITE, "id"),
                getResourceId(if (isDark)
                    INotification.DRAWABLE_NOTIFY_BTN_DARK_FAVORITE
                else
                    INotification.DRAWABLE_NOTIFY_BTN_LIGHT_FAVORITE, "drawable"))
        //设置歌词按钮
        mBigRemoteView
            ?.setImageViewResource(
                getResourceId(INotification.ID_IMG_NOTIFY_LYRICS, "id"),
                getResourceId(if (isDark)
                    INotification.DRAWABLE_NOTIFY_BTN_DARK_LYRICS
                else
                    INotification.DRAWABLE_NOTIFY_BTN_LIGHT_LYRICS, "drawable"))
        //设置下载按钮
        mBigRemoteView
            ?.setImageViewResource(
                getResourceId(INotification.ID_IMG_NOTIFY_DOWNLOAD, "id"),
                getResourceId(if (isDark)
                    INotification.DRAWABLE_NOTIFY_BTN_DARK_DOWNLOAD
                else
                    INotification.DRAWABLE_NOTIFY_BTN_LIGHT_DOWNLOAD, "drawable"))

        //上一首下一首按钮
        if (mPlaybackState != null) {
            val hasNextSong =
                mPlaybackState!!.actions and PlaybackStateCompat.ACTION_SKIP_TO_NEXT != 0L
            val hasPreSong =
                mPlaybackState!!.actions and PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS != 0L
            disableNextBtn(hasNextSong, isDark)
            disablePreviousBtn(hasPreSong, isDark)
        }

        //封面
        var fetchArtUrl: String? = null
        if (art == null) {
            fetchArtUrl = mMetadata?.albumArtUrl
            if (fetchArtUrl.isNullOrEmpty()) {
                art = BitmapFactory.decodeResource(mService.resources, R.drawable.default_art)
            }
        }
        mRemoteView?.setImageViewBitmap(
            getResourceId(INotification.ID_IMG_NOTIFY_ICON, "id"), art)
        mBigRemoteView
            ?.setImageViewBitmap(
                getResourceId(INotification.ID_IMG_NOTIFY_ICON, "id"), art)
        mNotificationManager?.notify(INotification.NOTIFICATION_ID, notification)

        if (!fetchArtUrl.isNullOrEmpty()) {
            fetchBitmapFromURLAsync(fetchArtUrl, notification)
        }
    }

    /**
     * 加载封面
     */
    private fun fetchBitmapFromURLAsync(fetchArtUrl: String, notification: Notification?) {
        val imageLoader = StarrySky.get().registry.imageLoader
        imageLoader.load(fetchArtUrl, object : ImageLoaderCallBack {
            override fun onBitmapLoaded(bitmap: Bitmap?) {
                if (bitmap == null) {
                    return
                }
                mRemoteView
                    ?.setImageViewBitmap(
                        getResourceId(INotification.ID_IMG_NOTIFY_ICON, "id"),
                        bitmap)
                mBigRemoteView
                    ?.setImageViewBitmap(
                        getResourceId(INotification.ID_IMG_NOTIFY_ICON, "id"),
                        bitmap)
                mNotificationManager?.notify(INotification.NOTIFICATION_ID, notification)
            }

            override fun onBitmapFailed(errorDrawable: Drawable?) {
            }
        })
    }

    /**
     * 下一首按钮样式
     */
    private fun disableNextBtn(disable: Boolean, isDark: Boolean) {
        val res: Int = if (disable) {
            this.getResourceId(if (isDark)
                INotification.DRAWABLE_NOTIFY_BTN_DARK_NEXT_PRESSED
            else
                INotification.DRAWABLE_NOTIFY_BTN_LIGHT_NEXT_PRESSED, "drawable")
        } else {
            this.getResourceId(if (isDark)
                INotification.DRAWABLE_NOTIFY_BTN_DARK_NEXT_SELECTOR
            else
                INotification.DRAWABLE_NOTIFY_BTN_LIGHT_NEXT_SELECTOR, "drawable")
        }
        mRemoteView
            ?.setImageViewResource(
                getResourceId(INotification.ID_IMG_NOTIFY_NEXT, "id"), res)
        mBigRemoteView
            ?.setImageViewResource(
                getResourceId(INotification.ID_IMG_NOTIFY_NEXT, "id"), res)
    }

    /**
     * 上一首按钮样式
     */
    private fun disablePreviousBtn(disable: Boolean, isDark: Boolean) {
        val res: Int = if (disable) {
            this.getResourceId(if (isDark)
                INotification.DRAWABLE_NOTIFY_BTN_DARK_PREV_PRESSED
            else
                INotification.DRAWABLE_NOTIFY_BTN_LIGHT_PREV_PRESSED, "drawable")
        } else {
            this.getResourceId(if (isDark)
                INotification.DRAWABLE_NOTIFY_BTN_DARK_PREV_SELECTOR
            else
                INotification.DRAWABLE_NOTIFY_BTN_LIGHT_PREV_SELECTOR, "drawable")
        }
        mRemoteView
            ?.setImageViewResource(
                getResourceId(INotification.ID_IMG_NOTIFY_PRE, "id"), res)
        mBigRemoteView
            ?.setImageViewResource(
                getResourceId(INotification.ID_IMG_NOTIFY_PRE, "id"),
                res)
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
                filter.addAction(INotification.ACTION_PLAY_OR_PAUSE)
                filter.addAction(INotification.ACTION_CLOSE)

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

    /**
     * 更新喜欢或收藏按钮样式
     */
    override fun updateFavoriteUI(isFavorite: Boolean) {
        if (mNotification == null) {
            return
        }
        val isDark = mColorUtils.isDarkNotificationBar(mService, mNotification!!)
        //喜欢或收藏按钮选中时样式
        if (isFavorite) {
            mBigRemoteView?.setImageViewResource(
                getResourceId(INotification.ID_IMG_NOTIFY_FAVORITE, "id"),
                getResourceId(INotification.DRAWABLE_NOTIFY_BTN_FAVORITE,
                    "drawable"))
        } else {
            //喜欢或收藏按钮没选中时样式
            mBigRemoteView?.setImageViewResource(
                getResourceId(INotification.ID_IMG_NOTIFY_FAVORITE, "id"),
                getResourceId(if (isDark)
                    INotification.DRAWABLE_NOTIFY_BTN_DARK_FAVORITE
                else
                    INotification.DRAWABLE_NOTIFY_BTN_LIGHT_FAVORITE, "drawable"))
        }
        mNotificationManager?.notify(INotification.NOTIFICATION_ID, mNotification)
    }

    /**
     * 更新歌词按钮UI
     */
    override fun updateLyricsUI(isChecked: Boolean) {
        if (mNotification == null) {
            return
        }
        val isDark = mColorUtils.isDarkNotificationBar(mService, mNotification!!)
        //歌词按钮选中时样式
        if (isChecked) {
            mBigRemoteView
                ?.setImageViewResource(
                    getResourceId(INotification.ID_IMG_NOTIFY_LYRICS, "id"),
                    getResourceId(INotification.DRAWABLE_NOTIFY_BTN_LYRICS,
                        "drawable"))
        } else {
            //歌词按钮没选中时样式
            mBigRemoteView
                ?.setImageViewResource(
                    getResourceId(INotification.ID_IMG_NOTIFY_LYRICS, "id"),
                    getResourceId(if (isDark)
                        INotification.DRAWABLE_NOTIFY_BTN_DARK_LYRICS
                    else
                        INotification.DRAWABLE_NOTIFY_BTN_LIGHT_LYRICS, "drawable"))
        }
        mNotificationManager?.notify(INotification.NOTIFICATION_ID, mNotification)
    }

    private fun getResourceId(name: String, className: String): Int {
        return res.getIdentifier(name, className, packageName)
    }

    private fun setStopIntent(pendingIntent: PendingIntent?) {
        mStopIntent = pendingIntent ?: getPendingIntent(INotification.ACTION_STOP)
    }

    private fun setNextPendingIntent(pendingIntent: PendingIntent?) {
        mNextIntent = pendingIntent ?: getPendingIntent(INotification.ACTION_NEXT)
    }

    private fun setPrePendingIntent(pendingIntent: PendingIntent?) {
        mPreviousIntent =
            pendingIntent ?: getPendingIntent(INotification.ACTION_PREV)
    }

    private fun setPlayPendingIntent(pendingIntent: PendingIntent?) {
        mPlayIntent = pendingIntent ?: getPendingIntent(INotification.ACTION_PLAY)
    }

    private fun setPausePendingIntent(pendingIntent: PendingIntent?) {
        mPauseIntent = pendingIntent ?: getPendingIntent(INotification.ACTION_PAUSE)
    }

    private fun setFavoritePendingIntent(pendingIntent: PendingIntent?) {
        mFavoriteIntent =
            pendingIntent ?: getPendingIntent(INotification.ACTION_FAVORITE)
    }

    private fun setLyricsPendingIntent(pendingIntent: PendingIntent?) {
        mLyricsIntent =
            pendingIntent ?: getPendingIntent(INotification.ACTION_LYRICS)
    }

    private fun setDownloadPendingIntent(pendingIntent: PendingIntent?) {
        mDownloadIntent =
            pendingIntent ?: getPendingIntent(INotification.ACTION_DOWNLOAD)
    }

    private fun setClosePendingIntent(pendingIntent: PendingIntent?) {
        mCloseIntent = pendingIntent ?: getPendingIntent(INotification.ACTION_CLOSE)
    }

    private fun setPlayOrPauseIntent(pendingIntent: PendingIntent?) {
        mPlayOrPauseIntent =
            pendingIntent ?: getPendingIntent(INotification.ACTION_PLAY_OR_PAUSE)
    }

    private fun getPendingIntent(action: String): PendingIntent {
        val intent = Intent(action)
        intent.setPackage(packageName)
        return PendingIntent
            .getBroadcast(mService, INotification.REQUEST_CODE, intent,
                PendingIntent.FLAG_CANCEL_CURRENT)
    }
}