package com.lzx.musiclibrary.bus;

import java.lang.reflect.Method;

/**
 * 订阅方法封装
 * create by lzx
 * time:2018/11/2
 */
public class SubscriberMethod {

    Method method;
    String eventTag;
    private Class<?> eventType;
    String methodString;

    SubscriberMethod(Method method, String eventTag, Class<?> eventType) {
        this.method = method;
        this.eventTag = eventTag;
        this.eventType = eventType;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        } else if (other instanceof SubscriberMethod) {
            checkMethodString();
            SubscriberMethod otherSubscriberMethod = (SubscriberMethod) other;
            otherSubscriberMethod.checkMethodString();
            // Don't use method.equals because of http://code.google.com/p/android/issues/detail?id=7811#c6
            return methodString.equals(otherSubscriberMethod.methodString);
        } else {
            return false;
        }
    }

    private synchronized void checkMethodString() {
        if (methodString == null) {
            // Method.toString has more overhead, just take relevant parts of the method
            StringBuilder builder = new StringBuilder(64);
            builder.append(method.getDeclaringClass().getName());
            builder.append('#').append(method.getName());
            builder.append('(').append(eventType.getName());
            methodString = builder.toString();
        }
    }

    @Override
    public int hashCode() {
        return method.hashCode();
    }
}
