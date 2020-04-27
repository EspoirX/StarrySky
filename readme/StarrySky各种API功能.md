# StarrySky各种API功能使用

## 媒体信息实体类 SongInfo

首先，在 StarrySky 中用户用来存储音频信息的实体类是 SongInfo，这是指定的，所以在项目中使用时，应该把后台返回的媒体信息都转换成 SongInfo 去使用。

**SongInfo 中必须要有的字段**

在实现播放功能时，SongInfo 有两个值必须要填，那就是 songId 和 songUrl，否则无法播放。其中 songId 是媒体 Id，你可以随便给他赋值，只需要保证唯一就行，
因为它是每个媒体的唯一标记，songUrl 就是媒体的播放地址。

**设置请求头**
有些音频需要添加请求 header 才能访问，所以 SongInfo 中 有一个 headData 字段，它可以设置请求头。


## PlayerControl 播放控制

PlayerControl 是一个接口，里面定义了很多播放相关的 API 方法，它的默认实现是 StarrySkyPlayerControl，通过调用
StarrySky.with() 方法得到实例。StarrySky 是一个单例。

StarrySky API 的使用主要通过 PlayerControl 去控制，举个例子，如何实现简单的播放功能：

java
SongInfo info = new SongInfo();
info.setSongId("111");
info.setSongUrl("http://music.163.com/song/media/outer/url?id=317151.mp3");
StarrySky.with().playMusicByInfo(info);


### PlayerControl的API方法


**1. void playMusic(List<SongInfo> mediaList, int index)**

播放，传入播放列表和要播放的歌曲在播放列表中的索引，比较适合在列表中使用

</br>

**3. void playMusicById(String songId)**

根据 songId 播放，调用前请确保已经设置了播放列表

</br>

**4. void playMusicByInfo(SongInfo info)**

 根据 SongInfo 播放，如果 SongInfo 没有在已有的播放列表中，会把当前的 SongInfo 添加到播放列表，再播放，如果存在，则实际也是根据 songId 播放

</br>

**5. void playMusicByIndex(int index)**

根据要播放的歌曲在播放列表中的下标播放,调用前请确保已经设置了播放列表

</br>


**6. void pauseMusic()**

暂停播放

</br>


**7. void restoreMusic()**

恢复播放，比如暂停后恢复播放。

</br>

**8. void stopMusic()**

停止播放

</br>


**9. void prepare()**

播放准备，这可以减少在接收到播放命令时开始播放所花费的时间，不需要准备。

</br>


**10.  void prepareFromSongId(String songId)**

根据 songId 给某个媒体播放准备播放

</br>


**11. void skipToNext()**

转跳到下一首

</br>


**12. void skipToPrevious()**

转跳到上一首

</br>


**13. boolean isSkipToNextEnabled()**

是否有下一首

</br>


**14. boolean isSkipToPreviousEnabled()**

是否有上一首

</br>


**15. void fastForward()**

快进，每调用一次增加 0.5 倍

</br>


**16. void rewind()**

快退，每调用一次减少 0.5 倍，最小为 0 ，0 的时候就会暂停

</br>

**17. void onDerailleur(boolean refer, float multiple)**

指定语速,通过此方法可配置任意倍速，注意结果要大于0
refer 是否已当前速度为基数
multiple 倍率

</br>
    
**18. void seekTo(long pos)**

移动到媒体流中的新位置,以毫秒为单位。

</br>


**21. setRepeatMode(repeatMode: Int, isLoop: Boolean)**

设置播放模式
必须是以下之一：
RepeatMode.REPEAT_MODE_NONE      顺序播放
RepeatMode.REPEAT_MODE_ONE       单曲播放
RepeatMode.REPEAT_MODE_SHUFFLE   随机播放
RepeatMode.REPEAT_MODE_REVERSE   倒序播放

isLoop 播放倒最后一首时是否从第一首开始循环播放,该参数对随机播放无效


</br>


**22. RepeatMode getRepeatMode()**

获取播放模式

</br>


**23. List<SongInfo> getPlayList()**

获取播放列表

</br>


**24. void updatePlayList(List<SongInfo> songInfos)**

更新播放列表，此方法会重新刷新和加载播放数据

</br>


**25. SongInfo getNowPlayingSongInfo()**

获取当前播放的歌曲信息，有可能为 null，所以使用时最好判断一下

</br>


**26. String getNowPlayingSongId()**

获取当前播放的歌曲信息的 songId

</br>


**27. int getNowPlayingIndex()**

获取当前播放歌曲在播放列表中的下标，获取不到时默认值为 -1

</br>


**28. long getBufferedPosition()**

以 ms 为单位获取当前缓冲的位置

</br>


**29. long getPlayingPosition()**

获取播放位置 毫秒为单位

</br>


**30. float getPlaybackSpeed()**
 
获取当前的播放速度，播放速度应该时正常播放的倍数，快退时这应该是负数，值为 1 表示正常播放，0 表示暂停。

</br>


**31. Object getPlaybackState()**

获取底层框架{@link android.media.session.PlaybackState}对象。此方法仅在API 21+上受支持。

</br>


**32. CharSequence getErrorMessage()**

 获取发送错误时的错误信息

</br>


**33. int getErrorCode()**

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

</br>


**34. int getState()**



获取当前的播放状态。 它的取值是以下之一：  
Playback.STATE_NONE = 100      //什么都没开始
Playback.STATE_IDLE = 200      //空闲
Playback.STATE_BUFFERING = 300 //正在缓冲
Playback.STATE_PLAYING = 400   //正在播放
Playback.STATE_PAUSED = 500    //暂停
Playback.STATE_STOPPED = 600   //停止
Playback.STATE_ERROR = 700     //出错

