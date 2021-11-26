## App信息获取
反复的尝试 MethodChannel 和 Socket后，最后选择了 Socket，因为在大量数据的情况下，MethodChannel 中 result.success 语句总是在主线程中执行，这
会直接使我们的 flutter app 处于卡死状态。

分以下几种数据响应:
**注意`:`也是协议的一部分**，方便做正则匹配

### 1.getIconData:
#### 解释
获取单个App Icon字节流，例如 `getIconData:com.nightmare\n`
#### 返回结果
```sh
// 直接返回字节流
```

### 2.getAllAppInfo:
#### 解释
获取所有App的简略信息，例如 `getAllAppInfo:0\n`
0为用户应用，1为系统应用

#### 返回结果
```sh
// 括号是解释
// 返回所有App的信息如下，以\n分隔，单个信息间以\r分割，
packageName apkLabel(app应用名) minSdkVersion(最小的sdk版本) targetSdkVersion(目标的sdk版本) versionName(版本名称) versionCode(版本号) enabled(是否启用) hide(是否被隐藏) uid apkPath
```

### 3.getAppInfos:
#### 解释
获取给定App列表的简略信息，例如 `getAppInfos:com.nightmare com.nightmare.remote\n`
0为用户应用，1为系统应用

#### 返回结果
```sh
// 括号是解释
// 返回App的信息如下，以\n分隔，单个信息间以\r分割，
packageName apkLabel(app应用名) minSdkVersion(最小的sdk版本) targetSdkVersion(目标的sdk版本) versionName(版本名称) versionCode(版本号) enabled(是否启用) hide(是否被隐藏) uid apkPath
```
### 4.getIconData:

#### 解释
获取单个App Icon字节流。例如`getIconData:com.nightmare\n`

#### 返回结果
```sh
// 返回图片的字节流，所以需要根据 PNG 图片的编码的图片头进行拆分。
```
### 5.getAllIconData:

#### 解释
获取多个App Icon字节流。例如`getAllIconData:com.nightmare com.nightmare.remote\n`

#### 返回结果
```sh
// 返回所有图片拼接的字节流，所以需要根据 PNG 图片的编码的图片头进行拆分。
```

### 6.getAppActivity:

#### 解释
获取单个App的activity的列表。
例如`getAppActivity:com.nightmare\n`
#### 返回结果
```sh
// 例如
["com.nightmare.MainActivity","com.nightmare.termare"]
```

### 7.getAppDetail:

#### 解释
获取App详细信息
例如`getAppDetail:com.nightmare\n`
#### 返回结果
```sh
// 返回下列信息拼接的字符串
应用安装时间 最近更新时间 Apk大小 ApkMd5 ApkSha1 ApkSha256 私有文件路径 lib路径
```
### 7.getAppPermissions:
#### 解释
获取单个 app 的权限信息
例如`getAppPermissions:com.nightmare\n`
#### 返回结果
```sh
// 返回所有的权限及描述,\r分隔开每一个权限
```
### 8.getAppMainActivity:
#### 解释
获取单个 app 的权限信息
例如`getAppMainActivity:com.nightmare\n`
#### 返回结果
```sh
// 可以启动的组件名
```
## PC端信息获取
pm 命令不支持详细信息，图标的获取，所以还是通过套接字收发消息的机制，
App 运行在安卓上时，会有套接字的服务端，而运行在 PC 平台的时候，我们
又如何运行这个套接字服务端呢？

分以下几步:

**1.Adb push apk 内的精简 dex 到安卓设备的 /data/loacl/tmp 文件夹**

**2.用 app_process 命令执行 dex，服务端运行。**

**3.PC 端执行 Adb forward 进行端口转发**

**4.连接 Socket，获取数据。**

## 备份功能实现
需要关注的细节是，当 App 被卸载重新安装后，App uid 是会改变的，
tar 解压会回复重装前 app 的 uid。

### 备份
```sh
tar -zcvf $gz $dataDir
```

### 恢复
```sh
tar -xvf     sysconfig.tar -C /
chown -R  10590:10590 /data/data/com.nightmare.adbtools/
```