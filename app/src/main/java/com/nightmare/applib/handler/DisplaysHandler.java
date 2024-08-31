package com.nightmare.applib.handler;

import static com.nightmare.applib.AppServer.appChannel;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.display.DisplayManager;
import android.view.Display;

import com.nightmare.applib.interfaces.IHTTPHandler;
import com.nightmare.applib.utils.DisplayUtil;
import com.nightmare.applib.utils.L;
import com.nightmare.applib.wrappers.DisplayManagerV2;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fi.iki.elonen.NanoHTTPD;

public class DisplaysHandler implements IHTTPHandler {
    @Override
    public String route() {
        return "/displays";
    }

    @Override
    public NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session) {
        @SuppressLint({"NewApi", "LocalSuppress"})
        DisplayManagerV2 displayManagerV2 = DisplayManagerV2.create();
        DisplayManager displayManager = (DisplayManager) appChannel.context.getSystemService(Context.DISPLAY_SERVICE);
        Display[] displayss = displayManager.getDisplays();
        L.d("DisplaysHandler Invoke");
        // 打印所有的显示器
        for (Display display : displayss) {
            L.d("display -> " + display);
        }

        JSONObject jsonObjectResult = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        for (Display display : displayss) {
            JSONObject jsonObject = null;
            try {
                jsonObject = DisplayUtil.getDisplayInfo(display);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            jsonArray.put(jsonObject);
        }
        try {
            jsonObjectResult.put("datas", jsonArray);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", jsonObjectResult.toString());
    }
}
