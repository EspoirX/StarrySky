package com.lzx.musiclibrary.bus;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;

/**
 * 包装一下实例和订阅方法
 * create by lzx
 * time:2018/11/7
 */
public class Subscription {
    private final Object subscriber;
    private final SubscriberMethod subscriberMethod;
    private volatile boolean active;

    private static final int HANDLER_MSG = 10001;
    private EventHandler mEventHandler;

    Subscription(Object subscriber, SubscriberMethod subscriberMethod) {
        this.subscriber = subscriber;
        this.subscriberMethod = subscriberMethod;
        active = true;
        mEventHandler = new EventHandler(this);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Subscription) {
            Subscription otherSubscription = (Subscription) other;
            return subscriber == otherSubscription.subscriber
                    && subscriberMethod.equals(otherSubscription.subscriberMethod);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return subscriber.hashCode() + subscriberMethod.methodString.hashCode();
    }

    /**
     * 执行订阅方法
     */
    void postToSubscription(Subscription subscription, Object event, boolean isMainThread) {
        if (isMainThread) {
            invokeSubscriber(subscription, event);
        } else {
            SubscriptionMsg msg = new SubscriptionMsg(subscription, event);
            mEventHandler.obtainMessage(HANDLER_MSG, msg).sendToTarget();
        }
    }

    /**
     * 通过反射执行订阅方法
     */
    private void invokeSubscriber(Subscription subscription, Object event) {
        try {
            subscription.subscriberMethod.method.invoke(subscription.subscriber, event);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * 主线程Handler
     */
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
                SubscriptionMsg msg = (SubscriptionMsg) message.obj;
                mSubscription.get().invokeSubscriber(msg.subscription, msg.event);
            }
        }
    }

    private static class SubscriptionMsg {
        Subscription subscription;
        Object event;

        SubscriptionMsg(Subscription subscription, Object event) {
            this.subscription = subscription;
            this.event = event;
        }
    }
}
