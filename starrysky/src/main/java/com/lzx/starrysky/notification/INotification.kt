package com.lzx.starrysky.notification

interface INotification {

    /**
     * 展示通知栏
     */
    fun startNotification()

    /**
     * 关闭通知栏
     */
    fun stopNotification()

    /**
     * 更新喜欢或收藏按钮UI
     */
    fun updateFavoriteUI(isFavorite: Boolean)

    /**
     * 更新歌词按钮UI
     */
    fun updateLyricsUI(isChecked: Boolean)

    companion object {
        const val NOTIFICATION_ID = 412
        const val REQUEST_CODE = 100

        //action
        const val ACTION_PLAY_OR_PAUSE = "com.lzx.starrysky.play_or_pause"
        const val ACTION_PAUSE = "com.lzx.starrysky.pause"
        const val ACTION_PLAY = "com.lzx.starrysky.play"
        const val ACTION_PREV = "com.lzx.starrysky.prev"
        const val ACTION_NEXT = "com.lzx.starrysky.next"
        const val ACTION_STOP = "com.lzx.starrysky.stop"
        const val ACTION_CLOSE = "com.lzx.starrysky.close"
        const val ACTION_FAVORITE = "com.lzx.starrysky.favorite"
        const val ACTION_LYRICS = "com.lzx.starrysky.lyrics"
        const val ACTION_DOWNLOAD = "com.lzx.starrysky.download"
        const val ACTION_INTENT_CLICK = "com.lzx.starrysky.EXTRY_NOTIFICATION_TO_MAINACTIVITY"

        const val ACTION_UPDATE_FAVORITE_UI = "com.lzx.starrysky.update_favorite_ui"
        const val ACTION_UPDATE_LYRICS_UI = "com.lzx.starrysky.update_lyrics_ui"

        const val CHANNEL_ID = "com.lzx.starrysky.MUSIC_CHANNEL_ID"

        //布局
        const val LAYOUT_NOTIFY_PLAY = "view_notify_play" //普通布局
        const val LAYOUT_NOTIFY_BIG_PLAY = "view_notify_big_play" //大布局
        //id
        const val ID_IMG_NOTIFY_PLAY = "img_notifyPlay" //播放按钮id
        const val ID_IMG_NOTIFY_PAUSE = "img_notifyPause" //暂停按钮id
        const val ID_IMG_NOTIFY_STOP = "img_notifyStop" //停止按钮id
        const val ID_IMG_NOTIFY_FAVORITE = "img_notifyFavorite" //喜欢/收藏按钮id
        const val ID_IMG_NOTIFY_LYRICS = "img_notifyLyrics" //歌词按钮id
        const val ID_IMG_NOTIFY_DOWNLOAD = "img_notifyDownload" //下载按钮id
        const val ID_IMG_NOTIFY_PLAY_OR_PAUSE = "img_notifyPlayOrPause" //播放或暂停按钮id
        const val ID_IMG_NOTIFY_NEXT = "img_notifyNext" //下一首按钮id
        const val ID_IMG_NOTIFY_PRE = "img_notifyPre" //上一首按钮id
        const val ID_IMG_NOTIFY_CLOSE = "img_notifyClose" //关闭按钮id
        const val ID_IMG_NOTIFY_ICON = "img_notifyIcon" //封面图片id
        const val ID_TXT_NOTIFY_SONGNAME = "txt_notifySongName" //歌名TextView id
        const val ID_TXT_NOTIFY_ARTISTNAME = "txt_notifyArtistName"//艺术家TextView id
        //资源
        const val DRAWABLE_NOTIFY_BTN_FAVORITE = "notify_btn_favorite_checked"//喜欢按钮选中时的图片资源
        const val DRAWABLE_NOTIFY_BTN_LYRICS = "notify_btn_lyrics_checked"//歌词按钮选中时的图片资源
        //通知栏白色背景资源
        const val DRAWABLE_NOTIFY_BTN_LIGHT_PLAY_SELECTOR =
            "notify_btn_light_play_selector" //白色背景时播放按钮selector
        const val DRAWABLE_NOTIFY_BTN_LIGHT_PAUSE_SELECTOR =
            "notify_btn_light_pause_selector"//白色背景时暂停按钮selector
        const val DRAWABLE_NOTIFY_BTN_LIGHT_FAVORITE =
            "notify_btn_light_favorite_normal"//白色背景时喜欢按钮的图片资源
        const val DRAWABLE_NOTIFY_BTN_LIGHT_LYRICS =
            "notify_btn_light_lyrics_normal"//白色背景时歌词按钮的图片资源
        const val DRAWABLE_NOTIFY_BTN_LIGHT_DOWNLOAD =
            "notify_btn_light_download_normal"//白色背景时下载按钮的图片资源
        const val DRAWABLE_NOTIFY_BTN_LIGHT_NEXT_PRESSED =
            "notify_btn_light_next_pressed"   //白色背景时下一首按钮按下时的图片资源
        const val DRAWABLE_NOTIFY_BTN_LIGHT_NEXT_SELECTOR =
            "notify_btn_light_next_selector" //白色背景时下一首按钮selector
        const val DRAWABLE_NOTIFY_BTN_LIGHT_PREV_PRESSED =
            "notify_btn_light_prev_pressed"   //白色背景时上一首按钮按下时的图片资源
        const val DRAWABLE_NOTIFY_BTN_LIGHT_PREV_SELECTOR =
            "notify_btn_light_prev_selector" //白色背景时上一首按钮selector
        //通知栏黑色背景资源
        const val DRAWABLE_NOTIFY_BTN_DARK_PLAY_SELECTOR =
            "notify_btn_dark_play_selector" //黑色背景时播放按钮selector
        const val DRAWABLE_NOTIFY_BTN_DARK_PAUSE_SELECTOR =
            "notify_btn_dark_pause_selector"//黑色背景时暂停按钮selector
        const val DRAWABLE_NOTIFY_BTN_DARK_FAVORITE =
            "notify_btn_dark_favorite_normal"//黑色背景时喜欢按钮按下时的图片资源
        const val DRAWABLE_NOTIFY_BTN_DARK_LYRICS =
            "notify_btn_dark_lyrics_normal"//黑色背景时歌词按钮按下时的图片资源
        const val DRAWABLE_NOTIFY_BTN_DARK_DOWNLOAD =
            "notify_btn_dark_download_normal"//黑色背景时下载按钮按下时的图片资源
        const val DRAWABLE_NOTIFY_BTN_DARK_NEXT_PRESSED =
            "notify_btn_dark_next_pressed"   //黑色背景时下一首按钮按下时的图片资源
        const val DRAWABLE_NOTIFY_BTN_DARK_NEXT_SELECTOR =
            "notify_btn_dark_next_selector" //黑色背景时下一首按钮selector
        const val DRAWABLE_NOTIFY_BTN_DARK_PREV_PRESSED =
            "notify_btn_dark_prev_pressed"   //黑色背景时上一首按钮按下时的图片资源
        const val DRAWABLE_NOTIFY_BTN_DARK_PREV_SELECTOR =
            "notify_btn_dark_prev_selector" //黑色背景时上一首按钮selector

        const val TIME_INTERVAL = 1000
    }
}
