package com.nightmare.applib.handler;

import static com.nightmare.applib.AppServer.appChannel;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.display.DisplayManager;
import android.view.Display;

import com.nightmare.applib.FakeContext;
import com.nightmare.applib.interfaces.IHTTPHandler;
import com.nightmare.applib.utils.DisplayUtil;
import com.nightmare.applib.utils.L;
import com.nightmare.applib.wrappers.DisplayManagerV2;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;

import fi.iki.elonen.NanoHTTPD;

public class DisplaysHandler implements IHTTPHandler {
    @Override
    public String route() {
        return "/displays";
    }

    @Override
    public NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session) {
        // Android 11/12/13/14/15 (test on 2024.09.17) is ok
        DisplayManager displayManager = null;
        try {
            //noinspection JavaReflectionMemberAccess
            displayManager = DisplayManager.class.getDeclaredConstructor(Context.class).newInstance(FakeContext.get());
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        Display[] displayss = displayManager.getDisplays();
        L.d("DisplaysHandler Invoke");
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
