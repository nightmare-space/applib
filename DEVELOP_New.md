## 模式
Applib 有两种启动模式
第一种是速享等软件需要选择本机应用进行发送的时候，也会用到读取本机应用列表的功能

这部分是速享等软件集成了 app_channel 插件即可自动拥有获取应用列表的能力

启动代码如下:

```java
    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "apputils");
//        int port = AppChannel.startServer(flutterPluginBinding.getApplicationContext());
        try {
            AppServer.startServerFromActivity(flutterPluginBinding.getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        channel.setMethodCallHandler(this);
    }
```

还有一个是从 adb 的 shell 中启动
这里需要明确一些上下文, java 代码可编译成 class 文件，再通过 dx 工具转换成 dex 文件

通过安卓上的 app_process 可以直接运行

代码

```sh
app_process -Djava.class.path=/sdcard/app_serve /system/bin --nice-name=com.nightmare.dex com.nightmare.applib.AppServer open
```

这种就和 scrcpy 的情况完全一样，需要模拟构造 Context, 和反射一系列的类才能调用系统 api

## 调试
在 applib 根目录直接运行 `scripts/auto.sh` 即可

不过需要改一些简单的配置，当脚本可以正常运行

其中包含了一行命令

adb forward tcp:15000 tcp:15000

所以，接下来用 postman 调用15000端口的api即可

curl --location 'http://127.0.0.1:15000/tasks'

curl --location --request POST 'http://127.0.0.1:15000/createVirtualDisplay?width=1200&height=2412&density=480'

这部分需要简单阅读一下 AppServer.java ,其中图标的获取也是通过接口，

Flutter 侧 直接用 Image.network 加载图标

```dart
Image.network(
          'http://127.0.0.1:${widget.channel?.port ?? channel.port}/icon/${widget.packageName}',
          gaplessPlayback: true,
          errorBuilder: (_, __, ___) {
            return Image.asset(
              '${Config.flutterPackage}assets/placeholder.png',
              gaplessPlayback: true,
            );
          },
        ),
```

整个设计的好处是，无论是哪种启动方式，对Flutter来说，不一样的只有端口号

所以需要调试图标丢失问题，需要先看下应用列表能不能正常获取

猜测是没有获取到应用包名，导致后续获取不到图标的

