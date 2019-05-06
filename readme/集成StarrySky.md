# 集成 StarrySky

**第一步**

StarrySky 的初始化方法在 MusicManager 类中。
首先在你的 Application 中调用 initMusicManager 方法并传入上下文即可。

```java
public class TestApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        MusicManager.initMusicManager(this);
    }
}
```

MusicManager 类是一个单例，initMusicManager 方法只是给里面需要用到的 Context 赋值而已，所以不必担心会有什么影响性能的问题。

**第二步**

使用 StarrySky 的时候需要先连接到后台处理服务，任何方法的调用都是连接成功后才会有效。这里提供了一个连接管理类 MediaSessionConnection，里面封装
了连接服务的相关方法，可以方便使用：

```java
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MediaSessionConnection.getInstance().connect();
    }

  
    @Override
    protected void onDestroy() {
        super.onDestroy();
        MediaSessionConnection.getInstance().disconnect();
    }
}
```

MediaSessionConnection 是一个单例，connect() 是连接方法，disconnect() 是断开连接方法，
可以按照实际需要调用。MediaSessionConnection 里面还有其他一些方法，大家可以[点开查看](https://github.com/lizixian18/MusicLibrary/blob/StarrySkyJava/starrysky/src/main/java/com/lzx/starrysky/manager/MediaSessionConnection.java)，
里面都有注释说明。

**第三步**

一些权限问题，比如你要使用缓存功能，那么就会有一些读写权限申请的问题，因为 StarrySky 里面没有做申请这些权限的逻辑，所以在使用的过程中如果有必要，
请自己灵活把握申请这些权限。


完成了这三步，StarrySky 就已经成功集成到你的项目里面了。

# 自定义图片加载器

StarrySky 中封面图片的加载，通知栏中封面的加载都需要图片加载器，图片加载不限定固定的框架，可以由自己定义，步骤如下：

**第一步**

实现 [ILoaderStrategy](https://github.com/lizixian18/MusicLibrary/blob/StarrySkyJava/starrysky/src/main/java/com/lzx/starrysky/utils/imageloader/ILoaderStrategy.java) 接口，实现 loadImage 方法，loadImage 就是你的图片加载方法：
```java
public interface ILoaderStrategy {
    void loadImage(LoaderOptions options);
}
```

其中参数 [LoaderOptions](https://github.com/lizixian18/MusicLibrary/blob/StarrySkyJava/starrysky/src/main/java/com/lzx/starrysky/utils/imageloader/LoaderOptions.java) 是图片加载所需要的参数，你可以通过它拿到图片加载需要用到的东西：

```java
public class LoaderOptions {
    public Context mContext; //上下文
    public int placeholderResId; //占位图
    public int targetWidth; //图片宽
    public int targetHeight; //图片高
    public BitmapCallBack bitmapCallBack; //返回 bitmap 回调
    public String url; //图片连接
}
```

如果你想用 Glide 去加载，可以参考项目中的 [GlideLoader](https://github.com/EspoirX/StarrySky/blob/StarrySkyJava/app/src/main/java/com/lzx/musiclib/imageloader/GlideLoader.java) 的写法，如果你是其他框架，只要实现 loadImage 方法即可。

**第二步**

在 Application 中 调用 setImageLoader 方法设置自定义的图片加载器即可。

```java
MusicManager.setImageLoader(new GlideLoader());
```

当然，如果你不设置的话，StarrySky 内部会使用默认的图片加载器去加载图片，默认的图片加载器的类叫 [DefaultImageLoader](https://github.com/lizixian18/MusicLibrary/blob/StarrySkyJava/starrysky/src/main/java/com/lzx/starrysky/utils/imageloader/DefaultImageLoader.java)。

为了更好的加载效果，推荐自己使用一些成熟的第三方框架去实现图片加载。