</br>


**35. long getDuration()**

获取媒体时长，单位毫秒

</br>


**36. List<SongInfo> querySongInfoInLocal()**

扫描本地媒体信息，并转化为 List<SongInfo> 返回

</br>


**37. boolean isPlaying()**

比较方便的判断当前媒体是否在播放

</br>


**38. boolean isPaused()**

比较方便的判断当前媒体是否暂停中

</br>


**39. boolean isIdea()**

比较方便的判断当前媒体是否空闲

</br>


**40. boolean isCurrMusicIsPlayingMusic(String songId)**

判断传入的音乐是不是正在播放的音乐，在Adapter中判断状态比较有用

</br>


**41. boolean isCurrMusicIsPlaying(String songId)**

判断传入的音乐是否正在播放

</br>


**42. boolean isCurrMusicIsPaused(String songId)**

判断传入的音乐是否正在暂停

</br>


**43. setVolume(float audioVolume)**

设置音量


</br>


**播放监听相关**

StarrySky 提供了一个监听器接口 OnPlayerEventListener：

java
public interface OnPlayerEventListener {
    void onMusicSwitch(SongInfo songInfo); //切歌时回调
    void onPlayerStart(); //开始播放时回调,与 onMusicSwitch 的关系是先回调 onMusicSwitch，再回调 onPlayerStart
    void onPlayerPause(); //暂停播放时回调
    void onPlayerStop(); //停止播放时回调
    void onPlayCompletion(SongInfo songInfo); //播放完成时回调
    void onBuffering(); //正在缓冲时回调
    void onError(int errorCode, String errorMsg); //发生错误时回调
}


**1. void addPlayerEventListener(OnPlayerEventListener listener)**

添加一个播放状态监听器

</br>


**2. void removePlayerEventListener(OnPlayerEventListener listener)**

移除一个播放状态监听器

</br>


**3. void clearPlayerEventListener()**

移除所有播放状态监听器

**LiveData方式监听**

同样地，如果你觉得上面的操作太麻烦，StarrySky 还提供了 LiveData 的方式去监听播放状态：

java
//状态监听
StarrySky.with().playbackState().observe(this, playbackStage -> {
    if (playbackStage == null) {
        return;
    }
    switch (playbackStage.getStage()) {
        case PlaybackStage.NONE:
            //空状态
            break;
        case PlaybackStage.START:
            //开始播放
            break;
        case PlaybackStage.PAUSE:
            //暂停
            break;
        case PlaybackStage.STOP:
            //停止
            break;
        case PlaybackStage.COMPLETION:
            //播放完成
            break;
        case PlaybackStage.BUFFERING:
            //缓冲中
            break;
        case PlaybackStage.ERROR:
            //播放出错
            break;
        default:
            break;
    }
});

代码如上所示，通过调用 playbackState 方法监听，这个方法返回的是 MutableLiveData<PlaybackStage>。播放状态信息都封装在
了 PlaybackStage 里面。通过 getStage 方法获取状态。如果是播放出错，还可以通过 getErrorCode 和 getErrorMessage 方法
获取出错信息。

通过 PlaybackStage 的 getSongInfo 方法可以获取当前播放的歌曲信息：

java
private void updateUIInfo(PlaybackStage playbackStage) {
    SongInfo songInfo = playbackStage.getSongInfo();
    if (songInfo != null) {
        //...
    } else {
        //...
    }
}



**添加播放进度监听：**

获取当前播放进度的原理就是一秒钟调用一次 getPlayingPosition()，为了方便，StarrySky 提供了一个工具类 TimerTaskManager ，当然你也可以完全自己去实现，
看看 TimerTaskManager 的用法：

java
public class MainActivity extends AppCompatActivity {

    private TimerTaskManager mTimerTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //创建 TimerTaskManager 对象
        mTimerTask = new TimerTaskManager();
        //设置更新回调
         mTimerTask.setUpdateProgressTask(() -> {
                    long position = StarrySky.with().getPlayingPosition();
                    long duration = StarrySky.with().getDuration();
                    long buffered = StarrySky.with().getBufferedPosition();
                     //SeekBar 设置 Max
                    if (mSeekBar.getMax() != duration) {
                        mSeekBar.setMax((int) duration);
                    }
                    mSeekBar.setProgress((int) position);
                    mSeekBar.setSecondaryProgress((int) buffered);
                });
        //开始获取进度，一般可以在 onPlayerStart 中调用
        mTimerTask.startToUpdateProgress();
        //停止获取进度，一般可以在 onPlayerPause 和 onPlayerStop，onPlayCompletion，onError 等方法中调用
        mTimerTask.stopToUpdateProgress();
        //释放资源，一般可以在 onStop 或者 onDestroy 中调用
        mTimerTask.removeUpdateProgressTask();
    }
}


定时播放功能：

定时播放原理也是定时调用 stopMusic() 方法，可以自己实现，或者使用 TimerTaskManager 中封装好的定时方法：

java
mTimerTask = new TimerTaskManager();

//开始定时
mTimerTask.startCountDownTask(10 * 1000, new TimerTaskManager.OnCountDownFinishListener() {
    @Override
    public void onFinish() {
        //定时完成
        StarrySky.with().stopMusic();
    }

    @Override
    public void onTick(long millisUntilFinished) {
        //定时监听
        mTimeView.setText("时间："+millisUntilFinished);
    }
});

//取消定时
mTimerTask.cancelCountDownTask();



