### ExoPlayer FLAC 扩展

该 module 是 ExoPlayer flac 扩展编译后的代码。

地址：[extensions-flac](https://github.com/google/ExoPlayer/blob/release-v2/extensions/flac/README.md)

目前用的版本号是 2.12.0

编译脚本：

```shell script
EXOPLAYER_ROOT="/Users/xxx/Downloads/ExoPlayer"
FLAC_EXT_PATH="${EXOPLAYER_ROOT}/extensions/flac/src/main"
NDK_PATH=/Users/xxx/Documents/android-ndk-r20b   #这里填你解压出来的地址
cd "${FLAC_EXT_PATH}/jni" && \
curl https://ftp.osuosl.org/pub/xiph/releases/flac/flac-1.3.2.tar.xz | tar xJ && \
mv flac-1.3.2 flac
cd "${FLAC_EXT_PATH}"/jni && \
${NDK_PATH}/ndk-build APP_ABI=all -j4
```

EXOPLAYER_ROOT 是 ExoPlayer 源码的路径
FLAC_EXT_PATH  是源码里面flac扩展的文件路径
ndk 用的是 android-ndk-r20b  