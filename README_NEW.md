## Android API Server(AAS)
这个库可以让你可以通过 Restful API 访问安卓的API，
支持上层框架为 Web 或者 Flutter 或者其他任意框架构建的界面时

例如在Flutter中，我们几乎需要使用 MethodChannel 来访问安卓的 API，并且如果无法在多个 Isolate 中访问
使用Method实现后，如果想要在Flutter Web中访问，就变得不可行

AAS 是基于Restful API实现的一个服务框架，支持插件化，也就是你可以通过很简短的代码，来让 AAS 加载你自定义的插件

## 功能特性

- 插件化: 通过简单的代码编写，可实现自定义插件的支持
- 内置 API: 内置开箱即用的在 Dex 中获取 Context、Services 的各种 API
- 内置插件: 内置多个插件，例如获取应用列表、应用图标、创建虚拟显示器等
- Flutter Plugin 支持: 只需要引入 Flutter 依赖，AAS 会随插件的注册而启动，在 Flutter 侧只需要调用 Dart API 即可
- 多种模式支持: 支持 Activity Mode 与 Dex Mode

## 运行模式

AAS 有两种运行模式

### Activity Mode

一种是由 Android App启动，这种情况下，
AAS 拥有真实的 Activity Context，对于获取应用列表，则普通安卓本身访问 API一样，需要申请权限

但基于 Restful API 的好处是，你可以通过这样的代码来获取一个 App 的图标

```dart
AppChannel appChannel = AppChannel();
Image.network(channel.iconUrl(widget.packageName))
```

AppChannel 是多实例，所有的 API 被封装到 AppChannel 下

多实例可以让同一个页面加载不同设备的应用列表，例如 Uncon


该库提供了一些快捷方便的API
同时也提供了 aas_plugin 来快速的让 Flutter App拥有这个能力，无需手动启动服务，aas随Plugin注册而启动

## Dex Mode
这种模式，会先将 java编译成class，再由 d 或 d8 工具转换成 dex 文件
通过 adb 运行 app_process 启动

这种模式带来的好处是，我们可以通过为连接到PC的设备启动这个服务，再通过 adb forward 获得通信的端口

接下来，你仍然只需要像这样就获得 App的图标

并且在这种模式下，所有 java 的权限为 shell(uid 2000)

你无需再为获取应用列表，创建虚拟显示器等单独申请权限

你甚至可以通过这样的代码
就能获取到应用的后台缩略图

亮点
例如我当前有个加载应用列表的Flutter界面，看起来像这样

而此时我需要这个界面展示其它设备的应用列表
我只需要更换端口
实际上，这样的模式已大量的在无界、速享、ADB KIT中使用
其中的应用列表页面，都是完全的同一份代码，仅仅是端口号不一样

所以对上层的应用层来说，只有端口的感知，它不在乎对方是哪种模式运行的

谁在用
速享: AAS以 Activity Mode的方式集成到速享，以让速享可以在 Flutter侧获取应用列表，来选择一个应用发送到其它设备
ADB KIT: AAS在 ADB KIT中同时以Activity Mode与Dex Mode存在，前者与速享类似，当我们需要安装本机已经安装的Apk到连接的设备后，就使用的这种模式
而当一个设备连接成功后，ADB KIT便会通过app_process唤起Dex Mode的AAS，

Uncon

你还可以自己实现各种各样的 API ，来获得远超 adb 命令行的功能，例如图标获取，adb 命令则就是不支持的

开发自定义插件

展望
我一直觉得腾讯的 PerfDog收费太贵，使用AAS，我觉得应该是能够编写一个 PerfDog的安卓部分的