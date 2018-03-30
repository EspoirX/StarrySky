<a href="art/logo.jpg"><img src="art/logo.jpg" width="100%"/></a>

<p align="center">
<a href="http://developer.android.com/index.html"><img src="https://img.shields.io/badge/platform-android-green.svg"></a>
<a href="https://jitpack.io/#lizixian18/MusicLibrary"><img src="https://jitpack.io/v/lizixian18/MusicLibrary.svg"></a>
<a href="http://choosealicense.com/licenses/mit/"><img src="https://img.shields.io/badge/license-MIT-green.svg"></a>
</p>

# MusicLibrary

一个比较完善的音乐播放封装库，针对快速集成音频播放功能。  

[中文文档](https://github.com/lizixian18/MusicLibrary/blob/master/README-ZH.md)

## Features

- Implement audio services based on IPC, reduce app memory peaks, and avoid OOM.
- Integrating and calling APIs is very simple, and audio functions can be integrated in almost one sentence.
- Provides rich API methods to easily implement various functions.
- In one sentence, you can customize the notification bar and the system notification bar to customize the control of the notification bar.
- Integrated MediaPlayer and ExoPlayer Players, Freely Switchable
- Supports multiple audio formats and supports audio streaming.

## WorkPlan

- 支持边听边存
- 支持保存播放进度
- 支持变速
 

## Demo

Specific application Demo Please refer to [NiceMusic](https://github.com/lizixian18/NiceMusic)

## Usage

1.Import library

```java
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}

dependencies {
     compile 'com.github.lizixian18:MusicLibrary:v1.2.5'
}
```

2. add MusicLibrary to your Application

```java
if (!BaseUtil.getCurProcessName(this).contains(":musicLibrary")) {
    MusicManager.get().setContext(this).init();
}
```
**note**
1. Be sure to call setContext to set the context, otherwise it will report an error. 
2. Because the music service is running in the musicLibrary process, in the multi-process case, Application will create multiple times,Therefore, you need to add the above judgment to initialize in the non-musicLibrary process.
3. There are some parameters that can be configured during initialization:

- setAutoPlayNext(boolean autoPlayNext) Whether to play the next song automatically after playing the current song
- setUseMediaPlayer(boolean isUseMediaPlayer) Whether to use MediaPlayer
- setNotificationCreater(NotificationCreater creater) Notification bar configuration

（All the apis in lib are called by the MusicManager. The static methods can be called directly.Non-static methods need to be called via MusicManager.get() .） 
  
3. Simple to use (play a song):

```java
SongInfo songInfo = new SongInfo();
songInfo.setSongId("your song Id"); 
songInfo.setSongUrl("your song url"); 

AlbumInfo albumInfo = new AlbumInfo();
albumInfo.setAlbumName("your album name");
albumInfo.setAlbumCover("your album cover");

songInfo.setAlbumInfo(albumInfo);

MusicManager.get().playMusicByInfo(songInfo);
```

## Wiki

1. MusicLibrary Model 
 
   See details [MusicLibrary Model Description](https://github.com/lizixian18/MusicLibrary/blob/master/readme/model.md)
   
2. MusicManager API
   
   See details [API Description](https://github.com/lizixian18/MusicLibrary/blob/master/readme/api.md)
 
3. Notification bar integration

   See details [Notification Description](https://github.com/lizixian18/MusicLibrary/blob/master/readme/notification.md)

4. Code implementation and principle
  
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
