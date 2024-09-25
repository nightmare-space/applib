LOCAL_DIR=$(
    cd $(dirname $0)
    pwd
)
PROJ_DIR=$LOCAL_DIR/../
cd $PROJ_DIR
$LOCAL_DIR/build_without_gradle.sh
cd $LOCAL_DIR
lsof_result=$(adb shell 'lsof')
# echo "$lsof_result"
# 安卓低版本没有awk
awk_result=$(echo "$lsof_result" | awk -v uid=shell '$3 == uid' | grep 15000 | awk "{print \$2}")
if [ -z "$awk_result" ]; then
    echo "服务未启动，启动中"
else
    echo "服务已启动，PID:$awk_result 重启中"
    echo "$awk_result" | adb shell "xargs kill -9"
fi
# adb shell 'lsof | awk -v uid=shell "\$3 == uid" | grep 15000 | awk "{print \$2}" | xargs kill '
# # 取MD5的前8位
SERVER_PATH=$LOCAL_DIR/build/app_server
MD5=$(md5sum $SERVER_PATH | cut -d ' ' -f1 | cut -c 1-8)

cp $SERVER_PATH /Users/nightmare/Desktop/nightmare-space/GitHub/super_launcher/assets/sula_app_server
cp $SERVER_PATH /Users/nightmare/Desktop/nightmare-core/uncon/assets/app_server
cp $SERVER_PATH /Users/nightmare/Desktop/nightmare-core/adb_kit/assets
echo MD5:$MD5
devices=`adb devices | grep -v List | grep device | wc -l`
echo devices:$devices
adb push "build/app_server" /sdcard/app_server$MD5
adb push "$PROJ_DIR/resource/executor" /data/local/tmp/executor
$LOCAL_DIR/adb_forward.sh
adb shell '/data/local/tmp/executor "app_process -Djava.class.path=/sdcard/app_server'$MD5' /system/bin --nice-name=com.nightmare.dex com.nightmare.applib.AppServer sula"'



