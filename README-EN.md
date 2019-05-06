# A Powerful and Streamline MusicLibrary

[ ![](https://img.shields.io/badge/platform-android-green.svg) ](http://developer.android.com/index.html)
[ ![Download](https://api.bintray.com/packages/lizixian/StarrySky/StarrySkyJava/images/download.svg) ](https://bintray.com/lizixian/StarrySky/StarrySkyJava/_latestVersion)
[ ![](https://img.shields.io/badge/license-MIT-green.svg) ](http://choosealicense.com/licenses/mit/)

<img src="art/logo.jpg"/>

# StarrySky

`StarrySky` `MusicLibrary` `Music` `Audio integration`

A rich music player package library for fast integrated audio playback, reducing the time you have to "move bricks", you deserve it.

>MusicLibrary changed its name to StarrySky, because the MusicLibrary name is a bit old-fashioned, and all think of a fashionable name.
 StarrySky is a new version based on the MediaBrowserService implementation.
 The code is more streamlined, the comments are more complete, and there are fewer problems. You are welcome to continue using it.。  

The original version of the code is on the master branch.


## Characteristics

- Easily play local and network audio
- Integrating and calling APIs is very simple, and audio functions can be integrated into almost one statement.
- Provide a rich API method to easily implement various functions.
- Convenient integration of custom notification bars and system notification bars.
- Use ExoPlayer as the underlying player.
- Support multiple audio formats and support audio live streaming (DASH, SmoothStreaming, HLS, rtmp).
- Support side-by-side storage function, no network can also broadcast.
- Support to change the playback speed
- Etc., etc

If you find bugs in use or have any suggestions, you can suggest or add QQ group communication in the issues. Welcome feedback.

## Import
```groovy
dependencies {
    implementation fileTree(dir: 'libs', include: ['*.jar'])
    implementation 'com.lzx:StarrySkyJava:x.x.x'
}
```
x.x.x is the current version number。


Please use Java8.



## Documents

- [Initialize StarrySky](https://github.com/lizixian18/MusicLibrary/blob/StarrySkyJava/readme/%E9%9B%86%E6%88%90StarrySky-EN.md)
- [StarrySky API](https://github.com/lizixian18/MusicLibrary/blob/StarrySkyJava/readme/StarrySky各种API功能-EN.md)
- [Quickly integrate notification bar](https://github.com/lizixian18/MusicLibrary/blob/StarrySkyJava/readme/快速集成通知栏-EN.md)
- [Media caching](https://github.com/lizixian18/MusicLibrary/blob/StarrySkyJava/readme/媒体缓存功能-EN.md)


PS：
- If you are interested, it is recommended to read the source code a little bit, which is very helpful for using or solving the problem.
- If you find that the features in the library can't meet your needs, it is recommended to modify the source code to use it as you want.
- If the project is helpful to you, welcome star or fork, thank you.

<a href="art/qq_qun.jpg"><img src="art/qq_qun.jpg" width="30%"/></a>

<br><br>


## About me

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
