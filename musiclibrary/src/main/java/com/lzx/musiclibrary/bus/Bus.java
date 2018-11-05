package com.lzx.musiclibrary.bus;

import com.lzx.musiclibrary.utils.LogUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 简单实现一个EventBus
 * create by lzx
 * time:2018/11/2
 */
public class Bus {

    private HashMap<String, Subscription> subscriptionMap;

    private Bus() {
        subscriptionMap = new HashMap<>();
    }

    public static Bus getInstance() {
        return SingletonHolder.sInstance;
    }

    private static class SingletonHolder {
        private static final Bus sInstance = new Bus();
    }

    /**
     * 订阅
     */
    public void register(Object obj) {
        if (obj == null) {
            return;
        }
        String key = obj.getClass().getName();
        LogUtil.i("key = " + key);
        if (!subscriptionMap.containsKey(key)) {
            subscriptionMap.put(key, new Subscription(obj));
        }
    }

    /**
     * 取消订阅
     */
    public void unregister(Object obj) {
        if (obj == null) {
            return;
        }
        String key = obj.getClass().getName();
        subscriptionMap.remove(key);
    }

    /**
     * 发布消息
     */
    public void post(Object msg, Class clazz) {
        post(msg, clazz.getName());
    }

    public void post(Object msg) {
        for (Map.Entry<String, Subscription> entry : subscriptionMap.entrySet()) {
            Subscription subscription = entry.getValue();
            subscription.invokeMessage(msg);
        }
    }

    public void post(Object msg, String tag) {
        for (Map.Entry<String, Subscription> entry : subscriptionMap.entrySet()) {
            Subscription subscription = entry.getValue();
            Map<String, String> tags = subscription.getTags();
            for (Map.Entry<String, String> mapEntry : tags.entrySet()) {
                if (mapEntry.getValue().equals(tag)) {
                    subscription.invokeMessage(msg);
                }
            }
        }
    }
}
