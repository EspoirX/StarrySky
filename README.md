# MusicLib
音乐播放封装

##### 为什么要做这样的一个库：  
1. 刚好在工作中有做到音频播放的业务
2. 浏览了GitHub上很多很优秀的音乐播放器代码，但是没有找到类似可以将音频播放封装起来的库，所以有了这想法。（也许是因为业务不一样的原因，但是如果把基本的功能写在一个库里面，也是挺好的。）

##### 为什么没用到IPC机制去实现：  
大家都知道音乐播放一般都会用到IPC去实现，其实一开始是有用的，但是毕竟多进程的话有很多问题并不那么简单，一开始写demo的时候看上去用得很完美，但是用到实际项目中的时候各种业务就复杂了，所以就出现了很多问题，其中最多的就是会自动断开与服务端的链接，和在实现监听器的时候偶尔会报异常。
想到IPC的作用主要有两个：第一是数据共享，第二是获取更多的内存大小。而且加上随着android版本的升高，保活基本很难实现了，所以想想还是不用IPC了。
关于IPC的相关知识，当然是推荐刚哥的《Android开发艺术探索》。

##### 关于 Demo：  
demo中的音乐来自易源接口，不知道为什么，易源的接口就是慢，当你点击播放的时候可能要等一会，不过应该是能播的，你也可以换成你自己的音乐链接来做测试。


##### Demo 截图：  
<a href="art/image1.png"><img src="art/image1.png" width="30%"/></a>
<a href="art/image2.png"><img src="art/image2.png" width="30%"/></a>

##### 用法：

整个库的功能是通过 MusicManager 这个类去获取和控制的。 
1. 首先在 Application 中进行初始化 ：
``` java
public class MusicApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        MusicManager.get().init(this);
    }
}
```
2. 然后进行服务的绑定和解绑定
``` java
MusicManager.get().bindToService(this,new ServiceConnectionCallback());
MusicManager.get().unbindService(this);
```
如果绑定的时候不想要回调，可以调用一个参数的 bindToService 。之后就可以使用了。

##### Api