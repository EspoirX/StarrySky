### API说明

通过 MusicManager 去调用 lib 中所有的 api ，静态方法可以直接调用，非静态方法需要通过 MusicManager.get() 去调用。

#### 播放相关API

 | 指令        |    描述                                                    |  
 | :--------  | :---------------------------------------------------------|  
 | playMusic(List<SongInfo> list, int index ,boolean isJustPlay) |  播放音乐，并设置播放列表<br>参数：<br>list 播放列表<br>index 当前索引<br>isJustPlay 是否只是单纯的播放，true就是单纯的重头开始播，<br>false就会自动判断暂停，开始，切歌等状态 |
 | playMusic(List<SongInfo> list, int index) | 播放音乐，跟上一个方法一样，只不过默认isJustPlay为false |
 | playMusicByInfo(SongInfo info,boolean isJustPlay) | 根据音乐信息播放<br>参数：<br>info 音乐信息<br>isJustPlay 是否只是单纯的播放(作用跟上面说的一样)|
 | playMusicByInfo(SongInfo info) | 根据音乐信息播放,默认isJustPlay为false |
 | playMusicByIndex(int index,boolean isJustPlay) | 根据索引播放，调用此方法前确认已经设置了播放列表，否则播放失败<br>参数：<br>index 当前索引<br> isJustPlay 是否只是单纯的播放|
 | playMusicByIndex(int index) | 根据索引播放,默认isJustPlay为false |
 
#### 播放器控制相关API

 | 指令        |    描述                                                    |  
 | :--------  | :---------------------------------------------------------|  
 | pauseMusic() | 暂停音乐 |
 | resumeMusic() | 继续播放（恢复暂停） |
 | stopMusic() | 停止音乐 |
 | playNext() | 播放下一首 |
 | playPre() | 播放上一首 |
 | seekTo(int position) | 定位到指定位置<br>参数：<br>position 具体的位置 |
 | setPlayMode(int mode) | 设置播放模式<br>参数：<br> mode 播放模式，取值为`PlayMode`中的下列之一:<br>PlayMode.PLAY_IN_SINGLE_LOOP 单曲循环<br>PlayMode.PLAY_IN_RANDOM 随机播放<br>PlayMode.PLAY_IN_LIST_LOOP 列表循环<br>PlayMode.PLAY_IN_ORDER 顺序播放<br>PlayMode.PLAY_IN_FLASHBACK 倒序播放 |
 | getPlayMode() | 得到播放模式，默认是列表循环 |
 | getStatus() | 得到播放状态 ，返回值为`State`中的下列之一: <br>State.STATE_IDLE 空闲<br>State.STATE_BUFFERING 正在缓冲<br>State.STATE_PLAYING 正在播放<br>State.STATE_PAUSED 暂停<br>State.STATE_ENDED 播放结束<br>State.STATE_ERROR 播放出错|
 | getCurrPlayingIndex()|得到当前播放索引|
 | getPreMusic()|得到基于当前播放音乐的上一首音乐信息，返回 `SongInfo`|
 | getNextMusic()|得到基于当前播放音乐的下一首音乐信息，返回 `SongInfo`|
 | getCurrPlayingMusic()|得到当前播放音乐信息，返回 `SongInfo`,没有的话返回`null`|
 | getProgress()|获取当前进度，返回 `long`|
 | hasPre()|判断是否有上一首|
 | hasNext()|判断是否有下一首|
 | setPlayList(List<SongInfo> list)| 设置播放列表，索引默认为0 <br>参数：<br>list 播放列表|
 | setPlayListWithIndex(List<SongInfo> list,int index)| 设置播放列表，并指定索引 <br>参数：<br>list 播放列表<br>index 当前索引|
 | getPlayList()| 得到当前播放列表,返回`List<SongInfo>`|
 | deleteSongInfoOnPlayList(SongInfo info,boolean isNeedToPlayNext)|从播放列表中删除一条信息 <br>参数：<br>info 需要删除的音乐信息<br>isNeedToPlayNext 删除后是否要播放接下来的下一首歌|
 | setCurrMusic(int index)|改变当前播放的音乐信息 <br>参数：<br>index 当前索引|
 | getDuration() | 获取当前音频时长 |
 | stopNotification() | 关闭通知栏 |
 | reset()| 重置，该方法会停止音频，关闭通知栏并清除所有监听器，但不会结束音频服务 |
 | unbindService | 解绑服务，该方法会解绑后台音频服务|
 | getBufferedPosition()| 获取当前缓冲进度 |
 | setPlaybackParameters(float speed, float pitch)|设置播放速度和播放音调 <br>参数：<br>speed 播放速度<br>pitch 播放音调|
 | setVolume(float audioVolume) | 设置播放音量 <br>参数：<br>audioVolume 播放音量,范围: 0f ~ 1f|
 | int getAudioSessionId() | 获取音频 audioSessionId，用于获取音频频谱等操作 |
 | float getPlaybackSpeed() | 获取当前播放速度 |
 | float getPlaybackPitch() | 获取当前播放音调 |
 
 *获取当前缓冲进度和设置播放速度和播放音调后面有特别说明，请耐心看完*
 
 #### 静态方法API
  | 指令        |    描述                                                    |  
  | :--------  | :---------------------------------------------------------|  
  | isCurrMusicIsPlayingMusic(SongInfo currMusic) | 判断当前传进来的音乐信息是不是正在播放的音乐信息 <br>参数：<br>currMusic 当前的音乐信息 |
  | isPaused() | 判断是否在暂停 |
  | isPlaying() | 判断是否正在播放 |
  | isIdea() | 判断是否空闲 |
  | isCurrMusicIsPlaying(SongInfo currMusic)  | 判断当前的音乐是否在播放  <br>参数：<br>currMusic 当前的音乐信息|
  | isCurrMusicIsPaused(SongInfo currMusic)  |  判断当前音乐是否在暂停  <br>参数：<br>currMusic 当前的音乐信息|
 
 
