### 版本更新记录

#### v1.3.6
- 支持播放 rtmp 格式的音频
- 修复播完完所有歌曲后再点击播放没反应的 Bug


#### v1.3.5
- 修复播放模式切换不了的 Bug
- 修复播放列表长度为 0 时切歌崩溃的 Bug
- 去除播放状态 State.NONE

#### v1.3.4（1.3.3 版本各种原因打包失败，所以直接改成 1.3.4 吧）
- 初始化操作与 MusicManager 分离，初始化以及相关配置由 MusicLibrary 类完成
- 优化了随机播放算法，之前的随机播放是简单的由 Math.random() 去完成的，这样的做法很多情况下都会随机到同一首音频，造成不好的体验
新的随机算法是伪随机，做法是通过 shuffle 洗牌算法将播放列表打乱然后顺序播放。
- 通知栏配置新增加了 isNotificationCanClearBySystemBtn 字段，该字段的作用是在使用自定义通知栏的时候，暂停播放时可以点击清除按钮清除通知栏，
具体看 [Issues#20](https://github.com/lizixian18/MusicLibrary/issues/20) 的描述。
- 新增 getAudioSessionId() API，如果需要获取音频频谱之类的操作，你就需要它了
- 新增音频回调 onPlayerStop()，之前播放完成和播放停止都会回调 onPlayCompletion()，现在分离了
- 完善和修复了一些bug


#### v1.3.2 
- 修复了播放和暂停回调会回调两次的 Bug
- 修复了播放完成回调会回调两次的 Bug

#### v1.3.1  
 
 - 添加焦点管理开关,关闭焦点管理可以满足多个音频混播的需求
 - 添加设置音量方法 setVolume(float audioVolume) 可代码设置音量
 - 修复定时播放回调没反应的 Bug
 - 修复播放本地文件，当文件名有空格时找不到文件的 Bug
 
#### v1.3.0

- 新增变速功能，实现改变播放速度和改变播放音调
- API变更 onBuffering(boolean isFinishBuffer) -> onAsyncLoading(boolean isFinishLoading) 详细见 API 文档
- 添加获取缓冲进度方法 getBufferedPosition()
- 新增边播边存相关方法：
  1. isFullyCached(String songUrl) 根据音频地址判断这个音频是否有缓存
  2. getCacheFile(String songUrl) 根据音频地址获取缓存文件的File对象
  3. getCachedSize(String songUrl) 根据音频地址获取缓存文件的大小
  
#### v1.2.9

- 完善 reset 方法
- 暴露 stopNotification 方法
 
#### v1.2.8

- 优化边播边存配置方式：
  在初始化的时候配置边播边存参数

  1. 可自定义缓存路径
  2. 可自定义最大总缓存大小
  3. 可自定义最大缓存文件数量
  4. 可在初始化的时候就把功能开关打开或关闭
  
#### v.1.2.7

- 支持边播边存
- 添加顺序播放模式

#### v1.2.6

- 支持保存播放进度功能
- 添加获取音频时长API

#### v1.2.5

- 升级ExoPlayer到最新版，支持音频直播（DASH, SmoothStreaming, HLS）


#### v1.2.4

- 适配通知栏背景色，兼容低版本as

#### v1.2.3

- 集成系统通知栏，修复自定义通知栏关闭 action 的 Bug
