package com.nightmare.applib;

import static android.media.MediaFormat.MIMETYPE_VIDEO_AVC;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.WindowManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
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
        L.d("Sula input socket server starting.");
        assert server != null;
        Workarounds.apply(true, true);
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

    public static final int IFRAME_INTERVAL = 0;
    // MediaFormat需要的，比特率
    public static final int BIT_RATE = 800_0000;
    // MediaFormat需要的
    public static final int REPEAT_FRAME_DELAY_US = 100_000;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private static MediaFormat createFormat(String videoMimeType) {
        MediaFormat format = new MediaFormat();
        format.setString(MediaFormat.KEY_MIME, videoMimeType);
        format.setInteger(MediaFormat.KEY_BIT_RATE, BIT_RATE);
        // must be present to configure the encoder, but does not impact the actual frame rate, which is variable
        format.setInteger(MediaFormat.KEY_FRAME_RATE, 60);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            format.setInteger(MediaFormat.KEY_COLOR_RANGE, MediaFormat.COLOR_RANGE_LIMITED);
        }
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFRAME_INTERVAL);
        // display the very first frame, and recover from bad quality when no new frames
        format.setLong(MediaFormat.KEY_REPEAT_PREVIOUS_FRAME_AFTER, REPEAT_FRAME_DELAY_US); // µs
//        if (maxFps > 0) {
//            // The key existed privately before Android 10:
//            // <https://android.googlesource.com/platform/frameworks/base/+/625f0aad9f7a259b6881006ad8710adce57d1384%5E%21/>
//            // <https://github.com/Genymobile/scrcpy/issues/488#issuecomment-567321437>
//            format.setFloat(KEY_MAX_FPS_TO_ENCODER, maxFps);
//        }

        return format;
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

//                    L.d("in.read start");
                    try {
                        bytesRead = in.read(data);
                    } catch (IOException e) {
                        L.d("in.read e : " + e);
                    }
                    if (bytesRead == -1) {
                        break;
                    }
