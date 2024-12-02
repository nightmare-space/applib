package com.nightmare.aas;

import android.annotation.SuppressLint;
import android.content.Context;

//饿汉式（Eager Initialization）
//优点：
//简单，类加载时即创建实例，线程安全。
//缺点：
//如果实例创建过程比较耗时，可能会影响启动性能。
public class ContextStore {
    @SuppressLint("StaticFieldLeak")
    private static final ContextStore INSTANCE = new ContextStore();

    private ContextStore() {
    }

    public static ContextStore getInstance() {
        return INSTANCE;
    }

    private Context context;

    public void setContext(Context context) {
        this.context = context;
    }

    static public Context getContext() {
        return INSTANCE.context;
    }

}

//静态内部类（Static Inner Class）
//优点：
//利用类加载机制保证线程安全，同时实现了延迟加载。
//缺点：
//代码稍微复杂一些。

//public class ContextStore {
//    private ContextStore() {}
//
//    private static class ContextStoreHolder {
//        private static final ContextStore INSTANCE = new ContextStore();
//    }
//
//    public static ContextStore getInstance() {
//        return ContextStoreHolder.INSTANCE;
//    }
//}

//枚举（Enum）
//优点：
//简洁，天生线程安全，防止反序列化创建新的对象。
//缺点：
//不支持延迟加载。

//public enum ContextStore {
//    INSTANCE;
//
//    public void someMethod() {
//        // Your method implementation
//    }
//}

//同步方法（Synchronized Method）
//优点：
//实现简单，保证线程安全。
//缺点：
//每次调用都会有同步开销，性能较低。
//public class ContextStore {
//    private static ContextStore instance;
//
//    private ContextStore() {}
//
//    public static synchronized ContextStore getInstance() {
//        if (instance == null) {
//            instance = new ContextStore();
//        }
//        return instance;
//    }
//}

//双重检查锁定（Double-Checked Locking）
//优点：
//线程安全，延迟加载，性能较好。
//缺点：
//实现稍微复杂。

//public class ContextStore {
//    private static volatile ContextStore uniqueInstance;
//
//    private ContextStore() {}
//
//    public static ContextStore getInstance() {
//        if (uniqueInstance == null) {
//            synchronized (ContextStore.class) {
//                if (uniqueInstance == null) {
//                    uniqueInstance = new ContextStore();
//                }
//            }
//        }
//        return uniqueInstance;
//    }
//}