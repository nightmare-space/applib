LOCAL_DIR=$(
    cd $(dirname $0)
    pwd
)
$LOCAL_DIR/../build_without_gradle.sh
# 取MD5的前8位
MD5=$(md5sum $LOCAL_DIR/../build/app_server | cut -d ' ' -f1 | cut -c 1-8)
echo MD5:$MD5
adb push 'build/app_server' /sdcard/app_server$MD5
echo '/data/local/tmp/fork "app_process -Djava.class.path=/sdcard/app_server'$MD5' /system/bin --nice-name=com.nightmare.dex com.nightmare.applib.AppServer open"'
adb shell '/data/local/tmp/fork "app_process -Djava.class.path=/sdcard/app_server'$MD5' /system/bin --nice-name=com.nightmare.dex com.nightmare.applib.AppServer open"'