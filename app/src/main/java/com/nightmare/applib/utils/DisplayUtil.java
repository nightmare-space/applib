package com.nightmare.applib.utils;

import android.annotation.TargetApi;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;

import com.nightmare.applib.wrappers.DisplayInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DisplayUtil {
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    static public JSONObject getDisplayInfo(Display display) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        jsonObject.put("id", display.getDisplayId());
        jsonObject.put("metrics", metrics.toString());
        jsonObject.put("name", display.getName());
        // TODO 目前获取到的不准
        jsonObject.put("width", display.getWidth());
        jsonObject.put("height", display.getHeight());
        jsonObject.put("rotation", display.getRotation());
        jsonObject.put("refreshRate", display.getRefreshRate());
        jsonObject.put("density", metrics.densityDpi);
        jsonObject.put("dump", display.toString());
        return jsonObject;
    }

    static public JSONObject getDisplayInfoFromCustom(DisplayInfo display) throws JSONException {
        JSONObject jsonObject = new JSONObject();
//        DisplayMetrics metrics = new DisplayMetrics();
        jsonObject.put("id", display.getDisplayId());
//        jsonObject.put("metrics", metrics.toString());
        jsonObject.put("name", display.getName());
        jsonObject.put("width", display.getSize().getWidth());
        jsonObject.put("height", display.getSize().getHeight());
        jsonObject.put("rotation", display.getRotation());
        jsonObject.put("dump", display.toString());
        return jsonObject;
    }
}
