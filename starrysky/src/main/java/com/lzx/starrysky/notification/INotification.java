package com.lzx.starrysky.notification;

public interface INotification {
    int NOTIFICATION_ID = 412;
    int REQUEST_CODE = 100;

    //action
    String ACTION_PLAY_OR_PAUSE = "com.lzx.starrysky.play_or_pause";
    String ACTION_PAUSE = "com.lzx.starrysky.pause";
    String ACTION_PLAY = "com.lzx.starrysky.play";
    String ACTION_PREV = "com.lzx.starrysky.prev";
    String ACTION_NEXT = "com.lzx.starrysky.next";
    String ACTION_STOP = "com.lzx.starrysky.stop";
    String ACTION_CLOSE = "com.lzx.starrysky.close";
    String ACTION_FAVORITE = "com.lzx.starrysky.favorite";
    String ACTION_LYRICS = "com.lzx.starrysky.lyrics";
    String ACTION_DOWNLOAD = "com.lzx.starrysky.download";
    String ACTION_INTENT_CLICK = "com.lzx.starrysky.EXTRY_NOTIFICATION_TO_MAINACTIVITY";

    String ACTION_UPDATE_FAVORITE_UI = "com.lzx.starrysky.update_favorite_ui";
    String ACTION_UPDATE_LYRICS_UI = "com.lzx.starrysky.update_lyrics_ui";

    String CHANNEL_ID = "com.lzx.starrysky.MUSIC_CHANNEL_ID";

    //布局
    String LAYOUT_NOTIFY_PLAY = "view_notify_play"; //普通布局
    String LAYOUT_NOTIFY_BIG_PLAY = "view_notify_big_play"; //大布局
    //id
    String ID_IMG_NOTIFY_PLAY = "img_notifyPlay"; //播放按钮id
    String ID_IMG_NOTIFY_PAUSE = "img_notifyPause"; //暂停按钮id
    String ID_IMG_NOTIFY_STOP = "img_notifyStop"; //停止按钮id
    String ID_IMG_NOTIFY_FAVORITE = "img_notifyFavorite"; //喜欢/收藏按钮id
    String ID_IMG_NOTIFY_LYRICS = "img_notifyLyrics"; //歌词按钮id
    String ID_IMG_NOTIFY_DOWNLOAD = "img_notifyDownload"; //下载按钮id
    String ID_IMG_NOTIFY_PLAY_OR_PAUSE = "img_notifyPlayOrPause"; //播放或暂停按钮id
    String ID_IMG_NOTIFY_NEXT = "img_notifyNext"; //下一首按钮id
    String ID_IMG_NOTIFY_PRE = "img_notifyPre"; //上一首按钮id
    String ID_IMG_NOTIFY_CLOSE = "img_notifyClose"; //关闭按钮id
    String ID_IMG_NOTIFY_ICON = "img_notifyIcon"; //封面图片id
    String ID_TXT_NOTIFY_SONGNAME = "txt_notifySongName"; //歌名TextView id
    String ID_TXT_NOTIFY_ARTISTNAME = "txt_notifyArtistName";//艺术家TextView id
    //资源
    String DRAWABLE_NOTIFY_BTN_FAVORITE = "notify_btn_favorite_checked";//喜欢按钮选中时的图片资源
    String DRAWABLE_NOTIFY_BTN_LYRICS = "notify_btn_lyrics_checked";//歌词按钮选中时的图片资源
    //通知栏白色背景资源
    String DRAWABLE_NOTIFY_BTN_LIGHT_PLAY_SELECTOR = "notify_btn_light_play_selector"; //白色背景时播放按钮selector
    String DRAWABLE_NOTIFY_BTN_LIGHT_PAUSE_SELECTOR = "notify_btn_light_pause_selector";//白色背景时暂停按钮selector
    String DRAWABLE_NOTIFY_BTN_LIGHT_FAVORITE = "notify_btn_light_favorite_normal";//白色背景时喜欢按钮的图片资源
    String DRAWABLE_NOTIFY_BTN_LIGHT_LYRICS = "notify_btn_light_lyrics_normal";//白色背景时歌词按钮的图片资源
    String DRAWABLE_NOTIFY_BTN_LIGHT_DOWNLOAD = "notify_btn_light_download_normal";//白色背景时下载按钮的图片资源
    String DRAWABLE_NOTIFY_BTN_LIGHT_NEXT_PRESSED = "notify_btn_light_next_pressed";   //白色背景时下一首按钮按下时的图片资源
    String DRAWABLE_NOTIFY_BTN_LIGHT_NEXT_SELECTOR = "notify_btn_light_next_selector"; //白色背景时下一首按钮selector
    String DRAWABLE_NOTIFY_BTN_LIGHT_PREV_PRESSED = "notify_btn_light_prev_pressed";   //白色背景时上一首按钮按下时的图片资源
    String DRAWABLE_NOTIFY_BTN_LIGHT_PREV_SELECTOR = "notify_btn_light_prev_selector"; //白色背景时上一首按钮selector
    //通知栏黑色背景资源
    String DRAWABLE_NOTIFY_BTN_DARK_PLAY_SELECTOR = "notify_btn_dark_play_selector"; //黑色背景时播放按钮selector
    String DRAWABLE_NOTIFY_BTN_DARK_PAUSE_SELECTOR = "notify_btn_dark_pause_selector";//黑色背景时暂停按钮selector
    String DRAWABLE_NOTIFY_BTN_DARK_FAVORITE = "notify_btn_dark_favorite_normal";//黑色背景时喜欢按钮按下时的图片资源
    String DRAWABLE_NOTIFY_BTN_DARK_LYRICS = "notify_btn_dark_lyrics_normal";//黑色背景时歌词按钮按下时的图片资源
    String DRAWABLE_NOTIFY_BTN_DARK_DOWNLOAD = "notify_btn_dark_download_normal";//黑色背景时下载按钮按下时的图片资源
    String DRAWABLE_NOTIFY_BTN_DARK_NEXT_PRESSED = "notify_btn_dark_next_pressed";   //黑色背景时下一首按钮按下时的图片资源
    String DRAWABLE_NOTIFY_BTN_DARK_NEXT_SELECTOR = "notify_btn_dark_next_selector"; //黑色背景时下一首按钮selector
    String DRAWABLE_NOTIFY_BTN_DARK_PREV_PRESSED = "notify_btn_dark_prev_pressed";   //黑色背景时上一首按钮按下时的图片资源
    String DRAWABLE_NOTIFY_BTN_DARK_PREV_SELECTOR = "notify_btn_dark_prev_selector"; //黑色背景时上一首按钮selector

    int TIME_INTERVAL = 1000;

    /**
     * 展示通知栏
     */
    void startNotification();

    /**
     * 关闭通知栏
     */
    void stopNotification();

    /**
     * 更新喜欢或收藏按钮UI
     */
    void updateFavoriteUI(boolean isFavorite);

    /**
     * 更新歌词按钮UI
     */
    void updateLyricsUI(boolean isChecked);
}
