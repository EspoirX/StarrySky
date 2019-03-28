# A Powerful and Streamline MusicLibrary

[ ![](https://img.shields.io/badge/platform-android-green.svg) ](http://developer.android.com/index.html)
[ ![Download](https://api.bintray.com/packages/lizixian/StarrySky/StarrySkyJava/images/download.svg) ](https://bintray.com/lizixian/StarrySky/StarrySkyJava/_latestVersion)
[ ![](https://img.shields.io/badge/license-MIT-green.svg) ](http://choosealicense.com/licenses/mit/)

<img src="art/logo.jpg"/>

# StarrySky

`StarrySky` `MusicLibrary` `Music` `音频集成` 

[English Document](https://github.com/lizixian18/StarrySky/blob/StarrySkyJava/README-EN.md)

一个丰富的音乐播放封装库，针对快速集成音频播放功能，减少大家搬砖的时间，你值得拥有。

>MusicLibrary 改名为 StarrySky，
StarrySky 是一个全新的版本，基于媒体浏览器服务(MediaBrowserService)实现，
代码比原来的版本更加精简，注释更全，而且问题更少，欢迎大家继续使用。  

原来版本的代码在 master 分支上（master 分支不想维护了，如果还在用的话建议用新的）。


## 特点

- 轻松播放本地和网络音频
- 集成和调用API非常简单，音频功能几乎可以集成到一个语句中。
- 提供丰富的API方法来轻松实现各种功能。
- 方便集成自定义通知栏和系统通知栏。
- 使用ExoPlayer作为底层播放器。
- 支持多种音频格式并支持音频直播流(DASH, SmoothStreaming, HLS，rtmp，flac)。
- 支持边播边存功能，没网也能播。
- 支持改变播放速度
- 等等等等

若在使用中发现 Bug 或者有什么建议问题的可以在 issues 中提出或者添加 QQ 群交流，欢迎反馈。

## 集成
```groovy
dependencies {
    implementation fileTree(dir: 'libs', include: ['*.jar'])
    implementation 'com.lzx:StarrySkyJava:x.x.x'
}
```
x.x.x 填的是当前的版本号。


StarrySky 里面依赖的第三方库除了 ExoPlayer 之外还有 Glide:4.8.0，用于封面的下载，如果版本跟你项目中的版本有冲突，建议通过导入源码的
方式自己修改。

请使用 Java8。



## 使用文档

- [集成StarrySky](https://github.com/lizixian18/MusicLibrary/blob/StarrySkyJava/readme/%E9%9B%86%E6%88%90StarrySky.md)
- [StarrySky各种API功能](https://github.com/lizixian18/MusicLibrary/blob/StarrySkyJava/readme/StarrySky各种API功能.md)
- [快速集成通知栏](https://github.com/lizixian18/MusicLibrary/blob/StarrySkyJava/readme/快速集成通知栏.md)
- [媒体缓存功能](https://github.com/lizixian18/MusicLibrary/blob/StarrySkyJava/readme/媒体缓存功能.md)
- [Flac音频格式集成说明](https://github.com/lizixian18/MusicLibrary/blob/StarrySkyJava/readme/Flac格式集成说明.md)


PS：
- 如果有兴趣，建议稍微阅读一下源码，这样对使用或者解决问题有很大帮助。
- 如果发现库中功能满足不了你的需求，建议通过下载源码修改成你要的样子来使用。
- 如果该项目对你有所帮助，欢迎 star 或 fork，谢谢各位。

## 使用 demo
StarrySky 的 API 调用 demo 在代码里已经有体现，这里还写了个小 APP 来具体演示一下，写得并非很完善，主要作用是简单演示一下 StarrySky 的使用方法，
demo 代码请点击这里 [NiceMusic](https://github.com/lizixian18/NiceMusic)，下面是 demo 的截图：  
<a href="art/art1.png"><img src="art/art1.png" width="30%"/></a>
<a href="art/art2.png"><img src="art/art2.png" width="30%"/></a> 
<a href="art/art3.png"><img src="art/art3.png" width="30%"/></a>


## QQ群


<a href="art/qq_qun.jpg"><img src="art/qq_qun.jpg" width="30%"/></a>

<br><br>



你的打赏是我的动力，感谢每一位付出的人，愿意的话可以佛性打赏，我会非常开心。
<a href="art/biaoqing.gif"><img src="art/biaoqing.gif"/></a>

<a href="art/WechatIMG1.jpeg"><img src="art/WechatIMG1.jpeg" width="30%"/></a>


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
