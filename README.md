# applib
纯 Android 的 Library

这是获取app信息的服务端，在尝试 Flutter Plugin后替代的方案，服务启动后仅与端口通信，支持任意语言获取 App 信息

app 文件夹是一个 Android Library，按照 jitpack 的文档进行配置用于集成。

目前 [apputils]()作为对应的客户端，flutter 项目想要使用本功能请集成 [apputils]()

目前 app_manager，adb_tool，speed_share 依赖 [apputils]() 这个 Plugin,apputils 依赖本仓库

## 安卓集成

### 添加依赖
```gradle
dependencies {
    implementation 'com.github.nightmare-space:applib:v0.0.5'
}
```
### 导包
```java
import com.nightmare.applib.AppChannel;
```

### 启动服务
```java
AppChannel.startServer(getApplicationContext());
```

## 端口占用
这个lib有一个端口占用的检测，
```java
    public static ServerSocket safeGetServerSocket() {
        for (int i = RANGE_START; i < RANGE_END; i++) {
            try {
                return new ServerSocket(i);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
```
## 为什么弄成了一个独立的库？
这个库起初是为了解决 app_manager 这个 flutter app 存在的问题， 后来另一个项目文件选择器有了选择 app 的需求，便独立出了这个仓库。

## 它的适用场景是什么
由于这个库是基于套接字的，所以只要能使用套接字的app开发语言，例如java，dart，c语言，都能使用。
但是java本就能直接拿到安卓api，就没必要再使用这样的中间层。
适用于flutter，可以在dart中用套接字拿到本机app信息，也适用于jni开发，可以直接在c语言拿到本机的app信息。



adb shell "lsof | awk -v uid=shell '$3 == uid' | grep 14000 | awk '{print $2}' | xargs kill -9"
