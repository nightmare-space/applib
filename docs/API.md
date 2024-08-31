## API Document(HTTP)

## /app_activity
current not available

## /appdetail

```json
{
    "firstInstallTime": 1719659247890,
    "lastUpdateTime": 1725076265920,
    "dataDir": "/data/user/0/com.nightmare.speedshare",
    "nativeLibraryDir": "/data/app/~~BzL3FLsh7z07pOgl3_TJ1w==/com.nightmare.speedshare-r8DgZV3_cJZ94hez9rNthQ==/lib/arm64"
}
```

## /allappinfo
```json
{
    "datas": [
       
        {
            "package": "com.nightmare.speedshare",
            "label": "速享",
            "minSdk": 21,
            "targetSdk": 28,
            "versionName": "2.2.8",
            "versionCode": 68,
            "enabled": true,
            "hide": false,
            "uid": 10391,
            "sourceDir": "/data/app/~~BzL3FLsh7z07pOgl3_TJ1w==/com.nightmare.speedshare-r8DgZV3_cJZ94hez9rNthQ==/base.apk"
        },
    ]
}
```

## /appmainactivity
```json
{
    "mainActivity": "com.nightmare.speedshare.MainActivity"
}
```

## /app_permission
return plain text
```
android.permission.READ_EXTERNAL_STORAGE 允许该应用读取您共享存储空间中的内容。 true
android.permission.WRITE_EXTERNAL_STORAGE 允许该应用写入您共享存储空间中的内容。 true
android.permission.INTERNET 允许该应用创建网络套接字和使用自定义网络协议。浏览器和其他某些应用提供了向互联网发送数据的途径，因此应用无需该权限即可向互联网发送数据。 true
android.permission.ACCESS_WIFI_STATE 允许该应用查看WLAN网络的相关信息，例如是否启用了WLAN以及连接的WLAN设备的名称。 true
android.permission.CHANGE_WIFI_MULTICAST_STATE 允许该应用使用多播地址接收发送到WLAN网络上所有设备（而不仅仅是您的手机）的数据包。该操作的耗电量比非多播模式要大。 true
android.permission.ACCESS_NETWORK_STATE 允许该应用查看网络连接的相关信息，例如存在和连接的网络。 true
android.permission.WAKE_LOCK 允许应用阻止手机进入休眠状态。 true
android.permission.SYSTEM_ALERT_WINDOW 此应用可显示在其他应用上方或屏幕的其他部分。这可能会妨碍您正常地使用应用，且其他应用的显示方式可能会受到影响。 false
android.permission.FOREGROUND_SERVICE 允许该应用使用前台服务。 true
android.permission.CAMERA 当您使用此应用时，它可以使用相机拍摄照片和录制视频。 true
android.permission.RECEIVE_BOOT_COMPLETED 允许应用在系统完成引导后立即自动启动。这样可能会延长手机的启动时间，并允许应用始终运行，从而导致手机总体运行速度减慢。 true
com.nightmare.speedshare.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION
```

## /createVirtualDisplayWithSurfaceView
return created display info json
```json
{
    "id": 3,
    "metrics": "DisplayMetrics{density=2.75, width=1080, height=2340, scaledDensity=2.75, xdpi=440.0, ydpi=440.0}",
    "name": "scrcpy-virtual",
    "width": 1080,
    "height": 2340,
    "rotation": 0,
    "refreshRate": 60,
    "density": 440,
    "dump": "Display id 3: DisplayInfo{\"scrcpy-virtual\", displayId 3, displayGroupId 0, FLAG_PRESENTATION, real 1080 x 2340, largest app 1080 x 2340, smallest app 1080 x 2340, appVsyncOff 0, presDeadline 16666666, mode 460.0, defaultMode 4, modes [{id=4, width=1080, height=2340, fps=60.0, alternativeRefreshRates=[], supportedHdrTypes=[]}], hdrCapabilities null, userDisabledHdrTypes [], minimalPostProcessingSupported false, rotation 0, state ON, committedState UNKNOWN}, DisplayMetrics{density=2.75, width=1080, height=2340, scaledDensity=2.75, xdpi=440.0, ydpi=440.0}, isValid=true"
}
```

## /displays
```json
{
    "datas": [
        {
            "id": 0,
            "metrics": "DisplayMetrics{density=2.75, width=1080, height=2340, scaledDensity=2.75, xdpi=391.885, ydpi=393.615}",
            "name": "内置屏幕",
            "width": 1080,
            "height": 2340,
            "rotation": 0,
            "refreshRate": 60.000003814697266,
            "density": 440,
            "dump": "Display id 0: DisplayInfo{\"内置屏幕\", displayId 0, displayGroupId 0, FLAG_SECURE, FLAG_SUPPORTS_PROTECTED_BUFFERS, FLAG_TRUSTED, real 1080 x 2340, largest app 2248 x 2179, smallest app 1080 x 988, appVsyncOff 1000000, presDeadline 16666666, mode 260.000004, defaultMode 1, modes [{id=1, width=1080, height=2340, fps=120.00001, alternativeRefreshRates=[60.000004], supportedHdrTypes=[2, 3, 4]}, {id=2, width=1080, height=2340, fps=60.000004, alternativeRefreshRates=[120.00001], supportedHdrTypes=[2, 3, 4]}], hdrCapabilities HdrCapabilities{mSupportedHdrTypes=[2, 3, 4], mMaxLuminance=800.0, mMaxAverageLuminance=400.1615, mMinLuminance=0.323}, userDisabledHdrTypes [], minimalPostProcessingSupported false, rotation 0, state ON, committedState ON}, DisplayMetrics{density=2.75, width=1080, height=2179, scaledDensity=2.75, xdpi=391.885, ydpi=393.615}, isValid=true"
        }
    ]
}
```

## /tasks

```json
{
    "datas": [
        {
            "id": -1,
            "persistentId": 6889,
            "displayId": -1,
            "topPackage": "",
            "topActivity": "",
            "label": ""
        }
    ]
}
```