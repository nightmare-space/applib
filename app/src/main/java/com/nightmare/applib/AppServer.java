package com.nightmare.applib;

import static com.nightmare.applib.AppChannel.print;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;


import com.nightmare.applib.wrappers.ServiceManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.List;

import fi.iki.elonen.NanoHTTPD;

public class AppServer extends NanoHTTPD {
    public AppServer(String address, int port) {
        super(address, port);
    }

    public ServiceManager serviceManager = new ServiceManager();
    static final int RANGE_START = 6000;
    static final int RANGE_END = 6040;
    AppChannel appInfo;

    public static void print(Object object) {
        System.out.println(">>>>" + object.toString());
        System.out.flush();
    }

    public static void main(String[] args) throws Exception {
        print("Welcome!!!");
        ServerSocket serverSocket = safeGetServerSocket();
        assert serverSocket != null;
        serverSocket.setReuseAddress(true);
        AppServer server = safeGetServer();
        Workarounds.prepareMainLooper();
//        Context ctx = getContextWithoutActivity();
        server.appInfo = new AppChannel();
        System.out.println("success start:" + server.getListeningPort());
        System.out.flush();
        // 不能让进程退了
        System.in.read();
    }

    public static AppServer safeGetServer() {
        for (int i = RANGE_START; i < RANGE_END; i++) {
            AppServer server = new AppServer("0.0.0.0", i);
            try {
                server.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
                return server;
            } catch (IOException e) {
                print("端口" + i + "被占用");
            }
        }
        return null;
    }

    // 更安全的拿到一个ServerSocket
    // 有的时候会端口占用
    public static ServerSocket safeGetServerSocket() {
        for (int i = RANGE_START; i < RANGE_END; i++) {
            try {
                return new ServerSocket(i);
            } catch (IOException e) {
                print("端口" + i + "被占用");
            }
        }
        return null;
    }

    public static void startServerFromActivity(Context context) throws IOException {
        AppServer server = safeGetServer();
        writePort(context.getFilesDir().getPath(), server.getListeningPort());
        server.appInfo = new AppChannel(context);
        System.out.println("success start:" + server.getListeningPort());
        System.out.flush();
    }

    public static void writePort(String path, int port) {
        OutputStream out = null;
        try {
            out = new FileOutputStream(path + "/server_port");
            Log.d("Nightmare", path);
            out.write((port + "").getBytes());
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Response serve(IHTTPSession session) {
        try {
            if (session.getUri().equals("/")) {
                return newFixedLengthResponse(Response.Status.OK, "application/json", genJson().toString());
            }
            if (session.getUri().startsWith("/icon/")) {
                byte[] bytes = appInfo.getBitmapBytes(session.getUri().substring("/icon/".length()));
                return newFixedLengthResponse(Response.Status.OK, "image/jpg", new ByteArrayInputStream(bytes), bytes.length);
            }
            if (session.getUri().startsWith("/" + AppChannelProtocol.getAllAppInfo)) {
                boolean isSystemApp;
                List<String> line = session.getParameters().get("is_system_app");
                if (line == null || line.isEmpty()) {
                    isSystemApp = false;
                } else {
                    isSystemApp = Boolean.parseBoolean(line.get(0));
                }
                byte[] bytes = appInfo.getAllAppInfo(isSystemApp).getBytes();
                return newFixedLengthResponse(Response.Status.OK, "application/json", new ByteArrayInputStream(bytes), bytes.length);
            }
            if (session.getUri().startsWith("/" + AppChannelProtocol.getAppInfos)) {
                List<String> line = session.getParameters().get("apps");
                byte[] bytes = appInfo.getAppInfos(line).getBytes();
                return newFixedLengthResponse(Response.Status.OK, "application/json", new ByteArrayInputStream(bytes), bytes.length);
            }
            if (session.getUri().startsWith("/" + AppChannelProtocol.getAppDetail)) {
                List<String> line = session.getParameters().get("package");
                String packageName = line.get(0);
                byte[] bytes = appInfo.getAppDetail(packageName).getBytes();
                return newFixedLengthResponse(Response.Status.OK, "application/json", new ByteArrayInputStream(bytes), bytes.length);
            }
            if (session.getUri().startsWith("/" + AppChannelProtocol.getTaskThumbnail)) {
                List<String> line = session.getParameters().get("id");
                String id = line.get(0);
                byte[] bytes = getTaskThumbnail(Integer.parseInt(id));
                return newFixedLengthResponse(Response.Status.OK, "image/jpg", new ByteArrayInputStream(bytes), bytes.length);
            }
            if (session.getUri().startsWith("/" + AppChannelProtocol.getAppMainActivity)) {
//                List<String> line = session.getParameters().get("package");
//                String packageName = line.get(0);
//                byte[] bytes = appInfo.getAppMainActivity(packageName).getBytes();
//                return newFixedLengthResponse(Response.Status.OK, "application/json", new ByteArrayInputStream(bytes), bytes.length);
            }
            if (session.getUri().startsWith("/" + AppChannelProtocol.getAppActivity)) {
//                List<String> line = session.getParameters().get("package");
//                String packageName = line.get(0);
//                byte[] bytes = appInfo.getAppActivitys(packageName).getBytes();
//                return newFixedLengthResponse(Response.Status.OK, "application/json", new ByteArrayInputStream(bytes), bytes.length);
            }
            if (session.getUri().startsWith("/" + AppChannelProtocol.getAppPermissions)) {
//                List<String> line = session.getParameters().get("package");
//                String packageName = line.get(0);
//                byte[] bytes = appInfo.getAppPermissions(packageName).getBytes();
//                return newFixedLengthResponse(Response.Status.OK, "application/json", new ByteArrayInputStream(bytes), bytes.length);
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

    public static void listAllObject(Class clazz) {
        try {

            print("Class " + clazz.getName());
            // 反射属性字段
            Field[] fields = clazz.getDeclaredFields();

            // 反射方法字段
            java.lang.reflect.Method[] methods = clazz.getDeclaredMethods();

            // 反射构造器
            Constructor[] constuctors = clazz.getDeclaredConstructors();

            print("FIELD========");
            for (Field f : fields) {
                System.out.print((char) 0x1b + "[32mTYPE:");
                System.out.print((char) 0x1b + "[31m");
                System.out.print(f.getType());
                System.out.print((char) 0x1b);
                System.out.println("[0;32m NAME:" + (char) 0x1b + "[31m" + f.getName() + (char) 0x1b + "[0m");
                System.out.flush();
            }

            print("METHOD========");
            for (java.lang.reflect.Method m : methods) {
                System.out.print((char) 0x1b + "[33mMETHOD NAME:");
                System.out.print((char) 0x1b + "[31m");
                System.out.print(m.getName());
                System.out.print((char) 0x1b);
                System.out.print("[0;33m Parameter:" + (char) 0x1b + "[31m" + Arrays.toString(m.getParameters()) + (char) 0x1b + "[0m");
                System.out.println((char) 0x1b + "[33m RETURE TYPE:" + (char) 0x1b + "[31m" + m.getGenericReturnType() + (char) 0x1b + "[0m");
                System.out.flush();
            }

            print("CONSTUCTOR========");
            for (Constructor c : constuctors) {
                System.out.print((char) 0x1b);
                System.out.print("[34m");
                System.out.flush();
                print("NAME:" + c.getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
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
                    PackageInfo packageInfo = appInfo.getPackageInfo(taskInfo.topActivity.getPackageName());
                    jsonObject.put("label", appInfo.getLabel(packageInfo.applicationInfo));
                }
            }
            jsonArray.put(jsonObject);
        }
        return jsonArray;
    }
}
