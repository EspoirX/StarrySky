### SoundPool

StarrySky 提供 SoundPool 功能，适合播放短且频繁的音频。具体实现类是 **SoundPoolPlayback**

要使用 SoundPool 功能，需要通过 soundPool() API 去获取相关实例。下面是相关API:

| 编号  | API              | 作用                                         |
|:----|:-----------------|:-------------------------------------------|
| 1   | prepareForAssets | 从 assets 加载，参数 list 里面传文件名即可，不需要传整个路径      |
| 2   | prepareForRaw    | 从 raw 加载，参数 list 里面传 R.raw.xxx             |
| 3   | prepareForFile   | 从 File 加载                                  |
| 4   | prepareForPath   | 从 本地路径 加载,参数 list 里面传完整路径                  |
| 5   | prepareForHttp   | 从网络加载，参数 list 里面传 url地址                    |
| 6   | playSound        | 播放，播放前请先完成加载音频，其他参数解释查看注释，方法返回 streamID    |
| 7   | pause            | 暂停指定播放流的音效，参数 streamID 通过 playSound 返回     |
| 8   | resume           | 继续播放指定播放流的音效，参数 streamID 通过 playSound 返回   |
| 9   | stop             | 终止指定播放流的音效，参数 streamID 通过 playSound 返回     |
| 10  | setLoop          | 设置指定播放流的循环.                                |
| 11  | setVolume        | 设置指定播放流的音量.                                |
| 12  | getVolume        | 获取指定播放流当前音量                                |
| 13  | setPriority      | 设置指定播放流的优先级，playSound 注释中已说明 priority 的作用. |
| 14  | unload           | 卸载一个指定的音频资源.注意参数是 soundID                  |
| 15  | release          | 释放SoundPool中的所有音频资源.                       |

prepareForXXX 相关方法加载音频，只会加载一次，所以重复调用也没关系，如果要重新加载，请调用 release 方法

使用例子：

<img src="https://s2.loli.net/2023/01/18/ZMTxeUurEqW6imI.png" width="850"> 