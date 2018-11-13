package com.lzx.musiclibrary.bus;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * create by lzx
 * time:2018/11/7
 */
public class SubscriberMethodFinder {

    private static final int BRIDGE = 0x40;
    private static final int SYNTHETIC = 0x1000;
    private static final int MODIFIERS_IGNORE = Modifier.ABSTRACT | Modifier.STATIC | BRIDGE | SYNTHETIC;

    private static final Map<Class<?>, List<SubscriberMethod>> METHOD_CACHE = new ConcurrentHashMap<>();
    private static final int POOL_SIZE = 4;
    private static final FindState[] FIND_STATE_POOL = new FindState[POOL_SIZE];

    /**
     * 将找到的注解方法封装成 List<SubscriberMethod>
     */
    public List<SubscriberMethod> findSubscriberMethods(Class<?> subscriberClass) {
        List<SubscriberMethod> subscription = METHOD_CACHE.get(subscriberClass);
        if (subscription != null) {
            return subscription;
        }
        subscription = findUsingReflection(subscriberClass);
        if (subscription == null) {
            throw new RuntimeException("没找到有 @Subscribe 注解的方法");
        } else {
            METHOD_CACHE.put(subscriberClass, subscription);
            return subscription;
        }
    }

    /**
     * 通过反射方式查找注解方法
     */
    private List<SubscriberMethod> findUsingReflection(Class<?> subscriberClass) {
        FindState findState = prepareFindState();
        findState.initForSubscriber(subscriberClass);
        while (findState.clazz != null) {
            findUsingReflectionInSingleClass(findState);
            findState.moveToSuperclass();
        }
        return getMethodsAndRelease(findState);
    }

    private void findUsingReflectionInSingleClass(FindState findState) {
        Method[] methods;
        try {
            methods = findState.clazz.getDeclaredMethods();
        } catch (Throwable th) {
            methods = findState.clazz.getMethods();
            findState.skipSuperClasses = true;
        }
        for (Method method : methods) {
            int modifiers = method.getModifiers();
            //忽略非public的方法和static等修饰的方法
            if ((modifiers & Modifier.PUBLIC) != 0 && (modifiers & MODIFIERS_IGNORE) == 0) {
                // 获取订阅方法的所有参数
                Class<?>[] parameterTypes = method.getParameterTypes();
                // 订阅方法只能有一个参数，否则忽略
                if (parameterTypes.length == 1) {
                    // 获取注解
                    Subscriber annotation = method.getAnnotation(Subscriber.class);
                    if (annotation != null) {
                        // 获取第一个参数
                        Class<?> eventType = parameterTypes[0];
                        // 检查eventType决定是否订阅，通常订阅者不能有多个eventType相同的订阅方法
                        if (findState.checkAdd(method, eventType)) {
                            String tag = annotation.tag();
                            findState.mSubscriptions.add(new SubscriberMethod(method, tag, eventType));
                        }
                    }
                }
            }
        }
    }

    /**
     * 创建FindState
     */
    private FindState prepareFindState() {
        synchronized (FIND_STATE_POOL) {
            for (int i = 0; i < POOL_SIZE; i++) {
                FindState state = FIND_STATE_POOL[i];
                if (state != null) {
                    FIND_STATE_POOL[i] = null;
                    return state;
                }
            }
        }
        return new FindState();
    }

    /**
     * 释放资源
     */
    private List<SubscriberMethod> getMethodsAndRelease(FindState findState) {
        List<SubscriberMethod> subscriberMethods = new ArrayList<>(findState.mSubscriptions);
        findState.recycle();
        synchronized (FIND_STATE_POOL) {
            for (int i = 0; i < POOL_SIZE; i++) {
                if (FIND_STATE_POOL[i] == null) {
                    FIND_STATE_POOL[i] = findState;
                    break;
                }
            }
        }
        return subscriberMethods;
    }

    public void clearCache() {
        METHOD_CACHE.clear();
    }

    /**
     * 包装类
     */
    static class FindState {
        final List<SubscriberMethod> mSubscriptions = new ArrayList<>();

        Class<?> subscriberClass;
        Class<?> clazz;
        boolean skipSuperClasses;

        final Map<Class, Object> anyMethodByEventType = new HashMap<>();
        final Map<String, Class> subscriberClassByMethodKey = new HashMap<>();
        final StringBuilder methodKeyBuilder = new StringBuilder(128);

        void initForSubscriber(Class<?> subscriberClass) {
            this.subscriberClass = clazz = subscriberClass;
            skipSuperClasses = false;
        }

        /**
         * 双重检查是否已经添加
         */
        boolean checkAdd(Method method, Class<?> eventType) {
            Object existing = anyMethodByEventType.put(eventType, method);
            if (existing == null) {
                return true;
            } else {
                if (existing instanceof Method) {
                    if (!checkAddWithMethodSignature((Method) existing, eventType)) {
                        throw new IllegalStateException();
                    }
                    anyMethodByEventType.put(eventType, this);
                }
                return checkAddWithMethodSignature(method, eventType);
            }
        }

        private boolean checkAddWithMethodSignature(Method method, Class<?> eventType) {
            methodKeyBuilder.setLength(0);
            methodKeyBuilder.append(method.getName());
            methodKeyBuilder.append('>').append(eventType.getName());

            String methodKey = methodKeyBuilder.toString();
            Class<?> methodClass = method.getDeclaringClass();
            Class<?> methodClassOld = subscriberClassByMethodKey.put(methodKey, methodClass);
            if (methodClassOld == null || methodClassOld.isAssignableFrom(methodClass)) {
                return true;
            } else {
                subscriberClassByMethodKey.put(methodKey, methodClassOld);
                return false;
            }
        }

        void recycle() {
            mSubscriptions.clear();
            anyMethodByEventType.clear();
            subscriberClassByMethodKey.clear();
            methodKeyBuilder.setLength(0);
            subscriberClass = null;
            clazz = null;
            skipSuperClasses = false;
        }

        /**
         * 使findState.clazz指向父类的Class，继续获取
         */
        void moveToSuperclass() {
            if (skipSuperClasses) {
                clazz = null;
            } else {
                clazz = clazz.getSuperclass();
                String clazzName = clazz.getName();
                if (clazzName.startsWith("java.") || clazzName.startsWith("javax.") || clazzName.startsWith("android.")) {
                    clazz = null;
                }
            }
        }

    }
}
