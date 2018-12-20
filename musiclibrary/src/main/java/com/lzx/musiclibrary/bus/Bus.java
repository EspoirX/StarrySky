package com.lzx.musiclibrary.bus;

import android.os.Looper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 简单实现一个EventBus
 * create by lzx
 * time:2018/11/2
 */
public class Bus {

    private final Map<String, CopyOnWriteArrayList<Subscription>> subscriptionsByTag;
    private SubscriberMethodFinder subscriberMethodFinder;

    private final ThreadLocal<PostingThreadState> currentPostingThreadState = new ThreadLocal<PostingThreadState>() {
        @Override
        protected PostingThreadState initialValue() {
            return new PostingThreadState();
        }
    };

    private Bus() {
        subscriptionsByTag = new HashMap<>();
        subscriberMethodFinder = new SubscriberMethodFinder();
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
    public void register(Object subscriber) {
        if (subscriber == null) {
            return;
        }
        Class<?> subscriberClass = subscriber.getClass();
        //找到所有订阅方法
        List<SubscriberMethod> subscriberMethods = subscriberMethodFinder.findSubscriberMethods(subscriberClass);
        synchronized (this) {
            for (SubscriberMethod subscriberMethod : subscriberMethods) {
                subscribe(subscriber, subscriberMethod);
            }
        }
    }

    /**
     * 遍历赋值到 subscriptionsByTag 中
     */
    private void subscribe(Object subscriber, SubscriberMethod subscriberMethod) {
        String eventTag = subscriberMethod.eventTag; //唯一标记
        Subscription newSubscription = new Subscription(subscriber, subscriberMethod);
        CopyOnWriteArrayList<Subscription> subscriptions = subscriptionsByTag.get(eventTag);
        if (subscriptions == null) {
            subscriptions = new CopyOnWriteArrayList<>();
            subscriptionsByTag.put(eventTag, subscriptions);
        }
        int size = subscriptions.size();
        for (int i = 0; i <= size; i++) {
            if (i == size) {
                subscriptions.add(i, newSubscription);
                break;
            }
        }
    }

    /**
     * 取消订阅
     */
    public void unregister(Object obj) {
        if (obj == null) {
            return;
        }
        subscriptionsByTag.clear();
        currentPostingThreadState.remove();
        subscriberMethodFinder.clearCache();
    }

    /**
     * 发送事件
     */
    public synchronized void post(Object event, String eventTag) {
        PostingThreadState postingState = currentPostingThreadState.get();
        List<Object> eventQueue = postingState.eventQueue;
        eventQueue.add(event);
        if (!postingState.isPosting) {
            postingState.isMainThread = isMainThread();
            postingState.eventTag = eventTag;
            postingState.isPosting = true;
            if (postingState.canceled) {
                throw new RuntimeException("Internal error. Abort state was not reset");
            }
            try {
                while (!eventQueue.isEmpty()) {
                    postSingleEventForTag(eventQueue.remove(0), postingState);
                }
            } finally {
                postingState.isPosting = false;
                postingState.isMainThread = false;
            }
        }
    }

    private void postSingleEventForTag(Object event, PostingThreadState postingState) {
        CopyOnWriteArrayList<Subscription> subscriptions;
        synchronized (this) {
            subscriptions = subscriptionsByTag.get(postingState.eventTag);
        }
        if (subscriptions != null && !subscriptions.isEmpty()) {
            for (Subscription subscription : subscriptions) {
                postingState.event = event;
                postingState.subscription = subscription;
                boolean aborted;
                try {
                    subscription.postToSubscription(subscription, event, postingState.isMainThread);
                    aborted = postingState.canceled;
                } finally {
                    postingState.event = null;
                    postingState.subscription = null;
                    postingState.canceled = false;
                }
                if (aborted) {
                    break;
                }
            }
        }
    }

    final static class PostingThreadState {
        final List<Object> eventQueue = new ArrayList<>();
        boolean isPosting;
        boolean isMainThread;
        Subscription subscription;
        Object event;
        boolean canceled;
        String eventTag;
    }

    private boolean isMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }
}
