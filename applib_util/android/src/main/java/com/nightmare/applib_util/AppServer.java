package com.nightmare.applib_util;

import static com.nightmare.applib_util.AppChannel.getContextWithoutActivity;
import static com.nightmare.applib_util.AppChannel.print;
import static com.nightmare.applib_util.AppChannel.writePort;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;

import fi.iki.elonen.NanoHTTPD;

public class AppServer extends NanoHTTPD {
    public AppServer(String address, int port) {
        super(address, port);
    }

    static final int RANGE_START = 6000;
    static final int RANGE_END = 6040;
    AppChannel appInfo;

    public static void main(String[] args) throws Exception {
        print("Welcome!!!");
        Context ctx = getContextWithoutActivity();
        AppServer server = safeGetServer();
        server.appInfo = new AppChannel(ctx);
        Workarounds.prepareMainLooper();
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

    public static void startServerFromActivity(Context context) throws IOException {
        print("123");
        AppServer server = safeGetServer();
        server.appInfo = new AppChannel(context);
        writePort(context.getFilesDir().getPath(), server.getListeningPort());
        System.out.println("success start:" + server.getListeningPort());
        System.out.flush();
    }

    @Override
    public Response serve(IHTTPSession session) {
        try {
            print(session.getParameters());
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
            if (session.getUri().startsWith("/" + AppChannelProtocol.getAppMainActivity)) {
                List<String> line = session.getParameters().get("package");
                String packageName = line.get(0);
                byte[] bytes = appInfo.getAppMainActivity(packageName).getBytes();
                return newFixedLengthResponse(Response.Status.OK, "application/json", new ByteArrayInputStream(bytes), bytes.length);
            }
            if (session.getUri().startsWith("/" + AppChannelProtocol.getAppActivity)) {
                List<String> line = session.getParameters().get("package");
                String packageName = line.get(0);
                byte[] bytes = appInfo.getAppActivitys(packageName).getBytes();
                return newFixedLengthResponse(Response.Status.OK, "application/json", new ByteArrayInputStream(bytes), bytes.length);
            }
            if (session.getUri().startsWith("/" + AppChannelProtocol.getAppPermissions)) {
                List<String> line = session.getParameters().get("package");
                String packageName = line.get(0);
                byte[] bytes = appInfo.getAppPermissions(packageName).getBytes();
                return newFixedLengthResponse(Response.Status.OK, "application/json", new ByteArrayInputStream(bytes), bytes.length);
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
        Object iam = getIAM();
        Object thumbnail = iam.getClass().getMethod("getTaskSnapshot", Integer.TYPE, Boolean.TYPE).invoke(iam, id, false);
        if (thumbnail == null) return null;
        Object graphicBuffer = (Object) thumbnail.getClass().getMethod("getSnapshot").invoke(thumbnail);
        if (graphicBuffer == null) return null;
        System.out.println(System.currentTimeMillis() - start);
        Bitmap bmp = (Bitmap) Bitmap.class.getMethod("createHardwareBitmap", graphicBuffer.getClass()).invoke(null, graphicBuffer);
        if (bmp == null) return null;
        System.out.println("create " + (System.currentTimeMillis() - start));
        Bitmap scaledBmp = Bitmap.createScaledBitmap(bmp, bmp.getWidth(), bmp.getHeight(), false);
        System.out.println(System.currentTimeMillis() - start);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        scaledBmp.compress(Bitmap.CompressFormat.JPEG, 80, baos);
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
            System.out.println("serving: " + taskInfo.toString());
            jsonObject.put("id", taskInfo.id);
            jsonObject.put("persistentId", taskInfo.persistentId);
            jsonObject.put("topPackage", taskInfo.topActivity == null ? null : taskInfo.topActivity.getPackageName());
            jsonArray.put(jsonObject);
        }
        return jsonArray;
    }
}
