# StarrySky各种API功能使用

## 媒体信息实体类 SongInfo

首先，在 StarrySky 中用来存储音频信息的实体类叫做 SongInfo，这是指定的，所以在项目中使用时，应该把后台返回的媒体信息都转换成 SongInfo 去使用。

因此 SongInfo 里面也有非常多的字段供大家去使用，具体可以[点开查看](https://github.com/lizixian18/MusicLibrary/blob/StarrySkyJava/starrysky/src/main/java/com/lzx/starrysky/model/SongInfo.java)
，里面都有一些注释，如果里面的字段不满足使用需求，我的建议是：

- 如果你想添加的字段是跟自己业务逻辑相关的，可以通过导入源码方式使用，自己去添加修改。
- 如果想要添加的字段跟媒体信息有关，可以留言给我添加上去。


**SongInfo中必须要有的字段**

在实现播放功能时，SongInfo 有两个值必须要填，那就是 songId 和 songUrl，否则无法播放。其中 songId 是媒体 Id，你可以随便给他赋值，只需要保证唯一就行，
因为它是每个媒体的唯一标记，songUrl 就是媒体的播放地址。


## MusicManager 播放管理类

StarrySky API 的使用主要通过 MusicManager 去调用，它是一个单例，举个例子，如何实现简单的播放功能：

```java
SongInfo s1 = new SongInfo();
s1.setSongId("111");
s1.setSongUrl("http://music.163.com/song/media/outer/url?id=317151.mp3");
MusicManager.getInstance().playMusicByInfo(s1);
```

### MusicManager的API方法

**播放相关**

**1. playMusic(List<SongInfo> songInfos, int index, boolean isResetPlayList)**

描述：  
@param songInfos       播放列表  
@param index           要播放的歌曲在播放列表中的下标  
@param isResetPlayList 是否重新设置播放列表，如果true，则会重新加载播放列表中的资源，比如封面下载等，
                       如果false，则使用原来的，相当于缓存，建议当播放列表改变或者第一次播放时才设为true

**2. void playMusic(List<SongInfo> songInfos, int index)**

描述：  
跟上面方法一样，默认 isResetPlayList 为 false

**3. void playMusicById(String songId)**

描述：  
根据 songId 播放,调用前请确保已经设置了播放列表

**4. void playMusicByInfo(SongInfo info)**

描述：  
根据 SongInfo 播放，如果 SongInfo 没有在已有的播放列表中，会把当前的 SongInfo 添加到播放列表，再播放，如果存在，则实际也是根据 songId 播放

**5. void playMusicByIndex(int index)**

描述：  
根据要播放的歌曲在播放列表中的下标播放,调用前请确保已经设置了播放列表

**6. void pauseMusic()**

描述：  
暂停播放

**7. void playMusic()**

描述：  
恢复播放，比如暂停后恢复播放。

**8. void stopMusic()**

描述：  
停止播放

**9. void prepare()**

描述：  
播放准备，这可以减少在接收到播放命令时开始播放所花费的时间，不需要准备。

**10.  void prepareFromSongId(String songId)**

描述：  
根据 songId 给某个媒体播放准备

**11. void skipToNext()**

描述：  
转跳到下一首

**12. void skipToPrevious()**

描述：  
转跳到上一首

**13. boolean isSkipToNextEnabled()**

描述：  
是否有下一首

**14. boolean isSkipToPreviousEnabled()**

描述：  
是否有上一首

**15. void fastForward()**

描述：  
快进，每调用一次增加 0.5 倍

**16. void rewind()**

描述：  
快退，每调用一次减少 0.5 倍，最小为 0 ，0 的时候就会暂停

**17. void seekTo(long pos)**

描述：  
移动到媒体流中的新位置,以毫秒为单位。

**18. void setShuffleMode(int shuffleMode)**

描述：  
设置随机播放模式，shuffleMode 必须是以下之一：  
PlaybackStateCompat.SHUFFLE_MODE_NONE 顺序播放  
PlaybackStateCompat.SHUFFLE_MODE_ALL  随机播放

**19. int getShuffleMode()**

描述：  
获取随机播放模式

**20. setRepeatMode(int repeatMode)**

描述：  
设置播放模式，repeatMode 必须是以下之一：  
PlaybackStateCompat.REPEAT_MODE_NONE  顺序播放  
PlaybackStateCompat.REPEAT_MODE_ONE   单曲循环  
PlaybackStateCompat.REPEAT_MODE_ALL   列表循环

**21. int getRepeatMode()**

描述：  
获取播放模式

**22. List<SongInfo> getPlayList()**

描述：  
获取播放列表

**23. void updatePlayList(List<SongInfo> songInfos)**

描述：  
更新播放列表，此方法会重新刷新和加载播放数据

**24. SongInfo getNowPlayingSongInfo()**

描述：  
获取当前播放的歌曲信息，有可能为 null，所以使用时最好判断一下

**25. String getNowPlayingSongId()**

描述：  
获取当前播放的歌曲信息的 songId

**26. int getNowPlayingIndex()**

描述：  
获取当前播放歌曲在播放列表中的下标，获取不到时默认值为 -1

**27. long getBufferedPosition()**

描述：  
以 ms 为单位获取当前缓冲的位置

**28. long getPlayingPosition()**

描述：  
获取播放位置 毫秒为单位

**29. float getPlaybackSpeed()**

描述：  
获取当前的播放速度，播放速度应该时正常播放的倍数，快退时这应该是负数，值为 1 表示正常播放，0 表示暂停。

**30. Object getPlaybackState()**

描述：  
获取底层框架{@link android.media.session.PlaybackState}对象。此方法仅在API 21+上受支持。

**31. CharSequence getErrorMessage()**

描述：  
获取发送错误时的错误信息

**32. int getErrorCode()**

描述：  
获取发送错误时的错误码，下面是错误码有可能的取值：  
0 : 这是默认的错误代码  
1 : 当应用程序状态无效以满足请求时的错误代码。  
2 : 应用程序不支持请求时的错误代码。  
3 : 由于身份验证已过期而无法执行请求时出现错误代码。  
4 : 成功请求需要高级帐户时的错误代码。  
5 : 检测到太多并发流时的错误代码。  
6 : 由于家长控制而阻止内容时出现错误代码。  
7 : 内容因区域不可用而被阻止时的错误代码。  
8 : 请求的内容已在播放时出现错误代码。  
9 : 当应用程序无法跳过任何更多歌曲时出现错误代码，因为已达到跳过限制。  
10: 由于某些外部事件而导致操作中断时的错误代码。  
11: 由于队列耗尽而无法播放导航（上一首，下一首）时出现错误代码。  

**33. int getState()**

描述：  
获取当前的播放状态。 它的取值是以下之一：
PlaybackStateCompat.STATE_NONE                   默认播放状态，表示尚未添加媒体，或者表示已重置且无内容可播放。  
PlaybackStateCompat.STATE_STOPPED                当前已停止。  
PlaybackStateCompat.STATE_PLAYING                正在播放  
PlaybackStateCompat.STATE_PAUSED                 已暂停  
PlaybackStateCompat.STATE_FAST_FORWARDING        当前正在快进  
PlaybackStateCompat.STATE_REWINDING              当前正在倒带   
PlaybackStateCompat.STATE_BUFFERING              当前正在缓冲  
PlaybackStateCompat.STATE_ERROR                  当前处于错误状态  
PlaybackStateCompat.STATE_CONNECTING             正在连接中  
PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS   正在转跳到上一首  
PlaybackStateCompat.STATE_SKIPPING_TO_NEXT       正在转跳到下一首  
PlaybackStateCompat.STATE_SKIPPING_TO_QUEUE_ITEM 正在切歌  

**34. long getDuration()**

描述：  
获取媒体时长，单位毫秒

**35. List<SongInfo> querySongInfoInLocal()**

描述：  
扫描本地媒体信息，并转化为 List<SongInfo> 返回


**播放监听相关**

StarrySky 提供了一个监听器接口 OnPlayerEventListener：

```java
public interface OnPlayerEventListener {
    void onMusicSwitch(SongInfo songInfo); //切歌时回调
    void onPlayerStart(); //开始播放时回调,与 onMusicSwitch 的关系是先回调 onMusicSwitch，再回调 onPlayerStart
    void onPlayerPause(); //暂停播放时回调
    void onPlayerStop(); //停止播放时回调
    void onPlayCompletion(); //播放完成时回调
    void onBuffering(); //正在缓冲时回调
    void onError(int errorCode, String errorMsg); //发生错误时回调
}
```

**1. void addPlayerEventListener(OnPlayerEventListener listener)**

描述：  
添加一个播放状态监听器

**2. void removePlayerEventListener(OnPlayerEventListener listener)**

描述：  
移除一个播放状态监听器

**3. void clearPlayerEventListener()**

描述：  
移除所有播放状态监听器


**通知栏相关**

通知栏相关的 API 可在[快速集成通知栏](https://github.com/lizixian18/MusicLibrary/blob/StarrySkyJava/readme/快速集成通知栏.md)中了解具体用法。

**1. void setNotificationConstructor(NotificationConstructor constructor)**

描述：  
设置通知栏配置,应该在 Application 中创建并调用

**2. NotificationConstructor getConstructor()**

描述：  
获取通知栏配置

**3. void updateFavoriteUI(boolean isFavorite)**

描述：  
如果通知栏中有喜欢或者收藏按钮，可调用此方法来改变按钮是否选中的 UI 显示状态

**4. void updateLyricsUI(boolean isChecked)**

描述：  
如果通知栏中有是否显示歌词的按钮，可调用此方法来改变按钮是否选中的 UI 显示状态


MusicManager 中的 API 方法大概就以上这些，以后会慢慢完善添加新的方法。


