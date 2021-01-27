# StarrySky 使用说明

## 初始化

```kotlin
open class TestApplication : Application() {
    @Override
    override fun onCreate() {
        super.onCreate()
        StarrySky.init(this).apply()
    }
}
```
StarrySky 最简单的初始化如上所示，但它可以配置更多的功能，让我们看看初始化的所有 API 方法：

```kotlin
 StarrySky.init(this)
        .setDebug(..)               //是否debug，区别就是是否打印一些内部 log
        .connService(..)            //是否需要后台服务，默认true
        .isStartService(..)         //是否需要 startService，默认false
        .onlyStartService(..)       //是否只是 startService 而不需要 startForegroundService，默认true
        .connServiceListener(..)    //连接服务回调
        .addInterceptor(..)         //添加全局拦截器，可以添加多个
        .setNotificationSwitch(..)  //通知栏开关
        .setNotificationFactory(..) //配置自定义通知栏
        .setNotificationConfig(..)  //配置通知栏其他参数
        .setNotificationType(..)    //选择通知栏类型，系统通知栏和自定义通知栏
        .setOpenCache(..)           //是否开启缓存
        .setCacheDestFileDir(..)    //配置缓存路径
        .setCacheMaxBytes(..)       //配置最大缓存大小，默认512 * 1024 * 1024
        .setCache(..)               //缓存自定义实现
        .setAutoManagerFocus(..)    //是否自动焦点管理
        .setPlayback(..)            //自定义实现播放器
        .setImageLoader(..)         //配置自定义图片加载器
        .apply()
```

可根据自己需要在初始化时配置不同的东西，具体的注释或者使用方法可以查看代码注释和 demo 代码。

### 1. 主页播放
如果你的项目中存在打开主页面需要马上需要播放音频，而你又需要后台 Service 的时候，最好通过 connServiceListener 监听
Service 是否连接成功，等成功后再去播放，否则会出现 Service 没初始化完成，你去播放时，是播放不了的。


### 2. 拦截器
拦截器的功能是在播放前处理一些自己的操作，比如播放器请求播放音频的 url，播放器请求一下某些权限等等操作。都可以通过拦截器去处理。
拦截器配置通过 addInterceptor 方法添加，可以添加多个拦截器，执行顺序跟添加顺序一致。

实现拦截器需要继承 AsyncInterceptor 或者 SyncInterceptor，然后实现 process 方法。

两者的区别相信看命名就知道，因为在实际运用中，有些操作是需要 callback 去回调的，有些则可以直接返回，比如在拦截器中进行同步
请求，你就可以直接拿到结果，如果进行异步请求，则需要一个 callback，这里可根据自己的实际需要选择继承不同的类。

注意一点，process 方法是运行在子线程中的，如果有 UI 操作，可以自己通过 Handler 或者使用库里面封装好的一个 MainLooper 工具类去操作。

拦截器分为两种，在初始化时添加的拦截器称为全局拦截器，然后通过 **  StarrySky.with().addInterceptor(..) ** 添加的拦截器称为局部拦截器，
他们的执行顺序是先执行局部，再执行全局，局部拦截器在当前 Activity onDestroy 后会清空。


### 3. 通知栏

通知栏分为系统通知栏和自定义通知栏，默认开启的是系统通知栏。

在初始化时可通过 setNotificationSwitch 去打开通知栏，可以通过 setNotificationType 去选择使用系统通知栏还是自己定义的通知栏。
在配置之后，你也可以通过 StarrySky 类里面的通知栏相关 API 去单独操作通知栏。

#### 通知栏配置 NotificationConfig 说明

初始化时通过 setNotificationConfig 方法可以设置一个 NotificationConfig，NotificationConfig 里面主要就是配置通知栏的一些
 PendingIntent，UI 资源以及点击时转跳的界面等内容，可以按需配置。

NotificationConfig 是一个 builder 模式，如果你用的是 kotlin 的话，也可以通过这样去构建：
```kotlin
val notificationConfig = NotificationConfig.create {
    targetClass { "com.lzx.musiclib.home.MainActivity" }
    targetClassBundle {
        val bundle = Bundle()
        bundle.putString("notifyKey", "我是点击通知栏转跳带的参数")
        return@targetClassBundle bundle
    }
    pauseIntent {
       //...
        return@pauseIntent xxx
    }
    //...
}
```

