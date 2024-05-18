package com.nightmare.applib;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.WindowManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

import com.nightmare.applib.utils.DisplayUtil;
import com.nightmare.applib.utils.L;
import com.nightmare.applib.utils.ServerUtil;
import com.nightmare.applib.utils.TaskUtil;
import com.nightmare.applib.utils.Workarounds;
import com.nightmare.applib.wrappers.DisplayInfo;
import com.nightmare.applib.wrappers.DisplayManagerV2;
import com.nightmare.applib.wrappers.ServiceManager;

/**
 * 基于HTTP服务提供能力
 *
 * @noinspection deprecation
 */
public class AppServer extends NanoHTTPD {
    public AppServer(String address, int port) {
        super(address, port);
    }

    AppChannel appChannel;
    InputDispatcher inputDispatcher = new InputDispatcher();

    final

    @SuppressLint("SdCardPath")
    public static void main(String[] args) throws Exception {
        L.d("Welcome!!!");
        L.d("args -> " + Arrays.toString(args));
        AppServer server = ServerUtil.safeGetServerForADB();
        Workarounds.prepareMainLooper();
        L.d("Sula input socket server starting.");
        assert server != null;
        server.startInputDispatcher();
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
        // 这个时候构造的是一个没有Context的Channel
        server.appChannel = new AppChannel();
        L.d("success start port -> " + server.getListeningPort() + ".");
        writePort("/sdcard", server.getListeningPort());
        // 让进程等待
        // 不能用 System.in.read(),如果执行 app_process 是类似于
        // Process.run 等方法就会出现异常，System.in.read()需要宿主进程由标准终端调用
        // System.in.read();
        while (true) {
            Thread.sleep(1000);
        }
    }

