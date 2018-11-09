<p align="center">
<a href="art/logo.jpg"><img src="art/logo.jpg"/></a>
</p>

<p align="center">
<a href="http://developer.android.com/index.html"><img src="https://img.shields.io/badge/platform-android-green.svg"></a>
<a href="https://bintray.com/lizixian/MusicLibrary/MusicLibrary/_latestVersion"><img src="https://api.bintray.com/packages/lizixian/MusicLibrary/MusicLibrary/images/download.svg"></a>
<a href="http://choosealicense.com/licenses/mit/"><img src="https://img.shields.io/badge/license-MIT-green.svg"></a>
</p>

# MusicLibrary

一个丰富的音乐播放封装库，针对快速集成音频播放功能，你值得拥有。 

[中文文档](https://github.com/lizixian18/MusicLibrary/blob/master/README-ZH.md)

## Features

- Easily play local and web audio.
- Implement audio services based on IPC, reduce app memory peaks, and avoid OOM.
- Integrating and calling APIs is very simple, and audio functions can be integrated in almost one sentence.
- Provides rich API methods to easily implement various functions.
- In one sentence, you can customize the notification bar and the system notification bar to customize the control of the notification bar.
- Integrated MediaPlayer and ExoPlayer Players, Freely Switchable
- Supports multiple audio formats and supports audio streaming(DASH, SmoothStreaming, HLS，rtmp.).
- Supports the playback progress
- Supports Cache while playing，no network can play.
- Supports changing the playing speed and changing the playing pitch.

## Version update record

See details [Version update record](https://github.com/lizixian18/MusicLibrary/blob/master/readme/version.md)

## project status

The project is basically stable and has been practically used in a number of commercial projects. The current status is to repair the bugs found and to satisfy the requirements raised in the issues. If you encounter any problems in use, welcome feedback.

## Demo

Specific application Demo Please refer to [NiceMusic](https://github.com/lizixian18/NiceMusic)

## Usage

1.Import library

```java
allprojects {
    repositories {
        maven{url 'https://dl.bintray.com/lizixian/MusicLibrary'}
    }
}

dependencies {
    implementation 'com.lzx:MusicLibrary:1.4.4'
}
```

If your appcompat-v7 package is using 27+, then you need an extra reference to support-media-compat. For example:

```java
implementation 'com.android.support:support-media-compat:27.1.1'
```

If you find that you still can't find the related class's crash after the reference, please check if the same package exists but the version is different and cause conflict.

2. add MusicLibrary to your Application

```java
public class NiceMusicApplication extends Application {

    @Override
    public void onCreate() {
        if (BaseUtil.getCurProcessName(this).equals("your package name")) {
            MusicLibrary musicLibrary = new MusicLibrary.Builder(this)
                              .build();
            musicLibrary.init();
        }
    }
}
```

**note**
1. Because the music service is running in the musicLibrary process, in the multi-process case, Application will create multiple times, so you need to add the above judgment in the initialization, initialized in your main process.
2. There are some parameters that can be configured during initialization:

- setAutoPlayNext(boolean autoPlayNext) Whether to play the next song automatically after playing the current song
- setUseMediaPlayer(boolean isUseMediaPlayer) Whether to use MediaPlayer
- setNotificationCreater(NotificationCreater creater) Notification bar configuration
- setCacheConfig(cacheConfig) Cache when playing configuration
- giveUpAudioFocusManager() Give up audio focus management, after give up, multiple audio will be mixed together

for example：

```java
//Notification configuration
NotificationCreater creater = new NotificationCreater.Builder()
        .setTargetClass("com.lzx.nicemusic.module.main.HomeActivity")
        .setCreateSystemNotification(true)
        .setNotificationCanClearBySystemBtn(true)
        .setSystemNotificationShowTime(true)
        .setPendingIntentMode(PendingIntentMode.MODE_ACTIVITY)
        .build();

//边播边存配置
CacheConfig cacheConfig = new CacheConfig.Builder()
        .setOpenCacheWhenPlaying(true)
        .setCachePath(CacheUtils.getStorageDirectoryPath() + "/NiceMusic/Cache/")
        .build();

MusicLibrary musicLibrary = new MusicLibrary.Builder(this)
        .setNotificationCreater(creater)
        .setCacheConfig(cacheConfig)
        .setUseMediaPlayer(false)
        .build();
musicLibrary.init();
```

3. Configuration AndroidManifest.xml

In order to allow the user to decide whether to open the multi-process to use the library, the configuration of the manifest file is given to the user, as follows:


```xml
<!--MusicService-->
<service
    android:name="com.lzx.musiclibrary.MusicService"
    android:exported="true"
    android:process=":MusicLibrary" />
<!--Wire control related-->
<receiver
    android:name="com.lzx.musiclibrary.receiver.RemoteControlReceiver"
    android:exported="true"
    android:process=":MusicLibrary">
    <intent-filter>
        <action android:name="android.intent.action.MEDIA_BUTTON" />
    </intent-filter>
</receiver>
<!--Notification bar event related-->
<receiver
    android:name="com.lzx.musiclibrary.receiver.PlayerReceiver"
    android:exported="true"
    android:process=":MusicLibrary">
    <intent-filter>
        <action android:name="com.lzx.nicemusic.close" />
        <action android:name="com.lzx.nicemusic.play_pause" />
        <action android:name="com.lzx.nicemusic.prev" />
        <action android:name="com.lzx.nicemusic.next" />
    </intent-filter>
</receiver>
```
The default is to use multi-process, if you do not want to use, remove android:process=":MusicLibrary", where MusicLibrary is the name of the process, you can define it yourself.


**other instructions**
If you use the System.exit(0); method to exit the APP, you may need to call the ActivityManager#killBackgroundProcesses method again.
Kill the audio process, otherwise it may report a crash, so try not to do it.

3. Simple to use (play a song):

```java
SongInfo songInfo = new SongInfo();
songInfo.setSongId("your song Id"); 
songInfo.setSongUrl("your song url"); 

MusicManager.get().playMusicByInfo(songInfo);
```

At least set songId and songUrl to play.To play audio in the local audio or assets folder, or streaming audio such as m3u8, just use set the songUrl and songId as usual.

## Function list

1. Play audio
2. Pause audio
3. Stop audio
4. Resume playback after pause
5. Timed playback
6. Get the current playback subscript
7. Get the current playlist
8. Set the current playlist
9. Delete a message from the playlist
10. Get the playback status
11. Get the audio duration
12. Play the next song
13. Play the previous one
14. Determine if there is a previous one
15. Determine if there is a next one
16. Get the previous message
17. Get the next message
18. Get the current playback information
19. Set current audio information
20. Set the play mode
21. Get the playback mode
22. Get current progress
23. Target to the specified location
24. Get the audio SessionId
25. Get the playback speed
26. Get the playback tone
27. Initialization
27. Side-by-side storage configuration
29. Player selection
30. Configure a custom notification bar
31. Configure the system notification bar
32. Close the notification bar
33. Update notification bar
34. Variable speed
35. Get buffer progress
36. Set the volume
37. Register/unregister a play status listener
38. Register/unregister a timed play listener
39. ...

## Wiki

1. MusicLibrary Model 
 
   See details [MusicLibrary Model Description](https://github.com/lizixian18/MusicLibrary/blob/master/readme/model.md)
   
2. MusicManager API
   
   See details [API Description](https://github.com/lizixian18/MusicLibrary/blob/master/readme/api.md)
 
3. Notification bar integration

   See details [Notification Description](https://github.com/lizixian18/MusicLibrary/blob/master/readme/notification.md)

4. Cache when playing configuration instructions
 
   See details [Cache when playing Description](https://github.com/lizixian18/MusicLibrary/blob/master/readme/playcache.md)

5. Code implementation and principle
  
   See details [Code implementation and principle](https://github.com/lizixian18/MusicLibrary/blob/master/readme/principle.md)


<br><br>

PS：
- If you have ideas or opinions and suggestions, please feel free to ask for an issue and like to have a star. Welcome everybody to give pointers.

<a href="art/qq_qun.jpg"><img src="art/qq_qun.jpg" width="30%"/></a>

<br><br>

##  About me

An android developer in GuangZhou  
简书：[http://www.jianshu.com/users/286f9ad9c417/latest_articles](http://www.jianshu.com/users/286f9ad9c417/latest_articles)   
Email:386707112@qq.com  
If you want to make friends with me, You can give me a Email and follow me。


## License

```
MIT License

Copyright (c) [2018] [lizixian]

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
