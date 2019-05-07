# 播放音频前需要先请求接口获取url这类需求的解决方案

有些同学可能的需求可能是这样的：音频播放的 url 是动态的，不是固定的，需要播放前先请求一下接口获取，然后再播放。

对于这样的需求，StarrySky 引入了一套 **目标方法前置检验模型** 去解决，让这类需求实现变得更加优雅。

模型Github地址：[DelayAction](https://github.com/feelschaotic/DelayAction)，感谢作者提供的优秀思路，该模型也适合于其他类似的场景。
建议先阅读 DelayAction 的相关文章，以便更加理解。

## 使用方法
（这里举一个例子，但不限定就是这样实现，可自己根据实际灵活运用。）

假设音频 A 播放的 url 要先请求接口获取才能得到。

**第一步**
 
 新建一个类，实现 Valid 接口：
 
```java
public class RequestMusicUrlValid implements Valid {

    private boolean isGetUrl; //是否已经得到url
    private SongInfo mSongInfo; //请求完后得到的 songInfo
    private Context mContext;

    public RequestMusicUrlValid(Context context) {
        mContext = context;
    }

    @Override
    public boolean preCheck() {
        return isGetUrl;
    }

    public SongInfo getSongInfo() {
        return mSongInfo;
    }

    @Override
    public void doValid() {
        //这里模拟请求接口操作，请求完成后修改 preCheck 的状态，然后做自己要做的操作，做完后调用一下 doCall 方方法
        
        //模拟接口请求成功
        Toast.makeText(mContext, "请求接口成功", Toast.LENGTH_SHORT).show();
        
        //请求成功后修改一下状态，告诉模型请求成功了，不需要再请求
        isGetUrl = true; 

        //请求完后做自己的操作，这里举例把接口信息包装成songInfo
        mSongInfo = new SongInfo();
        mSongInfo.setSongId("111");
        mSongInfo.setSongUrl("http://music.163.com/song/media/outer/url?id=317151.mp3&a=我");
        mSongInfo.setSongCover("https://www.qqkw.com/d/file/p/2018/04-21/c24fd86006670f964e63cb8f9c129fc6.jpg");
        mSongInfo.setSongName("心雨");
        mSongInfo.setArtist("贤哥");

        //调用一下 doCall ，继续执行，才会执行后续的 Action 
        DelayAction.getInstance().doCall();
    }
}
```

Valid 接口需要实现的有两个方法，preCheck 和 doValid

**第二步**

````java
//请求接口后再播放示例
findViewById(R.id.validPlay).setOnClickListener(v -> {
    
    RequestMusicUrlValid valid = new RequestMusicUrlValid(MainActivity.this);
    DelayAction.getInstance()
            .addAction(() -> {
                //添加验证完成后的 action，这里是播放音频
                MusicManager.getInstance().playMusicByInfo(valid.getSongInfo());
            })
            .addValid(valid) //添加验证模型，这里是刚刚的请求接口操作
            .doCall(); //执行
    
});
````

代码就是这样，算是一种比较优雅的实现方式了吧，接口请求和播放操作都得到了封装，解偶了请求和播放的操作，可以复用。

如果不理解可以先阅读下 [DelayAction](https://github.com/feelschaotic/DelayAction) 。
