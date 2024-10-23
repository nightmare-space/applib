package com.nightmare.applib.handler;

import static com.nightmare.applib.AppServer.appChannel;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

import android.annotation.SuppressLint;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Build;

import com.nightmare.applib.ContextStore;
import com.nightmare.applib.FakeContext;
import com.nightmare.applib.interfaces.IHTTPHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import fi.iki.elonen.NanoHTTPD;

public class AppInfosHandler extends IHTTPHandler {


    @Override
    public String route() {
        return "/allappinfo";
    }

    @Override
    public NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session) {
        String line = session.getParms().get("is_system_app");
        boolean isSystemApp = Boolean.parseBoolean(line);
        String apps = null;
        try {
            apps = getAllAppInfoV2(isSystemApp);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", apps);
    }

    public static String getLabel(ApplicationInfo info) {
        PackageManager pm = ContextStore.getInstance().getContext().getPackageManager();
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


    public String getAllAppInfoV2(boolean isSystemApp) throws JSONException {
        List<String> packages = getAppPackages();
        if (packages == null) {
            return "";
        }
        JSONObject jsonObjectResult = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        for (String packageName : packages) {
            JSONObject appInfoJson = new JSONObject();
            PackageInfo packageInfo = IconHandler.getPackageInfo(packageName);
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
            PackageInfo withoutHidePackage = IconHandler.getPackageInfo(packageInfo.packageName, PackageManager.GET_DISABLED_COMPONENTS);
            appInfoJson.put("hide", false);
            appInfoJson.put("uid", applicationInfo.uid);
            appInfoJson.put("sourceDir", applicationInfo.sourceDir);
            jsonArray.put(appInfoJson);
        }
        jsonObjectResult.put("datas", jsonArray);
        return jsonObjectResult.toString();
    }


    public List<String> getAppPackages() {
        PackageManager pm = ContextStore.getInstance().getContext().getPackageManager();
        List<String> packages = new ArrayList<String>();
        @SuppressLint("QueryPermissionsNeeded")
        List<PackageInfo> infos = pm.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
        for (PackageInfo info : infos) {
            packages.add(info.packageName);
        }
        return packages;
    }
}
