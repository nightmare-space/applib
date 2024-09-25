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
PROJ_DIR=$LOCAL_DIR/..
unset ANDROID_PLATFORM
unset ANDROID_BUILD_TOOLS
PLATFORM=${ANDROID_PLATFORM:-34}
BUILD_TOOLS=${ANDROID_BUILD_TOOLS:-30.0.3}
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


color_echo "$CLASSES_DIR/com/nightmare/applib"
# rm -rf "$CLASSES_DIR" "$BUILD_DIR/$SERVER_BINARY" classes.dex

mkdir -p "$CLASSES_DIR/com/nightmare/applib"

color_echo "Generating java from aidl..."
cd "$PROJ_DIR/app/src/main/aidl"
# "$BUILD_TOOLS_DIR/aidl" -p$ANDROID_HOME/platforms/android-$PLATFORM/framework.aidl -o"$GEN_DIR" com/nightmare/sula/IAdbService.aidl
# "$BUILD_TOOLS_DIR/aidl" -p$ANDROID_HOME/platforms/android-$PLATFORM/framework.aidl -o"$GEN_DIR" com/nightmare/sula/ISurfaceService.aidl


cd $PROJ_DIR/app/src/main/java

SRC=( \
    com/nightmare/applib/*.java \
    com/nightmare/applib/wrappers/*.java \
    com/nightmare/applib/utils/*.java \
    com/nightmare/applib/handler/*.java \
    com/nightmare/applib/interfaces/*.java \
)

CLASSES=()
for src in "${SRC[@]}"
do
    CLASSES+=("${src%.java}.class")
done

color_echo "Compiling java sources..."

JAR_PATH=$PROJ_DIR/app/libs

(cd $JAR_PATH && jar xf $JAR_PATH/junixsocket-selftest-2.10.1-jar-with-dependencies.jar)

cp -r $PROJ_DIR/app/libs/org $CLASSES_DIR/

# CLASSES+=("org/newsclub/net/unix/*.class")

/usr/bin/javac -bootclasspath "$ANDROID_JAR" \
    -Djava.ext.dirs=$JAR_PATH \
    -cp "$LAMBDA_JAR:$GEN_DIR" \
    -d "$CLASSES_DIR" \
    -source 1.8 -target 1.8 \
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


cp -f $BUILD_DIR/$SERVER_BINARY  '/Users/nightmare/Desktop/nightmare-space/GitHub/uncon/assets/'
cp -f $BUILD_DIR/$SERVER_BINARY  '/Users/nightmare/Desktop/nightmare-space/GitHub/adb_tool/assets'