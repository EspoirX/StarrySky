package com.lzx.musiclibrary.bus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * create by lzx
 * time:2018/11/2
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Subscriber {
    String tag() default Subscription.DEFAULT_TAG; //标记，用于点对点通信

    ThreadMode thread() default ThreadMode.SAMETHREAD;//用于线程切换
}
