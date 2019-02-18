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