    void startInputDispatcher() {
        new Thread(new Runnable() {
            @Override
            public void run() {
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
                            Socket socket = serverSocket.accept();
                            L.d("Sula input has connected");
                            InputStream in = socket.getInputStream();
                            OutputStream out = socket.getOutputStream();
                            handleSocket(in);
                        } catch (IOException e) {
                            L.d("startInputDispatcher error" + e);
                        }
                    }

                } catch (IOException e) {
                    L.d("startInputDispatcher error" + e);
                }
            }
        }).start();
    }

    void handleSocket(InputStream in) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    // 持续读出数据
                    byte[] data = new byte[36];
                    int bytesRead = 0;

                    L.d("in.read start");
                    try {
                        bytesRead = in.read(data);
                    } catch (IOException e) {
                        L.d("in.read e : " + e);
                    }
                    if (bytesRead == -1) {
                        break;
                    }
                    L.d("Sula input Received : " + Arrays.toString(data));
                    ByteBuffer buffer = ByteBuffer.wrap(data);
                    int displayIdInt = buffer.getInt();
                    int actionInt = buffer.getInt();
                    long pointerIdInt = buffer.getInt();
                    int xInt = buffer.getInt();
                    int yInt = buffer.getInt();
                    int widthInt = buffer.getInt();
                    int heightInt = buffer.getInt();
                    int actionButtonInt = buffer.getInt();
                    int buttonsInt = buffer.getInt();
                    L.d("Sula input Received : " + displayIdInt + " " + actionInt + " " + pointerIdInt + " " + xInt
                            + " " + yInt + " " + widthInt + " " + heightInt + " " + actionButtonInt + " " + buttonsInt);
                    Position position = new Position(xInt, yInt, widthInt, heightInt);
                    inputDispatcher.setDisplayId(displayIdInt);
                    float pressure = 0f;
                    // if (actionInt == MotionEvent.ACTION_DOWN || actionInt ==
                    // MotionEvent.ACTION_MOVE) {
                    // pressure = 1f;
                    // }
                    boolean success = inputDispatcher.injectTouch(actionInt, pointerIdInt, position, pressure,
                            actionButtonInt, buttonsInt);

                }
            }
        }).start();
    }

    /**
     * 与直接启动dex不同，从Activity中启动不用反射context上下问
     *
     * @param context
     * @throws IOException
     */
    public static void startServerFromActivity(Context context) throws IOException {
        AppServer server = ServerUtil.safeGetServer();
        writePort(context.getFilesDir().getPath(), server.getListeningPort());
        server.appChannel = new AppChannel(context);
        System.out.println("success start:" + server.getListeningPort());
        System.out.flush();
    }

    /**
     * 写入端口号，方便不同进程同App，获得这个端口号
     *
     * @param path
     * @param port
     */
    public static void writePort(String path, int port) {
        OutputStream out;
        try {
            String filePath = path + "/server_port";
            out = new FileOutputStream(filePath);
            L.d("port file -> " + filePath);
            out.write((port + "").getBytes());
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Object getParams(IHTTPSession session) {
        Map<String, List<String>> params = session.getParameters();
        return params.get(0);
    }

    Map<Integer, VirtualDisplay> cache = new HashMap<>();

    static final String resizeVDRoute = "/resize_vd";
    static final String displayGetRoute = "/displays";
    static final String createVirtualDisplay = "/createVirtualDisplay";
    static final String closeVirtualDisplay = "/closeVirtualDisplay";
    static final String getAllAppInfo = "/allappinfo";

    static final String openApp = "/openapp";

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public Response serve(IHTTPSession session) {
        try {
            // 获取最近任务
            if (session.getUri().startsWith("/tasks")) {
                return newFixedLengthResponse(
                        Response.Status.OK,
                        "application/json",
                        TaskUtil.getRecentTasksJson(appChannel).toString()
                );
            }
            // 获取图标
            if (session.getUri().startsWith("/icon")) {
                // Log.d(session.getParameters().toString());
                Map<String, List<String>> params = session.getParameters();
                if (!params.isEmpty()) {
                    List<String> line = session.getParameters().get("path");
                    String path = line.get(0);
                    byte[] bytes = appChannel.getApkBitmapBytes(path);
                    return newFixedLengthResponse(Response.Status.OK, "image/jpg", new ByteArrayInputStream(bytes),
                            bytes.length);
                }
                byte[] bytes = appChannel.getBitmapBytes(session.getUri().substring("/icon/".length()));
                // print(bytes);
                return newFixedLengthResponse(Response.Status.OK, "image/jpg", new ByteArrayInputStream(bytes), bytes.length);
            }
            // 获取所有的应用信息
            // 包含被隐藏的，被冻结的
            if (session.getUri().startsWith(getAllAppInfo)) {
                boolean isSystemApp = false;
                List<String> line = session.getParameters().get("is_system_app");
                if (line != null && !line.isEmpty()) {
                    isSystemApp = Boolean.parseBoolean(line.get(0));
                }
                byte[] bytes = appChannel.getAllAppInfo(isSystemApp).getBytes();
                return newFixedLengthResponse(Response.Status.OK, "application/json", new ByteArrayInputStream(bytes), bytes.length);
            }
            // 获取指定App列表的信息
            if (session.getUri().startsWith(AppChannelProtocol.getAppInfos)) {
                List<String> packages = session.getParameters().get("apps");
                byte[] bytes = appChannel.getAppInfos(packages).getBytes();
                return newFixedLengthResponse(Response.Status.OK, "application/json", new ByteArrayInputStream(bytes), bytes.length);
            }
            // 获取单个App的详细信息
            if (session.getUri().startsWith(AppChannelProtocol.getAppDetail)) {
                String packageName = session.getParms().get("package");
                byte[] bytes = appChannel.getAppDetail(packageName).getBytes();
                return newFixedLengthResponse(Response.Status.OK, "application/json", new ByteArrayInputStream(bytes), bytes.length);
            }
            // 获取缩略图
            if (session.getUri().startsWith(AppChannelProtocol.getTaskThumbnail)) {
                String id = session.getParms().get("id");
                byte[] bytes = TaskUtil.getTaskThumbnail(Integer.parseInt(id));
                return newFixedLengthResponse(Response.Status.OK, "image/jpg", new ByteArrayInputStream(bytes), bytes.length);
            }
            // 通过包名获取Main Activity
            if (session.getUri().startsWith(AppChannelProtocol.getAppMainActivity)) {
                String packageName = session.getParms().get("package");
                byte[] bytes = appChannel.getAppMainActivity(packageName).getBytes();
                return newFixedLengthResponse(Response.Status.OK, "application/json", new ByteArrayInputStream(bytes),
                        bytes.length);
            }
            if (session.getUri().startsWith("/createVirtualDisplayV2")) {
                DisplayManagerV2 displayManagerV2 = DisplayManagerV2.create();
                SurfaceView surfaceView = new SurfaceView(appChannel.context);
                displayManagerV2.createVirtualDisplay("test", 1080, 1920, 240, surfaceView.getHolder().getSurface());
                return newFixedLengthResponse(Response.Status.OK, "application/json", "");
            }
            if (session.getUri().startsWith(displayGetRoute)) {
                @SuppressLint({"NewApi", "LocalSuppress"})
                DisplayManagerV2 displayManagerV2 = DisplayManagerV2.create();
                int[] displays = displayManagerV2.getDisplayIds();
                JSONObject jsonObjectResult = new JSONObject();
                JSONArray jsonArray = new JSONArray();
                for (int displayId : displays) {
                    DisplayInfo info = displayManagerV2.getDisplayInfo(displayId);
                    JSONObject jsonObject = DisplayUtil.getDisplayInfoFromCustom(info);
                    jsonArray.put(jsonObject);
                }
                jsonObjectResult.put("datas", jsonArray);
                return newFixedLengthResponse(Response.Status.OK, "application/json", jsonObjectResult.toString());
            }
            // 创建虚拟显示器
            if (session.getUri().startsWith(createVirtualDisplay)) {
                SurfaceView surfaceView = new SurfaceView(appChannel.context);
                boolean useDeviceConfig = session.getParms().containsKey("useDeviceConfig");
                WindowManager windowManager = (WindowManager) appChannel.context.getSystemService(Context.WINDOW_SERVICE);
                DisplayMetrics displayMetrics = new DisplayMetrics();
                windowManager.getDefaultDisplay().getMetrics(displayMetrics);
                String width, height, density;
                if (useDeviceConfig) {
                    width = displayMetrics.widthPixels + "";
                    height = displayMetrics.heightPixels + "";
                    density = displayMetrics.densityDpi + "";
                } else {
                    width = session.getParms().get("width");
                    height = session.getParms().get("height");
                    density = session.getParms().get("density");
                }
                VirtualDisplay display = ServiceManager.getDisplayManager().createVirtualDisplay(
                        surfaceView.getHolder().getSurface(),
                        Integer.parseInt(width),
                        Integer.parseInt(height),
                        Integer.parseInt(density)
                );
                assert display != null;
                cache.put(display.getDisplay().getDisplayId(), display);
                JSONObject json = DisplayUtil.getDisplayInfo(display.getDisplay());
                return newFixedLengthResponse(
                        Response.Status.OK,
                        "application/json",
                        json.toString()
                );
            }
            // 创建虚拟显示器
            if (session.getUri().startsWith(resizeVDRoute)) {
                String id = session.getParms().get("id");
                String width = session.getParms().get("width");
                String height = session.getParms().get("height");
                String density = session.getParms().get("density");
                VirtualDisplay display = cache.get(Integer.parseInt(id));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    display.resize(Integer.parseInt(width), Integer.parseInt(height), Integer.parseInt(density));
                }
                return newFixedLengthResponse(Response.Status.OK, "application/json",
                        display.getDisplay().getDisplayId() + "");
            }

            if (session.getUri().startsWith(openApp)) {
                // 要保证参数存在，不然服务可能会崩
                // 待测试
                String packageName = session.getParms().get("package");
                String activity = session.getParms().get("activity");
                String id = session.getParms().get("displayId");
                appChannel.openApp(packageName, activity, id);
                return newFixedLengthResponse(Response.Status.OK, "application/json", "success");
            }
            if (session.getUri().startsWith("/" + "stopActivity")) {
                String packageName = session.getParms().get("package");
                String cmd = "am force-stop " + packageName;
                L.d("stopActivity activity cmd : " + cmd);
                // adb -s $serial shell am start -n $packageName/$activity
                try {
                    Runtime.getRuntime().exec(cmd);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return newFixedLengthResponse(Response.Status.OK, "application/json", "success");
            }
            // 获取一个App的所有Activity
            if (session.getUri().startsWith("/" + AppChannelProtocol.getAppActivity)) {
                String packageName = session.getParameters().get("package").get(0);
                byte[] bytes = appChannel.getAppActivitys(packageName).getBytes();
                return newFixedLengthResponse(Response.Status.OK, "application/json", new ByteArrayInputStream(bytes),
                        bytes.length);
            }
            // 获取App的权限信息
            if (session.getUri().startsWith("/" + AppChannelProtocol.getAppPermissions)) {
                List<String> line = session.getParameters().get("package");
                String packageName = line.get(0);
                byte[] bytes = appChannel.getAppPermissions(packageName).getBytes();
                return newFixedLengthResponse(Response.Status.OK, "application/json", new ByteArrayInputStream(bytes),
                        bytes.length);
            }
            if (session.getUri().startsWith("/" + "injectInputEvent")) {
                String action = session.getParms().get("action");
                String pointerId = session.getParms().get("pointerId");
                String deviceWidth = session.getParms().get("width");
                String deviceHeight = session.getParms().get("height");
                String x = session.getParms().get("x");
                String y = session.getParms().get("y");
                String displayId = session.getParms().get("displayId");
                String actionButton = session.getParms().get("actionButton");
                String buttons = session.getParms().get("buttons");
                int displayIdInt = Integer.parseInt(displayId);
                int actionInt = Integer.parseInt(action);
                long pointerIdInt = Long.parseLong(pointerId);
                int xInt = Integer.parseInt(x);
                int yInt = Integer.parseInt(y);
                int widthInt = Integer.parseInt(deviceWidth);
                int heightInt = Integer.parseInt(deviceHeight);
                int actionButtonInt = Integer.parseInt(actionButton);
                int buttonsInt = Integer.parseInt(buttons);
                Position position = new Position(xInt, yInt, widthInt, heightInt);
                inputDispatcher.setDisplayId(displayIdInt);
                float pressure = 0f;
                if (actionInt == MotionEvent.ACTION_DOWN || actionInt == MotionEvent.ACTION_MOVE) {
                    pressure = 1f;
                }
                boolean success = inputDispatcher.injectTouch(actionInt, pointerIdInt, position, pressure,
                        actionButtonInt, buttonsInt);
                return newFixedLengthResponse(Response.Status.OK, "application/text", "success:" + success);
            }
            return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "not found");
        } catch (Exception e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", e.toString());
        }
    }
}
