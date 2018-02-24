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
 | setPlayMode(int mode) | 设置播放模式<br>参数：<br> mode 播放模式，取值为`PlayMode`中的下列之一:<br>PlayMode.PLAY_IN_SINGLE_LOOP 单曲循环<br>PlayMode.PLAY_IN_RANDOM 随机播放<br>PlayMode.PLAY_IN_LIST_LOOP 列表循环 |
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
 
 #### 静态方法API
  | 指令        |    描述                                                    |  
  | :--------  | :---------------------------------------------------------|  
  | isCurrMusicIsPlayingMusic(SongInfo currMusic) | 判断当前传进来的音乐信息是不是正在播放的音乐信息 <br>参数：<br>currMusic 当前的音乐信息 |
  | isPaused() | 判断是否在暂停 |
  | isPlaying() | 判断是否正在播放 |
  | isIdea() | 判断是否空闲 |
  | isCurrMusicIsPlaying(SongInfo currMusic)  | 判断当前的音乐是否在播放  <br>参数：<br>currMusic 当前的音乐信息|
  | isCurrMusicIsPaused(SongInfo currMusic)  |  判断当前音乐是否在暂停  <br>参数：<br>currMusic 当前的音乐信息|
 
 
#### 监听器相关API
| 指令        |    描述                                                    |  
| :--------  | :---------------------------------------------------------|
| addStateObservable(Observer o) | 添加状态监听观察者 |
| deleteStateObservable(Observer o) | 删除状态监听观察者 |
| clearStateObservable() | 清空所有状态监听观察者 |
| addPlayerEventListener(OnPlayerEventListener listener) | 添加一个状态监听器 |
| removePlayerEventListener(OnPlayerEventListener listener) | 移除一个状态监听器 |
| clearPlayerEventListener() | 清除所有状态监听器 |


#### 监听器使用说明

一个监听器有六个方法，分别回调六种状态。分别为切歌，开始播放，暂停播放，播放完成，播放出错和缓冲,如下：
```java
public interface OnPlayerEventListener {
    //music 切歌信息
    void onMusicSwitch(SongInfo music);

    void onPlayerStart();

    void onPlayerPause();

    void onPlayCompletion();
    
    //errorMsg 错误信息
    void onError(String errorMsg);

    //isFinishBuffer true为缓冲完成，false为还没缓冲完成,缓冲的过程既是加载音乐的异步过程
    void onBuffering(boolean isFinishBuffer);
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
MusicManager.MSG_PLAY_COMPLETION  播放完成
MusicManager.MSG_PLAYER_ERROR     播放失败
MusicManager.MSG_BUFFERING        缓冲
```



 