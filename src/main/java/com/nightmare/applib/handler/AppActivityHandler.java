package com.nightmare.applib.handler;

import static com.nightmare.applib.AppServer.appChannel;
import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.nightmare.applib.FakeContext;
import com.nightmare.applib.interfaces.IHTTPHandler;

import java.io.ByteArrayInputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import fi.iki.elonen.NanoHTTPD;

// 获取一个App的所有Activity
public class AppActivityHandler extends IHTTPHandler {
    @Override
    public String route() {
        return "/app_activity";
    }

    @Override
    public NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session) {
        String packageName = session.getParms().get("package");
        byte[] bytes = getAppActivitys(packageName).getBytes();
        return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", new ByteArrayInputStream(bytes), bytes.length);
    }


    public String getAppActivitys(String data) {
        StringBuilder builder = new StringBuilder();
        @SuppressLint("QueryPermissionsNeeded")
        List<String> packages = getAppPackages();
        for (String packageName : packages) {
            PackageInfo info = null;
            info = IconHandler.getPackageInfo(packageName);
            if (info.packageName.equals(data)) {
                PackageInfo packageInfo = IconHandler.getPackageInfo(data);
                if (packageInfo.activities == null) {
                    return "";
                }
                for (ActivityInfo activityInfo : packageInfo.activities) {
                    builder.append(activityInfo.name).append("\n");
                }
            }
        }
        return builder.toString();

    }

    public List<String> getAppPackages() {
        PackageManager pm = FakeContext.get().getPackageManager();
        List<String> packages = new ArrayList<String>();
        @SuppressLint("QueryPermissionsNeeded")
        List<PackageInfo> infos = pm.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
        for (PackageInfo info : infos) {
            packages.add(info.packageName);
        }
        return packages;
    }
}
