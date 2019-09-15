# 集成 StarrySky

StarrySky 的初始化方法在 StarrySky 类中。集成它非常简单，一句话即可：

```java
public class TestApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        StarrySky.init(this);
    }
}
```

集成完成了! ^_^


# StarrySky 相关配置

StarrySky 是一个高扩展性的音频集成框架，你可以自定义实现各种功能，下面一一介绍。

1. 自定义内部的图片加载器
2. 自定义实现通知栏
3. 自定义缓存实现
4. 自定义音频数据管理
5. 自定义播放队列管理
6. 自定义播放器实现
7. 支持播放前操作（比如播放前要先请求接口获取 url 再播放等）

StarrySky 的配置相关信息都在 StarrySkyConfig 这个类里面，如果需要添加相关配置，只需要新建一个类，并继承它。然后
实现各种配置，在初始化时添加到第二个参数中即可，例如：

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

现在可能有点懵逼，现在开始一一说明：

## 1. 自定义内部的图片加载器

StarrySky 内部是需要图片加载的，主要用在音频封面的加载，默认的实现是基于 HttpURLConnection 去下载的，默认的实现类
是 DefaultImageLoader。

要自己实现图片加载的方法，首先是要实现 ImageLoaderStrategy 接口，这个接口提供图片加载信息的，比如我要用 Glide 去
替代默认的图片加载，可以这么写：

```java
public class GlideLoader implements ImageLoaderStrategy {

    @Override
    public void loadImage(Context context, String url, ImageLoaderCallBack callBack) {
        Glide.with(context).asBitmap().load(url)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        callBack.onBitmapLoaded(resource);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        callBack.onBitmapFailed(errorDrawable);
                    }
                });
    }
}
```

可以看到，实现 ImageLoaderStrategy 接口，需要实现 loadImage 方法，loadImage 方法有三个参，分别是上下文，图片链接和一个请求回调。
上下文 context 的来源是初始化时传入的 Application，url 即是图片的 url，callback 有两个回调方法：

```java
public interface ImageLoaderCallBack {
    void onBitmapLoaded(Bitmap bitmap);

    void onBitmapFailed(Drawable errorDrawable);
}
```

图片加载成功时回调 onBitmapLoaded ，需要传入一个 bitmap。失败是传入 onBitmapFailed。内部有对参数做非空判断，所以 null 时也不会蹦。

实现好自己的图片加载后，怎么设置给 StarrySky 呢，这时候需要用到上面说过的 StarrySkyConfig，创建一个类继承 StarrySkyConfig，并重写
applyStarrySkyRegistry 方法：

```java
private static class TestConfig extends StarrySkyConfig {

    @Override
    public void applyStarrySkyRegistry(@NonNull Context context, StarrySkyRegistry registry) {
        super.applyStarrySkyRegistry(context, registry);
        registry.registryImageLoader(new GlideLoader());
    }
}
```

通过 StarrySkyRegistry#registryImageLoader 方法注册刚刚实现好的 GlideLoader 到 StarrySky 即可。

applyStarrySkyRegistry 是注册组件的方法，StarrySkyRegistry 是注册器。


## 2. 自定义实现通知栏

通知栏可以分为系统通知栏和自定通知栏，StarrySky 内部默认实现了这两种通知栏，同时也支持用户自己去实现。


### NotificationConfig 通知栏配置类

