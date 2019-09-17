# StarrySky 介绍

StarrySky 是一个音频集成库，鉴于在一些项目中如果需要集成音频播放功能的话，都离不开对播放器的封装，对播放控制的封装，对 API 的
封装等操作，其实这些操作在不同项目中都是大同小异的，所以 StarrySky 就是这样一个集成了播放音频所需的操作的一个库。

StarrySky 主打集成简单，代码小巧简单易读懂，扩展性强，使用方便等方向。

StarrySky 总体来说可以分为几个模块：

1. API 控制模块
2. 音频数据存储模块
3. 播放队列管理模块
4. 播放器实现模块
5. 通知栏模块
6. 缓存模块

## StarrySky 包结构图

<img src="https://raw.githubusercontent.com/lizixian18/MusicLibrary/StarrySkyJava/art/starrysky包结构.png">

1. common 包里面主要是与 MusicService 端连接的管理类。
2. control 包主要是 API 的播放控制类。
3. ext 包主要是一些扩展类。
4. notification 主要是放通知栏相关的东西
5. playback 是播放相关的东西，里面分了几个包：

  （1）manager 是播放管理

  （2）offline 是缓存相关的东西

  （3）player 是播放器的实现

  （4）queue 是播放队列的管理

6. provider 包是放音频数据存储的相关东西。
7. registry 是一些组件的注册，主要用来配置
8. unils 就是一些工具类

了解了主要有哪些模块和包结构后可以更快地了解代码结构和解决问题。


## StarrySky 初始化配置信息介绍

在 Github 主页时，大家想必现在已经知道了 StarrySky 是通过 init 方法去初始化的。那么如何在初始化时配置一些信息呢（如通知栏，缓存等）？

那就要用到 init 方法的第二个参数 StarrySkyConfig 了：

```java
public abstract class StarrySkyConfig {

    /**
     * 通用配置
     */
    public void applyOptions(@NonNull Context context, @NonNull StarrySkyBuilder builder) {
    }

    /**
     * 添加组件
     */
    public void applyStarrySkyRegistry(@NonNull Context context, StarrySkyRegistry registry) {
    }

    /**
     * 通知栏配置
     */
    public StarrySkyNotificationManager.NotificationFactory getNotificationFactory() {
        return null;
    }

    /**
     * 缓存配置
     */
    public StarrySkyCacheManager.CacheFactory getCacheFactory() {
        return null;
    }
}
```
StarrySkyConfig 是一个抽象类，StarrySky 的所有配置都是通过它去实现的，使用时需要继承它，然后重写相关方法即可，比如：

```java
public class TestApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        StarrySky.init(this, new TestConfig());
    }

    private static class TestConfig extends StarrySkyConfig {
        //...
    }
}
```

### applyOptions

StarrySkyConfig 有四个方法，作用如注释所示，applyOptions 的调用时机是在 StarrySky 实例化前，其他方法是在StarrySky 实例化后。
先看第一个方法 applyOptions，它是一个通用配置方法，通过 StarrySkyBuilder 去构建一些参数，然后再实例化 StarrySky。

```java
public class StarrySkyBuilder {

    //客户端与Service链接管理类
    private IMediaConnection mConnection;
    //媒体信息存储管理类
    private MediaQueueProvider mMediaQueueProvider;
    //播放队列管理类
    private MediaQueue mMediaQueue;
    //通知栏开关
    boolean isOpenNotification;
    //缓存开关
    boolean isOpenCache;
    //缓存文件夹
    String cacheDestFileDir;

    //...
}
```
可以看到 StarrySkyBuilder 里面有以上几个变量，分别是负责与 MusicService 链接的管理类，媒体信息存储管理类，播放队列管理类以及一些开关配置，
**这就意味着，用户如果不满意默认实现的话，可以自定义实现这些内容。**


IMediaConnection 是一个接口，它的默认实现类是 MediaSessionConnection，它负责管理与音频服务 MusicService 链接以及从服务中获取一些信息等操作。
如果用户需要自己去实现它，则可以参考 MediaSessionConnection，最后重写 StarrySkyConfig 的 applyOptions
方法并在 StarrySkyBuilder 中赋值给 mConnection 即可。


MediaQueueProvider 是一个接口，它的默认实现类是 MediaQueueProviderImpl，它的作用是保存播放时的播放列表信息，并提供增删查改等一些功能。
如果要自己实现的话，同样地自己实现 MediaQueueProvider 接口再赋值给 StarrySkyBuilder 即可。


