package com.nightmare.applib;

import static com.nightmare.applib.handler.InjectInputEvent.inputDispatcher;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.util.Log;
import android.view.Display;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import fi.iki.elonen.NanoHTTPD;

import com.nightmare.applib.handler.*;
import com.nightmare.applib.interfaces.IHTTPHandler;
import com.nightmare.applib.utils.Binary;
import com.nightmare.applib.utils.L;
import com.nightmare.applib.utils.ReflectUtil;
import com.nightmare.applib.utils.ServerUtil;
import com.nightmare.applib.utils.Workarounds;

/**
 * 基于HTTP服务提供能力
 *
 * @noinspection deprecation
 */
public class AppServer extends NanoHTTPD {
    public AppServer(String address, int port) {
        super(address, port);
    }

    static public AppChannel appChannel;
    @SuppressLint("SdCardPath")
    static private String portDirectory = "/sdcard";
    List<IHTTPHandler> handlers = new ArrayList<>();

    void addHandler(IHTTPHandler handler) {
        handlers.add(handler);
    }

    public static void main(String[] args) {
        if (Objects.equals(args[0], "sula")) {
            // TODO: 这个多在几个安卓模拟器上测试一下
            L.serverLogPath = "/storage/emulated/0/Android/data/com.nightmare.sula" + "/app_server_log";
            portDirectory = "/storage/emulated/0/Android/data/com.nightmare.sula";
            L.d("Dex Server for Sula");
        }
        L.d("Welcome!!!");
        L.d("args -> " + Arrays.toString(args));
        startServerForShell();
    }

    String version = "0.0.1";

