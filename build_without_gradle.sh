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
JAVA_HOME="/Library/Java/JavaVirtualMachines/zulu-8.jdk/Contents/Home"
set -e
LOCAL_DIR=$(cd `dirname $0`; pwd)
unset ANDROID_PLATFORM
unset ANDROID_BUILD_TOOLS
PLATFORM=${ANDROID_PLATFORM:-33}
BUILD_TOOLS=${ANDROID_BUILD_TOOLS:-30.0.3}
# BUILD_DIR="$(realpath ${BUILD_DIR:-build})"
BUILD_DIR="$LOCAL_DIR/${BUILD_DIR:-build}"
CLASSES_DIR="$BUILD_DIR/classes"
SERVER_DIR=$(dirname "$0")
SERVER_BINARY=app_server
printf "%-10s %-10s\n" "Variable" "Value"
printf "%-10s %-10s\n" "--------" "-----"
printf "%-10s %-10s\n" "PLATFORM" "$PLATFORM"
printf "%-10s %-10s\n" "ANDROID_HOME" "$ANDROID_HOME"
echo PLATFORM:$PLATFORM
echo ANDROID_HOME:$ANDROID_HOME
echo BUILD_DIR:-build:${BUILD_DIR:-build}
echo BUILD_DIR:$BUILD_DIR
echo CLASSES_DIR:$CLASSES_DIR
echo LOCAL_DIR:$LOCAL_DIR
echo "Platform: android-$PLATFORM"
echo "Build-tools: $BUILD_TOOLS"
echo "Build dir: $BUILD_DIR"
echo "$CLASSES_DIR/com/nightmare/applib"
# rm -rf "$CLASSES_DIR" "$BUILD_DIR/$SERVER_BINARY" classes.dex

mkdir -p "$CLASSES_DIR/com/nightmare/applib"
cd $LOCAL_DIR/app/src/main/java
echo "Compiling java sources..."

/usr/bin/javac -bootclasspath "$ANDROID_HOME/platforms/android-$PLATFORM/android.jar" \
    -Djava.ext.dirs=$LOCAL_DIR \
    -cp "$CLASSES_DIR" -d "$CLASSES_DIR" -source 1.8 -target 1.8 \
    com/nightmare/applib/*.java \
    com/nightmare/applib/wrappers/*.java \
    com/nightmare/applib/utils/*.java
cp -r $LOCAL_DIR/fi $CLASSES_DIR/
echo "Dexing..."
cd "$CLASSES_DIR"
"$ANDROID_HOME/build-tools/$BUILD_TOOLS/dx" --dex \
    --output "$BUILD_DIR/classes.dex" \
    com/nightmare/applib/wrappers/*.class \
    com/nightmare/applib/utils/*.class \
    com/nightmare/applib/*.class \
    fi/iki/elonen/*.class \
    fi/iki/elonen/util/*.class

echo "Archiving..."
cd "$BUILD_DIR"
jar cvf "$SERVER_BINARY" classes.dex
rm -rf classes.dex classes

echo "App Server generated in $BUILD_DIR/$SERVER_BINARY"


# cp -f $BUILD_DIR/$SERVER_BINARY  '/Users/nightmare/Desktop/nightmare-space/GitHub/uncon/assets/'
# cp -f $BUILD_DIR/$SERVER_BINARY  '/Users/nightmare/Desktop/nightmare-space/GitHub/adb_tool/assets'