# StarrySky API 

## Media information entity class： SongInfo

First, the entity class used to store audio information in StarrySky is called SongInfo, which is specified, so when used in a project, the media information returned in the background should be converted to SongInfo for use.

So there are also a lot of fields in SongInfo for everyone to use, you can [click to view](https://github.com/lizixian18/MusicLibrary/blob/StarrySkyJava/starrysky/src/main/java/com/lzx/starrysky/model/SongInfo.java)
There are some comments inside, if the fields inside do not meet the usage requirements, my suggestion is:

- If the field you want to add is related to your own business logic, you can add it by importing the source code and adding it yourself.
- If the field you want to add is related to the media information, you can leave me a message.


**Must have fields in SongInfo**

When implementing the playback function, SongInfo has two values that must be filled, that is, songId and songUrl, otherwise it cannot be played. Where songId is the media Id, you can assign him a value, just need to be sure that it is the only one.
Because it is the only tag for each media, songUrl is the media's play address.


## MusicManager ：Play management class

The use of the StarrySky API is primarily invoked through the MusicManager, which is a singleton mode, for example, how to implement simple playback:

```java
SongInfo s1 = new SongInfo();
s1.setSongId("111");
s1.setSongUrl("http://music.163.com/song/media/outer/url?id=317151.mp3");
MusicManager.getInstance().playMusicByInfo(s1);
```

### MusicManager API

**About Play**

**~~1. playMusic(List<SongInfo> songInfos, int index, boolean isResetPlayList)~~**


**2. void playMusic(List<SongInfo> songInfos, int index)**

`Description：  `

`Play, the incoming playlist and the index of the song to be played in the playlist`

**3. void playMusicById(String songId)**

`Description：  Play according to songId, please make sure the playlist has been set before calling`

**4. void playMusicByInfo(SongInfo info)**

`Description：  `

`According to SongInfo playback, if SongInfo is not in the existing playlist, the current SongInfo will be added to the playlist and then played. If it exists, it will actually play according to songId.`

**5. void playMusicByIndex(int index)**

`Description：  `

`According to the index play, please make sure the playlist has been set before calling`

**6. void pauseMusic()**

`Description：  Pause playback`

**7. void playMusic()**

`Description：  Resume playback, such as resume playback after pause.`

**8. void stopMusic()**

`Description：  Stop play`

**9. void prepare()**

`Description：  Request that the player prepare for playback. This can decrease the time it takes to start playback when a play command is received. Preparation is not required`

**10.  void prepareFromSongId(String songId)**

`Description：  Request that the player prepare playback for a specific media id. This can decrease the time it takes to start playback when a play command is received. `

**11. void skipToNext()**

`Description：  Skips to the next item.`

**12. void skipToPrevious()**

`Description：  Skips to the previous item.`

**13. boolean isSkipToNextEnabled()**

`Description：  Is there a next one`

**14. boolean isSkipToPreviousEnabled()**

`Description：  Is there a previous one?`

**15. void fastForward()**

`Description：  开始快速转发。 如果播放已经快速转发，则可能会提高速率。`

**16. void rewind()**

`Description：  Starts rewinding. If playback is already rewinding this may increase the rate.`

**17. void seekTo(long pos)**

`Description：  Moves to a new location in the media stream. in milliseconds`

**18. void setShuffleMode(int shuffleMode)**

`Description：  `

`Sets the shuffle mode for this session.The shuffle mode. Must be one of the followings：`    
`PlaybackStateCompat.SHUFFLE_MODE_NONE play in order `   
`PlaybackStateCompat.SHUFFLE_MODE_ALL  play in shuffled order`

**19. int getShuffleMode()**

`Description：  Gets the shuffle mode for this session.`

**20. setRepeatMode(int repeatMode)**

`Description：  `

`Sets the repeat mode for this session，the repeatMode Must be one of the followings：`    
`PlaybackStateCompat.REPEAT_MODE_NONE  play in order`  
`PlaybackStateCompat.REPEAT_MODE_ONE   Single cycle`    
`PlaybackStateCompat.REPEAT_MODE_ALL   List loop`

**21. int getRepeatMode()**

`Description：  Gets the repeat mode for this session.`

**22. List<SongInfo> getPlayList()**

`Description：  get playlist`

**23. void updatePlayList(List<SongInfo> songInfos)**

`Description：  Update playlist, this method will refresh and load the play data`

**24. SongInfo getNowPlayingSongInfo()**

`Description：  Get the currently playing song information, it may be null, so it is best to judge when using`

**25. String getNowPlayingSongId()**

`Description：  Get the songId of the currently playing song information`

**26. int getNowPlayingIndex()**

`Description：  Get the subscript of the currently playing song in the playlist. If it is not available, the default value is -1.`

**27. long getBufferedPosition()**

`Description：  Get the current buffered position in milliseconds`

**28. long getPlayingPosition()**

`Description：  Get the playback position in milliseconds`

**29. float getPlaybackSpeed()**

`Description：  `

`Get the current playback speed as a multiple of normal playback. This  should be negative when rewinding. A value of 1 means normal playback and 0 means paused.`

**30. Object getPlaybackState()**

`Description：  `

`Gets the underlying framework {@link android.media.session.PlaybackState} object.This method is only supported on API 21+.`

**31. CharSequence getErrorMessage()**

`Description：  Get the user readable optional error message.`

**32. int getErrorCode()**

`Description：`
  
`Get the error code.，The following are possible values for the error code:`    
`PlaybackStateCompat.ERROR_CODE_UNKNOWN_ERROR`  
`PlaybackStateCompat.ERROR_CODE_APP_ERROR`  
`PlaybackStateCompat.ERROR_CODE_NOT_SUPPORTED`  
`PlaybackStateCompat.ERROR_CODE_AUTHENTICATION_EXPIRED`  
`PlaybackStateCompat.ERROR_CODE_PREMIUM_ACCOUNT_REQUIRED`  
`PlaybackStateCompat.ERROR_CODE_CONCURRENT_STREAM_LIMIT`  
`PlaybackStateCompat.ERROR_CODE_PARENTAL_CONTROL_RESTRICTED`  
`PlaybackStateCompat.ERROR_CODE_NOT_AVAILABLE_IN_REGION`  
`PlaybackStateCompat.ERROR_CODE_CONTENT_ALREADY_PLAYING`  
`PlaybackStateCompat.ERROR_CODE_SKIP_LIMIT_REACHED`  
`PlaybackStateCompat.ERROR_CODE_ACTION_ABORTED`  
`PlaybackStateCompat.ERROR_CODE_END_OF_QUEUE`  

**33. int getState()**

`Description：  `

`Get the current state of playback. One of the following:`  
`PlaybackStateCompat.STATE_NONE                   `    
`PlaybackStateCompat.STATE_STOPPED                `    
`PlaybackStateCompat.STATE_PLAYING                `    
`PlaybackStateCompat.STATE_PAUSED                 `    
`PlaybackStateCompat.STATE_FAST_FORWARDING        `    
`PlaybackStateCompat.STATE_REWINDING              `     
`PlaybackStateCompat.STATE_BUFFERING              `    
`PlaybackStateCompat.STATE_ERROR                  `    
`PlaybackStateCompat.STATE_CONNECTING             `    
`PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS   `    
`PlaybackStateCompat.STATE_SKIPPING_TO_NEXT       `    
`PlaybackStateCompat.STATE_SKIPPING_TO_QUEUE_ITEM `

**34. long getDuration()**

`Description：  Get media duration, in milliseconds`

**35. List<SongInfo> querySongInfoInLocal()**

`Description：  Scan local media information and convert it to List<SongInfo>`

**36. boolean isPlaying()**

`Description：  It is more convenient to judge whether the current media is playing.`

**37. boolean isPaused()**

`Description：  It is more convenient to judge whether the current media is suspended.`

**38. boolean isIdea()**

`Description：  It is more convenient to judge whether the current media is free.`

**39. boolean isCurrMusicIsPlayingMusic(String songId)**

`Description：  Determine whether the incoming music is playing music, and it is useful to judge the status in the Adapter.`

**40. boolean isCurrMusicIsPlaying(String songId)**

`Description：  Determine if the incoming music is playing`

**41. boolean isCurrMusicIsPaused(String songId)**

`Description：  Determine if the incoming music is paused`

**42. setVolume(float audioVolume)**

`Description：  Set the volume`




**About listener**

StarrySky provides a listener interface OnPlayerEventListener:

```java
public interface OnPlayerEventListener {
    void onMusicSwitch(SongInfo songInfo); //Callback when cutting songs
    void onPlayerStart(); //Callback when starting playback, the relationship with onMusicSwitch is to callback onMusicSwitch first, then callback onPlayerStart
    void onPlayerPause(); //Callback when paused
    void onPlayerStop(); //Callback when stopping playback
    void onPlayCompletion(SongInfo songInfo); //Callback when playback is complete
    void onBuffering(); //Callback while buffering
    void onError(int errorCode, String errorMsg); //Callback when an error occurs
}
```

**1. void addPlayerEventListener(OnPlayerEventListener listener)**

`Description：  Add a play status listener`

**2. void removePlayerEventListener(OnPlayerEventListener listener)**

`Description：  Remove a play status listener`

**3. void clearPlayerEventListener()**

`Description：  Remove all play status listeners`


Add playback progress monitor:

The principle of getting the current playback progress is to call getPlayingPosition() once a second. For convenience, StarrySky provides a tool class TimerTaskManager , of course, you can also implement it yourself.
Take a look at the usage of the TimerTaskManager:

```java
public class MainActivity extends AppCompatActivity {

    private TimerTaskManager mTimerTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Create a TimerTaskManager object
        mTimerTask = new TimerTaskManager();
        //Set update callback
         mTimerTask.setUpdateProgressTask(() -> {
                    long position = MusicManager.getInstance().getPlayingPosition();
                    long duration = MusicManager.getInstance().getDuration();
                     //SeekBar set Max
                    if (mSeekBar.getMax() != duration) {
                        mSeekBar.setMax((int) duration);
                    }
                    mSeekBar.setProgress((int) position);
                });
        //Start getting progress, which can usually be called in onPlayerStart
        mTimerTask.startToUpdateProgress();
        //Stop the progress, usually in the onPlayerPause and onPlayerStop, onPlayCompletion, onError and other methods
        mTimerTask.stopToUpdateProgress();
        //Free up resources, which can usually be called in onStop or onDestroy
        mTimerTask.removeUpdateProgressTask();
    }
}
```

Timed playback function:

The timing play principle is also to call the stopMusic() method periodically, which can be implemented by itself, or by using the encapsulated timing method in the TimerTaskManager:

```java
mTimerTask = new TimerTaskManager();

//Start timing
mTimerTask.startCountDownTask(10 * 1000, new TimerTaskManager.OnCountDownFinishListener() {
    @Override
    public void onFinish() {
        //on finish
        MusicManager.getInstance().stopMusic();
    }

    @Override
    public void onTick(long millisUntilFinished) {
        //on tick
        mTimeView.setText("time："+millisUntilFinished);
    }
});

//cancel timing
mTimerTask.cancelCountDownTask();
```


**About Notification bar**

The API related to the notification bar can be found in the [Quick Integration Notification Bar](https://github.com/lizixian18/MusicLibrary/blob/StarrySkyJava/readme/快速集成通知栏-EN.md).

**1. void setNotificationConstructor(NotificationConstructor constructor)**

`Description：  Set the notification bar configuration, which should be created and called in the Application`

**2. NotificationConstructor getConstructor()**

`Description：  Get notification bar configuration`

**3. void updateFavoriteUI(boolean isFavorite)**

`Description：  If there is a favorite or favorite button in the notification bar, you can call this method to change whether the button is selected or not.`

**4. void updateLyricsUI(boolean isChecked)**

`Description：  If there is a button for displaying lyrics in the notification bar, this method can be called to change whether the button is selected or not.`


The API method in MusicManager is probably the same, and will gradually improve the new method in the future.。


