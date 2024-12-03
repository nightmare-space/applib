#!/usr/bin/env bash
#
# This script generates the scrcpy binary "manually" (without gradle).
#
# Adapt Android platform and build tools versions (via ANDROID_PLATFORM and
# ANDROID_BUILD_TOOLS environment variables).
#
# Then execute:
#
#     BUILD_DIR=my_build_dir ./build_without_gradle.sh
function color_echo()
{
    echo -e "\033[1;32m$1\033[0m"
}
JAVA_HOME="/Library/Java/JavaVirtualMachines/zulu-8.jdk/Contents/Home"
set -e
LOCAL_DIR=$(cd `dirname $0`; pwd)
PROJ_DIR=$(cd $LOCAL_DIR/..; pwd)
unset ANDROID_PLATFORM
unset ANDROID_BUILD_TOOLS
PLATFORM=${ANDROID_PLATFORM:-35}
BUILD_TOOLS=${ANDROID_BUILD_TOOLS:-35.0.0}
BUILD_TOOLS_DIR="$ANDROID_HOME/build-tools/$BUILD_TOOLS"
# BUILD_DIR="$(realpath ${BUILD_DIR:-build})"
BUILD_DIR="$LOCAL_DIR/${BUILD_DIR:-build}"
GEN_DIR="$BUILD_DIR/gen"
CLASSES_DIR="$BUILD_DIR/classes"
SERVER_DIR=$(dirname "$0")
SERVER_BINARY=app_server
ANDROID_JAR="$ANDROID_HOME/platforms/android-$PLATFORM/android.jar"
LAMBDA_JAR="$BUILD_TOOLS_DIR/core-lambda-stubs.jar"
printf "%-20s %-20s\n" "Variable" "Value"
printf "%-20s %-20s\n" "--------" "-----"
printf "%-20s %-20s\n" "PLATFORM" "$PLATFORM"
printf "%-20s %-20s\n" "PROJ_DIR" "$PROJ_DIR"
printf "%-20s %-20s\n" "ANDROID_HOME" "$ANDROID_HOME"
printf "%-20s %-20s\n" "BUILD_DIR" "${BUILD_DIR}"
printf "%-20s %-20s\n" "CLASSES_DIR" "$CLASSES_DIR"
printf "%-20s %-20s\n" "LOCAL_DIR" "$LOCAL_DIR"
printf "%-20s %-20s\n" "Platform:" "android-$PLATFORM"
printf "%-20s %-20s\n" "Build-tools:" "$BUILD_TOOLS"
printf "%-20s %-20s\n" "BUILD_TOOLS_DIR" "$BUILD_TOOLS_DIR"
printf "%-20s %-20s\n" "GEN_DIR" "$GEN_DIR"

# rm -rf "$CLASSES_DIR" "$BUILD_DIR/$SERVER_BINARY" classes.dex

mkdir -p "$CLASSES_DIR/com/nightmare/aas"

color_echo "Generating java from aidl..."
# cd "$PROJ_DIR/src/main/aidl"
# "$BUILD_TOOLS_DIR/aidl" -p$ANDROID_HOME/platforms/android-$PLATFORM/framework.aidl -o"$GEN_DIR" com/nightmare/sula/IAdbService.aidl
# "$BUILD_TOOLS_DIR/aidl" -p$ANDROID_HOME/platforms/android-$PLATFORM/framework.aidl -o"$GEN_DIR" com/nightmare/sula/ISurfaceService.aidl


# cd $PROJ_DIR/src/main/java
AAS_INTEGRATE_DIR=$PROJ_DIR/aas_integrated
AAS_INTEGRATE_SRC_DIR=$AAS_INTEGRATE_DIR/src/main/java
HIDDEN_API_DIR=$PROJ_DIR/aas_hidden_api/src/main/java
AAS_SRC_DIR=$PROJ_DIR/aas/src/main/java
ASS_PLUGINS_SRC_DIR=$PROJ_DIR/aas_plugins/src/main/java

SRC=( \
    $AAS_SRC_DIR/com/nightmare/aas/*.java \
    $ASS_PLUGINS_SRC_DIR/com/nightmare/aas_plugins/*.java \
    $ASS_PLUGINS_SRC_DIR/com/nightmare/aas_plugins/util/*.java \
    $AAS_INTEGRATE_SRC_DIR/com/nightmare/aas_integrated/*.java \
)

HIDDEN=( \
    $HIDDEN_API_DIR/android/content/pm/*.java \
    $HIDDEN_API_DIR/android/os/*.java \
    $HIDDEN_API_DIR/android/app/*.java \
    $HIDDEN_API_DIR/android/window/*.java \
    $HIDDEN_API_DIR/android/graphics/*.java \
    $HIDDEN_API_DIR/android/hardware/display/*.java \
    $HIDDEN_API_DIR/android/ddm/*.java \
    $HIDDEN_API_DIR/androidx/annotation/*.java \
)

CLASSES=()
for src in "${SRC[@]}"
do
    # 删除 src 中 com/nightmare/aas 前面的部分
    src=$(echo $src | sed 's|.*\(com/nightmare/.*\)|\1|')
    CLASSES+=("${src%.java}.class")
done

color_echo "Compiling java sources..."

JAR_PATH=$PROJ_DIR/aas/libs

/usr/bin/javac -encoding UTF-8 -bootclasspath "$ANDROID_JAR" \
    -Djava.ext.dirs=$JAR_PATH \
    -cp "$LAMBDA_JAR:$GEN_DIR:$PROJ_DIR/aas/libs/nanohttpd-2.3.1.jar" \
    -d "$CLASSES_DIR" \
    -source 1.8 -target 1.8 \
    ${HIDDEN[@]} \
    ${SRC[@]}

cp -r $PROJ_DIR/fi $CLASSES_DIR/
color_echo "Dexing..."
cd "$CLASSES_DIR"

if [[ $PLATFORM -lt 31 ]]
then
    # use dx
    color_echo "Dexing with dx..."
    "$BUILD_TOOLS_DIR/dx" --dex --output "$BUILD_DIR/classes.dex" \
        ${CLASSES[@]} \
        android/content/pm/*.class \
        android/os/*.class \
        android/hardware/display/*.class \
        android/graphics/*.class \
        android/app/*.class \
        android/window/*.class \
        android/ddm/*.class \
        androidx/annotation/*.class \
        fi/iki/elonen/*.class \
        fi/iki/elonen/util/*.class

    echo "Archiving..."
    cd "$BUILD_DIR"
    jar cvf "$SERVER_BINARY" classes.dex
    rm -rf classes.dex
else
    color_echo "Dexing with d8..."
    # use d8
    "$BUILD_TOOLS_DIR/d8" --classpath "$ANDROID_JAR" \
        --output "$BUILD_DIR/classes.zip" \
        ${CLASSES[@]} \
        fi/iki/elonen/*.class \
        fi/iki/elonen/util/*.class

    cd "$BUILD_DIR"
    mv classes.zip "$SERVER_BINARY"
fi

rm -rf classes.dex classes gen

echo "App Server generated in $BUILD_DIR/$SERVER_BINARY"


cp -f $BUILD_DIR/$SERVER_BINARY  '/Users/nightmare/Desktop/nightmare-core/uncon/assets'
cp -f $BUILD_DIR/$SERVER_BINARY  '/Users/nightmare/Desktop/nightmare-core/adb_kit'