    /**
     * usually cmd is 'adb shell app_process *'
     */
    @SuppressLint("SdCardPath")
    public static void startServerForShell() {
        AppServer server = ServerUtil.safeGetServerForADB();
        L.d("Sula input socket server starting.(version: " + server.version + ")");
        Workarounds.apply();
//        SulaServer.start();
        server.startInputDispatcher();
        server.registerRoutes();
//        server.tryChangeDisplayConfig();
        // 获取安卓版本
        String sdk = Build.VERSION.SDK;
        String release = Build.VERSION.RELEASE;
        L.d("Info: Android " + release + "(" + sdk + ")");
        // 获取设备的型号信息
        String manufacturer = Build.MANUFACTURER; // 获取设备制造商，例如 "Samsung"
        String model = Build.MODEL; // 获取设备型号，例如 "Galaxy S10"
        // 构建显示信息
        String deviceInfo = "Info: " + manufacturer + "(" + model + ")";
        L.d(deviceInfo);
        appChannel = new AppChannel();
        L.d("success start port -> " + server.getListeningPort() + ".");
        writePort(portDirectory, server.getListeningPort());
        // 让进程等待
        // 不能用 System.in.read(), System.in.read()需要宿主进程由标准终端调用
        // Process.run 等方法就会出现异常，
        // System.in.read();
        // noinspection InfiniteLoopStatement
        while (true) {
            try {
                //noinspection BusyWait
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

    }


    /** @noinspection DataFlowIssue*/
    @SuppressLint("PrivateApi")
    void tryChangeDisplayConfig() {
        L.d("bindServer invoke");
        Class<?> clazz = null;
        try {
            clazz = Class.forName("android.hardware.display.DisplayManagerGlobal");
            @SuppressLint("DiscouragedPrivateApi") java.lang.reflect.Method getInstanceMethod = clazz.getDeclaredMethod("getInstance");
            Object dmg = getInstanceMethod.invoke(null);
            ReflectUtil.invokeMethod(dmg, "setRefreshRateSwitchingType", 2);
            //getRefreshRateSwitchingType
            int type = (int) ReflectUtil.invokeMethod(dmg, "getRefreshRateSwitchingType");
            L.d("RefreshRateSwitchingType -> " + type);
            //noinspection JavaReflectionMemberAccess
            DisplayManager displayManager = DisplayManager.class.getDeclaredConstructor(Context.class).newInstance(FakeContext.get());
            int matchContentFrameRateUserPreference = (int) ReflectUtil.invokeMethod(displayManager, "getMatchContentFrameRateUserPreference");
            L.d("matchContentFrameRateUserPreference -> " + matchContentFrameRateUserPreference);
            // getSystemPreferredDisplayMode
            Object systemMode = ReflectUtil.invokeMethod(dmg, "getSystemPreferredDisplayMode", 0);
            L.d("SystemPreferredDisplayMode -> " + systemMode);
            // same with adb shell cmd display get-user-preferred-display-mode 0
            Object userMode = ReflectUtil.invokeMethod(dmg, "getUserPreferredDisplayMode", 0);
            L.d("UserPreferredDisplayMode -> " + userMode);
            // getGlobalUserPreferredDisplayMode
            Object globalUserMode = ReflectUtil.invokeMethod(displayManager, "getGlobalUserPreferredDisplayMode");
            L.d("GlobalUserPreferredDisplayMode -> " + globalUserMode);
            ReflectUtil.invokeMethod(dmg, "setShouldAlwaysRespectAppRequestedMode", true);
            boolean should = (boolean) ReflectUtil.invokeMethod(dmg, "shouldAlwaysRespectAppRequestedMode");
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
                                ReflectUtil.invokeMethod(dmg, "setUserPreferredDisplayMode", display.getDisplayId(), mode);
                                ReflectUtil.invokeMethod(displayManager, "setGlobalUserPreferredDisplayMode", mode);
                            }
                        }
                    }
                }
            }
            ReflectUtil.listAllObject(dmg);
            ReflectUtil.listAllObject(displayManager);

        } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException |
                 IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 与直接启动dex不同，从Activity中启动不用反射context上下文
     *
     * @param context: Context
     * @throws IOException: IOException
     */
    public static int startServerFromActivity(Context context) throws IOException {
        L.serverLogPath = context.getFilesDir().getPath() + "/app_server_log";
        AppServer server = ServerUtil.safeGetServerForActivity();
        // TODO 在确认下这个断言在 release 下是怎么的
        assert server != null;
        server.registerRoutes();
        writePort(context.getFilesDir().getPath(), server.getListeningPort());
        appChannel = new AppChannel(context);
        Log.d("Applib", "success start:" + server.getListeningPort());
        System.out.flush();
        return server.getListeningPort();
    }

    void registerRoutes() {
        addHandler(new AppActivityHandler());
        addHandler(new AppDetailHandler());
        addHandler(new AppInfosHandler());
//        addHandler(new AppInfosHandlerV1());
        addHandler(new AppMainactivity());
        addHandler(new AppPermissionHandler());
        addHandler(new ChangeDisplayHandler());
        addHandler(new CMDHandler());
        addHandler(new DisplayHandler());
        addHandler(new IconHandler());
        addHandler(new InjectInputEvent());
        addHandler(new OpenAppHandler());
        addHandler(new Resizevd());
        addHandler(new StopActivityHandler());
        addHandler(new TaskHandler());
        addHandler(new Taskthumbnail());
    }


    void startInputDispatcher() {
        new Thread(() -> {
            try {
                L.d("Sula input Thread run");
                ServerSocket serverSocket = new ServerSocket(12345);
                L.d("Sula input socket server started");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                while (true) {
                    try {
                        L.d("Sula input Wait Client Connect");
                        Socket socket = serverSocket.accept();
                        L.d("Sula input has connected");
                        InputStream in = socket.getInputStream();
//                            OutputStream out = socket.getOutputStream();
                        new Thread(() -> {
                            L.d("Sula input Handle socket in new Thread");
                            handleSocket(in);
                        }).start();
                    } catch (IOException e) {
                        L.d("startInputDispatcher error" + e);
                    }
                }

            } catch (IOException e) {
                L.d("startInputDispatcher error" + e);
            }
        }).start();
    }

    public static final int TYPE_INJECT_KEYCODE = 0;
    public static final int TYPE_INJECT_TOUCH_EVENT = 2;
    static final int INJECT_KEYCODE_PAYLOAD_LENGTH = 13;
    static final int INJECT_TOUCH_EVENT_PAYLOAD_LENGTH = 31;

    // 目前暂时支持两种协议，后续考虑直接复用 scrcpy-server
    // 目前看是不能复用 scrcpy-server, scrcpy 在投屏开始便指定了 display id
    // applib server 需要支持往不同的 display 中下发事件
    void handleSocket(InputStream in) {
        while (true) {
            // 持续读出数据
            int type = -1;
            try {
                type = in.read();
            } catch (IOException e) {
                L.d("in.read e : " + e);
            }
            if (type == -1) {
                break;
            }
            L.d("Sula input type : " + type);
            switch (type) {
                case TYPE_INJECT_KEYCODE:
                    handleKeyEvent(in);
                    break;
                case TYPE_INJECT_TOUCH_EVENT:
                    handleTouchEvent(in);
                    break;
            }

        }
    }

    void handleKeyEvent(InputStream in) {
        byte[] data = new byte[INJECT_KEYCODE_PAYLOAD_LENGTH];
        int bytesRead = 0;
//                            L.d("in.read start");
        try {
            bytesRead = in.read(data);
        } catch (IOException e) {
            L.d("in.read e : " + e);
        }
        if (bytesRead == -1) {
            return;
        }
        ByteBuffer buffer = ByteBuffer.wrap(data);
        // 1 byte
        int action = buffer.get();
        // 4 bytes total 5 bytes
        int keycode = buffer.getInt();
        // 4 bytes total 9 bytes
        int repeat = buffer.getInt();
        // 4 bytes total 13 bytes
        int metaState = buffer.getInt();
        boolean success = inputDispatcher.injectKeyEvent(action, keycode, repeat, metaState, InputDispatcher.INJECT_MODE_ASYNC);
    }

    void handleTouchEvent(InputStream in) {
        byte[] data = new byte[INJECT_TOUCH_EVENT_PAYLOAD_LENGTH];
        int bytesRead = 0;
//        L.d("in.read start");
        try {
            bytesRead = in.read(data);
        } catch (IOException e) {
            L.d("in.read e : " + e);
        }
        if (bytesRead == -1) {
            return;
        }
//        L.d("Sula input Received : " + Arrays.toString(data));
        ByteBuffer buffer = ByteBuffer.wrap(data);
        // 1 byte
        int actionInt = buffer.get();
        // 1 byte total 2 bytes
        // 为什么 value & 0xffff; 可以把有符号转成无符号
        int displayIdInt = Binary.toUnsigned(buffer.getShort());
        // 4 bytes total 6 bytes
        long pointerIdInt = buffer.getInt();
        // 4 bytes total 10 bytes
        int xInt = buffer.getInt();
        // 4 bytes total 14 bytes
        int yInt = buffer.getInt();
        // 4 bytes total 18 bytes
        int widthInt = buffer.getInt();
        // 4 bytes total 22 bytes
        int heightInt = buffer.getInt();
        // 4 bytes total 26 bytes
        int actionButtonInt = buffer.getInt();
        // 4 bytes total 30 bytes
        int buttonsInt = buffer.getInt();

        L.d("Sula input Received : d" + displayIdInt + " a" + actionInt + " p" + pointerIdInt + " x" + xInt
                + " y" + yInt + " w" + widthInt + " h" + heightInt + " ab" + actionButtonInt + " b" + buttonsInt);
        Position position = new Position(xInt, yInt, widthInt, heightInt);
        inputDispatcher.setDisplayId(displayIdInt);
        float pressure = 0f;
        // if (actionInt == MotionEvent.ACTION_DOWN || actionInt ==
        // MotionEvent.ACTION_MOVE) {
        // pressure = 1f;
        // }
        boolean success = inputDispatcher.injectTouch(actionInt, pointerIdInt, position, pressure, actionButtonInt, buttonsInt);
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

    public static byte[] readAllBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[1024];
        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        return buffer.toByteArray();
    }

    @Override
    public Response serve(IHTTPSession session) {
        try {
            String url = session.getUri();
            if (url.startsWith("/check")) {
                return newFixedLengthResponse(Response.Status.OK, "text/plain", "ok");
            }
            // 获取最近任务
            for (IHTTPHandler handler : handlers) {
                if (url.startsWith(handler.route())) {
//                    L.d("url -> " + url);
//                    L.d("handler -> " + handler);
                    return handler.handle(session);
                }
            }
            return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "route not found");
        } catch (Exception e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", e.toString());
        }
    }
}