#### 通知栏相关API
通知栏相关的API使用详见[通知栏集成](https://github.com/lizixian18/MusicLibrary/blob/master/Notification_README.md)中的介绍。

  | 指令        |    描述                                                    |  
  | :--------  | :---------------------------------------------------------| 
  | updateNotificationFavorite(boolean isFavorite) | 更新通知栏中喜欢或收藏按钮的选中状态<br>参数：<br> isFavorite 是否喜欢或收藏<br>true为按钮变成选中状态，false为未选中状态 |
  | updateNotificationLyrics(boolean isChecked) | 更新通知栏中桌面歌词按钮的选中状态 <br>参数：<br> isChecked 是否选中<br>true为按钮变成选中状态，false为未选中状态 |
  | updateNotificationContentIntent(Bundle bundle, String targetClass) | 更新通知栏点击的时候传递的参数和转跳的界面 <br>参数：<br> bundle 点击通知栏转跳界面的时候传递的值<br>targetClass 转跳界面，不需要改变时传 null 即可 |
  | updateNotificationCreater(NotificationCreater creater) | 更新通知栏,这个方法实际上就是重新new一个Notification了 <br>参数：<br> creater 通知栏创建类,具体用法看`通知栏集成` |

#### 定时播放相关API
| 指令        |    描述                                                    |  
| :--------  | :---------------------------------------------------------|
| pausePlayInMillis(long time) | 在 time 毫秒后暂停播放<br>参数：<br>time 定时时间，单位毫秒，如果想停止定时，传 -1 即可。<br> 此方法调用的时候马上就会开始计时，<br> 当定时结束后，如果有音频正在播放，则暂停播放，什么都不会做 |

#### 保存音频播放进度API
音频进度保存在数据库里面，数据表有两个字段，分别是音频 ID 和 当前进度。相关
API 由 `SongHistoryManager` 类去管理。

| 指令        |    描述                                                    |  
| :--------  | :---------------------------------------------------------|
| saveSongHistory(String songId, int progress) |保存音频进度<br>参数：<br> songId 音频 Id<br>progress 当前进度|
| findSongProgressById(String songId) | 根据音频 ID 获取保存的进度 <br>参数：<br>songId 音频 Id|
| hasSongHistory(String songId) | 根据音频 ID 查找是否有对应的进度  <br>参数：<br>songId 音频 Id|
| deleteSongProgressById(String songId) | 根据音频 ID 删除对应的进度记录 <br>参数：<br>songId 音频 Id|
| clearAllSongProgress() | 清除所以进度记录|




#### 监听器相关API
| 指令        |    描述                                                    |  
| :--------  | :---------------------------------------------------------|
| addStateObservable(Observer o) | 添加状态监听观察者 |
| deleteStateObservable(Observer o) | 删除状态监听观察者 |
| clearStateObservable() | 清空所有状态监听观察者 |
| addPlayerEventListener(OnPlayerEventListener listener) | 添加一个状态监听器 |
| removePlayerEventListener(OnPlayerEventListener listener) | 移除一个状态监听器 |
| clearPlayerEventListener() | 清除所有状态监听器 |
| addTimerTaskEventListener(OnTimerTaskListener listener) | 添加一个定时播放监听器 |
| removeTimerTaskEventListener(OnTimerTaskListener listener) | 移除一个定时播放监听器 |
| clearTimerTaskEventListener() | 清除所有定时播放监听器 |



#### 监听器使用说明

一个监听器有七个方法，分别回调六种状态。分别为切歌，开始播放，暂停播放，播放完成，播放停止，播放出错和缓冲,如下：
```java
public interface OnPlayerEventListener {
    //music 切歌信息
    void onMusicSwitch(SongInfo music);

    void onPlayerStart();

    void onPlayerPause();

    void onPlayerStop();

    void onPlayCompletion();
    
    //errorMsg 错误信息
    void onError(String errorMsg);

    //isFinishBuffer true为缓冲完成，false为还没缓冲完成,缓冲的过程既是加载音乐的异步过程
    void onBuffering(boolean isFinishBuffer);
    
    //API变更：从v1.3.0开始，onBuffering(boolean isFinishBuffer) 回调变为 onAsyncLoading(boolean isFinishLoading) 
    //说明请继续往后看
}
```

因为状态监听的方法太多，有时候并不需要这么多方法，所以还提供了一种观察者的监听方法。

一个使用场景：一个音乐列表，列表中只需要知道两种状态，播放和暂停，根据这状态来显示不同的UI。
如果实现 `OnPlayerEventListener` ，方法太多，很麻烦。

这时候可以这样：
在 Adapter 中实现 `Observer` 接口，然后实现 `update` 方法，然后调用 `addStateObservable` 方法把 Adapter 添加到监听队列中即可。

具体代码展示：
```java
Adapter：
class SongListAdapter extends RecycleAdapter implements Observer{
    ...
    protected void BindViewHolder(BaseViewHolder viewHolder, int position) {
         SongInfo songInfo = mDataList.get(position);
        if (MusicManager.isCurrMusicIsPlayingMusic(songInfo)) {
            //展示当前播放音乐的UI
        }else {
            //展示其他没在播放音乐的UI
        }
    }
    
    @Override
    public void update(Observable observable, Object arg) {
        //监听到状态改变，如果是开始和暂停两种的话就刷新adapter改变UI
        int msg = (int) arg;
        if (msg == MusicManager.MSG_PLAYER_START || msg == MusicManager.MSG_PLAYER_PAUSE) {
            notifyDataSetChanged();
        }
    }
    ...
}

Activity:
...
mAdapter = new SongListAdapter();
mRecyclerView.setAdapter(mAdapter);
//添加一个观察者
MusicManager.get().addStateObservable(mAdapter);
...
```

不止是开始和暂停，所有状态都能监听到，分别为：

```java
MusicManager.MSG_MUSIC_CHANGE     切歌
MusicManager.MSG_PLAYER_START     开始
MusicManager.MSG_PLAYER_PAUSE     暂停
MusicManager.MSG_PLAYER_STOP      播放停止
MusicManager.MSG_PLAY_COMPLETION  播放完成
MusicManager.MSG_PLAYER_ERROR     播放失败
MusicManager.MSG_BUFFERING        缓冲
```

为了提高性能，监听器中没有实时获取当前进度的方法，进度的获取原则上只有当你需要的时候再开线程获取。lib 提供了`TimerTaskManager`去获取进度，使用方法如下：
```java
TimerTaskManager mTimerTaskManager = new TimerTaskManager();

mTimerTaskManager.setUpdateProgressTask(new Runnable() {
    @Override
    public void run() {
        updateProgress();
    }
});

private void updateProgress() {
    long progress = MusicManager.get().getProgress();
    mSeekBar.setProgress((int) progress);
}

开始获取进度：
mTimerTaskManager.scheduleSeekBarUpdate();

暂停获取进度：
mTimerTaskManager.stopSeekBarUpdate();

回收资源：
mTimerTaskManager.onRemoveUpdateProgressTask();
```

在开始播放的时候调用 `scheduleSeekBarUpdate` 方法开启线程去获取进度，这时候 Runnable 就会调用，当暂停播放的时候调用 `stopSeekBarUpdate` 方法暂停获取进度。最后不要忘记了在 `onDestroy()` 的时候调用 `onRemoveUpdateProgressTask` 回收资源。同理当你添加了监听器，记得在 `onDestroy()` 的时候移除它。

#### 部分API特别说明

1. API变更说明

从 v1.3.0 开始，监听器中 onBuffering(boolean isFinishBuffer) 回调变为 onAsyncLoading(boolean isFinishLoading) 
原因是 onBuffering 这个方法名有歧义，容易理解为音频缓冲的回调，所以改为 onAsyncLoading。这个回调的意思音频异步加载的过程，
即开始加载音频前 isFinishLoading 为 false，音频加载好后 isFinishLoading 为 true，觉得难懂的话可以通俗的理解为
加载音频的时候转菊花的过程。

2. getBufferedPosition() 获取当前缓冲进度说明

第一点中的回调是音频加载回调，是 loading 的时候转菊花用到的，这个方法才是获取缓冲进度，你可以通过它设置 SeekBar 的 secondaryProgress
用法跟获取当前进度一样，你可以参考 demo。特别说明的就是， 这个方法返回的大小范围是 0 到 音频的时长，即 
0 < bufferedPosition < duration，而 SeekBar 的 secondaryProgress 方法的注释也写着范围是 0 到 getMax():
```java
/**
     * <p>
     * Set the current secondary progress to the specified value. Does not do
     * anything if the progress bar is in indeterminate mode.
     * </p>
     *
     * @param secondaryProgress the new secondary progress, between 0 and {@link #getMax()}
     * @see #setIndeterminate(boolean)
     * @see #isIndeterminate()
     * @see #getSecondaryProgress()
     * @see #incrementSecondaryProgressBy(int)
     */
    @android.view.RemotableViewMethod
    public synchronized void setSecondaryProgress(int secondaryProgress) {
       ...
    }
```

所以 SeekBar 在 setMax 的时候就比较重要了，最好是设置成音频的时长，否则如果设置成 100 的话，你需要自己再按比例转换一次。  
如果你开启了边播边存功能，在有缓存的情况下该方法会直接返回音频时长。

 
3. setPlaybackParameters 变速功能说明

该方法可以设置音频播放的参数 speed 和 pitch，即速度和音调，从而达到变速功能。正常情况下 speed 和 pitch 的值是 1f，
如果你传 1.1f 即变为 1.1 倍，1.2f 则 1.2 倍，如此类推。 当你需要改变音频和音调时，请确保参数都大于 0，否则会无效。 

变速功能的实现没有依赖第三方插件，比如 ffmpeg 等，所以如果你使用的是 MediaPlayer 该功能只支持 Android M 及以上版本。
如果你使用的是 ExoPlayer ，目前测试到支持的最低版本是 Android 4.4（因为找不到比4.4还低的手机了，模拟器也创建不了）





 