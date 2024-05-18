package com.nightmare.applib.utils;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RecentTaskInfo;
import android.app.TaskInfo;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.os.Build;

import com.nightmare.applib.AppChannel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class TaskUtil {
    static public byte[] getTaskThumbnail(int id) throws Exception {
        long start = System.currentTimeMillis();
        @SuppressLint("PrivateApi")
        Class<?> cls = Class.forName("android.app.ActivityTaskManager");
        @SuppressLint({"BlockedPrivateApi", "PrivateApi"})
        Method services = cls.getDeclaredMethod("getService");
        Object iam = services.invoke(null);
        assert iam != null;
        Method snapshotMethod = iam.getClass().getDeclaredMethod(
                "getTaskSnapshot", int.class,
                boolean.class
        );
        snapshotMethod.setAccessible(true);
        Object snapshot = snapshotMethod.invoke(iam, id, true);
        if (snapshot == null) return null;
        Field buffer = snapshot.getClass().getDeclaredField("mSnapshot");
        buffer.setAccessible(true);
        Object hardBuffer = buffer.get(snapshot);
        Object colorSpace = snapshot.getClass().getMethod("getColorSpace").invoke(snapshot);
        Class<?> bitmapCls = Class.forName("android.graphics.Bitmap");
        Class<?> colorSpaceCls = Class.forName("android.graphics.ColorSpace");
        assert hardBuffer != null;
        //noinspection JavaReflectionMemberAccess
        Method wrapHardwareBufferMethod = bitmapCls.getMethod(
                "wrapHardwareBuffer",
                hardBuffer.getClass(),
                colorSpaceCls
        );
        //noinspection JavaReflectionInvocation
        Bitmap bmp = (Bitmap) wrapHardwareBufferMethod.invoke(null, hardBuffer, colorSpace);
        if (bmp == null)
            return null;
        System.out.println("create " + (System.currentTimeMillis() - start));
        Bitmap scaledBmp = Bitmap.createScaledBitmap(bmp, bmp.getWidth(), bmp.getHeight(), false);
        System.out.println(System.currentTimeMillis() - start);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        scaledBmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        System.out.println(System.currentTimeMillis() - start);
        return baos.toByteArray();
    }


    private static List<ActivityManager.RecentTaskInfo> getRecentTasks(
            int maxNum, int flags,
            int userId
    ) throws Exception {
        Object iam = getIAM();
        Object tasksParcelled = iam.getClass().getMethod("getRecentTasks", Integer.TYPE,
                Integer.TYPE, Integer.TYPE).invoke(iam, 25, 0, 0);
        assert tasksParcelled != null;
        Class<?> taskParceledCls = tasksParcelled.getClass();
        Method getList = taskParceledCls.getMethod("getList");
        //noinspection unchecked
        return (List<RecentTaskInfo>) getList.invoke(tasksParcelled);
    }

    private static Object getIAM() throws Exception {
        //noinspection JavaReflectionMemberAccess
        Object iam = ActivityManager.class.getMethod("getService").invoke(null);
        return iam;
    }

    // 获取最近任务，以json格式返回
    static public JSONObject getRecentTasksJson(AppChannel appChannel) throws Exception {
        List<RecentTaskInfo> tasks = getRecentTasks(5, 0, 0);
        JSONObject jsonObjectResult = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        for (RecentTaskInfo taskInfo : tasks) {
            JSONObject jsonObject = new JSONObject();
            // System.out.println("serving: " + taskInfo.toString());
            jsonObject.put("id", taskInfo.id);
            // above Android 3.1
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
                jsonObject.put("persistentId", taskInfo.persistentId);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    // 30是安卓11
                    //noinspection JavaReflectionMemberAccess
                    @SuppressLint({"BlockedPrivateApi", "PrivateApi"})
                    Field field = TaskInfo.class.getDeclaredField("displayId");
                    field.setAccessible(true);
                    Object displayId = field.get(taskInfo);
                    jsonObject.put("displayId", displayId);
                }
                // 有的任务后台久了，会拿不到topActivity
                jsonObject.put("topPackage", taskInfo.topActivity == null ? "" : taskInfo.topActivity.getPackageName());
                jsonObject.put("topActivity", taskInfo.topActivity == null ? "" : taskInfo.topActivity.getClassName());
                if (taskInfo.topActivity != null) {
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
