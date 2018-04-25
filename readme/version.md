### 版本更新记录

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
