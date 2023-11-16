package com.nightmare.applib;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.TaskInfo;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.hardware.input.InputManager;
import android.os.Build;
import android.os.SystemClock;
import android.view.Display;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.MotionEvent;
import android.view.SurfaceView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

import com.nightmare.applib.utils.L;
import com.nightmare.applib.utils.ServerUtil;
import com.nightmare.applib.utils.Workarounds;
import com.nightmare.applib.wrappers.InputManagerSimulate;
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

    public static void main(String[] args) throws Exception {
        L.d("Welcome!!!");
        AppServer server = ServerUtil.safeGetServer();
        Workarounds.prepareMainLooper();
        // 这个时候构造的是一个没有Context的Channel
        server.appChannel = new AppChannel();
        L.d("success start port -> " + server.getListeningPort());
        writePort("/sdcard", server.getListeningPort());
        // 让进程等待
        // 不能用 System.in.read(),如果执行 app_process 是类似于
        // Process.run 等方法就会出现异常，System.in.read()需要宿主进程由标准终端调用
//        System.in.read();
        while (true) {
            Thread.sleep(1000);
        }
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

    @Override
    public Response serve(IHTTPSession session) {
        try {
            // 获取最近任务
            if (session.getUri().startsWith("/tasks")) {
                return newFixedLengthResponse(Response.Status.OK, "application/json", getRecentTasksJson().toString());
            }
            // 获取图标
            if (session.getUri().startsWith("/icon")) {
//                Log.d(session.getParameters().toString());
                Map<String, List<String>> params = session.getParameters();
                if (!params.isEmpty()) {
                    List<String> line = session.getParameters().get("path");
                    String path = line.get(0);
                    byte[] bytes = appChannel.getApkBitmapBytes(path);
                    return newFixedLengthResponse(Response.Status.OK, "image/jpg", new ByteArrayInputStream(bytes), bytes.length);
                }
                byte[] bytes = appChannel.getBitmapBytes(session.getUri().substring("/icon/".length()));
                // print(bytes);
                return newFixedLengthResponse(Response.Status.OK, "image/jpg", new ByteArrayInputStream(bytes), bytes.length);
            }
            // 获取所有的应用信息
            // 包含被隐藏的，被冻结的
            if (session.getUri().startsWith("/" + AppChannelProtocol.getAllAppInfo)) {
                boolean isSystemApp = false;
                List<String> line = session.getParameters().get("is_system_app");
                if (line != null && !line.isEmpty()) {
                    isSystemApp = Boolean.parseBoolean(line.get(0));
                }
                byte[] bytes = appChannel.getAllAppInfo(isSystemApp).getBytes();
                return newFixedLengthResponse(Response.Status.OK, "application/json", new ByteArrayInputStream(bytes), bytes.length);
            }
            // 获取指定App列表的信息
            if (session.getUri().startsWith("/" + AppChannelProtocol.getAppInfos)) {
                List<String> packages = session.getParameters().get("apps");
                byte[] bytes = appChannel.getAppInfos(packages).getBytes();
                return newFixedLengthResponse(Response.Status.OK, "application/json", new ByteArrayInputStream(bytes), bytes.length);
            }
            // 获取单个App的详细信息
            if (session.getUri().startsWith("/" + AppChannelProtocol.getAppDetail)) {
                List<String> line = session.getParameters().get("package");
                String packageName = line.get(0);
                byte[] bytes = appChannel.getAppDetail(packageName).getBytes();
                return newFixedLengthResponse(Response.Status.OK, "application/json", new ByteArrayInputStream(bytes), bytes.length);
            }
            // 获取缩略图
            if (session.getUri().startsWith("/" + AppChannelProtocol.getTaskThumbnail)) {
                List<String> line = session.getParameters().get("id");
                String id = line.get(0);
                byte[] bytes = getTaskThumbnail(Integer.parseInt(id));
                return newFixedLengthResponse(Response.Status.OK, "image/jpg", new ByteArrayInputStream(bytes), bytes.length);
            }
            // 通过包名获取Main Activity
            if (session.getUri().startsWith("/" + AppChannelProtocol.getAppMainActivity)) {
                List<String> line = session.getParameters().get("package");
                String packageName = line.get(0);
                byte[] bytes = appChannel.getAppMainActivity(packageName).getBytes();
                return newFixedLengthResponse(Response.Status.OK, "application/json", new ByteArrayInputStream(bytes), bytes.length);
            }
            // 创建虚拟显示器
            if (session.getUri().startsWith("/" + AppChannelProtocol.createVirtualDisplay)) {
                SurfaceView surfaceView = new SurfaceView(appChannel.context);
                String width = session.getParameters().get("width").get(0);
                String height = session.getParameters().get("height").get(0);
                String density = session.getParameters().get("density").get(0);
                VirtualDisplay display = ServiceManager.getDisplayManager().createVirtualDisplay(
                        surfaceView.getHolder().getSurface(),
                        Integer.parseInt(width),
                        Integer.parseInt(height),
                        Integer.parseInt(density)
                );
                cache.put(display.getDisplay().getDisplayId(), display);
                return newFixedLengthResponse(Response.Status.OK, "application/json", display.getDisplay().getDisplayId() + "");
            }
            // 创建虚拟显示器
            if (session.getUri().startsWith("/" +"resize_vd")) {
                String id = session.getParameters().get("id").get(0);
                String width = session.getParameters().get("width").get(0);
                String height = session.getParameters().get("height").get(0);
                String density = session.getParameters().get("density").get(0);
                VirtualDisplay display = cache.get(Integer.parseInt(id));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    display.resize(Integer.parseInt(width), Integer.parseInt(height), Integer.parseInt(density));
                }
                return newFixedLengthResponse(Response.Status.OK, "application/json", display.getDisplay().getDisplayId() + "");
            }
            if (session.getUri().startsWith("/" + AppChannelProtocol.openAppByPackage)) {
                // 要保证参数存在，不然服务可能会崩
                // 待测试
                String packageName = session.getParameters().get("package").get(0);
                String activity = session.getParameters().get("activity").get(0);
                String id = session.getParameters().get("displayId").get(0);
                appChannel.openApp(packageName, activity, id);
                byte[] result = "success".getBytes();
                return newFixedLengthResponse(Response.Status.OK, "application/json", new ByteArrayInputStream(result), result.length);
            }
            if (session.getUri().startsWith("/" + "stopActivity")) {
                String packageName = session.getParameters().get("package").get(0);
                String cmd = "am force-stop " + packageName;
                L.d("stopActivity activity cmd : " + cmd);
                // adb -s $serial shell am start -n $packageName/$activity
                try {
                    Runtime.getRuntime().exec(cmd);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                byte[] result = "success".getBytes();
                return newFixedLengthResponse(Response.Status.OK, "application/json", new ByteArrayInputStream(result), result.length);
            }
            // 获取一个App的所有Activity
            if (session.getUri().startsWith("/" + AppChannelProtocol.getAppActivity)) {
                String packageName = session.getParameters().get("package").get(0);
                byte[] bytes = appChannel.getAppActivitys(packageName).getBytes();
                return newFixedLengthResponse(Response.Status.OK, "application/json", new ByteArrayInputStream(bytes), bytes.length);
            }
            // 获取App的权限信息
            if (session.getUri().startsWith("/" + AppChannelProtocol.getAppPermissions)) {
                List<String> line = session.getParameters().get("package");
                String packageName = line.get(0);
                byte[] bytes = appChannel.getAppPermissions(packageName).getBytes();
                return newFixedLengthResponse(Response.Status.OK, "application/json", new ByteArrayInputStream(bytes), bytes.length);
            }
            if (session.getUri().startsWith("/" + "displays")) {
                DisplayManager displayManager = (DisplayManager) appChannel.context.getSystemService(Context.DISPLAY_SERVICE);
                Display[] displays = displayManager.getDisplays();
                StringBuilder builder = new StringBuilder();
                for (Display display : displays) {
                    builder.append(display.getDisplayId());
                    builder.append("\n");
                }
                byte[] bytes = builder.toString().getBytes();
                return newFixedLengthResponse(Response.Status.OK, "application/json", new ByteArrayInputStream(bytes), bytes.length);
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
                boolean success = inputDispatcher.injectTouch(actionInt, pointerIdInt, position, pressure, actionButtonInt, buttonsInt);
                return newFixedLengthResponse(Response.Status.OK, "application/text", "success:" + success);
            }
            return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "not found");
        } catch (Exception e) {
            e.printStackTrace();
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", e.toString());
        }
    }


    private byte[] getTaskThumbnail(int id) throws Exception {
        long start = System.currentTimeMillis();
//        listAllObject(serviceManager.getActivityManager().manager.getClass());
//        Object se = serviceManager.getActivityManager().manager.getClass().getMethod("getServices").invoke(serviceManager.getActivityManager().manager);

        Class<?> cls = Class.forName("android.app.ActivityTaskManager");
        @SuppressLint("BlockedPrivateApi") java.lang.reflect.Method services = cls.getDeclaredMethod("getService");
        Object iam = services.invoke(null);
//        listAllObject(iam.getClass());
//        listAllObject(iam.getClass());
        java.lang.reflect.Method snapshotMethod = iam.getClass().getDeclaredMethod("getTaskSnapshot", int.class, boolean.class);
        snapshotMethod.setAccessible(true);
//        Bitmap.w
        Object snapshot = snapshotMethod.invoke(iam, id, true);
        if (snapshot == null) return null;
//        listAllObject(snapshot.getClass());
        java.lang.reflect.Field buffer = snapshot.getClass().getDeclaredField("mSnapshot");
        buffer.setAccessible(true);
        Object hardBuffer = buffer.get(snapshot);
//        Object hardBuffer = Class.forName("android.hardware.HardwareBuffer").getMethod("createFromGraphicBuffer", buffer.getClass()).invoke(null, buffer);
        Object colorSpace = snapshot.getClass().getMethod("getColorSpace").invoke(snapshot);
        Class<?> bitmapCls = Class.forName("android.graphics.Bitmap");
//        listAllObject(bitmapCls);
//        Bitmap.wrapHardwareBuffer()
        java.lang.reflect.Method wrapHardwareBufferMethod = bitmapCls.getMethod("wrapHardwareBuffer", hardBuffer.getClass(), Class.forName("android.graphics.ColorSpace"));
        Bitmap bmp = (Bitmap) wrapHardwareBufferMethod.invoke(null, hardBuffer, colorSpace);
        if (bmp == null) return null;
        System.out.println("create " + (System.currentTimeMillis() - start));
        Bitmap scaledBmp = Bitmap.createScaledBitmap(bmp, bmp.getWidth(), bmp.getHeight(), false);
        System.out.println(System.currentTimeMillis() - start);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        scaledBmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        System.out.println(System.currentTimeMillis() - start);
        return baos.toByteArray();
    }


    public List<ActivityManager.RecentTaskInfo> getRecentTasks(int maxNum, int flags, int userId) throws Exception {
        Object iam = getIAM();
        Object tasksParcelled = iam.getClass().getMethod("getRecentTasks", Integer.TYPE,
                Integer.TYPE, Integer.TYPE).invoke(iam, 25, 0, 0);
        List<ActivityManager.RecentTaskInfo> tasks = (List<ActivityManager.RecentTaskInfo>) tasksParcelled.getClass().getMethod("getList").invoke(tasksParcelled);
        return tasks;
    }

    private static Object getIAM() throws Exception {
        Object iam = ActivityManager.class.getMethod("getService").invoke(null);
        return iam;
    }

    private JSONObject getRecentTasksJson() throws Exception {
        List<ActivityManager.RecentTaskInfo> tasks = getRecentTasks(5, 0, 0);
        JSONObject jsonObjectResult = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        for (ActivityManager.RecentTaskInfo taskInfo : tasks) {
            JSONObject jsonObject = new JSONObject();
//            System.out.println("serving: " + taskInfo.toString());
            jsonObject.put("id", taskInfo.id);
            jsonObject.put("persistentId", taskInfo.persistentId);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    // 30是安卓11
                    @SuppressLint("BlockedPrivateApi") java.lang.reflect.Field field = TaskInfo.class.getDeclaredField("displayId");
                    field.setAccessible(true);
                    Object displayId = field.get(taskInfo);
                    jsonObject.put("displayId", displayId);
                }
                // 有的任务后台久了，会拿不到topActivity
                jsonObject.put("topPackage", taskInfo.topActivity == null ? "" : taskInfo.topActivity.getPackageName());
                jsonObject.put("topAcivity", taskInfo.topActivity == null ? "" : taskInfo.topActivity.getClassName());
                if (taskInfo.topActivity == null) {
                    PackageInfo packageInfo = appChannel.getPackageInfo(taskInfo.topActivity.getPackageName());
                    jsonObject.put("label", appChannel.getLabel(packageInfo.applicationInfo));
                } else {
                    jsonObject.put("label", "");
                }
            }
            jsonArray.put(jsonObject);
        }
        jsonObjectResult.put("datas", jsonArray);
        return jsonObjectResult;
    }
}
