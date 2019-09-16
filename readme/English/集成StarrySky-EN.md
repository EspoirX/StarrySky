# Initialize StarrySky

**First step**

The initialization method for StarrySky is in the MusicManager class.
First call the initMusicManager method in your Application and pass in the context.

```java
public class TestApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        MusicManager.initMusicManager(this);
    }
}
```

The MusicManager class is a singleton mode. The initMusicManager method simply assigns a value to the Context that needs to be used, so don't worry about performance issues.

**Second step**

When using StarrySky, you need to connect to the background processing service first. The call of any method is valid only after the connection is successful. Here is a connection management class MediaSessionConnection, which encapsulates
The related methods of connecting services can be easily used:

```java
public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MediaSessionConnection.getInstance(this).connect();
    }

  
    @Override
    protected void onDestroy() {
        super.onDestroy();
        MediaSessionConnection.getInstance(this).disconnect();
    }
}
```

MediaSessionConnection is a singleton mode, connect() is the connection method, and disconnect() is the disconnect method, 
 It can be called as needed. There are other methods in MediaSessionConnection, you can [click to view](https://github.com/lizixian18/MusicLibrary/blob/StarrySkyJava/starrysky/src/main/java/com/lzx/starrysky/manager/MediaSessionConnection.Java),
There are notes in it.

**Third step**

Some permission issues, such as you want to use the cache function, then there will be some problems with the read and write permissions application, because StarrySky does not have the logic to apply for these permissions, so if necessary in the process of use,
Please be flexible in applying for these permissions.


After completing these three steps, StarrySky has been successfully integrated into your project.

# Custom image loader

The loading of the cover image in StarrySky, the loading of the cover in the notification bar requires the image loader.
The image loading does not limit the fixed frame, which can be defined by itself. The steps are as follows:

**First step**

Implement the [ILoaderStrategy](https://github.com/lizixian18/MusicLibrary/blob/StarrySkyJava/starrysky/src/main/java/com/lzx/starrysky/utils/imageloader/ILoaderStrategy.java) interface, implement the loadImage method, and loadImage is your image loading method:

```java
public interface ILoaderStrategy {
    void loadImage(LoaderOptions options);
}
```

The parameter [LoaderOptions](https://github.com/lizixian18/MusicLibrary/blob/StarrySkyJava/starrysky/src/main/java/com/lzx/starrysky/utils/imageloader/LoaderOptions.java) is the parameter required for image loading. You can use it to get the image loading:

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

If you want to load with Glide, you can refer to the [GlideLoader](https://github.com/EspoirX/StarrySky/blob/StarrySkyJava/app/src/main/java/com/lzx/musiclib/imageloader/GlideLoader.java) in the project. If you are another framework, just implement the loadImage method.

**Second step**

Set the custom image loader by calling the setImageLoader method in the Application.

```java
MusicManager.setImageLoader(new GlideLoader());
```

Of course, if you don't set it, StarrySky will use the default image loader to load the image. The default image loader class is called [DefaultImageLoader](https://github.com/lizixian18/MusicLibrary/blob/StarrySkyJava/starrysky/src/main/java/com/lzx/starrysky/utils/imageloader/DefaultImageLoader.java).

In order to better load the effect, it is recommended to use some mature third-party framework to achieve image loading.