//                    L.d("Sula input Received : " + Arrays.toString(data));
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
     * 与直接启动dex不同，从Activity中启动不用反射context上下文
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
            L.d("port file path-> " + filePath);
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
    public static final String getAppMainActivity = "/appmainactivity";

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

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public Response serve(IHTTPSession session) {
        L.d("uri -> " + session.getUri());
        try {
            String url = session.getUri();
            // 获取最近任务
            if (url.startsWith("/tasks")) {
                return newFixedLengthResponse(
                        Response.Status.OK,
                        "application/json",
                        TaskUtil.getRecentTasksJson(appChannel).toString()
                );
            }
            if (url.startsWith("/cmd")) {
                Map<String, String> body = new HashMap<>();
                session.parseBody(body);
                L.d("body -> " + body.get("postData"));
                Process process = null;
                try {
                    process = Runtime.getRuntime().exec(body.get("postData"));

                    // 处理命令的输出
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                    PrintWriter writer = new PrintWriter(process.getOutputStream());

                    // 如果需要向命令写入输入
                    writer.println("your_input_here"); // 替换为你的输入
                    writer.flush();

                    // 读取命令的标准输出
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println("Output: " + line);
                    }

                    // 读取命令的错误输出
                    while ((line = errorReader.readLine()) != null) {
                        System.err.println("Error: " + line);
                    }

                    // 等待命令执行完毕
                    int exitCode = process.waitFor();
                    System.out.println("Exit Code: " + exitCode);

                    // 关闭流
                    reader.close();
                    errorReader.close();
                    writer.close();
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    if (process != null) {
                        process.destroy();
                    }
                }


                return newFixedLengthResponse(Response.Status.OK, "application/json", "success");
            }
            // 获取图标
            if (url.startsWith("/icon")) {
                Map<String, List<String>> params = session.getParameters();
                String path = session.getParms().get("path");
                L.d("path -> " + path);
                if (path != null) {
                    byte[] bytes = appChannel.getApkBitmapBytes(path);
                    return newFixedLengthResponse(Response.Status.OK, "image/jpg", new ByteArrayInputStream(bytes),
                            bytes.length);
                }
                byte[] bytes = appChannel.getBitmapBytes(url.substring("/icon/".length()));
                // print(bytes);
                return newFixedLengthResponse(Response.Status.OK, "image/jpg", new ByteArrayInputStream(bytes), bytes.length);
            }
            if (url.startsWith("/allappinfo_v2")) {
                String line = session.getParms().get("is_system_app");
                boolean isSystemApp = Boolean.parseBoolean(line);
                String apps = appChannel.getAllAppInfoV2(isSystemApp);
                return newFixedLengthResponse(Response.Status.OK, "application/json", apps);
            }
            // 获取所有的应用信息
            // 包含被隐藏的，被冻结的
            if (url.startsWith("/allappinfo")) {
                boolean isSystemApp = false;
                String line = session.getParms().get("is_system_app");
                isSystemApp = Boolean.parseBoolean(line);
                byte[] bytes = appChannel.getAllAppInfo(isSystemApp).getBytes();
                return newFixedLengthResponse(Response.Status.OK, "application/json", new ByteArrayInputStream(bytes), bytes.length);
            }
            // 获取指定App列表的信息
            if (url.startsWith("/appinfos")) {
                List<String> packages = session.getParameters().get("apps");
                byte[] bytes = appChannel.getAppInfos(packages).getBytes();
                return newFixedLengthResponse(Response.Status.OK, "application/json", new ByteArrayInputStream(bytes), bytes.length);
            }
            // 获取单个App的详细信息
            if (url.startsWith("/appdetail")) {
                String packageName = session.getParms().get("package");
                byte[] bytes = appChannel.getAppDetail(packageName).getBytes();
                return newFixedLengthResponse(Response.Status.OK, "application/json", new ByteArrayInputStream(bytes), bytes.length);
            }
            // 获取缩略图
            if (url.startsWith("/taskthumbnail")) {
                String id = session.getParms().get("id");
                byte[] bytes = TaskUtil.getTaskThumbnail(Integer.parseInt(id));
                return newFixedLengthResponse(Response.Status.OK, "image/jpg", new ByteArrayInputStream(bytes), bytes.length);
            }
            // 通过包名获取Main Activity
            if (url.startsWith(getAppMainActivity)) {
                String packageName = session.getParms().get("package");
                String mainActivity = appChannel.getAppMainActivity(packageName);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("mainActivity", mainActivity);
                return newFixedLengthResponse(Response.Status.OK, "application/json", jsonObject.toString());
            }
            if (url.startsWith("/createVirtualDisplayV2")) {
                DisplayManagerV2 displayManagerV2 = DisplayManagerV2.create();
                SurfaceView surfaceView = new SurfaceView(appChannel.context);
                displayManagerV2.createVirtualDisplay("test", 1080, 1920, 240, surfaceView.getHolder().getSurface());
                return newFixedLengthResponse(Response.Status.OK, "application/json", "");
            }
            if (url.startsWith(displayGetRoute)) {
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
            if (url.startsWith(createVirtualDisplay)) {
                boolean useDeviceConfig = session.getParms().get("useDeviceConfig").equals("true");
                WindowManager windowManager = (WindowManager) appChannel.context.getSystemService(Context.WINDOW_SERVICE);
                DisplayMetrics displayMetrics = new DisplayMetrics();
                windowManager.getDefaultDisplay().getRealMetrics(displayMetrics);
                String width, height, density;
                L.d("useDeviceConfig -> " + useDeviceConfig);
                if (useDeviceConfig) {
                    width = displayMetrics.widthPixels + "";
                    height = displayMetrics.heightPixels + "";
                    density = displayMetrics.densityDpi + "";
                } else {
                    width = session.getParms().get("width");
                    height = session.getParms().get("height");
                    density = session.getParms().get("density");
                }
                L.d("width -> " + width + " height -> " + height + " density -> " + density);
                MediaFormat format = createFormat(MIMETYPE_VIDEO_AVC);
                format.setInteger(MediaFormat.KEY_WIDTH, Integer.parseInt(width));
                format.setInteger(MediaFormat.KEY_HEIGHT, Integer.parseInt(height));
                MediaCodec codec = MediaCodec.createEncoderByType(MIMETYPE_VIDEO_AVC);
                codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
//                SurfaceView surfaceView = new SurfaceView(appChannel.context);
//                Surface surface = surfaceView.getHolder().getSurface();
                Surface surface = codec.createInputSurface();
                VirtualDisplay display = ServiceManager.getDisplayManager().createVirtualDisplay(
                        surface,
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
            if (url.startsWith("/change_display")) {
//                DisplayManager displayManager = (DisplayManager) appChannel.context.getSystemService(Context.DISPLAY_SERVICE);
//                Display[] displays = displayManager.getDisplays();
//                Display currentDisplay = displays[0];
//                Display.Mode[] modes = new Display.Mode[0];
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                    modes = currentDisplay.getSupportedModes();
//                }
////                currentDisplay.
//                Log.d("SecondaryActivityWithFloatWindow", "modes -> " + Arrays.toString(modes));
//                Display.Mode mode = modes[1]; // 选择第一个支持的模式
//                for (Display.Mode m : modes) {
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                        if (m.getModeId() == 88) { // 选择刷新率为60Hz的模式
//                            mode = m;
//                            final Window window = getWindow();
//                            final WindowManager.LayoutParams params = window.getAttributes();
//                            Log.d("SecondaryActivityWithFloatWindow", "modes -> " + mode);
//                            params.preferredDisplayModeId = mode.getModeId();
//                            window.setAttributes(params);
//                            break;
//                        }
//                    }
//                }
            }
            // 改变虚拟显示器尺寸
            if (url.startsWith(resizeVDRoute)) {
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

            if (url.startsWith(openApp)) {
                // 要保证参数存在，不然服务可能会崩
                String packageName = session.getParms().get("package");
                String activity = session.getParms().get("activity");
                String id = session.getParms().get("displayId");
                appChannel.openApp(packageName, activity, id);
                return newFixedLengthResponse(Response.Status.OK, "text/plain", "success");
            }
            if (url.startsWith("/" + "stopActivity")) {
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
            if (url.startsWith("/appactivity")) {
                String packageName = session.getParameters().get("package").get(0);
                byte[] bytes = appChannel.getAppActivitys(packageName).getBytes();
                return newFixedLengthResponse(Response.Status.OK, "application/json", new ByteArrayInputStream(bytes),
                        bytes.length);
            }
            // 获取App的权限信息
            if (url.startsWith("/apppermission")) {
                String packageName = session.getParms().get("package");
                byte[] bytes = appChannel.getAppPermissions(packageName).getBytes();
                return newFixedLengthResponse(Response.Status.OK, "application/json", new ByteArrayInputStream(bytes),
                        bytes.length);
            }
            if (url.startsWith("/" + "injectInputEvent")) {
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
