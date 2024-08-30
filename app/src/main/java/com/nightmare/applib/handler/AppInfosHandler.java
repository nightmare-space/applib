package com.nightmare.applib.handler;

import static com.nightmare.applib.AppServer.appChannel;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

import com.nightmare.applib.interfaces.IHTTPHandler;

import org.json.JSONException;

import java.lang.reflect.InvocationTargetException;

import fi.iki.elonen.NanoHTTPD;

public class AppInfosHandler implements IHTTPHandler {


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
            apps = appChannel.getAllAppInfoV2(isSystemApp);
        } catch (InvocationTargetException | IllegalAccessException | JSONException e) {
            throw new RuntimeException(e);
        }
        return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", apps);
    }
}
