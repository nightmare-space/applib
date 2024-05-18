LOCAL_DIR=$(
    cd $(dirname $0)
    pwd
)
$LOCAL_DIR/../build_without_gradle.sh
lsof_result=$(adb shell 'lsof')
# echo "$lsof_result"
# 安卓低版本没有awk
awk_result=$(echo "$lsof_result" | awk -v uid=shell '$3 == uid' | grep 15000 | awk "{print \$2}")
echo "awk_result"
echo "$awk_result" | adb shell "xargs kill -9"
# adb shell 'lsof | awk -v uid=shell "\$3 == uid" | grep 15000 | awk "{print \$2}" | xargs kill '
# # 取MD5的前8位
MD5=$(md5sum $LOCAL_DIR/../build/app_server | cut -d ' ' -f1 | cut -c 1-8)
echo MD5:$MD5
devices=`adb devices | grep -v List | grep device | wc -l`
echo devices:$devices
adb push 'build/app_server' /sdcard/app_server$MD5
adb push 'executor' /data/local/tmp/executor
echo '/data/local/tmp/executor "app_process -Djava.class.path=/sdcard/app_server'$MD5' /system/bin --nice-name=com.nightmare.dex com.nightmare.applib.AppServer open"'
adb forward tcp:15000 tcp:15000
adb shell '/data/local/tmp/executor "app_process -Djava.class.path=/sdcard/app_server'$MD5' /system/bin --nice-name=com.nightmare.dex com.nightmare.applib.AppServer open"'