[NotificationConstructor](https://github.com/lizixian18/MusicLibrary/blob/StarrySkyJava/starrysky/src/main/java/com/lzx/starrysky/notification/NotificationConstructor.java)
是通知栏构造者，也是配置类，里面可以配置很多参数去对应配置通知栏的相关操作：

| 变量名  |   功能  |
| :--------     |   :----------   |
| String targetClass | 通知栏点击转跳界面，传入的是类的全路径 |
| String contentTitle       | 通知栏标题    |
| String contentText        | 通知栏内容    |
| PendingIntent nextIntent  |  下一首按钮 PendingIntent,如果想自己实现下一首按钮点击，可设置这个 |
| PendingIntent preIntent      |  上一首按钮 PendingIntent,功能同上    |
| PendingIntent closeIntent       | 关闭按钮 PendingIntent,功能同上，closeIntent 的默认实现是 stopMusic()  |
| PendingIntent playIntent        | 播放按钮 PendingIntent,功能同上    |
| PendingIntent pauseIntent   | 暂停按钮 PendingIntent,功能同上    |
| PendingIntent playOrPauseIntent    | 播放/暂停按钮 PendingIntent,功能同上    |
| PendingIntent stopIntent         |  停止按钮 PendingIntent,功能同上    |
| PendingIntent downloadIntent        | 下载按钮 PendingIntent    |
| PendingIntent favoriteIntent    | 喜欢或收藏按钮 PendingIntent    |
| PendingIntent lyricsIntent  | 桌面歌词按钮 PendingIntent，同 喜欢或收藏按钮    |

在系统通知栏中，有默认实现的 PendingIntent 是 nextIntent，preIntent，playIntent，pauseIntent，其他都没有默认实现。
在自定义通知栏中，有默认实现的 PendingIntent 是 nextIntent，preIntent，playIntent，pauseIntent，playOrPauseIntent，closeIntent，
其他都没有默认实现。如果你的通知栏中还有其他按钮，则需要自己实现点击事件。

| 变量名  |   功能  |
| :--------     |   :----------   |
|pendingIntentMode  |  设置通知栏点击模式，有三种：MODE_ACTIVITY，MODE_BROADCAST，MODE_SERVICE。分别对应 PendingIntent.getActivity()，PendingIntent.getBroadcast()，PendingIntent.getService()，默认是 PendingIntent.getActivity()  |
|skipPreviousDrawableRes | 在系统通知栏中，上一首按钮的 drawable res，如果不传，则使用默认的 [drawable res](https://github.com/lizixian18/MusicLibrary/blob/StarrySkyJava/starrysky/src/main/res/drawable-xxhdpi/ic_skip_previous_white_24dp.png)    |
|skipNextDrawableRes |在系统通知栏中，下一首按钮的 drawable res，如果不传，则使用默认的 [drawable res](https://github.com/lizixian18/MusicLibrary/blob/StarrySkyJava/starrysky/src/main/res/drawable-xxhdpi/ic_skip_next_white_24dp.png)    |
|pauseDrawableRes |在系统通知栏中，正在播放时，播放按钮显示的 drawable res，如果不传，则使用默认的 [drawable res](https://github.com/lizixian18/MusicLibrary/blob/StarrySkyJava/starrysky/src/main/res/drawable-xxhdpi/ic_pause_white_24dp.png) |
|playDrawableRes |在系统通知栏中，暂停状态时，播放按钮显示的 drawable res，如果不传，则使用默认的 [drawable res](https://github.com/lizixian18/MusicLibrary/blob/StarrySkyJava/starrysky/src/main/res/drawable-xxhdpi/ic_play_arrow_white_24dp.png)|
|smallIconRes |对应通知栏的 smallIcon，不传则使用默认的 [smallIcon](https://github.com/lizixian18/MusicLibrary/blob/StarrySkyJava/starrysky/src/main/res/drawable-xxhdpi/ic_notification.png)|


### 打开通知栏开关

无论是什么通知栏，如果不打开通知栏开关的话，是不会显示的，继承 StarrySkyConfig 并重写 applyOptions 方法，这个方法就是配置各种配置用的：

```java
private static class TestConfig extends StarrySkyConfig {
    @Override
    public void applyOptions(@NonNull Context context, @NonNull StarrySkyBuilder builder) {
        super.applyOptions(context, builder);
        builder.setOpenNotification(true);
    }
}
```
StarrySkyBuilder 负责构建各种配置，这里通过设置 StarrySkyBuilder#setOpenNotification 为 true 打开通知栏开关。

打开通知栏开关后，默认显示的是系统通知栏。这时候播放时，你会发现已经有系统通知栏显示出来了。

### 自定义通知栏

通知栏是通过工厂模式去创建的，当然自定义通知栏也是在 StarrySkyConfig 中配置，但为了代码清晰，特单独做了个方法出来配置，这个
方法是 getNotificationFactory：

```java
private static class TestConfig extends StarrySkyConfig {
    @Override
    public void applyOptions(@NonNull Context context, @NonNull StarrySkyBuilder builder) {
        super.applyOptions(context, builder);
        builder.setOpenNotification(true);
    }

    @Override
    public StarrySkyNotificationManager.NotificationFactory getNotificationFactory() {
        return StarrySkyNotificationManager.CUSTOM_NOTIFICATION_FACTORY;
    }
}
```

如上代码所示，StarrySky 内部已经默认实现了一个自定义的通知栏 CUSTOM_NOTIFICATION_FACTORY，当返回这个后，自定义通知栏
已经实现好了，是不是很快。 当然自定义通知栏还需要自定义一些布局，因为不需要写一句代码，只需要自己实现布局即可，所以布局上
的控件肯定是要有一些规则才能被 StarrySky 匹配到的。可以看这个文档：

快速实现通知栏






