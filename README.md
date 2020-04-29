# A Powerful and Streamline MusicLibrary

[ ![](https://img.shields.io/badge/platform-android-green.svg) ](http://developer.android.com/index.html)
[ ![Download](https://api.bintray.com/packages/lizixian/StarrySky/StarrySkyKt/images/download.svg)](https://bintray.com/lizixian/StarrySky/StarrySkyKt/_latestVersion)
[ ![](https://img.shields.io/badge/license-MIT-green.svg) ](http://choosealicense.com/licenses/mit/)

<a href="art/logo.jpg"><img src="art/logo.jpg" /></a>
<a href="art/a4074094959_10.jpg"><img src="art/a4074094959_10.jpg"/></a>

# StarrySky

`StarrySky` `MusicLibrary` `Music` `音频集成` 


一个丰富的音乐播放封装库，针对快速集成音频播放功能，减少大家搬砖的时间，你值得拥有。


## 特点

- 轻松播放本地和网络音频
- 集成和调用API非常简单，音频功能几乎可以集成到一个语句中。
- 提供丰富的API方法来轻松实现各种功能。
- 方便集成自定义通知栏和系统通知栏。
- 使用 ExoPlayer 作为底层播放器，但支持自定义实现
- 基于 ExoPlayer 支持多种普通音频格式并支持多种流式音频格式(DASH, SmoothStreaming, HLS，rtmp，flac)。
- 支持边播边存功能，没网也能播。支持自定义缓存实现。
- 支持改变播放速度。
- 几乎所有组件都支持自定义实现，高度的扩展性。
- 等等等等

若在使用中发现 Bug 或者有什么建议问题的可以在 issues 中提出或者添加 QQ 群交流，欢迎反馈。

## 集成
```groovy
dependencies {
    implementation fileTree(dir: 'libs', include: ['*.jar'])
    implementation 'com.lzx:StarrySkyKt:x.x.x'
}
```

## androidX 版本集成
```groovy
dependencies {
    implementation fileTree(dir: 'libs', include: ['*.jar'])
    implementation 'com.lzx:StarrySkyX:x.x.x'
}
```

x.x.x 填的是当前的版本号。


请使用 Java8。

如果导入不了可以试试加上这个：
```groovy
maven{
    url "https://dl.bintray.com/lizixian/StarrySky/"
}
```

## 按需导入

若要支持 dash 类流音频，请另外导入
```groovy
dependencies {
    implementation 'com.google.android.exoplayer:exoplayer-dash:2.10.4'
}
```

若要支持 hls 类流音频，请另外导入
```groovy
dependencies {
    implementation 'com.google.android.exoplayer:exoplayer-hls:2.10.4'
}
```

若要支持 smoothstreaming 类流音频，请另外导入
```groovy
dependencies {
    implementation 'com.google.android.exoplayer:exoplayer-smoothstreaming:2.10.4'
}
```

若要支持 rtmp 类流音频，请另外导入
```groovy
dependencies {
    implementation 'com.google.android.exoplayer:extension-rtmp:2.10.4'
}
```

若不知道要导入哪一种，可以在播放时抛出的异常崩溃中根据异常信息提示导入。

## 初始化

下面是最简单的初始化以及播放音频代码，更多功能请阅读使用文档。

```java
public class TestApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        StarrySky.init(this);
    }
}

//播放一首歌曲
SongInfo info = new SongInfo();
info.setSongId("111");
info.setSongUrl("http://music.163.com/song/media/outer/url?id=317151.mp3");
StarrySky.with().playMusicByInfo(info);
```

## 使用文档

- [版本更新记录](https://github.com/lizixian18/MusicLibrary/blob/StarrySkyJava/readme/version.md)
- [StarrySky介绍](https://github.com/lizixian18/MusicLibrary/blob/StarrySkyJava/readme/StarrySky介绍.md)
- [StarrySky各种API功能](https://github.com/lizixian18/MusicLibrary/blob/StarrySkyJava/readme/StarrySky各种API功能.md)
- [自定义实现图片加载器](https://github.com/lizixian18/MusicLibrary/blob/StarrySkyJava/readme/自定义图片加载器说明.md)
- [快速集成通知栏](https://github.com/lizixian18/MusicLibrary/blob/StarrySkyJava/readme/快速集成通知栏.md)
- [拦截器](https://github.com/lizixian18/MusicLibrary/blob/StarrySkyJava/readme/拦截器.md)
- [媒体缓存功能](https://github.com/lizixian18/MusicLibrary/blob/StarrySkyJava/readme/媒体缓存功能.md)
- [自定义播放器说明](https://github.com/lizixian18/MusicLibrary/blob/StarrySkyJava/readme/自定义播放器说明.md)
- [自定义播放队列管理说明](https://github.com/lizixian18/MusicLibrary/blob/StarrySkyJava/readme/自定义播放队列管理说明.md)
- [自定义音频数据提供器说明](https://github.com/lizixian18/MusicLibrary/blob/StarrySkyJava/readme/自定义音频数据提供器说明.md)
- [Flac音频格式集成说明](https://github.com/lizixian18/MusicLibrary/blob/StarrySkyJava/readme/Flac格式集成说明.md)


PS：
- 如果有兴趣，建议稍微阅读一下源码，这样对使用或者解决问题有很大帮助。
- 如果发现库中功能满足不了你的需求，建议通过下载源码修改成你要的样子来使用。
- 如果该项目对你有所帮助，欢迎 star 或 fork，谢谢各位。

## 成功案例

StarrySky 目前为止有 **8100+** 的下载量，感谢各位开发者的支持，下面是部分 app 成功案例。

<a href="art/成功案例.png"><img src="art/成功案例.png"/></a>

（还有很多 App，只不过没要到 😂 。如有违法或者侵权行为请联系我删除！）


## QQ群（929420228）

<a href="art/qq_qun.jpg"><img src="art/qq_qun.jpg" width="30%"/></a>

<br><br>

你的打赏是我改 Bug 的动力
<a href="art/biaoqing.gif"><img src="art/biaoqing.gif"/></a>

<a href="art/WechatIMG1.jpeg"><img src="art/WechatIMG1.jpeg" width="30%"/></a>


## 关于我

An android developer in GuangZhou

掘金：[https://juejin.im/user/5861c3bb128fe10069e69f0a](https://juejin.im/user/5861c3bb128fe10069e69f0a)

语雀：[https://www.yuque.com/espoir](https://www.yuque.com/espoir)

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
