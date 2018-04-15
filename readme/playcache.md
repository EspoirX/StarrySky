### 边播边存配置说明

 
边存边播功能是基于 [AndroidVideoCache](https://github.com/danikula/AndroidVideoCache) 这个库实现的。作用如同这个库中的描述一样
因为在流式传输过程中很多次下载视频是没有意义的！所以就有了边播边存功能。  


要开启边播边存功能，只需要调用 `openCacheWhenPlaying(true)` 即可。示例：

```java
MusicManager.get().openCacheWhenPlaying(true);
```

这个方法不可在 `Application` 中调用，若要自定义配置边播边存的参数和想要在初始化的时候
就开启这个功能，可以使用 `CacheConfig` 去配置。示例：

```java
//边播边存配置
CacheConfig cacheConfig = new CacheConfig.Builder()
        .setOpenCacheWhenPlaying(true)
        .setCachePath(CacheUtils.getStorageDirectoryPath() + "/NiceMusic/Cache/")
        .build();

MusicManager.get()
        .setContext(this)
        .setCacheConfig(cacheConfig)
        .init();
```

`CacheConfig` 中一共可以配置四个参数：

- openCacheWhenPlaying 功能开关，true 即开启，如果设置为 false 或者不设置，功能不会生效，但其他参数配置
是生效的，想要在需要的时候才打开开关的话，调用 `MusicManager` 的 `openCacheWhenPlaying` 方法设为 true 即可。

- cachePath 缓存路径，填写全路径，如果不设置则默认路径为 `/musicLibrary/song-cache/`

- maxCacheSize 最大总缓存大小，Kb 为单位，如果不设置默认为 1G，即 `1024 * 1024 * 1024`

- maxCacheFilesCount 最大缓存文件数量，默认大小为 `512 * 1024 * 1024`

缓存文件的命名方式为 URL 的 MD5，文件名对后缀进行隐藏，以防止被其他 app 或系统扫描到，不支持自定义缓存文件名。


当功能开启之后，第一次播放的时候会自动缓存网络音频到本地，然后再播时就不会取网络资源，而是取缓存文件，从而实现了没网也能播的功能。

**边播边存功能不支持本地音频，不支持任何流式音频，库中会自动过滤，用户无需做任何操作**

`MusicManager` 中提供了三个方法来获取缓存文件相关信息：

- isFullyCached(String songUrl)  根据音频地址判断这个音频是否有缓存
- getCacheFile(String songUrl)   根据音频地址获取缓存文件的File对象
- getCachedSize(String songUrl)  根据音频地址获取缓存文件的大小

getCacheFile 和 getCachedSize 方法是在 isFullyCached 方法的基础上实现的，即如果没有缓存文件，则 getCacheFile 会
返回一个 null 对象，getCachedSize 返回大小是 0。isFullyCached 方法的前提是开启了边播边存功能，即如果没有开启边播
边存功能，isFullyCached 返回 false。

<br>
<br>
<br>