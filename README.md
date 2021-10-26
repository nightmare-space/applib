# applib
这是获取app信息的服务端，在尝试flutter plugin后替代的方案，支持pc桌面应用获取adb连接的应用信息。

## 开始使用

### 集成
```gradle
dependencies {
    implementation 'com.github.nightmare-space:applib:v0.0.4'
}
```
### 导入依赖
```java
import com.nightmare.applib.AppChannel;
```

### 启动服务
```java
        new Thread(() -> {
            try {
                AppChannel.startServer(getApplicationContext());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
```

## 设计原理
使用套接字响应app信息，这样无论是安卓的本地app，还是pc桌面端，只要能启动这个服务，就能从指定端口获得安卓的app信息。

## 为什么弄成了一个独立的库？
这个库起初是为了解决app_manager这个app存在的问题， 后来另一个项目文件选择器有了选择app的需求，便独立出了这个仓库。

## 它的适用场景是什么
由于这个库是基于套接字的，所以只要能使用套接字的app开发语言，例如java，dart，c语言，都能使用。
但是java本就能直接拿到安卓api，就没必要再使用这样的中间层。
适用于flutter，可以在dart中用套接字拿到本机app信息，也适用于jni开发，可以直接在c语言拿到本机的app信息。