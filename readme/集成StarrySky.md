# 集成 StarrySky

StarrySky 的初始化方法在 StarrySky 类中。集成它非常简单，一句话即可：

```java
public class TestApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        StarrySky.init(this);
    }
}
```

集成完成了! ^_^




