# StarrySky 介绍

StarrySky 是一个音频集成库，鉴于在一些项目中如果需要集成音频播放功能的话，都离不开对播放器的封装，对播放控制的封装，对 API 的
封装等操作，其实这些操作在不同项目中都是大同小异的，所以 StarrySky 就是这样一个集成了播放音频所需的操作的一个库。

StarrySky 主打集成简单，代码小巧简单易读懂，扩展性强，使用方便等方向。

StarrySky 总体来说可以分为几个模块：

1. API 控制模块
2. 音频数据存储模块
3. 播放队列管理模块
4. 播放器实现模块
5. 通知栏模块
6. 缓存模块

## StarrySky 包结构图

<img src="https://raw.githubusercontent.com/lizixian18/MusicLibrary/StarrySkyJava/art/starrysky包结构.png">

1. common 包里面主要是与 MusicService 端连接的管理类。
2. control 包主要是 API 的播放控制类。
3. ext 包主要是一些扩展类。
4. notification 主要是放通知栏相关的东西
5. playback 是播放相关的东西，里面分了几个包：
  （1）manager 是播放管理
  （2）offline 是缓存相关的东西
  （3）player 是播放器的实现
  （4）queue 是播放队列的管理
6. provider 包是放音频数据存储的相关东西。
7. registry 是一些组件的注册，主要用来配置
8. unils 就是一些工具类




## StarrySky 结构图
![](https://raw.githubusercontent.com/EspoirX/StarrySky/StarrySkyJava/art/StarrySky%E7%BB%93%E6%9E%84%E5%9B%BE.png)