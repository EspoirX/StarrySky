### Service相关配置

<img src="https://s2.loli.net/2023/01/18/ExCjMfNHyIc1hVK.png" > 

主要有这 4 个方法，可以让你在后台服务这方面有更多选择：

**connService**： 是否需要后台服务，默认true，区别是播放器是不是运行在 Service 中，false的话所有逻辑是不经过 Service 的。

**isStartService**：是否需要 startService，默认false，false的话就是只有 bindService

**onlyStartService**：是否只是 startService 而不需要 startForegroundService，默认true

**connServiceListener**：连接服务回调，可通过这个监听查看 Service 是否连接成功