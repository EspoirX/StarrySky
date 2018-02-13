# MusicLibrary

<a href="art/bg1.jpg"><img src="art/bg1.jpg" width="100%"/></a>

一个比较完善的音乐播放封装库。

PS：虽然写了aidl，但是因为还没有踩完多进程的坑，所以目前还没开多进程。

#### Demo
Demo 参考 https://github.com/lizixian18/NiceMusic

#### 待完成功能
1. 单首歌曲定时播放功能
2. 全局的所有歌曲定时播放功能
3. reset重置音乐信息功能
4. 添加MediaPlayer播放器
5. 通知栏的集成
6. 其他功能的扩展

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
