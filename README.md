# MusicLibrary

<a href="art/bg1.jpg"><img src="art/bg1.jpg" width="100%"/></a>

一个比较完善的音乐播放封装库。有了它，你不用管什么播放器和相关的封装，只需要调用相关的方法即可实现音频播放功能。

PS：
1. 虽然写了aidl，但是因为还没有踩完多进程的坑，所以目前还没开多进程。
2. 还在不断的完善中，所以暂时不提供依赖的使用方法，可以先Clone源码导入
3. 如果你有想法或者意见和建议，欢迎提issue，喜欢点个star。
    


#### Demo
Demo 参考 [NiceMusic](https://github.com/lizixian18/NiceMusic)


#### 已完成功能(API)

```java
播放相关：

/**
* 播放音乐，并设置播放列表
* @param list 播放列表
* @param index 当前索引
* @param isJustPlay 是否只是单纯的播放，
*                   true就是单纯的重头开始播，fasle就会自动判断暂停，开始，切歌等状态
*/
void playMusic(List<SongInfo> list, int index ,boolean isJustPlay); 

/**
*  播放音乐，跟上一个方法一样，只不过默认isJustPlay为fasle 
*/
void playMusic(List<SongInfo> list, int index);

/**
* 根据音乐信息播放
* @param info 音乐信息
* @param isJustPlay 是否只是单纯的播放(作用跟上面说的一样)
*/
void playMusicByInfo(SongInfo info,boolean isJustPlay);

/**
* 根据音乐信息播放,默认isJustPlay为fasle 
*/
void playMusicByInfo(SongInfo info);

/**
* 根据索引播放，调用此方法前确认已经设置了播放列表，否则播放失败
* @param index 当前索引
* @param isJustPlay 是否只是单纯的播放
*/
void playMusicByIndex(int index,boolean isJustPlay);

/**
*  根据索引播放,默认isJustPlay为fasle 
*/
void playMusicByIndex(int index);

/**
* 暂停音乐
*/
void pauseMusic();

/**
* 继续播放（恢复暂停）
*/
void resumeMusic();

/**
* 停止音乐
*/
void stopMusic();

/**
* 播放下一首
*/
void playNext();

/**
* 播放上一首
*/
void playPre();

/**
* 定位到指定位置
*/
void seekTo(int position);



播放信息相关：

/**
* 得到当前播放索引
*/
int getCurrPlayingIndex();

/**
* 得到上一首音乐信息
*/
SongInfo getPreMusic();

/**
* 得到下一首音乐信息
*/
SongInfo getNextMusic();

/**
* 得到当前播放信息,没有的话返回null
*/
SongInfo getCurrPlayingMusic();

/**
* 获取当前进度
*/
long getProgress();

/**
* 是否有上一首
*/
boolean hasPre();

/**
* 是否有下一首
*/
boolean hasNext();



播放列表相关：

/**
* 设置播放列表，索引默认为0
* @param list 播放列表
*/
void setPlayList(List<SongInfo> list);

/**
* 设置播放列表
* @param list 播放列表
* @param index 索引
*/
void setPlayListWithIndex(List<SongInfo> list,int index);

/**
* 得到播放列表
*/
List<SongInfo> getPlayList();

/**
* 从播放列表中删除一条信息
* @param info 删除的音乐信息
* @param isNeedToPlayNext 删除后是否要播放接下来的下一首歌
*/
void deleteSongInfoOnPlayList(SongInfo info,boolean isNeedToPlayNext);

/**
* 改变当前播放的音乐信息
* @param index 索引
*/
void setCurrMusic(int index);



播放模式相关：

/**
* 设置播放模式
*  @param index 播放模式（三种）  
*               PlayMode.PLAY_IN_SINGLE_LOOP 单曲循环
*               PlayMode.PLAY_IN_RANDOM      随机播放
*               PlayMode.PLAY_IN_LIST_LOOP   列表循环
*/
void setPlayMode(int mode);

/**
* 得到播放模式，默认是列表循环
*/
int getPlayMode();


/**
* 获取播放状态 
* @param state 
*              State.STATE_IDLE      空闲
*              State.STATE_BUFFERING 正在缓冲
*              State.STATE_PLAYING   正在播放
*              State.STATE_PAUSED    暂停
*              State.STATE_ENDED     播放结束
*              State.STATE_ERROR     播放出错
*/
int getStatus();



其他相关：

/**
* 判断当前传进来的音乐信息是不是正在播放的音乐信息
* @param currMusic 当前的音乐信息
*/
static boolean isCurrMusicIsPlayingMusic(SongInfo currMusic);

/**
* 是否在暂停
*/
static boolean isPaused();

/**
* 是否正在播放
*/
public static boolean isPlaying();

/**
* 是否空闲
*/
public static boolean isIdea();

/**
* 当前的音乐是否在播放
* @param currMusic 当前的音乐信息
*/
static boolean isCurrMusicIsPlaying(SongInfo currMusic);

/**
* 当前音乐是否在暂停
* @param currMusic 当前的音乐信息
*/
static boolean isCurrMusicIsPaused(SongInfo currMusic);

```


#### 使用方法

```java
通过 MusicManager 去调用上面说到的所有方法，静态方法可以直接调用，非静态方法需要通过 MusicManager.get() 去调用。

在 Application 中绑定音乐服务完成初始化：
MusicManager.get().setContext(this).bindService();

其中，一定要调用 setContext 设置上下文，否则会报错。  
初始化的时候还有一些参数可以配置：  
setAutoPlayNext(boolean autoPlayNext) //是否在播放完当前歌曲后自动播放下一首


其他方法介绍：  
/**
* 解绑 Service
*/
void unbindService();

/**
* 添加状态监听观察者
*/
void addStateObservable(Observer o);

/**
* 删除状态监听观察者
*/
void deleteStateObservable(Observer o);

/**
* 清空所有状态监听观察者
*/
void clearStateObservable();

/**
* 添加一个状态监听器
*/
void addPlayerEventListener(OnPlayerEventListener listener);

/**
* 移除一个状态监听器
*/
void removePlayerEventListener(OnPlayerEventListener listener);

/**
* 清除所有状态监听器
*/
void clearPlayerEventListener();


音乐状态监听说明：

一个监听器有六个方法，分别回调六种状态。分别为切歌，开始播放，暂停播放，播放完成，播放出错和缓存,如下：

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

然后因为状态监听的方法太多，有时候并不需要这么多方法，所以还提供了一种观察者的监听方法。

一个使用场景：一个音乐列表，列表中只需要知道两种状态，播放和暂停，根据这状态来显示不同的UI。
如果实现 OnPlayerEventListener ，方法太多，很麻烦。

这时候可以这样：
在 Adapter 中实现 Observer接口，然后实现 update 方法，然后调用 addStateObservable 方法把 Adapter添加到监听队列中即可。

具体代码展示

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

不止是开始和暂停，所有状态都能监听到，分别为：

MusicManager.MSG_MUSIC_CHANGE     切歌
MusicManager.MSG_PLAYER_START     开始
MusicManager.MSG_PLAYER_PAUSE     暂停
MusicManager.MSG_PLAY_COMPLETION  播放完成
MusicManager.MSG_PLAYER_ERROR     播放失败
MusicManager.MSG_BUFFERING        缓冲


```

#### 通知栏集成
通知栏集成详细见 [通知栏集成](https://github.com/lizixian18/MusicLibrary/blob/master/Notification_README.md)


#### About me
An android developer in GuangZhou  
简书：[http://www.jianshu.com/users/286f9ad9c417/latest_articles](http://www.jianshu.com/users/286f9ad9c417/latest_articles)   
Email:386707112@qq.com  
If you want to make friends with me, You can give me a Email and follow me。

#### License
```
Copyright 2018 L_Xian   

Licensed under the Apache License, Version 2.0 (the "License");  
you may not use this file except in compliance with the License.  
You may obtain a copy of the License at  

http://www.apache.org/licenses/LICENSE-2.0  

Unless required by applicable law or agreed to in writing, software  
distributed under the License is distributed on an "AS IS" BASIS,  
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  
See the License for the specific language governing permissions and  
limitations under the License.
```
