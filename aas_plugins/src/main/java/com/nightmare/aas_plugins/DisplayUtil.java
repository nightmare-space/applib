package com.nightmare.aas_plugins;

import android.annotation.TargetApi;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;

import org.json.JSONException;
import org.json.JSONObject;

public class DisplayUtil {
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    static public JSONObject getDisplayInfo(Display display) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getRealMetrics(metrics);
        jsonObject.put("id", display.getDisplayId());
        jsonObject.put("metrics", metrics.toString());
        jsonObject.put("name", display.getName());
        jsonObject.put("width", metrics.widthPixels);
        jsonObject.put("height", metrics.heightPixels);
        jsonObject.put("rotation", display.getRotation());
        jsonObject.put("refreshRate", display.getRefreshRate());
        jsonObject.put("density", metrics.densityDpi);
        jsonObject.put("dump", display.toString());
        return jsonObject;
    }

}