#### 如何使用自定义通知栏
使用自定义通知栏，需要在初始化是配置 setNotificationType 为 INotification.CUSTOM_NOTIFICATION 即可。对应的内部实现是 CustomNotification
而使用自定义通知栏，还需要自己按照命名规则定义好自己想要的布局和资源，CustomNotification 内部会自动读取。

在 CustomNotification 内部，使用两种 RemoteViews，对应的是通知栏展开和收起的两种布局。

两种布局的命名规则分别为 ** view_notify_play.xml ** 和 ** view_notify_big_play.xml ** ， 分别对应收起和展开两种。

在布局内部，如果你的布局有使用以下功能按钮的话，按钮对应的控件 id 命名需要按照以下规则来命名：

| 通知栏控件名称    |   命名                |   通知栏控件名称  |   命名         |
| :--------       |   :----------         | :--------      |   :----------       |
| 播放按钮         | img_notifyPlay        | 上一首按钮      | img_notifyPre       |
| 暂停按钮         | img_notifyPause       | 关闭按钮        | img_notifyClose      |
| 停止按钮         | img_notifyStop        | 喜欢或收藏按钮   | img_notifyFavorite   |
| 播放或暂停按钮    | img_notifyPlayOrPause | 桌面歌词按钮    | img_notifyLyrics      |
| 下一首按钮       | img_notifyNext        | 下载按钮        | img_notifyDownload    |
| 封面图片         | img_notifyIcon        | 歌名 TextView   | txt_notifySongName   |
| 艺术家 TextView  | txt_notifyArtistName  |                |                      |

然而，不同的手机系统通知栏背景有可能是浅色也有可能是深黑色，比如 mumu 模拟器是黑底的，华为 mate20 是白底的。
所以通知栏的字体颜色，以及按钮资源等则需要准备深浅颜色两套。（可以参考 demo）

首先是字体适配：

创建 values-v19 和 values-v21 文件夹，然后里面新建一个 style.xml，它们的内容是一样的，就是：

```java
<resources>
    <style name="notification_info" parent="android:TextAppearance.Material.Notification.Info"/>
    <style name="notification_title" parent="android:TextAppearance.Material.Notification.Title"/>
</resources>
```

然后在你写布局的时候，对应的通知栏 title 和 info 的 TextView 就可以引用它们。记住 TextView 不能写死字体颜色，不然就不能适配了。

然后是资源适配：

为了更好的 UI 效果，StarrySky 中的通知栏上一首、下一首、播放、暂停、播放或暂停这五个按钮使用的资源是 `selector`，
 `selector` 里面就是你对应的 normal 和 pressed 图片了。

如果你的布局有使用以下资源的话，对应的命名需要按照以下规则来命名：

| 通知栏背景色  | 资源名称  |   命名       | 通知栏背景色  | 资源名称  |   命名       |
| :-------- | :--------   | :------   | :-------- | :--------   | :------   |
| 浅色背景   | 播放按钮 selector | notify_btn_light_play_selector.xml | 深色背景   | 播放按钮 selector | notify_btn_dark_play_selector.xml |
| 浅色背景   | 暂停按钮 selector | notify_btn_light_pause_selector.xml | 深色背景   | 暂停按钮 selector | notify_btn_dark_pause_selector.xml |
| 浅色背景   | 下一首按钮 selector | notify_btn_light_prev_selector.xml |  深色背景   | 下一首按钮 selector | notify_btn_dark_next_selector.xml |
| 浅色背景   | 上一首按钮 selector | notify_btn_light_prev_selector.xml | 深色背景   | 上一首按钮 selector | notify_btn_dark_prev_selector.xml |
| 浅色背景   | 下一首按钮当没有下一首时的图片资源 | notify_btn_light_next_pressed | 深色背景   | 下一首按钮当没有下一首时的图片资源 | notify_btn_dark_next_pressed |
| 浅色背景   | 上一首按钮当没有上一首时的图片资源 | notify_btn_light_prev_pressed | 深色背景   | 上一首按钮当没有上一首时的图片资源 | notify_btn_dark_prev_pressed |
| 浅色背景   | 喜欢或收藏按钮的图片资源 | notify_btn_light_favorite_normal | 深色背景   | 喜欢或收藏按钮的图片资源 | notify_btn_dark_favorite_normal |
| 浅色背景   | 桌面歌词按钮的图片资源 | notify_btn_light_lyrics_normal | 深色背景   | 桌面歌词按钮的图片资源 | notify_btn_dark_lyrics_normal |
| 深白通用   | 喜欢按钮被选中时的图片资源 | notify_btn_favorite_checked | 深白通用   | 桌面歌词按钮选中时的图片资源 | notify_btn_lyrics_checked |
| 深白通用   | 通知栏 smallIcon 图片资源 | icon_notification | 深白通用   | 下载按钮暂 | 暂时没什么规定，可以随便命名 |   |                      |

