# StarrySky 介绍

StarrySky 是一个音频集成库，鉴于在一些项目中如果需要集成音频播放功能的话，都离不开对播放器的封装，对播放控制的封装，对 API 的
封装等操作，其实这些操作在不同项目中都是大同小异的，所以 StarrySky 就是这样一个集成了播放音频所需的操作的一个库。

StarrySky 主打集成简单，代码小巧简单易读懂，扩展性强，使用方便等方向。

## StarrySky 结构图

<img src="https://raw.githubusercontent.com/lizixian18/MusicLibrary/StarrySkyJava/art/StarrySky结构图.png">

StarrySkyConfig：StarrySky 配置类，通过 builder 模式设置各种配置。

IMediaConnection：负责管理与 MusicService 的连接，以及处理各种回调，比如状态回调等操作。

MusicService：音频服务

PlayerControl：StarrySky 的 API 控制类几乎全部 API 都通过它来调用

IMediaSourceProvider：音频数据提供者，存放这音频数据列表，提供查找，添加等功能

PlaybackManager：播放管理类，PlayerControl 的调用最后会回调到这个类里面，处理播放前的一些逻辑操作，最终调用播放器方法播放。

MediaQueue：播放队列管理，主要作用是维护好当前播放的下标，根据下标当前播放的音频信息等。

Playback： 具体的播放器，默认使用 ExoPlayer，通过实现这个接口来自定义播放器

ICache：缓存管理，开关在 StarrySkyConfig 中打开，默认使用 ExoPlayer 的边播边存功能，用户可以实现这个接口来自定义缓存功能。

NotificationManager：通知栏管理，默认实现了两个通知栏，分别是系统通知栏和自定义通知栏，开关在 StarrySkyConfig 中打开，
同时用户可以在 StarrySkyConfig 中自定义自己的通知栏

以上的 IMediaConnection，PlayerControl，IMediaSourceProvider，PlaybackManager，MediaQueue，Playback，ICache，NotificationManager
 在 StarrySky 中都支持自定义实现。

## StarrySky 播放流程

<img src="https://raw.githubusercontent.com/lizixian18/MusicLibrary/StarrySkyJava/art/StarrySky播放流程.png">

 
如果问题请加群咨询。