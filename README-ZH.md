<p align="center">
<a href="art/logo.jpg"><img src="art/logo.jpg"/></a>
</p>

<p align="center">
<a href="http://developer.android.com/index.html"><img src="https://img.shields.io/badge/platform-android-green.svg"></a>
<a href="https://jitpack.io/#lizixian18/MusicLibrary"><img src="https://jitpack.io/v/lizixian18/MusicLibrary.svg"></a>
<a href="http://choosealicense.com/licenses/mit/"><img src="https://img.shields.io/badge/license-MIT-green.svg"></a>
</p>

# MusicLibrary

一个丰富的音乐播放封装库，针对快速集成音频播放功能。  

## 特点

- 轻松播放本地和网络音频
- 基于IPC实现音频服务，减少应用内存峰值，避免OOM。
- 集成和调用API非常简单，音频功能几乎可以集成到一个语句中。
- 提供丰富的API方法来轻松实现各种功能。
- 用一句话，您可以自定义通知栏和系统通知栏来自定义通知栏的控件。
- 集成MediaPlayer和ExoPlayer播放器，可自由切换
- 支持多种音频格式并支持音频直播流(DASH, SmoothStreaming, HLS.)。
- 支持保存播放进度。
- 支持边播边存功能，没网也能播。

## 开发计划

- 支持边听边存 [![](https://img.shields.io/badge/goal%20progress-100%25-brightgreen.svg)](https://github.com/lizixian18/MusicLibrary)
- 支持保存播放进度 [![](https://img.shields.io/badge/goal%20progress-100%25-brightgreen.svg)](https://github.com/lizixian18/MusicLibrary)
- 支持变速 [![](https://img.shields.io/badge/goal%20progress-0%25-brightgreen.svg)](https://github.com/lizixian18/MusicLibrary)


## 使用例子

具体应用请参考 [NiceMusic](https://github.com/lizixian18/NiceMusic)

## 使用方式

1.导入library

```java
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}

dependencies {
     compile 'com.github.lizixian18:MusicLibrary:v1.2.7'
}
```

2. 添加 MusicLibrary 到你的 Application 中

```java
public class NiceMusicApplication extends Application {

    @Override
    public void onCreate() {
        if (!BaseUtil.getCurProcessName(this).contains(":musicLibrary")) {
            MusicManager.get().setContext(this).init();
        }
    }
}
```

**说明**
1. 一定要调用 setContext 设置上下文，否则会报错。 
2. 因为音乐服务是运行在 musicLibrary 进程里面的，多进程的情况下，Application 会创建多次，所以需要加上以上判断，在非 musicLibrary 进程里面初始化。
3. 初始化的时候还有一些参数可以配置：  

- setAutoPlayNext(boolean autoPlayNext) 是否在播放完当前歌曲后自动播放下一首
- setUseMediaPlayer(boolean isUseMediaPlayer) 是否使用 MediaPlayer
- setNotificationCreater(NotificationCreater creater) 通知栏配置
 
（通过 MusicManager 去调用 lib 中所有的 api ，静态方法可以直接调用，非静态方法需要通过 MusicManager.get() 去调用。）
  

3. 简单应用 (播放一首音乐):

```java
SongInfo songInfo = new SongInfo();
songInfo.setSongId("your song Id"); 
songInfo.setSongUrl("your song url"); 

MusicManager.get().playMusicByInfo(songInfo);
```

最少要设置 songId 和 songUrl 才能播放。若要播放本地音频或者 assets 文件夹下的音频，或者 m3u8 等流式音频，用法一样，只要设置对 songUrl 和 songId 就行。

  
## 文档
  
1. Model字典
 
   详细见 [Model字典说明](https://github.com/lizixian18/MusicLibrary/blob/master/readme/model.md)
   
2. 播放器API
   
   详细见 [API说明](https://github.com/lizixian18/MusicLibrary/blob/master/readme/api.md)
 
3. 通知栏集成

   详细见 [通知栏集成](https://github.com/lizixian18/MusicLibrary/blob/master/readme/notification.md)

4. 代码实现以及原理
  
   详细见 [代码实现以及原理](https://github.com/lizixian18/MusicLibrary/blob/master/readme/principle.md)


<br><br>

PS：
- 如果你有想法或者意见和建议，欢迎提issue，喜欢点个star。欢迎各位大佬指点指点。

<br><br>


## 关于我

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