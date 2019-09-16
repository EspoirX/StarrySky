# 播放音频前需要先请求接口获取url这类需求的解决方案

有些同学可能的需求可能是这样的：
1. 音频播放的 url 是动态的，不是固定的，需要播放前先请求一下接口获取，然后再播放。
2. 播放前需要先请求一些权限或先进行一些埋点等其他播放前的操作。


StarrySky 提供了一个验证接口 Valid 去解决这个问题。

**第一步**
新建一个类并实现 Valid 接口：

```java
public static class RequestSongInfoValid implements Valid {
    private MusicRequest mMusicRequest;

    RequestSongInfoValid() {
        mMusicRequest = new MusicRequest();
    }

    @Override
    public void doValid(SongInfo songInfo, ValidCallback callback) {
        if (TextUtils.isEmpty(songInfo.getSongUrl())) {
            mMusicRequest.getSongInfoDetail(songInfo.getSongId(), songUrl -> {
                songInfo.setSongUrl(songUrl); //给songInfo设置Url
                callback.finishValid();
            });
        } else {
            callback.doActionDirect();
        }
    }
}
```

Valid 有一个回调方法 doValid，这个方法会在播放前执行，它有两个参数，songInfo 是当前要播放的音频信息，
由于 doValid 方法里面的逻辑有可能是请求网络等这些异步操作，所以需要一个回调去标识执行完成。

如上代码所示的是播放前请求 url 的例子，首先判断一下 songInfo 里面的 url 是否为空，如果空的话就调用接口
去获取 url，获取到之后给 songInfo 赋值，然后调用 callback 的 finishValid 方法告诉验证系统完成验证，可以
执行下一步了。

如果 url 不为空的话，就不需要请求接口了，直接调用 callback 的 doActionDirect 方法告诉验证系统不需要验证，直接
执行下一步的 action。

实现好 Valid 后，通过 applyStarrySkyRegistry 方法注册进 StarrySky 即可：

```java
private static class TestConfig extends StarrySkyConfig {

    @Override
    public void applyStarrySkyRegistry(@NonNull Context context, StarrySkyRegistry registry) {
        super.applyStarrySkyRegistry(context, registry);
        registry.appendValidRegistry(new RequestSongInfoValid());
    }
}
```

这样，每当播放前，都会先执行 doValid 去获取 url 后再播放了。

