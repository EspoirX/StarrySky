# A Powerful and Streamline MusicLibrary

[ ![](https://img.shields.io/badge/platform-android-green.svg) ](http://developer.android.com/index.html)
[![](https://jitpack.io/v/EspoirX/StarrySky.svg)](https://jitpack.io/#EspoirX/StarrySky)
[ ![](https://img.shields.io/badge/license-MIT-green.svg) ](http://choosealicense.com/licenses/mit/)
 

一个丰富，舒服的音乐播放封装库，针对快速集成音频播放功能，减少大家搬砖的时间，你值得拥有。


## 特点

- 上百个 API，满足你一切播放欲望。
- 只需要正确的播放地址，即可轻松播放本地和网络音频。
- 集成和调用API非常简单，音频功能几乎可以集成到一个语句中。
- 提供丰富的API方法来轻松实现各种功能。
- 一句话集成自定义通知栏和系统通知栏，支持通知栏自由切换。
- 使用 ExoPlayer 作为默认底层播放器，但支持自定义实现。
- 基于 ExoPlayer 支持多种普通音频格式并支持多种流式音频格式(DASH, SmoothStreaming, HLS，rtmp，flac)。
- 支持边播边存功能，没网也能播。支持自定义缓存实现。
- 通知栏，缓存，播放器等都支持自定义实现，高度的扩展性。
- 支持 SoundPool 使用，支持均衡器音效，操作简单。
- 等等等等

若在使用中发现 Bug 或者有什么建议问题的可以在 issues 中提出或者添加 QQ 群交流，欢迎反馈。
（七牛账号欠费没钱了，demo的音乐都播放不了，但是功能是没问题的，哈哈哈）

## 集成
 
```groovy
dependencies {
    implementation fileTree(dir: 'libs', include: ['*.jar'])
    implementation 'com.github.EspoirX:StarrySky:vX.X.X'
}
```
[![](https://jitpack.io/v/EspoirX/StarrySky.svg)](https://jitpack.io/#EspoirX/StarrySky)
  
如果导入不了可以试试加上这个：
```groovy
maven { url 'https://jitpack.io' }
```

## 按需导入
因为目前默认播放器 ExoPlayer 使用的是 2.14.1 版本，所以以下依赖都使用相同版本

若要支持 dash 类流音频，请另外导入
```groovy
dependencies {
   implementation 'com.google.android.exoplayer:exoplayer-dash:2.14.1'
}
```

若要支持 hls 类流音频，请另外导入
```groovy
dependencies {
    implementation 'com.google.android.exoplayer:exoplayer-hls:2.14.1'
}
```

若要支持 smoothstreaming 类流音频，请另外导入
```groovy
dependencies {
    implementation 'com.google.android.exoplayer:exoplayer-smoothstreaming:2.14.1'
}
```

若要支持 rtmp 类流音频，请另外导入
```groovy
dependencies {
    implementation 'com.google.android.exoplayer:extension-rtmp:2.14.1'
}

若要支持 rtsp 类流音频，请另外导入
```groovy
dependencies {
    implementation 'com.google.android.exoplayer:exoplayer-rtsp:2.14.1'
}
```

若需支持 flac 无损音频，可直接播放，已经支持。

flac 音频特别说明：  
ExoPlayer 要播放 flac 音频，是需要自己编译 so 的，具体怎么操作可以看 ExoPlayer 的 github，本项目已经把编译好的代码放在了 extension-flac2120 
这个 module 里面，2120 代表 版本号是 2.12.0。

若不知道要导入哪一种，可以在播放时抛出的异常崩溃中根据异常信息提示导入。

### 4.4 机器上如果报 MediaButtonReceiver 的崩溃，可以尝试注册下广播
```xml
<receiver android:name="android.support.v4.media.session.MediaButtonReceiver">
    <intent-filter>
        <action android:name="android.intent.action.MEDIA_BUTTON" />
    </intent-filter>
</receiver>
```

### 4.5 一些网友反馈的问题和解决办法
1. 当项目里存在GSY播放器时，或者说别的库已经引入了ExoPlayer的时候，首次播放音频会造成崩溃，后续播放无问题，而且是只有打包了之后的正式包会存在这种情况，AS直接运行时没有这个问题。
解决办法：在 gradle.properties 中加入 android.enableDexingArtifactTransfrom=false 即可。

 
PS：
- 如果有兴趣，建议稍微阅读一下源码，这样对使用或者解决问题有很大帮助。
- 如果发现库中功能满足不了你的需求，建议通过下载源码修改成你要的样子来使用。
- 如果该项目对你有所帮助，欢迎 star 或 fork，谢谢各位。

## QQ群（929420228）

<a target="_blank" href="https://qm.qq.com/cgi-bin/qm/qr?k=1mxC3aClBm7IoynoMi6Faz1YMwwrxaMq&jump_from=webapi"><img border="0" src="https://pub.idqqimg.com/wpa/images/group.png" alt="StarrySky交流和反馈" title="StarrySky交流和反馈"></a>
<br><br>

你的打赏是我改 Bug 的动力
<a href="#"><img src="https://github.com/EspoirX/StarrySky/blob/androidx/art/biaoqing.gif?raw=true"/></a>

<a href="#"><img src="https://github.com/EspoirX/StarrySky/blob/androidx/art/WechatIMG1.jpeg?raw=true" width="30%"/></a>


## 关于我

An android developer in GuangZhou

掘金：[https://juejin.im/user/5861c3bb128fe10069e69f0a](https://juejin.im/user/5861c3bb128fe10069e69f0a)

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
