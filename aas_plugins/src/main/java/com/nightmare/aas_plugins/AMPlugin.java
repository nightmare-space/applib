package com.nightmare.aas_plugins;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.app.IActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;

import com.nightmare.aas.AndroidAPIPlugin;
import com.nightmare.aas.ContextStore;
import com.nightmare.aas.FakeContext;
import com.nightmare.aas.L;
import com.nightmare.aas_plugins.util.TaskUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.util.ArrayList;
import java.util.List;

import fi.iki.elonen.NanoHTTPD;

public class AMPlugin extends AndroidAPIPlugin {

    @Override
    public String route() {
        return "/activity_manager";
    }

    @Override
    public NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session) {
        String packageName = session.getParms().get("package");
        String action = session.getParms().get("action");
        assert action != null;
        if (action.equals("start_activity")) {
            String activity = session.getParms().get("activity");
            String id = session.getParms().get("displayId");
            startActivity(packageName, activity, id);
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("result", "success");
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", jsonObject.toString());
        } else if (action.equals("stop_activity")) {
            IBinder binder = ServiceManager.getService(Context.ACTIVITY_SERVICE);
            IActivityManager activityManagerServices = IActivityManager.Stub.asInterface(binder);
            JSONObject jsonObject = new JSONObject();
            try {
                try {
                    activityManagerServices.forceStopPackage(packageName, -2);
                    jsonObject.put("result", "success");
                } catch (RemoteException | JSONException e) {
                    jsonObject.put("result", "failed");
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", jsonObject.toString());
        } else if (action.equals("get_app_activities")) {
            String activitys = getAppActivities(packageName);
            return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", activitys);
        } else if (action.equals("get_app_detail")) {
            L.d("get_app_detail");
            String detail = getAppDetail(packageName);
            return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", detail);
        } else if (action.equals("get_all_app_info")) {
            String line = session.getParms().get("is_system_app");
            boolean isSystemApp = Boolean.parseBoolean(line);
            String apps = getAllAppInfoV2(isSystemApp);
            return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", apps);
        } else if (action.equals("app_main_activity")) {
            String mainActivity = getAppMainActivity(packageName);
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("activity", mainActivity);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", jsonObject.toString());
        } else if (action.equals("get_tasks")) {
            try {
                return newFixedLengthResponse(
                        NanoHTTPD.Response.Status.OK,
                        "application/json",
                        TaskUtil.getRecentTasksJson().toString()
                );
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", "{}");
    }

    public void startActivity(String packageName, String activity, String displayId) {
        Intent launchIntent = new Intent();
        launchIntent.setClassName(packageName, activity);

        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        Bundle options = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ActivityOptions launchOptions = ActivityOptions.makeBasic();
            launchOptions.setLaunchDisplayId(Integer.parseInt(displayId));
            options = launchOptions.toBundle();
        }
        try {
            IBinder binder = ServiceManager.getService(Context.ACTIVITY_SERVICE);
            IActivityManager activityManagerServices = IActivityManager.Stub.asInterface(binder);
            activityManagerServices.startActivityAsUser(
                    /* caller */ null,
                    /* callingPackage */ FakeContext.PACKAGE_NAME,
                    /* intent */ launchIntent,
                    /* resolvedType */ null,
                    /* resultTo */ null,
                    /* resultWho */ null,
                    /* requestCode */ 0,
                    /* startFlags */ 0,
                    /* profilerInfo */ null,
                    /* bOptions */ options,
                    /* userId */ /* UserHandle.USER_CURRENT */ -2
            );
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }


    public String getAppActivities(String packageName) {
        PackageManager packageManager = ContextStore.getContext().getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            ActivityInfo[] activities = packageInfo.activities;
            if (activities == null) {
                return "[]";
            }
            JSONObject jsonObject = new JSONObject();
            JSONArray jsonArray = new JSONArray();
            jsonObject.put("package", packageName);
            jsonObject.put("activitys", jsonArray);
            for (ActivityInfo activityInfo : activities) {
                jsonArray.put(activityInfo.name);
            }
            return jsonObject.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "[]";
        }
    }

    public String getAppDetail(String data) {
        JSONObject jsonObject = new JSONObject();
        try {
            PackageInfo packageInfo = PMPlugin.getPackageInfo(data);
            jsonObject.put("firstInstallTime", packageInfo.firstInstallTime);
            jsonObject.put("lastUpdateTime", packageInfo.lastUpdateTime);
            jsonObject.put("dataDir", packageInfo.applicationInfo.dataDir);
            jsonObject.put("nativeLibraryDir", packageInfo.applicationInfo.nativeLibraryDir);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return jsonObject.toString();
    }


    public static String getLabel(ApplicationInfo info) {
        PackageManager pm = ContextStore.getContext().getPackageManager();
        return (String) info.loadLabel(pm);
        // TODO(lin) 下面是个啥
//        int res = info.labelRes;
//        if (info.nonLocalizedLabel != null) {
//            return (String) info.nonLocalizedLabel;
//        }
//        if (res != 0) {
//            AssetManager assetManager = getAssetManagerFromPath(info.sourceDir);
//            Resources resources = new Resources(assetManager, displayMetrics, configuration);
//            return (String) resources.getText(res);
//        }
//        return null;
    }


    public String getAllAppInfoV2(boolean isSystemApp) {
        List<String> packages = getAppPackages();
        if (packages == null) {
            return "";
        }
        JSONObject jsonObjectResult = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        try {
            for (String packageName : packages) {
                JSONObject appInfoJson = new JSONObject();
                PackageInfo packageInfo = PMPlugin.getPackageInfo(packageName);
                if (packageInfo == null) {
                    continue;
                }
                int resultTag = packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM;
                if (isSystemApp) {
                    // 是否只列表系统应用,=0为用户应用，跳过
                    if (resultTag == 0) {
                        continue;
                    }
                } else {
                    if (resultTag > 0) {
                        continue;
                    }
                }
                ApplicationInfo applicationInfo = packageInfo.applicationInfo;
                appInfoJson.put("package", applicationInfo.packageName);
                appInfoJson.put("label", getLabel(applicationInfo));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    appInfoJson.put("minSdk", packageInfo.applicationInfo.minSdkVersion);
                } else {
                    appInfoJson.put("minSdk", 0);
                }
                appInfoJson.put("targetSdk", applicationInfo.targetSdkVersion);
                appInfoJson.put("versionName", packageInfo.versionName);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    appInfoJson.put("versionCode", packageInfo.getLongVersionCode());
                } else {
                    appInfoJson.put("versionCode", packageInfo.versionCode);
                }
                appInfoJson.put("enabled", applicationInfo.enabled);
                // TODO 判断 Apk 是否隐藏需要重新实现
                PackageInfo withoutHidePackage = PMPlugin.getPackageInfo(packageInfo.packageName, PackageManager.GET_DISABLED_COMPONENTS);
                appInfoJson.put("hide", false);
                appInfoJson.put("uid", applicationInfo.uid);
                appInfoJson.put("sourceDir", applicationInfo.sourceDir);
                jsonArray.put(appInfoJson);
            }
            jsonObjectResult.put("datas", jsonArray);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return jsonObjectResult.toString();
    }


    public List<String> getAppPackages() {
        PackageManager pm = ContextStore.getContext().getPackageManager();
        List<String> packages = new ArrayList<String>();
        @SuppressLint("QueryPermissionsNeeded")
        List<PackageInfo> infos = pm.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
        for (PackageInfo info : infos) {
            packages.add(info.packageName);
        }
        return packages;
    }


    /**
     * 通过包名获取Main Activity
     *
     * @param packageName
     * @return
     */
    public String getAppMainActivity(String packageName) {
        StringBuilder builder = new StringBuilder();
        PackageManager pm = ContextStore.getContext().getPackageManager();
        Intent launchIntent = pm.getLaunchIntentForPackage(packageName);
        if (launchIntent != null) {
            builder.append(launchIntent.getComponent().getClassName());
        } else {
            L.d(packageName + "获取启动Activity失败");
        }
        return builder.toString();
//        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
//        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
//        List<ResolveInfo> appList = pm.queryIntentActivities(mainIntent, null, 0, 0);
//        for (int i = 0; i < appList.size(); i++) {
//            ResolveInfo resolveInfo = appList.get(i);
//            String packageStr = resolveInfo.activityInfo.packageName;
//            if (packageStr.equals(packageName)) {
//                builder.append(resolveInfo.activityInfo.name).append("\n");
//                break;
//            }
//        }
//        return builder.toString();
    }
}
