# StarrySky 介绍

StarrySky 是一个音频集成库，鉴于在一些项目中如果需要集成音频播放功能的话，都离不开对播放器的封装，对播放控制的封装，对 API 的
封装等操作，其实这些操作在不同项目中都是大同小异的，所以 StarrySky 就是这样一个集成了播放音频所需的操作的一个库。

StarrySky 主打集成简单，代码小巧简单易读懂，扩展性强，使用方便等方向。



## StarrySky 播放流程图：

<img src="https://github.com/EspoirX/StarrySky/raw/androidx/art/StarrySky流程图.png">

StarrySky 播放流程图如上，可以看到大概分为几个部分。

| 编号      | 模块           |    作用                                    |
|:-------  | :--------      | :--------                                  |
|    1     | StarrySky      | 操作类，主要负责初始化等操作                   |
|    2     | PlayerControl  | 播放控制，里面集成了大部分库的 api             |
|    3     | Interceptor    | 拦截器，如果有什么操作需要播放前做，可通过它实现 |
|    4     | PlayerManager  | 播放管理类，负责播放队列，播放模式，通知栏等操作 |
|    5     | MediaQueue     | 播放队列管理                                 |
|    6     | SourceProvider | 数据存储类，负责保存传进去的音频数据            |
|    7     | Notification   | 通知栏，运行在 Service                       |
|    8     | Playback       | 播放器，运行在 Service                       |
|    9     | Cache          | 播放缓存，运行在 Service                     |
|    10    | ImageLoader    | 图片加载，主要作用于通知栏音频封面的加载        |


## StarrySky API 一览表

StarrySky 类 API:

| 编号      | API                   |    作用                           |
|:-------  | :--------              | :--------                        |
|    1     | with                   | 获取 PlayerControl 实例，操作 API  |
|    2     | soundPool              | 获取 soundPool 操作               |
|    3     | changeNotification     | 切换系统和自定义通知栏              |
|    4     | closeNotification      | 关闭通知栏                         |
|    5     | setIsOpenNotification  | 控制是否打开通知栏                  |
|    6     | getNotificationType    | 获取当前通知栏类型                  |
|    7     | isOpenCache            | 是否打开了缓存开关                  |
|    8     | getPlayerCache         | 获取播放缓存类                     |
|    9     | bindService            | 绑定服务                          |
|    10    | unBindService          | 解邦服务                          |

具体详细注释，参数和实现请查看源代码或者参考 demo。

PlayerControl 类 API:

通过 StarrySky.with() 调用

| 编号      | API                       |    作用                           | 编号      | API                       |    作用                           |
|:-------  | :--------                  | :--------                        |:-------  | :--------                  | :--------                        |
|    1     | playMusicById              | 根据 songId 播放                  |  31    | getNowPlayingSongUrl       | 获取当前播放的歌曲url                 |
|    2     | playMusicByUrl             | 根据 songUrl 播放                 |  32    | getNowPlayingIndex         | 获取当前播放歌曲的下标                 |
|    3     | playMusicByInfo            | 根据 SongInfo 播放                |   33    | getBufferedPosition        | 获取缓存位置 毫秒为单位                |
|    4     | playMusic                  | 播放，传入播放列表和下标            | 34    | getPlayingPosition         | 获取播放位置 毫秒为单位                |
|    5     | addInterceptor             | 添加拦截器                        |   35    | isSkipToNextEnabled        | 是否有下一首                          |
|    6     | pauseMusic                 | 暂停                              | 36    | isSkipToPreviousEnabled    | 是否有上一首                          |
|    7     | restoreMusic               | 恢复播放（暂停后恢复）              |  37    | getPlaybackSpeed           | 获取播放速度                          |
|    8     | stopMusic                  | 停止播放                           |  38    | isPlaying                  | 是否在播放                            |
|    9     | prepare                    | 准备播放，准备的是队列当前下标的音频  |  39    | isPaused                   | 是否暂停                              |
|    11    | prepareById                | 根据 songId 准备                   |  40    | isIdea                     | 是否空闲                              |
|    12    | prepareByUrl               | 根据 songUrl 准备                  |  41    | isBuffering                | 是否缓冲                              |
|    13    | prepareByInfo              | 根据 SongInfo 准备                 |   42    | isCurrMusicIsPlayingMusic  | 判断传入的音乐是不是正在播放的音乐       |
|    14    | skipToNext                 | 下一首                             |  43    | isCurrMusicIsPlaying       | 判断传入的音乐是否正在播放              |
|    15    | skipToPrevious             | 上一首                             |  44    | isCurrMusicIsPaused        | 判断传入的音乐是否正在暂停              |
|    16    | fastForward                | 快进 每调一次加 speed 倍            |   45    | isCurrMusicIsIdea          | 判断传入的音乐是否空闲                 |
|    17    | rewind                     | 快退 每调一次减 speed 倍            |  46    | isCurrMusicIsBuffering     | 判断传入的音乐是否缓冲                  |
|    18    | onDerailleur               | 配置任意倍速                        |  47    | setVolume                  | 设置音量, 范围 0到1                   |
|    19    | seekTo                     | 移动到媒体流中的新位置,以毫秒为单位   |  48    | getVolume                  | 获取音量                              |
|    20    | setRepeatMode              | 设置播放模式                        |  49    | getDuration                | 获取媒体时长，单位毫秒                  |
|    21    | getRepeatMode              | 获取播放模式                        |  50    | getAudioSessionId          | 获取 AudioSessionId                  |
|    22    | getPlayList                | 获取播放列表                        | 51    | querySongInfoInLocal       | 扫描本地媒体信息                       |
|    23    | updatePlayList             | 更新播放列表                        | 52    | cacheSwitch                | 缓存开关，可控制是否使用缓存功能         |
|    24    | addPlayList                | 添加更多播放列表                    | 53    | stopByTimedOff             | 定时暂停或停止                         |
|    25    | addSongInfo                | 添加一首歌                          |  54    | addPlayerEventListener     | 添加一个状态监听                       |
|    26    | addSongInfo                | 添加一首歌,指定位置                  | 55    | removePlayerEventListener  | 删除一个状态监听                       |
|    27    | removeSongInfo             | 删除歌曲                            | 56    | clearPlayerEventListener   | 删除所有状态监听                       |
|    28    | clearPlayList              | 清除播放列表                         |   57    | focusStateChange           | 焦点变化监听,LiveData 方式             |
|    29    | getNowPlayingSongInfo      | 获取当前播放的歌曲信息                |    58    | playbackState              | 状态监听,LiveData 方式                 |
|    30    | getNowPlayingSongId        | 获取当前播放的歌曲songId             |  59    | setOnPlayProgressListener  | 设置进度监听                           |
|    60    | skipMediaQueue             | 是否跳过播放队列                     |  61    | setWithOutCallback         | 是否需要状态回调                        |



具体详细注释，参数和实现请查看源代码或者参考 demo。

如果问题请加群咨询。