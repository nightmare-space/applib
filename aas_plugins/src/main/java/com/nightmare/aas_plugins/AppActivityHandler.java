package com.nightmare.aas_plugins;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.nightmare.aas.AndroidAPIPlugin;
import com.nightmare.aas.ContextStore;
import com.nightmare.aas.FakeContext;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import fi.iki.elonen.NanoHTTPD;

// 获取一个App的所有Activity
public class AppActivityHandler extends AndroidAPIPlugin {
    @Override
    public String route() {
        return "/app_activitys";
    }

    @Override
    public NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session) {
        String packageName = session.getParms().get("package");
        byte[] bytes = getAppActivities(packageName).getBytes();
        return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", new ByteArrayInputStream(bytes), bytes.length);
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
}
