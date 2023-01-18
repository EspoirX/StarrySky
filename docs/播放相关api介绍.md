### 播放相关api介绍

#### SongInfo

SongInfo 是播放信息的载体，所有播放信息都会存储在里面。在播放时，**songId** 和 **songUrl** 是必须要有的，否则会播放失败。  
songId 是每个音频唯一的 id，不同音频之间不能相同。

#### PlayerControl

所有播放 api 都是通过 **PlayerControl** 这个类去调用的。通过 StarrySky.with() 即可获取 PlayerControl 对象。  
StarrySky 是一个单例模式。

### 播放相关API

| 编号  | API                   | 作用                 | 编号  | API                       | 作用                 |
|:----|:----------------------|:-------------------|:----|:--------------------------|:-------------------|
| 1   | updateCurrIndex       | 根据当前播放信息更新下标       | 31  | getNowPlayingSongUrl      | 获取当前播放的歌曲url       |
| 2   | playMusicById         | 根据 songId 播放       | 32  | getNowPlayingIndex        | 获取当前播放歌曲的下标        |
| 3   | playMusicByUrl        | 根据 songUrl 播放      | 33  | getBufferedPosition       | 获取缓存位置 毫秒为单位       |
| 4   | playMusicByInfo       | 根据 SongInfo 播放     | 34  | getPlayingPosition        | 获取播放位置 毫秒为单位       |
| 5   | playMusic             | 播放，传入播放列表和下标       | 35  | isSkipToNextEnabled       | 是否有下一首             |
| 6   | pauseMusic            | 暂停                 | 36  | isSkipToPreviousEnabled   | 是否有上一首             |
| 7   | restoreMusic          | 恢复播放（暂停后恢复）        | 37  | getPlaybackSpeed          | 获取播放速度             |
| 8   | stopMusic             | 停止播放               | 38  | isPlaying                 | 是否在播放              |
| 9   | prepare               | 准备播放，准备的是队列当前下标的音频 | 39  | isPaused                  | 是否暂停               |
| 11  | prepareById           | 根据 songId 准备       | 40  | isIdea                    | 是否空闲               |
| 12  | prepareByUrl          | 根据 songUrl 准备      | 41  | isBuffering               | 是否缓冲               |
| 13  | prepareByInfo         | 根据 SongInfo 准备     | 42  | isCurrMusicIsPlayingMusic | 判断传入的音乐是不是正在播放的音乐  |
| 14  | skipToNext            | 下一首                | 43  | isCurrMusicIsPlaying      | 判断传入的音乐是否正在播放      |
| 15  | skipToPrevious        | 上一首                | 44  | isCurrMusicIsPaused       | 判断传入的音乐是否正在暂停      |
| 16  | fastForward           | 快进 每调一次加 speed 倍   | 45  | isCurrMusicIsIdea         | 判断传入的音乐是否空闲        |
| 17  | rewind                | 快退 每调一次减 speed 倍   | 46  | isCurrMusicIsBuffering    | 判断传入的音乐是否缓冲        |
| 18  | onDerailleur          | 配置任意倍速             | 47  | setVolume                 | 设置音量, 范围 0到1       |
| 19  | seekTo                | 移动到媒体流中的新位置,以毫秒为单位 | 48  | getVolume                 | 获取音量               |
| 20  | setRepeatMode         | 设置播放模式             | 49  | getDuration               | 获取媒体时长，单位毫秒        |
| 21  | getRepeatMode         | 获取播放模式             | 50  | getAudioSessionId         | 获取 AudioSessionId  |
| 22  | getPlayList           | 获取播放列表             | 51  | querySongInfoInLocal      | 扫描本地媒体信息           |
| 23  | updatePlayList        | 更新播放列表             | 52  | stopByTimedOff            | 定时暂停或停止            |
| 24  | addPlayList           | 添加更多播放列表           | 53  | addPlayerEventListener    | 添加一个状态监听           |
| 25  | addSongInfo           | 添加一首歌              | 54  | removePlayerEventListener | 删除一个状态监听           |
| 26  | addSongInfo           | 添加一首歌,指定位置         | 55  | clearPlayerEventListener  | 删除所有状态监听           |
| 27  | removeSongInfo        | 删除歌曲               | 56  | focusStateChange          | 焦点变化监听,LiveData 方式 |
| 28  | clearPlayList         | 清除播放列表             | 57  | playbackState             | 状态监听,LiveData 方式   |
| 29  | getNowPlayingSongInfo | 获取当前播放的歌曲信息        | 58  | setOnPlayProgressListener | 设置进度监听             |
| 30  | getNowPlayingSongId   | 获取当前播放的歌曲songId    | 59  | skipMediaQueue            | 是否跳过播放队列           |


### 其他API

| 编号  | API                | 作用                                                         |
|:----|:-------------------|:-----------------------------------------------------------|
| 1   | skipMediaQueue     | 是否跳过播放队列，false的话，播放将不经过播放队列，直接走播放器，当前Activity结束后恢复false状态  |
| 2   | setWithOutCallback | 是否需要状态回调，false的话将收不到回调，即使你已经设置了，当前Activity结束后恢复true状态      | 
| 3   | addInterceptor     | 添加临时拦截器，执行顺序是先执行局部拦截器再执行全局拦截器，当Activity结束或playMusic之后会自动清理 | 
| 4   | cacheSwitch        | 缓存开关，可控制是否使用缓存功能                                           | 

### StarrySky 类直接调用的API

| 编号  | API                   | 作用                         |
|:----|:----------------------|:---------------------------|
| 1   | with                  | 获取 PlayerControl 实例，操作 API |
| 2   | soundPool             | 获取 soundPool 操作            |
| 3   | changeNotification    | 切换系统和自定义通知栏                |
| 4   | openNotification      | 打开通知栏                      |
| 5   | closeNotification     | 关闭通知栏                      |
| 6   | setIsOpenNotification | 控制是否打开通知栏（开关性质）            |
| 7   | getNotificationType   | 获取当前通知栏类型                  |
| 8   | isOpenCache           | 是否打开了缓存开关                  |
| 9   | getPlayerCache        | 获取播放缓存类                    |
| 10  | bindService           | 绑定服务                       |
| 11  | unBindService         | 解邦服务                       |
| 13  | effectSwitch          | 音效开关                       |
| 13  | getEffectSwitch       | 获取音效开关                     |
| 13  | saveEffectConfig      | 音效的相关配置是否缓存到本地             |
| 13  | effect                | 获取音效操作类                    |
| 13  | interceptors          | 获取全局拦截器集合                  |
| 13  | clearInterceptor      | 清除全局拦截器                    |