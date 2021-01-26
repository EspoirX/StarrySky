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
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.lzx.starrysky.R
import com.lzx.starrysky.SongInfo
import com.lzx.starrysky.StarrySky
import com.lzx.starrysky.manager.PlaybackStage
import com.lzx.starrysky.notification.INotification.Companion.ACTION_CLOSE
import com.lzx.starrysky.notification.INotification.Companion.ACTION_DOWNLOAD
import com.lzx.starrysky.notification.INotification.Companion.ACTION_FAVORITE
import com.lzx.starrysky.notification.INotification.Companion.ACTION_LYRICS
import com.lzx.starrysky.notification.INotification.Companion.ACTION_NEXT
import com.lzx.starrysky.notification.INotification.Companion.ACTION_PAUSE
import com.lzx.starrysky.notification.INotification.Companion.ACTION_PLAY
import com.lzx.starrysky.notification.INotification.Companion.ACTION_PLAY_OR_PAUSE
import com.lzx.starrysky.notification.INotification.Companion.ACTION_PREV
import com.lzx.starrysky.notification.INotification.Companion.ACTION_STOP
import com.lzx.starrysky.notification.INotification.Companion.CHANNEL_ID
import com.lzx.starrysky.notification.INotification.Companion.DRAWABLE_NOTIFY_BTN_DARK_DOWNLOAD
import com.lzx.starrysky.notification.INotification.Companion.DRAWABLE_NOTIFY_BTN_DARK_FAVORITE
import com.lzx.starrysky.notification.INotification.Companion.DRAWABLE_NOTIFY_BTN_DARK_LYRICS
import com.lzx.starrysky.notification.INotification.Companion.DRAWABLE_NOTIFY_BTN_DARK_NEXT_PRESSED
import com.lzx.starrysky.notification.INotification.Companion.DRAWABLE_NOTIFY_BTN_DARK_NEXT_SELECTOR
import com.lzx.starrysky.notification.INotification.Companion.DRAWABLE_NOTIFY_BTN_DARK_PAUSE_SELECTOR
import com.lzx.starrysky.notification.INotification.Companion.DRAWABLE_NOTIFY_BTN_DARK_PLAY_SELECTOR
import com.lzx.starrysky.notification.INotification.Companion.DRAWABLE_NOTIFY_BTN_DARK_PREV_PRESSED
import com.lzx.starrysky.notification.INotification.Companion.DRAWABLE_NOTIFY_BTN_DARK_PREV_SELECTOR
import com.lzx.starrysky.notification.INotification.Companion.DRAWABLE_NOTIFY_BTN_FAVORITE
import com.lzx.starrysky.notification.INotification.Companion.DRAWABLE_NOTIFY_BTN_LIGHT_DOWNLOAD
import com.lzx.starrysky.notification.INotification.Companion.DRAWABLE_NOTIFY_BTN_LIGHT_FAVORITE
import com.lzx.starrysky.notification.INotification.Companion.DRAWABLE_NOTIFY_BTN_LIGHT_LYRICS
import com.lzx.starrysky.notification.INotification.Companion.DRAWABLE_NOTIFY_BTN_LIGHT_NEXT_PRESSED
import com.lzx.starrysky.notification.INotification.Companion.DRAWABLE_NOTIFY_BTN_LIGHT_NEXT_SELECTOR
import com.lzx.starrysky.notification.INotification.Companion.DRAWABLE_NOTIFY_BTN_LIGHT_PAUSE_SELECTOR
import com.lzx.starrysky.notification.INotification.Companion.DRAWABLE_NOTIFY_BTN_LIGHT_PLAY_SELECTOR
import com.lzx.starrysky.notification.INotification.Companion.DRAWABLE_NOTIFY_BTN_LIGHT_PREV_PRESSED
import com.lzx.starrysky.notification.INotification.Companion.DRAWABLE_NOTIFY_BTN_LIGHT_PREV_SELECTOR
import com.lzx.starrysky.notification.INotification.Companion.DRAWABLE_NOTIFY_BTN_LYRICS
import com.lzx.starrysky.notification.INotification.Companion.ID_IMG_NOTIFY_CLOSE
import com.lzx.starrysky.notification.INotification.Companion.ID_IMG_NOTIFY_DOWNLOAD
import com.lzx.starrysky.notification.INotification.Companion.ID_IMG_NOTIFY_FAVORITE
import com.lzx.starrysky.notification.INotification.Companion.ID_IMG_NOTIFY_ICON
import com.lzx.starrysky.notification.INotification.Companion.ID_IMG_NOTIFY_LYRICS
import com.lzx.starrysky.notification.INotification.Companion.ID_IMG_NOTIFY_NEXT
import com.lzx.starrysky.notification.INotification.Companion.ID_IMG_NOTIFY_PAUSE
import com.lzx.starrysky.notification.INotification.Companion.ID_IMG_NOTIFY_PLAY
import com.lzx.starrysky.notification.INotification.Companion.ID_IMG_NOTIFY_PLAY_OR_PAUSE
import com.lzx.starrysky.notification.INotification.Companion.ID_IMG_NOTIFY_PRE
import com.lzx.starrysky.notification.INotification.Companion.ID_IMG_NOTIFY_STOP
import com.lzx.starrysky.notification.INotification.Companion.ID_TXT_NOTIFY_ARTISTNAME
import com.lzx.starrysky.notification.INotification.Companion.ID_TXT_NOTIFY_SONGNAME
import com.lzx.starrysky.notification.INotification.Companion.LAYOUT_NOTIFY_BIG_PLAY
import com.lzx.starrysky.notification.INotification.Companion.LAYOUT_NOTIFY_PLAY
import com.lzx.starrysky.notification.INotification.Companion.NOTIFICATION_ID
import com.lzx.starrysky.notification.INotification.Companion.TIME_INTERVAL
import com.lzx.starrysky.notification.imageloader.ImageLoaderCallBack
import com.lzx.starrysky.notification.utils.NotificationColorUtils
import com.lzx.starrysky.notification.utils.NotificationUtils
import com.lzx.starrysky.playback.Playback
import com.lzx.starrysky.service.MusicService
import com.lzx.starrysky.utils.getPendingIntent
import com.lzx.starrysky.utils.getResourceId
import com.lzx.starrysky.utils.getTargetClass
import com.lzx.starrysky.utils.orDef

