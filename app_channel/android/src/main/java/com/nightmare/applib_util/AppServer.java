package com.nightmare.applib_util;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.view.Display;

import androidx.annotation.RequiresApi;

import com.nightmare.applib.Workarounds;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

import com.nightmare.applib_util.utils.Lg;

/**
 * 基于HTTP服务提供能力
 */
public class AppServer extends NanoHTTPD {
    public AppServer(String address, int port) {
        super(address, port);
    }

    // 端口尝试的范围
    static final int RANGE_START = 14000;
    static final int RANGE_END = 14040;

    AppChannel appChannel;

    public static void main(String[] args) throws Exception {
        Lg.d("Welcome!!!");
        AppServer server = safeGetServer();
        Workarounds.prepareMainLooper();
        // 这个时候构造的是一个没有Context的Channel
        server.appChannel = new AppChannel();
        Lg.d("success start port : >" + server.getListeningPort() + "<");
        // 让进程等待
        System.in.read();
    }

    /**
     * 安全获得服务器的的方法
     */
    public static AppServer safeGetServer() {
        for (int i = RANGE_START; i < RANGE_END; i++) {
            AppServer server = new AppServer("0.0.0.0", i);
            try {
                server.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
                return server;
            } catch (IOException e) {
                Lg.d("端口" + i + "被占用");
            }
        }
        return null;
    }

    /**
     * 更安全的拿到一个ServerSocket
     * 有的时候会端口占用
     *
     * @return
     */
    public static ServerSocket safeGetServerSocket() {
        for (int i = RANGE_START; i < RANGE_END; i++) {
            try {
                return new ServerSocket(i);
            } catch (IOException e) {
                Lg.d("端口" + i + "被占用");
            }
        }
        return null;
    }

    /**
     * 与直接启动dex不同，从Activity中启动不用反射context上下问
     *
     * @param context
     * @throws IOException
     */
    public static void startServerFromActivity(Context context) throws IOException {
        AppServer server = safeGetServer();
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
        OutputStream out = null;
        try {
            out = new FileOutputStream(path + "/server_port");
            Lg.d(path);
            out.write((port + "").getBytes());
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public Response serve(IHTTPSession session) {
        try {
            if (session.getUri().equals("/")) {
                return newFixedLengthResponse(Response.Status.OK, "application/json", genJson().toString());
            }
            // 获取图标
            if (session.getUri().startsWith("/icon")) {
                Lg.d(session.getParameters().toString());
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
            // 通过报名获取Main Activity
            if (session.getUri().startsWith("/" + AppChannelProtocol.getAppMainActivity)) {
                List<String> line = session.getParameters().get("package");
                String packageName = line.get(0);
                byte[] bytes = appChannel.getAppMainActivity(packageName).getBytes();
                return newFixedLengthResponse(Response.Status.OK, "application/json", new ByteArrayInputStream(bytes), bytes.length);
            }
            // 创建虚拟显示器
            if (session.getUri().startsWith("/" + AppChannelProtocol.createVirtualDisplay)) {
//                appInfo.creatVirtualDisplay();
                return newFixedLengthResponse(Response.Status.OK, "application/json", "ok");
            }
            if (session.getUri().startsWith("/" + com.nightmare.applib.AppChannelProtocol.openAppByPackage)) {
                // 要保证参数存在，不然服务可能会崩
                // 待测试
                String packageName = session.getParameters().get("package").get(0);
                String activity = session.getParameters().get("activity").get(0);
//                String id = session.getParameters().get("displayId").get(0);
//                String id = appChannel.context.getDisplay().getDisplayId() + "";
                DisplayManager displayManager = (DisplayManager) appChannel.context.getSystemService(Context.DISPLAY_SERVICE);
                Display[] displays = displayManager.getDisplays();
                Lg.d("当前display" + Arrays.toString(displays));
                appChannel.openApp(packageName, activity, "2");
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

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
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
            }
            if (session.getUri().startsWith("/thumb/")) {
                int id = Integer.parseInt(session.getUri().substring("/thumb/".length()));
                byte[] bytes = getTaskThumbnail(id);
                if (bytes == null)
                    return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "not found");
                return newFixedLengthResponse(Response.Status.OK, "image/jpg", new ByteArrayInputStream(bytes), bytes.length);
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
        java.lang.reflect.Method services = cls.getDeclaredMethod("getService");
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

    private JSONArray genJson() throws Exception {
        List<ActivityManager.RecentTaskInfo> tasks = getRecentTasks(5, 0, 0);
        JSONArray jsonArray = new JSONArray();
        for (ActivityManager.RecentTaskInfo taskInfo : tasks) {
            JSONObject jsonObject = new JSONObject();
//            System.out.println("serving: " + taskInfo.toString());
            jsonObject.put("id", taskInfo.id);
            jsonObject.put("persistentId", taskInfo.persistentId);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                jsonObject.put("topPackage", taskInfo.topActivity == null ? null : taskInfo.topActivity.getPackageName());
                if (jsonObject.has("topPackage")) {
                    PackageInfo packageInfo = appChannel.getPackageInfo(taskInfo.topActivity.getPackageName());
                    jsonObject.put("label", appChannel.getLabel(packageInfo.applicationInfo));
                }
            }
            jsonArray.put(jsonObject);
        }
        return jsonArray;
    }
}
