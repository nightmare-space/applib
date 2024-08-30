package com.nightmare.applib.handler;

import static com.nightmare.applib.AppServer.appChannel;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

import com.nightmare.applib.interfaces.IHTTPHandler;

import org.json.JSONException;
import org.json.JSONObject;

import fi.iki.elonen.NanoHTTPD;

// 通过包名获取Main Activity
public class Appmainactivity implements IHTTPHandler {
    @Override
    public String route() {
        return "/appmainactivity";
    }

    @Override
    public NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session) {
        String packageName = session.getParms().get("package");
        String mainActivity = appChannel.getAppMainActivity(packageName);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("mainActivity", mainActivity);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", jsonObject.toString());
    }
}
