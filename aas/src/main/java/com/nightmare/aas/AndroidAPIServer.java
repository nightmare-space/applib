package com.nightmare.aas;

import android.annotation.SuppressLint;
import android.content.Context;
import android.ddm.DdmHandleAppName;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.SystemProperties;
import android.view.Display;

import com.nightmare.aas.foundation.AndroidAPIPlugin;
import com.nightmare.aas.foundation.FakeContext;
import com.nightmare.aas.foundation.Workarounds;
import com.nightmare.aas.helper.L;
import com.nightmare.aas.helper.ReflectionHelper;
import com.nightmare.aas.helper.ServerHelper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class AndroidAPIServer {
    public AndroidAPIServer() {
    }

//    private final Class<?> type;

    @SuppressLint("SdCardPath")
    static public String portDirectory = "/sdcard";
    List<AndroidAPIPlugin> plugins = new ArrayList<>();

    public void registerPlugin(AndroidAPIPlugin handler) {
        plugins.add(handler);
        L.d("add handler -> " + handler);
    }

    String version = "0.0.1";

    /**
     * usually cmd is 'adb shell app_process *'
     */
    @SuppressLint("SdCardPath")
    public void startServerForShell(String[] args) {
        if (Objects.equals(args[0], "sula")) {
            // TODO: 这个多在几个安卓模拟器上测试一下
            L.serverLogPath = "/storage/emulated/0/Android/data/com.nightmare.sula" + "/app_server_log";
            portDirectory = "/storage/emulated/0/Android/data/com.nightmare.sula";
            L.d("Dex Server for Sula");
        }
        L.d("Welcome!!!");
        L.d("args -> " + Arrays.toString(args));
        DdmHandleAppName.setAppName("RAS", 0);
        AndroidAPIServerHTTPD androidAPIServerHTTPD = ServerHelper.safeGetServerForShell();
        androidAPIServerHTTPD.setAndroidAPIServer(this);
        L.d("Sula server starting.(version: " + version + ")");
        Workarounds.apply();
        ContextStore.getInstance().setContext(FakeContext.get());
        // 获取安卓版本
        String sdk = Build.VERSION.SDK;
        String release = Build.VERSION.RELEASE;
        L.d("Info: Android " + release + "(" + sdk + ")");
        // 获取设备制造商，例如 "Samsung"
        String manufacturer = Build.MANUFACTURER;
        // 获取设备型号，例如 "Galaxy S10"
        String model = getMarketName();
        if(model==null){
            model = Build.MODEL;
        }
        // 构建显示信息
        String deviceInfo = "Info: " + manufacturer + "(" + model + ")";
        L.d(deviceInfo);
        L.d("success start port -> " + androidAPIServerHTTPD.getListeningPort() + ".");
        writePort(portDirectory, androidAPIServerHTTPD.getListeningPort());
        // 让进程等待
        // 不能用 System.in.read()
        // System.in.read() 需要宿主进程由标准终端调用
        // Process.run 等方法就会出现异常
    }

    public static String getMarketName() {
        return SystemProperties.get("ro.product.marketname");
    }
    /**
     * 与直接启动dex不同，从 Activity 中启动不用反射 context 上下文
     * 但是一些权限需要动态申请
     * different from start dex directly, start from Activity doesn't need to reflect context
     *
     * @param context: Context
     */
    public int startServerFromActivity(Context context) {
        String dataPath = context.getFilesDir().getPath();
        L.enableTerminalLog = false;
        L.serverLogPath = dataPath + "/app_server_log";
        portDirectory = dataPath;
        String portPath = context.getFilesDir().getPath();
        ContextStore.getInstance().setContext(context);
        AndroidAPIServerHTTPD server = ServerHelper.safeGetServerForActivity();
        server.setAndroidAPIServer(this);
        // TODO 在确认下这个断言在 release 下是怎么的
        assert server != null;
        writePort(portPath, server.getListeningPort());
        L.d("port path:" + portPath);
        L.d("success start:" + server.getListeningPort());
        return server.getListeningPort();
    }

    /**
     * @noinspection DataFlowIssue
     */
    @SuppressLint("PrivateApi")
    void tryChangeDisplayConfig() {
        L.d("bindServer invoke");
        Class<?> clazz = null;
        try {
            clazz = Class.forName("android.hardware.display.DisplayManagerGlobal");
            @SuppressLint("DiscouragedPrivateApi") java.lang.reflect.Method getInstanceMethod = clazz.getDeclaredMethod("getInstance");
            Object dmg = getInstanceMethod.invoke(null);
            ReflectionHelper.invokeMethod(dmg, "setRefreshRateSwitchingType", 2);
            //getRefreshRateSwitchingType
            int type = (int) ReflectionHelper.invokeMethod(dmg, "getRefreshRateSwitchingType");
            L.d("RefreshRateSwitchingType -> " + type);
            //noinspection JavaReflectionMemberAccess
            DisplayManager displayManager = DisplayManager.class.getDeclaredConstructor(Context.class).newInstance(FakeContext.get());
            int matchContentFrameRateUserPreference = (int) ReflectionHelper.invokeMethod(displayManager, "getMatchContentFrameRateUserPreference");
            L.d("matchContentFrameRateUserPreference -> " + matchContentFrameRateUserPreference);
            // getSystemPreferredDisplayMode
            Object systemMode = ReflectionHelper.invokeMethod(dmg, "getSystemPreferredDisplayMode", 0);
            L.d("SystemPreferredDisplayMode -> " + systemMode);
            // same with adb shell cmd display get-user-preferred-display-mode 0
            Object userMode = ReflectionHelper.invokeMethod(dmg, "getUserPreferredDisplayMode", 0);
            L.d("UserPreferredDisplayMode -> " + userMode);
            // getGlobalUserPreferredDisplayMode
            Object globalUserMode = ReflectionHelper.invokeMethod(displayManager, "getGlobalUserPreferredDisplayMode");
            L.d("GlobalUserPreferredDisplayMode -> " + globalUserMode);
            ReflectionHelper.invokeMethod(dmg, "setShouldAlwaysRespectAppRequestedMode", true);
            boolean should = (boolean) ReflectionHelper.invokeMethod(dmg, "shouldAlwaysRespectAppRequestedMode");
            L.d("shouldAlwaysRespectAppRequestedMode -> " + should);
            for (Display display : displayManager.getDisplays()) {
                if (display.getDisplayId() != 0) {
                    L.d("display -> " + display);
                    L.d("");
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        Display.Mode[] supportedModes = display.getSupportedModes();
                        for (Display.Mode mode : supportedModes) {
                            L.d("mode -> " + mode);
                            L.d("");
                            if (mode.getRefreshRate() > 60) {
                                L.d("set mode -> " + mode);
                                ReflectionHelper.invokeMethod(dmg, "setUserPreferredDisplayMode", display.getDisplayId(), mode);
                                ReflectionHelper.invokeMethod(displayManager, "setGlobalUserPreferredDisplayMode", mode);
                            }
                        }
                    }
                }
            }
            ReflectionHelper.listAllObject(dmg);
            ReflectionHelper.listAllObject(displayManager);

        } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException |
                 IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 写入端口号，方便不同进程同App，获得这个端口号
     *
     * @param path: 写入的路径
     * @param port: 端口号
     * @noinspection CallToPrintStackTrace
     */
    public static void writePort(String path, int port) {
        OutputStream out;
        try {
            String filePath = path + "/server_port";
            out = new FileOutputStream(filePath);
            L.d("port file path -> " + filePath);
            out.write((port + "").getBytes());
            out.close();
        } catch (IOException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

}
