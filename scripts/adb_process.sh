adb shell "app_process -Djava.class.path=/sdcard/app_server /system/bin --nice-name=com.nightmare.dex com.nightmare.applib.AppServer open"
# adb shell "nohup sh /sdcard/start.sh &"

# adb shell CLASSPATH=/sdcard/app_server app_process /sdcard/ com.nightmare.applib.AppServer open 
# adb shell CLASSPATH=/sdcard/app-release.apk app_process /sdcard/ com.nightmare.applib_util.AppServer open

