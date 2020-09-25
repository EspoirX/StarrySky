# StarrySky 使用说明


## 其他说明（如有需要请先阅读说明）
- [StarrySky介绍](https://github.com/lizixian18/MusicLibrary/blob/StarrySkyJava/readme/StarrySky介绍.md)

## 初始化

```kotlin
open class TestApplication : Application() {
    @Override
    override fun onCreate() {
        super.onCreate()
       StarrySky.init(this)
    }
}
```
StarrySky 最简单的初始化如上所示，但它可以配置更多的功能，让我们看看 init 方法：
```kotlin
/**
 * 上下文，配置，连接服务监听
 */
@JvmStatic
fun init(application: Application, config: StarrySkyConfig = StarrySkyConfig(), connection: ServiceConnection? = null) {
   //...
}
```
可以看到有三个参数，第三个参数是连接音频 Service 的监听，大家可以通过它来监听 Service 是否连接成功。  
重点看下第二个配置参数 StarrySkyConfig。

## StarrySkyConfig
StarrySkyConfig 是一个配置类，通过 Build 模式去设置各种配置，具体有什么配置可以自行查看代码，里面有注释。

### 1. 配置通知栏

通知栏分为系统通知栏和自定义通知栏，默认开启的是系统通知栏。

** 开启系统通知栏： ** 

把配置中的 isOpenNotification 设为 true 即可开启。

** 开启自定义通知栏： ** 

在 StarrySky 中，通知栏都是实现了 INotification 接口的，而所有通知栏都是通过 NotificationFactory 工厂类去创建，StarrySky 默认帮大家创建了
一个自定义通知栏实现：CustomNotification。所有要配置自定义通知栏，只需要通过配置的 setNotificationFactory 方法即可：
```kotlin
val config = StarrySkyConfig().newBuilder()
    .setNotificationFactory(object : StarrySkyNotificationManager.NotificationFactory {
        override fun build(context: Context, config: NotificationConfig?): INotification {
            //使用自定义通知栏
            return StarrySkyNotificationManager.CUSTOM_NOTIFICATION_FACTORY.build(context, config)
        }
    })
    .build()
```

CUSTOM_NOTIFICATION_FACTORY 对象指的就是 CustomNotification 的 NotificationFactory 实现。  
当然，自定义通知栏并不是这样就完成了，还需要自己根据需要和相应的命名规则创建对应的布局才算完成。  
具体操作请参考文档： [快速集成通知栏](https://github.com/lizixian18/MusicLibrary/blob/StarrySkyJava/readme/快速集成通知栏.md)

### 2. 配置缓存
打开缓存功能只需要在配置中设置 isOpenCache 为 true 即可完成，同时，你也可以通过配置 cacheDestFileDir 来自定义缓存的文件夹。
缓存的默认实现是使用了 Exoplayer 自带的缓存功能。如果你要自定义，比如使用 videocache 等第三方缓存库的话，你也可以通过配置 cache 去完成：
```kotlin
val config = StarrySkyConfig().newBuilder()
    .isOpenCache(true)
    .setCacheDestFileDir("xxx")
    .setCache(object :ICache{
        override fun getProxyUrl(url: String): String? {
        }
    
        override fun getCacheDirectory(context: Context, destFileDir: String?): File? {
        }
    
        override fun isCache(url: String): Boolean {
        }
    
        override fun startCache(url: String) {
        }
    })
    .build()
```
更多操作请参考文档：[媒体缓存功能](https://github.com/lizixian18/MusicLibrary/blob/StarrySkyJava/readme/媒体缓存功能.md)

### 3. 配置拦截器
拦截器的功能是在播放前处理一些自己的操作，比如播放器请求播放音频的 url，播放器请求一下某些权限等等操作。都可以通过拦截器去处理。
拦截器配置通过 addInterceptor 方法添加，可以添加多个拦截器，执行顺序跟添加顺序一致。比如项目 demo 中的两个拦截器配置：
```kotlin
val config = StarrySkyConfig().newBuilder()
    .addInterceptor(RequestSongInfoInterceptor())
    .addInterceptor(RequestSongCoverInterceptor())
    .build()
```
更多操作请参考文档： [拦截器](https://github.com/lizixian18/MusicLibrary/blob/StarrySkyJava/readme/拦截器.md)

### 4. 配置是否需要后台服务
StarrySky 启动后，音频播放默认会在服务 MusicService 中运行，以方便可以在后台播放。但是，StarrySky 提供是否需要后台服务的配置
功能，即你可以正常的让音频运行在后台服务中，也可以不要后台服务，只把 StarrySky 当作是一个普通的播放器封装。该功能可以通过
isUserService 配置即可：
```kotlin
val config = StarrySkyConfig().newBuilder()
    .isUserService(false)
    .build()
```

### 5. 配置图片加载器
大家知道，在通知栏中，需要显示歌曲封面，所有就需要用到图片加载功能，StarrySky 默认的图片加载使用最普通的 HttpURLConnection 去完成。
如果大家需要自己实现，比如用 Glide 之类的，即可以通过 imageLoader 去配置：
```kotlin
val config = StarrySkyConfig().newBuilder()
    .setImageLoader(object : ImageLoaderStrategy {
            //使用自定义图片加载器
            override fun loadImage(context: Context, url: String?, callBack: ImageLoaderCallBack) {
                Glide.with(context).asBitmap().load(url).into(object : CustomTarget<Bitmap?>() {
                    override fun onLoadCleared(placeholder: Drawable?) {}
    
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap?>?) {
                        callBack.onBitmapLoaded(resource)
                    }
    
                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        super.onLoadFailed(errorDrawable)
                        callBack.onBitmapFailed(errorDrawable)
                    }
                })
            }
        })
    .build()
```
具体实现就是实现 ImageLoaderStrategy 接口，然后通过 callback 返回。注意参数中的 Context 上下文并不是 Activity 的上下文，而是音频服务  
MusicService 的上下文，如果你配置了不需要后台服务的话，那它就是 Application 的上下文。

### 6. 配置播放器实现
StarrySky 的默认播放器实现是 ExoPlayer ，并且支持了多种音频格式：DASH, SmoothStreaming, HLS，rtmp，flac，  
但是在某些特殊需求里，或许默认的播放实现并不能满足你的需要，所有 StarrySky 支持配置自定义播放器，播放器实现需要实现 Playback 接口。
默认的实现类是 ExoPlayback ，大家若有自定义播放器的需要可以先看看这个类，参加实现。如何配置自定义播放器？只需要配置 playback 方法即可：
```kotlin
val config = StarrySkyConfig().newBuilder()
    .setPlayback(MediaPlayerImpl())
    .build()

class MediaPlayerImpl : Playback {
  //...
}
```

### 7. 配置是否让播放器自动管理焦点
什么是焦点，焦点管理有什么用？不明白的可以自己百度谷歌一下音频焦点的定义。ExoPlayer 有一个功能，是可以自己自动管理焦点，该功能是默认打开的，
如果不想自动管理，要把它关掉，可以通过配置 isAutoManagerFocus 即可：
```kotlin
val config = StarrySkyConfig().newBuilder()
    .isAutoManagerFocus(false) 
    .build()
```
那么关掉之后，想要自己处理怎么办，StarrySky 默认实现了一套自定义焦点管理，具体的实现类是 FocusAndLockManager，在配置中可以通过 setOnAudioFocusChangeListener 
去监听焦点变化以便自己去做一些操作：
```kotlin
val config = StarrySkyConfig().newBuilder()
    .isAutoManagerFocus(false)
    .setOnAudioFocusChangeListener(object : AudioFocusChangeListener {
        override fun onAudioFocusChange(songInfo: SongInfo?, state: Int) {
             //...
        }
    })
    .build()
```
当然这个监听只有在 isAutoManagerFocus 为 false 的时候才会生效。参数里，songInfo 就是当前播放的音频信息。state 就是焦点状态。  
state 的取值为 AUDIO_NO_FOCUS_NO_DUCK，AUDIO_NO_FOCUS_CAN_DUCK，AUDIO_FOCUSED。都定义在 FocusAndLockManager 中。  
而 state 在什么时候取什么值，也可以查看 FocusAndLockManager 这个类了解，就不多说了。

### 8. 配置副歌播放器
副歌播放器？说白了就是允许同时播放 2 个音频的功能，比如在正常播放的同时再播放一些伴奏，播放一些音效的功能，通过 isCreateRefrainPlayer 即可开启：
```kotlin
val config = StarrySkyConfig().newBuilder()
    .isAutoManagerFocus(false)
    .isCreateRefrainPlayer(true)
    .build()
```
同时播放 2 个音频，需要把焦点自动管理给关掉，不然当另一首音频播放的时候，第一首音频会自动停掉。  
如何操作第二个音频播放停止这些功能，可以阅读一下 PlayerControl 这个类，里面定义了音频操作的所有方法，并且都有注释，写得很清楚。  
如何实现这个功能的？原理就是创建了 2 个播放器实例，如果你的需求要同时播放 3 个音频，那么你可以根据这个原理去自己改源码。

