# Media caching

The media caching feature, also known as the side-by-side feature, is implemented using ExoPlayer's own download stream feature, although ExoPlayer supports the ability to download live streams, but to be on the safe side,
StarrySky only supports caching for non-streaming audio.

## Initialize the cache function

To use the caching feature, first configure it in the Application via the ExoDownload class:

```java
public class TestApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        //初始化
        MusicManager.initMusicManager(this);
        //Setting the cache
        String destFileDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/ExoCacheDir";
        ExoDownload.getInstance().setOpenCache(true); //打开缓存开关
        ExoDownload.getInstance().setShowNotificationWhenDownload(true);
        ExoDownload.getInstance().setCacheDestFileDir(destFileDir); //设置缓存文件夹
    }
}
```

As you can see, ExoDownload is a singleton with three main configurations: 

- setOpenCache()  
Whether to enable the cache function, this function will only be enabled if it is set to true. This method can be called anywhere, that is, you can turn the media cache function on or off at any time.

- setCacheDestFileDir()  
Configure the storage path of the download cache. If not set, use the default path to store.

- setShowNotificationWhenDownload()  
Configure whether there is a notification bar prompt during the media download process and when the download succeeds or fails. The default is false. Here is a description.
When there is a notification bar, the download service is a front-end service, the system is not easy to kill, and when there is no notification bar, there is a risk that the download service is killed by the system.

After the configuration is complete, the media caching function is completed, as long as the cached media can be played when there is no network.

## Other functions

ExoDownload also provides some other features, mainly for some tool methods for saving cached folder operations.

**boolean deleteAllCacheFile()**  

`Description: Delete all cache files and return the deletion result`

**void deleteCacheFileByUrl(String url)**

`Description: Delete a specific cache file (if any) based on the media url`

**long getCachedSize()**

`Description: Get the size of the cache file`