# Quickly integrate notification bar

## NotificationConstructor 

[NotificationConstructor](https://github.com/lizixian18/MusicLibrary/blob/StarrySkyJava/starrysky/src/main/java/com/lzx/starrysky/notification/NotificationConstructor.java) is the notification bar constructor, which can be configured with many parameters to correspond to the related operations of the configuration notification bar:

| variable name  |   Features  |
| :--------     |   :----------   |
| String targetClass | The notification bar clicks on the jump interface, and the full path of the class is passed in. |
| String contentTitle       | Notification bar title    |
| String contentText        | Notification bar content    |
| PendingIntent nextIntent  |  Next button PendingIntent, if you want to implement the next button click, you can set this |
| PendingIntent preIntent      |  Previous button PendingIntent, function as above    |
| PendingIntent closeIntent       | Close button PendingIntent, function the same as above, the default implementation of closeIntent is stopMusic()  |
| PendingIntent playIntent        | Play button PendingIntent, function as above    |
| PendingIntent pauseIntent   | Pause button PendingIntent, function as above    |
| PendingIntent playOrPauseIntent    | Play/pause button PendingIntent, function as above    |
| PendingIntent stopIntent         |  Stop button PendingIntent, function as above    |
| PendingIntent downloadIntent        | Download button PendingIntent   |
| PendingIntent favoriteIntent    | Like or favorite button PendingIntent   |
| PendingIntent lyricsIntent  | Desktop lyrics button PendingIntent, same Like or favorite button   |

In the system notification bar, there are default implementations of PendingIntent are nextIntent, preIntent, playIntent, pauseIntent, and others have no default implementation.
In the custom notification bar, the default implementation of PendingIntent is nextIntent, preIntent, playIntent, pauseIntent, playOrPauseIntent, closeIntent,
Nothing else has a default implementation. If you have other buttons in your notification bar, you'll need to implement the click event yourself.

| variable name  |   Features  |
| :--------     |   :----------   |
|pendingIntentMode  |  Set the notification bar click mode, there are three：MODE_ACTIVITY，MODE_BROADCAST，MODE_SERVICE。分别对应 PendingIntent.getActivity()，PendingIntent.getBroadcast()，PendingIntent.getService()，The default is PendingIntent.getActivity()  |
|skipPreviousDrawableRes | In the system notification bar, the drawable res of the previous button, if not passed, the default [drawable res](https://github.com/lizixian18/MusicLibrary/blob/StarrySkyJava/starrysky/src/main/res/drawable-xxhdpi/ic_skip_previous_white_24dp.png)    |
|skipNextDrawableRes |In the system notification bar, the drawable res of the next button, if not passed, the default [drawable res](https://github.com/lizixian18/MusicLibrary/blob/StarrySkyJava/starrysky/src/main/res/drawable-xxhdpi/ic_skip_next_white_24dp.png)    |
|pauseDrawableRes |In the system notification bar, the drawable res displayed by the play button when playing, if not, the default is used. [drawable res](https://github.com/lizixian18/MusicLibrary/blob/StarrySkyJava/starrysky/src/main/res/drawable-xxhdpi/ic_pause_white_24dp.png) |
|playDrawableRes |In the system notification bar, the drawable res displayed by the play button when the state is paused, if not passed, the default is used. [drawable res](https://github.com/lizixian18/MusicLibrary/blob/StarrySkyJava/starrysky/src/main/res/drawable-xxhdpi/ic_play_arrow_white_24dp.png)|
|smallIconRes |Corresponding to the smallIcon of the notification bar, if not passed, the default is used. [smallIcon](https://github.com/lizixian18/MusicLibrary/blob/StarrySkyJava/starrysky/src/main/res/drawable-xxhdpi/ic_notification.png)|


## Integrated system notification bar

Simply add two sentences to the Application to integrate the system notification bar:
```java
public class TestApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        //initialize
        MusicManager.initMusicManager(this);
        //Configure notification bar
        NotificationConstructor constructor = new NotificationConstructor.Builder()
                .bulid();
        MusicManager.getInstance().setNotificationConstructor(constructor);
    }
}
```

This way the system notification bar is integrated.

## Integrated custom notification bar

The integration step of the custom notification bar will be a bit more, but it is relatively simple. Before the integration, let's take a look at the construction of the custom notification bar. Let's look at the following four images:
<img src="https://raw.githubusercontent.com/lizixian18/MusicLibrary/StarrySkyJava/art/notification1.png"> <img src="https://raw.githubusercontent.com/lizixian18/MusicLibrary/StarrySkyJava/art/notification2.png">

<img src="https://raw.githubusercontent.com/lizixian18/MusicLibrary/StarrySkyJava/art/notification3.png"> <img src="https://raw.githubusercontent.com/lizixian18/MusicLibrary/StarrySkyJava/art/notification4.png">

It can be seen that in different mobile phones, the background of the notification bar may be dark or light, and there are two styles. One is that there are fewer buttons, I call it a small layout, and the other is a button. It's bigger, let it make a big layout, and it can switch back and forth between them.
So in the custom notification bar, we have to adapt to these four situations.

**first step**

First adjust the color of the notification bar font, let him automatically change the color according to the depth of the background color.
First create the values-v19 and values-v21 folders, then create a new style.xml with the same content, which is:

```java
<resources>
    <style name="notification_info" parent="android:TextAppearance.Material.Notification.Info"/>
    <style name="notification_title" parent="android:TextAppearance.Material.Notification.Title"/>
</resources>
```

Then when you write the layout, the corresponding notification bar title and info's TextView can reference them. Remember that TextView can't write dead font colors, or you can't fit it.

**Second step**

Since there are two backgrounds, you need to prepare two sets of resource files, because StarrySky can quickly integrate the notification bar, so we don't need to write any code about the notification bar, just
Write the layout on the line, so in the naming of the layout, the naming of some resource files and the id naming of some controls, it is necessary to make StarrySky match.

1. Layout file naming  

    The layout file of the small layout notification bar should be named view_notify_play.xml  
    The layout file of the big layout notification bar should be named view_notify_big_play.xml

2. Control id naming

    If you have the following buttons in your notification bar layout, you need to name them according to the following rules:
    
    | Notification bar control name  |   name  |
    | :--------     |   :----------   |
    | Play button       | img_notifyPlay    |
    | Pause button       | img_notifyPause    |
    | Stop button       | img_notifyStop    |
    | Play or pause button  | img_notifyPlayOrPause |
    | Next button     | img_notifyNext    |
    | Previous button     | img_notifyPre    |
    | Close button       | img_notifyClose    |
    | Like or favorite button  | img_notifyFavorite    |
    | Desktop lyrics button    | img_notifyLyrics    |
    | Download button       | img_notifyDownload    |
    | cover image       | img_notifyIcon    |
    | Song name TextView   | txt_notifySongName    |
    | artist TextView  | txt_notifyArtistName    |

3. Resource naming 

     For better UI effects, the resources used in the first, next, play, pause, play, or pause buttons on the notification bar in StarrySky are `selector`.
     Inside `selector` is your corresponding normal and pressed image.
     Because the last one and the next two buttons still need to determine whether there is a previous one and whether there is a next one, and there may be different styles when there is no previous and next one, such as graying.
     So there are some conventions for naming the image resources of these two buttons.
     Similarly, if you have the appropriate resources in your layout, please name them by convention.
    
    | Notification bar background color  | resources name  |   name  |
    | :-------- | :--------   | :------   |
    | Light background   | Play button selector | notify_btn_light_play_selector.xml | 
    | Light background   | Pause button selector | notify_btn_light_pause_selector.xml | 
    | Light background   | Next button selector | notify_btn_light_prev_selector.xml | 
    | Light background   | Previous button selector | notify_btn_light_prev_selector.xml | 
    | Light background   | The next button when there is no picture resource at the next one | notify_btn_light_next_pressed | 
    | Light background   | The previous button does not have the image resource of the previous one. | notify_btn_light_prev_pressed | 
    | Light background   | Like or favorite button image resources | notify_btn_light_favorite_normal | 
    | Light background   | Desktop lyrics button image resource | notify_btn_light_lyrics_normal | 
    | Dark background   | Play button selector | notify_btn_dark_play_selector.xml | 
    | Dark background   | Pause button selector | notify_btn_dark_pause_selector.xml | 
    | Dark background   | Next button selector | notify_btn_dark_next_selector.xml | 
    | Dark background  | Previous button selector | notify_btn_dark_prev_selector.xml | 
    | Dark background   | The next button when there is no picture resource at the next one  | notify_btn_dark_next_pressed | 
    | Dark background   | The previous button does not have the image resource of the previous one | notify_btn_dark_prev_pressed | 
    | Dark background   | Like or favorite button image resources | notify_btn_dark_favorite_normal | 
    | Dark background   | Desktop lyrics button image resource | notify_btn_dark_lyrics_normal | 
    | Light and Dark in common use   | Like or favorite button image resources  | notify_btn_favorite_checked | 
    | Light and Dark in common use    | Desktop lyrics button image resource | notify_btn_lyrics_checked | 
    | Light and Dark in common use    | Notification bar smallIcon Image Resources| icon_notification | 
    | Light and Dark in common use    | Download button | There is no provision for the time being, you can just name it. | 
    
   The layout of the custom notification bar, resources, etc., are all examples in the code. If you don't understand it, you can open the reference.

**third step**

The layout is done, because StarrySky creates the system notification bar by default, so the next step is to configure the NotificationConstructor:

```java
public class TestApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        //初始化
        MusicManager.initMusicManager(this);
        //配置通知栏
        NotificationConstructor constructor = new NotificationConstructor.Builder()
                .setCreateSystemNotification(false)
                .bulid();
        MusicManager.getInstance().setNotificationConstructor(constructor);
    }
}
```

At this point, the custom notification bar is integrated.

## Custom notification bar click event example

Sometimes we have to do some logic on the button click event in the notification bar, then we need to customize the click event, how to do it, here is an example:

Just like the large layout shown in the previous picture, suppose we now have to implement the playback pause, the previous one, the next one, the collection, the lyrics and so on.

**first step**

Create a notification bar to respond to the broadcast NotificationReceiver and define the Action for the corresponding button, then set it to the NotificationConstructor:

```java
public class TestApplication extends Application {

    public static String ACTION_PLAY_OR_PAUSE = "ACTION_PLAY_OR_PAUSE";
    public static String ACTION_NEXT = "ACTION_NEXT";
    public static String ACTION_PRE = "ACTION_PRE";
    public static String ACTION_FAVORITE = "ACTION_FAVORITE";
    public static String ACTION_LYRICS = "ACTION_LYRICS";

    private PendingIntent getPendingIntent(String action) {
        Intent intent = new Intent(action);
        intent.setClass(this, NotificationReceiver.class);
        return PendingIntent.getBroadcast(this, 0, intent, 0);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //初始化
        MusicManager.initMusicManager(this);
        //配置通知栏
        NotificationConstructor constructor = new NotificationConstructor.Builder()
                .setCreateSystemNotification(false)
                .setPlayOrPauseIntent(getPendingIntent(ACTION_PLAY_OR_PAUSE))
                .setNextIntent(getPendingIntent(ACTION_NEXT))
                .setPreIntent(getPendingIntent(ACTION_PRE))
                .setFavoriteIntent(getPendingIntent(ACTION_FAVORITE))
                .setLyricsIntent(getPendingIntent(ACTION_LYRICS))
                .bulid();
        MusicManager.getInstance().setNotificationConstructor(constructor);
    }
}
```

**Second step**

Implement the corresponding method in the broadcast:

```java
public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (TextUtils.isEmpty(action)) {
            return;
        }
        if (TestApplication.ACTION_PLAY_OR_PAUSE.equals(action)) {
            int state = MusicManager.getInstance().getState();
            if (state == PlaybackStateCompat.STATE_PLAYING) {
                MusicManager.getInstance().pauseMusic();
            } else {
                MusicManager.getInstance().playMusic();
            }
        }
        if (TestApplication.ACTION_NEXT.equals(action)) {
            MusicManager.getInstance().skipToNext();
        }
        if (TestApplication.ACTION_PRE.equals(action)) {
            MusicManager.getInstance().skipToPrevious();
        }
        if (TestApplication.ACTION_FAVORITE.equals(action)) {
            //Here to achieve their favorite or favorite logic, if selected can pass true to turn the button into the selected state, false is not selected
            MusicManager.getInstance().updateFavoriteUI(true);
        }
        if (TestApplication.ACTION_LYRICS.equals(action)) {
            //Here to achieve their own display lyrics logic, if selected can pass true to turn the button into the selected state, false is not selected
            MusicManager.getInstance().updateLyricsUI(true);
        }
    }
}
```

This will complete the custom notification bar click. Of course, don't forget to register for the broadcast.

