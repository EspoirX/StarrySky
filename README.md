<p align="center">
<a href="art/logo.jpg"><img src="art/logo.jpg"/></a>
</p>

<p align="center">
<a href="http://developer.android.com/index.html"><img src="https://img.shields.io/badge/platform-android-green.svg"></a>
[ ![Download](https://api.bintray.com/packages/lizixian/MusicLibrary/MusicLibrary/images/download.svg) ](https://bintray.com/lizixian/MusicLibrary/MusicLibrary/_latestVersion)
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
- Supports multiple audio formats and supports audio streaming(DASH, SmoothStreaming, HLS.).
- Supports the playback progress
- Supports Cache while playing，no network can play.
- Supports changing the playing speed and changing the playing pitch.

## Version update record

 See details [Version update record](https://github.com/lizixian18/MusicLibrary/blob/master/readme/version.md)

## WorkPlan

- Supports Cache while playing [![](https://img.shields.io/badge/goal%20progress-100%25-brightgreen.svg)](https://github.com/lizixian18/MusicLibrary)
- Supports the playback progress [![](https://img.shields.io/badge/goal%20progress-100%25-brightgreen.svg)](https://github.com/lizixian18/MusicLibrary)
- Supports changing the playing speed and changing the playing pitch  [![](https://img.shields.io/badge/goal%20progress-100%25-brightgreen.svg)](https://github.com/lizixian18/MusicLibrary)

All current development plans have been completed. If you encounter any problems in use, welcome to feedback.

## Demo

Specific application Demo Please refer to [NiceMusic](https://github.com/lizixian18/NiceMusic)

## Usage

1.Import library

```java
allprojects {
    repositories {
        ...
        maven { url 'https://dl.bintray.com/lizixian/MusicLibrary' }
    }
}

dependencies {
    implementation 'com.lzx:MusicLibrary:1.3.3'
}
```

2. add MusicLibrary to your Application

```java
public class NiceMusicApplication extends Application {

    @Override
    public void onCreate() {
        if (!BaseUtil.getCurProcessName(this).contains(":musicLibrary")) {
             MusicLibrary musicLibrary = new MusicLibrary.Builder(this)
                               .build();
             musicLibrary.init();
        }
    }
}
```

**note**
1. Because the music service is running in the musicLibrary process, in the multi-process case, Application will create multiple times,Therefore, you need to add the above judgment to initialize in the non-musicLibrary process.
2. There are some parameters that can be configured during initialization:

- setAutoPlayNext(boolean autoPlayNext) Whether to play the next song automatically after playing the current song
- setUseMediaPlayer(boolean isUseMediaPlayer) Whether to use MediaPlayer
- setNotificationCreater(NotificationCreater creater) Notification bar configuration
- setCacheConfig(cacheConfig) Cache when playing configuration
- giveUpAudioFocusManager() Give up audio focus management, after give up, multiple audio will be mixed together


3. Simple to use (play a song):

```java
SongInfo songInfo = new SongInfo();
songInfo.setSongId("your song Id"); 
songInfo.setSongUrl("your song url"); 

MusicManager.get().playMusicByInfo(songInfo);
```

At least set songId and songUrl to play.To play audio in the local audio or assets folder, or streaming audio such as m3u8, just use set the songUrl and songId as usual.

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