MediaQueue 是一个接口，它是播放队列管理类，它的默认实现类是 MediaQueueManager，MediaQueueManager 继承了 MediaQueueProvider，
用 MediaQueueProvider 使用的是里面的数据源，它主要管理的是当前播放下标等信息，自己实现的话可以参考 MediaQueueManager。


### applyStarrySkyRegistry

这个方法的作用是通过 StarrySkyRegistry 去注册添加或替换 StarrySky 里的一些组件。

```java
public class StarrySkyRegistry {

    private ValidRegistry mValidRegistry;
    private ImageLoaderRegistry mImageLoaderRegistry;
    private NotificationRegistry mNotificationRegistry;
    private CacheRegistry mCacheRegistry;
    private Playback mPlayback;

    public StarrySkyRegistry(Context context) {
        mValidRegistry = new ValidRegistry();
        mNotificationRegistry = new NotificationRegistry();
        mCacheRegistry = new CacheRegistry();
        mImageLoaderRegistry = new ImageLoaderRegistry(context);
    }

    //...
}
```

可以看到 StarrySkyRegistry 里面有这么几个变量信息。

ValidRegistry 是一个叫做播放前验证的组件，它的实现是一个简化的责任链模式，主要作用是可以在播放之前做一些操作，比如
播放前先请求接口获取 url；播放前先埋点；播放前先获取一下读写文件权限等等。</br>

用法：调用 StarrySkyRegistry#appendValidRegistry 方法添加验证即可：

```java
private static class TestConfig extends StarrySkyConfig {
    @Override
    public void applyStarrySkyRegistry(@NonNull Context context, StarrySkyRegistry registry) {
        super.applyStarrySkyRegistry(context, registry);
        registry.appendValidRegistry(new RequestSongInfoValid());
    }
}

public static class RequestSongInfoValid implements Valid {

    @Override
    public void doValid(SongInfo songInfo, ValidCallback callback) {
        //...
    }
}
```
实现 Valid 接口需要实现 doValid 方法，该方法会在播放前执行，考虑到 doValid 的实现会有异步的请求（比如请求网络），所以需要参数 ValidCallback，
如果执行完验证需要调用一下 callback 的方法。具体可查看 [播放音频前需要先请求接口获取url这类需求的解决方案](https://github.com/lizixian18/MusicLibrary/blob/StarrySkyJava/readme/播放前需求先请求接口获取url时的处理方法.md)
 这个文档。


ImageLoaderRegistry 是图片加载引擎，StarrySky 内的图片加载主要用于封面加载，用户可通过实现 ImageLoaderRegistry 接口实现自己的图片加载引擎，
同样地通过 StarrySkyRegistry 注册进去即可，具体可查看 [自定义实现图片加载器](https://github.com/lizixian18/MusicLibrary/blob/StarrySkyJava/readme/自定义图片加载器说明.md)
文档。


NotificationRegistry 是通知栏注册，用户可以通过他实现自定义通知栏以及自己实现自己的通知栏，具体可参考文档 [快速集成通知栏](https://github.com/lizixian18/MusicLibrary/blob/StarrySkyJava/readme/快速集成通知栏.md)


CacheRegistry 是缓存组件，即边播边存，StarrySky 默认实现了一个基于 ExoPlayer 的边播边存功能，同时支持用户自定义实现自己的边播边存，具体
可以参考文档 [媒体缓存功能](https://github.com/lizixian18/MusicLibrary/blob/StarrySkyJava/readme/媒体缓存功能.md)


Playback 是播放器实现，StarrySky 内部默认的播放器实现是 ExoPlayer，如果觉得实现得不好或者不想用 ExoPlayer，通过 registryPlayback 方法即可
自定义实现播放器。只需要实现 Playback 接口即可，具体的实现可以参考默认实现类 ExoPlayback。


### getNotificationFactory

为了代码简洁清晰，特意分出了一个通知栏创建的相关方法 getNotificationFactory，通知栏是通过通知栏工厂 NotificationFactory 构建出来的，重写
getNotificationFactory 方法即可实现自己的通知栏，具体可参数文档  [快速集成通知栏](https://github.com/lizixian18/MusicLibrary/blob/StarrySkyJava/readme/快速集成通知栏.md)


## getCacheFactory

同样地，也是为了代码简洁清晰，分出来来这么一个方法用来自定义实现媒体缓存，缓存的结构跟通知栏差不多，也是通过工厂类构建的，重写 getCacheFactory
即可实现自己的缓存功能，具体可参考文档 [媒体缓存功能](https://github.com/lizixian18/MusicLibrary/blob/StarrySkyJava/readme/媒体缓存功能.md)


StarrySky 的配置讲完了，可以看到 StarrySky 完全支持用户自定义实现各种组件，具有良好的扩展性。更多功能自己探索哦。