## App信息获取

**1.将 applib 生成 dex**: 可通过反编译带 applib 的 apk，再通过精简回编译得到，也可以直接将 java 代码编译成 class 再通过 dx 工具转换得到

**2.push dex 到安卓设备的 /data/loacl/tmp 文件夹**

**3.用 app_process 命令执行 dex，服务端运行。**

**4.PC 端执行 Adb forward 进行端口转发**

**5.PC 连接 Socket，获取数据。**

### 备份
```sh
tar -zcvf $gz $dataDir
```

### 恢复
```sh
tar -xvf     sysconfig.tar -C /
chown -R  10590:10590 /data/data/com.nightmare.adbtools/
```