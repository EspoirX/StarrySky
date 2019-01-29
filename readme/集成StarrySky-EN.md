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
    }

    @Override
    protected void onStart() {
        super.onStart();
        MediaSessionConnection.getInstance(this).connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        MediaSessionConnection.getInstance(this).disconnect();
    }
}
```

MediaSessionConnection is a singleton mode, connect() is the connection method, and disconnect() is the disconnect method, which is usually called in pairs in onStart() and onStop() .
This can be written to your BaseActivity or it can be called as needed. There are other methods in MediaSessionConnection, you can [click to view](https://github.com/lizixian18/MusicLibrary/blob/StarrySkyJava/starrysky/src/main/java/com/lzx/starrysky/manager/MediaSessionConnection.Java),
There are notes in it.

**Third step**

Some permission issues, such as you want to use the cache function, then there will be some problems with the read and write permissions application, because StarrySky does not have the logic to apply for these permissions, so if necessary in the process of use,
Please be flexible in applying for these permissions.


After completing these three steps, StarrySky has been successfully integrated into your project.


