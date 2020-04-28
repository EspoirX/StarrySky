版本更新记录

2.3.7 版本变更比较大，这里说明一下主要的改动：

1. 基本上都用上了 Kotlin，所以还在用 Java 的小伙伴要注意对应调用的修改，比如 StarrySky.with() 要变成  StarrySky.Companion.with() 之类的。

2. 删除了很多没必要的类以及一些回调实现，代码阅读起来会更加简单

3. SongInfo 删除了大部分字段，只保留了几个有必要的字段，同时有一个 objectValue 的 Object 字段，可以在这里设置一些额外的字段

4. 初始化的改变，主要变化在 StarrySkyConfig 这个类，StarrySkyConfig 改成了 builder 模式去设置各种配置，可以通过
StarrySkyConfig().newBuilder().setXXX().build() 去完成你的配置。

5. 原来的 Valid 现在改成了拦截器，逻辑清晰了很多，可以通过 StarrySkyConfig 去添加。

6. 播放模式现在支持 7 种，分别是 顺序播放 列表循环 单曲播放 单曲循环 随机播放 倒序播放 倒序列表循环，循环的意思就是列表播放完了是否要
重新开始播放，应该从字面意思就能理解吧。播放模式现在默认会保存在本地，即如果你设置了 单曲播放，下次打开的时候获取到的也是 单曲播放。

7. 设置播放模式的时候要传两个参数，一个是对应的模式，一个是是否循环，通过这两个参数即可设置成上面说
到的 7 种播放模式了。获取播放模式的时候获取到的是 RepeatMode 对象，里面其实就是包含对应的模式以及是否循环标记。

8. 库的扩展性提高了很多，现在支持自定义实现的有：缓存功能，通知栏，播放器，播放控制类，音频数据提供类，播放队列管理类，
图片加载器，连接管理类。具体的话可以看对应文档。

9. 支持 androidX 了，对应代码在 androidx 分支。

10. 因为改成 kotlin 了，所以从 2.3.7 开始，导入方式不再使用

```groovy
dependencies {
    implementation fileTree(dir: 'libs', include: ['*.jar'])
    implementation 'com.lzx:StarrySkyJava:x.x.x'
}
```

而是使用

```groovy
dependencies {
    implementation fileTree(dir: 'libs', include: ['*.jar'])
    implementation 'com.lzx:StarrySkyKt:x.x.x'
}
```


如果导入不了可以试试加上这个：
```groovy
maven{
    url "https://dl.bintray.com/lizixian/StarrySky/"
}
```