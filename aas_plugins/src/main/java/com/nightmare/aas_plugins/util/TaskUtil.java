package com.nightmare.aas_plugins.util;

import android.annotation.SuppressLint;
import android.app.TaskInfo;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.IInterface;
import com.nightmare.aas_plugins.ActivityManagerPlugin;
import com.nightmare.aas_plugins.PackageManagerPlugin;

import org.json.JSONArray;
import org.json.JSONObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class TaskUtil {

    private static List<ActivityManager.RecentTaskInfo> getRecentTasks(int maxNum, int flags) throws Exception {
        IInterface iam = getIAM();
        // 第二个 flags 参数是0，表示不过滤
        // RECENT_IGNORE_UNAVAILABLE
        //Added in API level 11
        //
        //public static final int RECENT_IGNORE_UNAVAILABLE
        //Provides a list that does not contain any recent tasks that currently are not available to the user.
        //
        //Constant Value: 2 (0x00000002)
        //
        //RECENT_WITH_EXCLUDED
        //Added in API level 1
        //
        //public static final int RECENT_WITH_EXCLUDED
        //Flag for use with getRecentTasks(int, int): return all tasks, even those that have set their Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS flag.
        //
        //Constant Value: 1 (0x00000001)
        Method getRecentTasks = iam.getClass().getMethod("getRecentTasks", Integer.TYPE, Integer.TYPE, Integer.TYPE);
        Object tasksParcelled = getRecentTasks.invoke(iam, maxNum, flags, 0);
        assert tasksParcelled != null;
        Class<?> taskParceledCls = tasksParcelled.getClass();
        Method getList = taskParceledCls.getMethod("getList");
        //noinspection unchecked
        return (List<ActivityManager.RecentTaskInfo>) getList.invoke(tasksParcelled);
    }

    private static IInterface getIAM() throws Exception {
        try {
            // On old Android versions, the ActivityManager is not exposed via AIDL,
            // so use ActivityManagerNative.getDefault()
            @SuppressLint("PrivateApi") Class<?> cls = Class.forName("android.app.ActivityManagerNative");
            @SuppressLint("DiscouragedPrivateApi") Method getDefaultMethod = cls.getDeclaredMethod("getDefault");
            return (IInterface) getDefaultMethod.invoke(null);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    private static boolean isSystemPackage(Context context, String packageName) {
        try {
            return (context.getPackageManager().getApplicationInfo(packageName, 0).flags & android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 获取最近任务，以json格式返回
    static public JSONObject getRecentTasksJson() throws Exception {
//        L.d("getRecentTasksJson handle");
        /// TODO check 这个功能在低版本 Android 下是否可用
//        ActivityManager activityManager = (ActivityManager) appChannel.context.getSystemService(Context.ACTIVITY_SERVICE);
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
//            // For Android Lollipop and above
//            List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = activityManager.getRunningAppProcesses();
//            for (ActivityManager.RunningAppProcessInfo processInfo : runningAppProcesses) {
//                // print info
//                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
//                    L.d("processInfo -> " + processInfo.processName);
//                    L.d("importance -> " + processInfo.importance);
//                    for (String activeProcess : processInfo.pkgList) {
//                        if (!isSystemPackage(appChannel.context, activeProcess)) {
//                            L.d("activeProcess -> " + activeProcess);
//                        }
//                    }
//                }
////                ReflectUtil.listAllObject(processInfo);
////                break;
////                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
////                    for (String activeProcess : processInfo.pkgList) {
////                        if (activeProcess.equals(packageName)) {
////                            return true;
////                        }
////                    }
////                }
//            }
//        } else {
//            // For Android versions below Lollipop
////            List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(1);
////            if (!tasks.isEmpty()) {
////                ActivityManager.RunningTaskInfo taskInfo = tasks.get(0);
////                if (taskInfo.topActivity.getPackageName().equals(packageName)) {
////                    return true;
////                }
////            }
//        }
        List<ActivityManager.RecentTaskInfo> tasks = getRecentTasks(100, 0);
        JSONObject jsonObjectResult = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        for (ActivityManager.RecentTaskInfo taskInfo : tasks) {
            JSONObject jsonObject = new JSONObject();
            // System.out.println("serving: " + taskInfo.toString());
            jsonObject.put("id", taskInfo.id);
            // above Android 3.1
            jsonObject.put("persistentId", taskInfo.persistentId);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    // 30是安卓11
                    //noinspection JavaReflectionMemberAccess
                    @SuppressLint({"BlockedPrivateApi", "PrivateApi"})
                    Field field = TaskInfo.class.getDeclaredField("displayId");
                    field.setAccessible(true);
                    Object displayId = field.get(taskInfo);
                    jsonObject.put("displayId", displayId);
                    // isVisible
                    @SuppressLint({"BlockedPrivateApi", "PrivateApi"})
                    Field isVisibleField = TaskInfo.class.getDeclaredField("isVisible");
                    field.setAccessible(true);
                    Object isVisible = isVisibleField.get(taskInfo);
                    jsonObject.put("isVisible", isVisible);
                    // isFocused
                    // isRunning
                    Field isRunningField = TaskInfo.class.getDeclaredField("isRunning");
                    field.setAccessible(true);
                    Object isRunning = isRunningField.get(taskInfo);
                    jsonObject.put("isRunning", isRunning);
                    @SuppressLint({"BlockedPrivateApi", "PrivateApi"})
                    Field isFocusedField = TaskInfo.class.getDeclaredField("isFocused");
                    field.setAccessible(true);
                    Object isFocused = isFocusedField.get(taskInfo);
                    jsonObject.put("isFocused", isFocused);

                }
                // 有的任务后台久了，会拿不到topActivity
                jsonObject.put("topPackage", taskInfo.topActivity == null ? "" : taskInfo.topActivity.getPackageName());
                jsonObject.put("topActivity", taskInfo.topActivity == null ? "" : taskInfo.topActivity.getClassName());
                if (taskInfo.topActivity != null) {
                    PackageInfo packageInfo = PackageManagerPlugin.getPackageInfo(taskInfo.topActivity.getPackageName());
                    jsonObject.put("label", ActivityManagerPlugin.getLabel(packageInfo.applicationInfo));
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