class CustomNotification constructor(
    val context: Context,
    var config: NotificationConfig = NotificationConfig.Builder().build()
) : BroadcastReceiver(), INotification {

    companion object {
        const val ACTION_UPDATE_FAVORITE = "ACTION_UPDATE_FAVORITE"
        const val ACTION_UPDATE_LYRICS = "ACTION_UPDATE_LYRICS"
    }

    private var remoteView: RemoteViews? = null
    private var bigRemoteView: RemoteViews? = null

    private var playOrPauseIntent: PendingIntent
    private var playIntent: PendingIntent
    private var pauseIntent: PendingIntent
    private var stopIntent: PendingIntent
    private var nextIntent: PendingIntent
    private var previousIntent: PendingIntent
    private var favoriteIntent: PendingIntent
    private var lyricsIntent: PendingIntent
    private var downloadIntent: PendingIntent
    private var closeIntent: PendingIntent


    private var playbackState: String = PlaybackStage.IDEA
    private var songInfo: SongInfo? = null

    private val notificationManager: NotificationManager?
    private val packageName: String
    private var mStarted = false
    private var mNotification: Notification? = null

    private val colorUtils: NotificationColorUtils

    private var lastClickTime: Long = 0

    private var hasNextSong = false
    private var hasPreSong = false

    init {
        notificationManager = context.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
        packageName = context.applicationContext.packageName
        colorUtils = NotificationColorUtils()
        playOrPauseIntent = config.playOrPauseIntent ?: ACTION_PLAY_OR_PAUSE.getPendingIntent()
        playIntent = config.playIntent ?: ACTION_PLAY.getPendingIntent()
        pauseIntent = config.pauseIntent ?: ACTION_PAUSE.getPendingIntent()
        stopIntent = config.stopIntent ?: ACTION_STOP.getPendingIntent()
        nextIntent = config.nextIntent ?: ACTION_NEXT.getPendingIntent()
        previousIntent = config.preIntent ?: ACTION_PREV.getPendingIntent()
        favoriteIntent = config.favoriteIntent ?: ACTION_FAVORITE.getPendingIntent()
        lyricsIntent = config.lyricsIntent ?: ACTION_LYRICS.getPendingIntent()
        downloadIntent = config.downloadIntent ?: ACTION_DOWNLOAD.getPendingIntent()
        closeIntent = config.closeIntent ?: ACTION_CLOSE.getPendingIntent()
        notificationManager.cancelAll()
    }

    override fun onPlaybackStateChanged(songInfo: SongInfo?, state: String,
                                        hasNextSong: Boolean, hasPreSong: Boolean) {
        this.hasNextSong = hasNextSong
        this.hasPreSong = hasPreSong
        playbackState = state
        this.songInfo = songInfo
        if (state == PlaybackStage.IDEA) {
            stopNotification()
        } else {
            val notification = createNotification()
            if (notification != null && state != PlaybackStage.BUFFERING) {
                mStarted = true
                notificationManager?.notify(NOTIFICATION_ID, notification)
            }
        }
    }

    override fun setSessionToken(mediaSession: MediaSessionCompat.Token?) {
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
        if (nowTime - lastClickTime <= TIME_INTERVAL) {
            return
        }
        val player = (context as MusicService).binder?.player
        when (action) {
            ACTION_PAUSE -> pauseMusic(player)
            ACTION_PLAY -> restoreMusic(player)
            ACTION_PLAY_OR_PAUSE -> {
                if (playbackState == PlaybackStage.PLAYING) {
                    pauseMusic(player)
                } else {
                    restoreMusic(player)
                }
            }
            ACTION_NEXT -> player?.skipToNext()
            ACTION_PREV -> player?.skipToPrevious()
            ACTION_CLOSE -> stopNotification()
        }
        lastClickTime = nowTime
    }

    private fun createNotification(): Notification? {
        if (songInfo == null) {
            return null
        }
        val smallIcon = if (config.smallIconRes != -1) config.smallIconRes else R.drawable.ic_notification
        //适配8.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationUtils.createNotificationChannel(context, notificationManager!!)
        }
        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
        notificationBuilder
            .setOnlyAlertOnce(true)
            .setSmallIcon(smallIcon)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentTitle(songInfo?.songName) //歌名
            .setContentText(songInfo?.artist) //艺术家
        //setContentIntent
        val clazz = config.targetClass.getTargetClass()
        clazz?.let {
            val intent = NotificationUtils.createContentIntent(context, config, songInfo, config.targetClassBundle, it)
            notificationBuilder.setContentIntent(intent)
        }
        //这里不能复用，会导致内存泄漏，需要每次都创建
        remoteView = createRemoteViews(false)
        bigRemoteView = createRemoteViews(true)

        //setCustomContentView and setCustomBigContentView
        if (Build.VERSION.SDK_INT >= 24) {
            notificationBuilder.setCustomContentView(remoteView)
            notificationBuilder.setCustomBigContentView(bigRemoteView)
        }

        setNotificationPlaybackState(notificationBuilder)

        //create Notification
        mNotification = notificationBuilder.build()
        mNotification?.contentView = remoteView
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mNotification?.bigContentView = bigRemoteView
        }
        updateRemoteViewUI(mNotification, songInfo, smallIcon)
        return mNotification
    }

    private fun setNotificationPlaybackState(builder: NotificationCompat.Builder) {
        if (!mStarted) {
            (context as MusicService).stopForeground(true)
        }
        builder.setOngoing(playbackState == PlaybackStage.PLAYING)
    }

    /**
     * 创建RemoteViews
     */
    private fun createRemoteViews(isBigRemoteViews: Boolean): RemoteViews {
        val remoteView: RemoteViews = if (isBigRemoteViews) {
            RemoteViews(packageName, LAYOUT_NOTIFY_BIG_PLAY.getResLayout())
        } else {
            RemoteViews(packageName, LAYOUT_NOTIFY_PLAY.getResLayout())
        }
        remoteView.setOnClickPendingIntent(ID_IMG_NOTIFY_PLAY.getResId(), playIntent)
        remoteView.setOnClickPendingIntent(ID_IMG_NOTIFY_PAUSE.getResId(), pauseIntent)
        remoteView.setOnClickPendingIntent(ID_IMG_NOTIFY_STOP.getResId(), stopIntent)
        remoteView.setOnClickPendingIntent(ID_IMG_NOTIFY_FAVORITE.getResId(), favoriteIntent)
        remoteView.setOnClickPendingIntent(ID_IMG_NOTIFY_LYRICS.getResId(), lyricsIntent)
        remoteView.setOnClickPendingIntent(ID_IMG_NOTIFY_DOWNLOAD.getResId(), downloadIntent)
        remoteView.setOnClickPendingIntent(ID_IMG_NOTIFY_NEXT.getResId(), nextIntent)
        remoteView.setOnClickPendingIntent(ID_IMG_NOTIFY_PRE.getResId(), previousIntent)
        remoteView.setOnClickPendingIntent(ID_IMG_NOTIFY_CLOSE.getResId(), closeIntent)
        remoteView.setOnClickPendingIntent(ID_IMG_NOTIFY_PLAY_OR_PAUSE.getResId(), playOrPauseIntent)
        return remoteView
    }

    /**
     * 更新RemoteViews
     */
    private fun updateRemoteViewUI(
        notification: Notification?, songInfo: SongInfo?, smallIcon: Int
    ) {
        val isDark = colorUtils.isDarkNotificationBar(context, notification)
        var art: Bitmap? = songInfo?.coverBitmap
        val artistName = songInfo?.artist ?: ""
        val songName = songInfo?.songName ?: ""
        //设置文字内容
        remoteView?.setTextViewText(ID_TXT_NOTIFY_SONGNAME.getResId(), songName)
        remoteView?.setTextViewText(ID_TXT_NOTIFY_ARTISTNAME.getResId(), artistName)
        //设置播放暂停按钮
        if (playbackState == PlaybackStage.PLAYING || playbackState == PlaybackStage.BUFFERING) {
            val name =
                if (isDark) DRAWABLE_NOTIFY_BTN_DARK_PAUSE_SELECTOR else DRAWABLE_NOTIFY_BTN_LIGHT_PAUSE_SELECTOR
            remoteView?.setImageViewResource(ID_IMG_NOTIFY_PLAY_OR_PAUSE.getResId(), name.getResDrawable())
        } else {
            val name =
                if (isDark) DRAWABLE_NOTIFY_BTN_DARK_PLAY_SELECTOR else DRAWABLE_NOTIFY_BTN_LIGHT_PLAY_SELECTOR
            remoteView?.setImageViewResource(ID_IMG_NOTIFY_PLAY_OR_PAUSE.getResId(), name.getResDrawable())
        }

        //大布局
        //设置文字内容
        bigRemoteView?.setTextViewText(ID_TXT_NOTIFY_SONGNAME.getResId(), songName)
        bigRemoteView?.setTextViewText(ID_TXT_NOTIFY_ARTISTNAME.getResId(), artistName)
        //设置播放暂停按钮
        if (playbackState == PlaybackStage.PLAYING || playbackState == PlaybackStage.BUFFERING) {
            val name =
                if (isDark) DRAWABLE_NOTIFY_BTN_DARK_PAUSE_SELECTOR else DRAWABLE_NOTIFY_BTN_LIGHT_PAUSE_SELECTOR
            bigRemoteView?.setImageViewResource(ID_IMG_NOTIFY_PLAY_OR_PAUSE.getResId(), name.getResDrawable())
        } else {
            val name =
                if (isDark) DRAWABLE_NOTIFY_BTN_DARK_PLAY_SELECTOR else DRAWABLE_NOTIFY_BTN_LIGHT_PLAY_SELECTOR
            bigRemoteView?.setImageViewResource(ID_IMG_NOTIFY_PLAY_OR_PAUSE.getResId(), name.getResDrawable())
        }
        //设置喜欢或收藏按钮
        bigRemoteView?.setImageViewResource(ID_IMG_NOTIFY_FAVORITE.getResId(),
            isDark.getResDrawableByDark(DRAWABLE_NOTIFY_BTN_DARK_FAVORITE, DRAWABLE_NOTIFY_BTN_LIGHT_FAVORITE))
        //设置歌词按钮
        bigRemoteView?.setImageViewResource(ID_IMG_NOTIFY_LYRICS.getResId(),
            isDark.getResDrawableByDark(DRAWABLE_NOTIFY_BTN_DARK_LYRICS, DRAWABLE_NOTIFY_BTN_LIGHT_LYRICS))
        //设置下载按钮
        bigRemoteView?.setImageViewResource(ID_IMG_NOTIFY_DOWNLOAD.getResId(),
            isDark.getResDrawableByDark(DRAWABLE_NOTIFY_BTN_DARK_DOWNLOAD, DRAWABLE_NOTIFY_BTN_LIGHT_DOWNLOAD))


//        //上一首下一首按钮
        disableNextBtn(hasNextSong, isDark)
        disablePreviousBtn(hasPreSong, isDark)

        //封面
        var fetchArtUrl: String? = null
        if (art == null) {
            fetchArtUrl = songInfo?.songCover
            if (fetchArtUrl.isNullOrEmpty()) {
                art = BitmapFactory.decodeResource(context.resources, R.drawable.default_art)
            }
        }
        if (art != null) {
            remoteView?.setImageViewBitmap(ID_IMG_NOTIFY_ICON.getResId(), art)
            bigRemoteView?.setImageViewBitmap(ID_IMG_NOTIFY_ICON.getResId(), art)
        }
        notificationManager?.notify(NOTIFICATION_ID, notification)

        if (!fetchArtUrl.isNullOrEmpty()) {
            fetchBitmapFromURLAsync(fetchArtUrl, notification)
        }
    }

    /**
     * 加载封面
     */
    private fun fetchBitmapFromURLAsync(fetchArtUrl: String, notification: Notification?) {
        StarrySky.getImageLoader()?.load(fetchArtUrl, object : ImageLoaderCallBack {
            override fun onBitmapLoaded(bitmap: Bitmap?) {
                bitmap?.let {
                    remoteView?.setImageViewBitmap(ID_IMG_NOTIFY_ICON.getResId(), it)
                    bigRemoteView?.setImageViewBitmap(ID_IMG_NOTIFY_ICON.getResId(), it)
                    //https://github.com/EspoirX/StarrySky/issues/188
                    if (mNotification != null) {
                        notificationManager?.notify(NOTIFICATION_ID, notification)
                    }
                }
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
            isDark.getResDrawableByDark(DRAWABLE_NOTIFY_BTN_DARK_NEXT_PRESSED, DRAWABLE_NOTIFY_BTN_LIGHT_NEXT_PRESSED)
        } else {
            isDark.getResDrawableByDark(DRAWABLE_NOTIFY_BTN_DARK_NEXT_SELECTOR, DRAWABLE_NOTIFY_BTN_LIGHT_NEXT_SELECTOR)
        }
        remoteView?.setImageViewResource(ID_IMG_NOTIFY_NEXT.getResId(), res)
        bigRemoteView?.setImageViewResource(ID_IMG_NOTIFY_NEXT.getResId(), res)
    }

    /**
     * 上一首按钮样式
     */
    private fun disablePreviousBtn(disable: Boolean, isDark: Boolean) {
        val res: Int = if (disable) {
            isDark.getResDrawableByDark(DRAWABLE_NOTIFY_BTN_DARK_PREV_PRESSED, DRAWABLE_NOTIFY_BTN_LIGHT_PREV_PRESSED)
        } else {
            isDark.getResDrawableByDark(DRAWABLE_NOTIFY_BTN_DARK_PREV_SELECTOR, DRAWABLE_NOTIFY_BTN_LIGHT_PREV_SELECTOR)
        }
        remoteView?.setImageViewResource(ID_IMG_NOTIFY_PRE.getResId(), res)
        bigRemoteView?.setImageViewResource(ID_IMG_NOTIFY_PRE.getResId(), res)
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
                filter.addAction(ACTION_PLAY_OR_PAUSE)
                filter.addAction(ACTION_CLOSE)
                context.registerReceiver(this, filter)
                (context as MusicService).startForeground(NOTIFICATION_ID, notification)
                mStarted = true
            }
        }
    }

    override fun stopNotification() {
        if (mStarted) {
            mStarted = false
            try {
                notificationManager?.cancel(NOTIFICATION_ID)
                context.unregisterReceiver(this)
            } catch (ex: IllegalArgumentException) {
                ex.printStackTrace()
            }
            (context as MusicService).stopForeground(true)
        }
    }

    override fun onCommand(command: String?, extras: Bundle?) {
        when (command) {
            ACTION_UPDATE_FAVORITE -> {
                val isFavorite = extras?.getBoolean("isFavorite").orDef()
                updateFavoriteUI(isFavorite)
            }
            ACTION_UPDATE_LYRICS -> {
                val isChecked = extras?.getBoolean("isChecked").orDef()
                updateLyricsUI(isChecked)
            }
        }
    }

    /**
     * 更新喜欢或收藏按钮样式
     */
    private fun updateFavoriteUI(isFavorite: Boolean) {
        if (mNotification == null) {
            return
        }
        val isDark = colorUtils.isDarkNotificationBar(context, mNotification!!)
        //喜欢或收藏按钮选中时样式
        if (isFavorite) {
            bigRemoteView?.setImageViewResource(ID_IMG_NOTIFY_FAVORITE.getResId(),
                DRAWABLE_NOTIFY_BTN_FAVORITE.getResDrawable())
        } else {
            //喜欢或收藏按钮没选中时样式
            bigRemoteView?.setImageViewResource(ID_IMG_NOTIFY_FAVORITE.getResId(),
                isDark.getResDrawableByDark(DRAWABLE_NOTIFY_BTN_DARK_FAVORITE, DRAWABLE_NOTIFY_BTN_LIGHT_FAVORITE))
        }
        if (mNotification != null) {
            notificationManager?.notify(NOTIFICATION_ID, mNotification)
        }
    }

    /**
     * 更新歌词按钮UI
     */
    private fun updateLyricsUI(isChecked: Boolean) {
        if (mNotification == null) {
            return
        }
        val isDark = colorUtils.isDarkNotificationBar(context, mNotification!!)
        //歌词按钮选中时样式
        if (isChecked) {
            bigRemoteView?.setImageViewResource(ID_IMG_NOTIFY_LYRICS.getResId(),
                DRAWABLE_NOTIFY_BTN_LYRICS.getResDrawable())
        } else {
            //歌词按钮没选中时样式
            bigRemoteView?.setImageViewResource(ID_IMG_NOTIFY_LYRICS.getResId(),
                isDark.getResDrawableByDark(DRAWABLE_NOTIFY_BTN_DARK_LYRICS, DRAWABLE_NOTIFY_BTN_LIGHT_LYRICS))
        }
        if (mNotification != null) {
            notificationManager?.notify(NOTIFICATION_ID, mNotification)
        }
    }

    private fun String.getPendingIntent(): PendingIntent {
        return context.getPendingIntent(INotification.REQUEST_CODE, this)
    }

    private fun String.getResId(): Int {
        return context.getResourceId(this, "id")
    }

    private fun String.getResLayout(): Int {
        return context.getResourceId(this, "layout")
    }

    private fun String.getResDrawable(): Int {
        return context.getResourceId(this, "drawable")
    }

    private fun Boolean.getResDrawableByDark(a: String, b: String): Int {
        return if (this) a.getResDrawable() else b.getResDrawable()
    }
}