自定义通知栏的布局还有资源等，都在代码中有例子，大家如果看得不太明白可以打开参考一下。


#### 如何自己实现自定义通知栏
库内部以及实现好一个默认的自定义通知栏，你只需要按照规则创建好相关布局和资源即可使用，但如果不能满足你的需求，
可以在初始化的时候通过 setNotificationFactory 方法去自己实现一个通知栏。

该方法传入的是 NotificationFactory 接口，我们需要实现里面的 build 方法，build 方法返回的是一个 INotification 接口，所以说
你自己实现的通知栏需要实现 INotification 接口。

INotification 接口有几个重要的方法，分别是 startNotification，stopNotification，onPlaybackStateChanged 等，在实现的时候可以参考
已经有的默认实现 SystemNotification 或者 CustomNotification 即可。


#### 自定义通知栏点击事件例子

有时候我们要在通知栏里面的按钮点击事件上做一些自己的逻辑，比如埋点等，那么就需要自定义点击事件了。自己管理点击事件，其实就是配置一开始说了的
 NotificationConfig 里面的各种 PendingIntent。

关于如何使用，这里举个例子：
```kotlin
private fun getPendingIntent(action: String): PendingIntent? {
    val intent = Intent(action)
    intent.setClass(this, NotificationReceiver::class.java)
    return PendingIntent.getBroadcast(this, 0, intent, 0)
}

val notificationConfig = NotificationConfig.create {
        targetClass { "com.lzx.musiclib.example.PlayDetailActivity" }
        favoriteIntent { getPendingIntent(ACTION_PAUSE) }
    }

//然后初始化的时候设置 notificationConfig 进去

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            INotification.ACTION_PAUSE -> {
                StarrySky.with().pauseMusic()
            }
        }
    }
}
```

当然广播别忘记注册了。


### 4. 配置缓存
打开缓存功能只需要在配置中设置 isOpenCache 为 true 即可完成，同时，你也可以通过配置 setCacheDestFileDir 来自定义缓存的文件夹。

缓存的默认实现是使用了 Exoplayer 自带的缓存功能。如果你要自定义，比如使用 videocache 等第三方缓存库的话，你也可以通过配置 setCache 去完成，
自定义缓存需要实现 ICache 接口。在 demo 中，有使用 videocache 实现的自定义缓存例子，大家可以参考一下。


### 5. 配置图片加载器
大家知道，在通知栏中，需要显示歌曲封面，所有就需要用到图片加载功能，StarrySky 默认的图片加载使用最普通的 HttpURLConnection 去完成。
如果大家需要自己实现，比如用 Glide 之类的，即可以通过 setImageLoader 去配置，demo 中有使用 Glide 加载的例子，大家可以参考一下。



### 6. 配置播放器实现
StarrySky 的默认播放器实现是 ExoPlayer ，并且支持了多种音频格式：DASH, SmoothStreaming, HLS，rtmp，flac，  
但是在某些特殊需求里，或许默认的播放实现并不能满足你的需要，所以 StarrySky 支持配置自定义播放器，播放器实现需要实现 Playback 接口。
默认的实现类是 ExoPlayback ，大家若有自定义播放器的需要可以先看看这个类，参加实现


### 7. 配置是否让播放器自动管理焦点
什么是焦点，焦点管理有什么用？不明白的可以自己百度谷歌一下音频焦点的定义。ExoPlayer 有一个功能，是可以自己自动管理焦点，该功能是默认打开的，
如果不想自动管理，要把它关掉，可以通过配置 setAutoManagerFocus 即可。


如果问题请查看项目 demo 或者加群咨询。
