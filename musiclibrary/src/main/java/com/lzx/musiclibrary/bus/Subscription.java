package com.lzx.musiclibrary.bus;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * create by lzx
 * time:2018/11/2
 */
public class Subscription {
    static final String DEFAULT_TAG = "DEFAULT_TAG";
    private static final int HANDLER_MSG = 10001;

    private Object subscriber;
    private List<Method> methods;
    private Map<String, String> tags;
    private Map<String, ThreadMode> threadModes;

    private EventHandler mEventHandler;


    Subscription(Object obj) {
        subscriber = obj;
        mEventHandler = new EventHandler(this);
        methods = new ArrayList<>();
        tags = new HashMap<>();
        threadModes = new HashMap<>();
        findMethod();
    }

    /**
     * 找出有注解Subscriber标记的方法
     */
    private void findMethod() {
        methods.clear();
        Method[] allMethod = subscriber.getClass().getDeclaredMethods();
        for (Method method : allMethod) {
            Subscriber annotation = method.getAnnotation(Subscriber.class);
            if (annotation != null) {
                methods.add(method);
                String tag = DEFAULT_TAG.equals(annotation.tag()) ? subscriber.getClass().getName() : annotation.tag();
                tags.put(method.getName(), tag);
                threadModes.put(method.getName(), annotation.thread());
            }
        }
    }

    String getTag(String method) {
        return tags.get(method);
    }

    public Map<String, String> getTags() {
        return tags;
    }

    /**
     * 反射执行方法
     */
    void invokeMessage(Object msg) {
        for (Method method : methods) {
            if (method != null) {
                try {
                    if (threadModes.get(method.getName()) == ThreadMode.MAIN) {
                        mEventHandler.obtainMessage(HANDLER_MSG, msg).sendToTarget();
                    } else {
                        method.invoke(subscriber, msg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static class EventHandler extends Handler {

        private final WeakReference<Subscription> mSubscription;

        EventHandler(Subscription subscription) {
            super(Looper.getMainLooper());
            mSubscription = new WeakReference<>(subscription);
        }

        @Override
        public void handleMessage(Message message) {
            super.handleMessage(message);
            if (message.what == HANDLER_MSG) {
                Object msg = message.obj;
                for (Method method : mSubscription.get().methods) {
                    if (method != null) {
                        try {
                            method.invoke(mSubscription.get().subscriber, msg);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}
