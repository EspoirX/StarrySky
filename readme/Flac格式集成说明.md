# Flac 格式集成说明

因为Flac格式音频的集成需要用到一个 aar 包，但是库中如果有 aar 包，打包上传后，再依赖的时候
会报错，依赖不了，好像是因为 aar 打不进去，但是 jar 又可以，这个问题我一直找不到解决方案，所以特意写一个说明，如果谁
知道怎么搞，麻烦告诉一下我。

## 第一步
如果你需要播放 Flac 音频，就不能通过依赖的方式使用该库了，原因如上，所以第一步下载源代码。在项目文件夹下
有一个 resource 的文件夹，里面有一个 extension-flac.aar 的文件，请把它引入到 starrysky 里面。  

如果不会引入，可以参考下面（会的可以忽略）：

### 第一步在 main 目录下创建 libs 文件夹并吧 aar 文件拷贝进去

### 第二步在 starrysky 的 build.gradle 文件中添加下面代码引入一下
```groovy
   repositories {
        flatDir {
            dirs 'libs' 
        }
    }
    
    ...
    
    implementation(name: 'extension-flac', ext: 'aar')
```

### 第三步在你自己的项目 build.gradle 文件下添加下面代码：

```groovy
 repositories {
        flatDir {
            dirs project(':starrysky').file('libs')
        }
    }
```

### 第四步

打开 starrysky 中的 ExoPlayback 类，找到 buildMediaSource 方法，看到 case C.TYPE_OTHER 这个判断条件，
可以看到我是根据后缀名来判断是不是 flac 文件的，如果需要修改就根据自己需要去修改，不需要就可以不用管。

完成这些后就可以播放 flac 格式的音频了。

