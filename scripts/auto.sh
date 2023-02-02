LOCAL_DIR=$(
    cd $(dirname $0)
    pwd
)
$LOCAL_DIR/../build_without_gradle.sh
$LOCAL_DIR/adb_push.sh
$LOCAL_DIR/adb_process.sh