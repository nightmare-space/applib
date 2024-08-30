package com.nightmare.applib.handler;

import static com.nightmare.applib.AppServer.appChannel;
import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

import com.nightmare.applib.interfaces.IHTTPHandler;

import java.io.ByteArrayInputStream;

import fi.iki.elonen.NanoHTTPD;

// 获取一个App的所有Activity
public class AppActivityHandler implements IHTTPHandler {
    @Override
    public String route() {
        return "/app_activity";
    }

    @Override
    public NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session) {
        String packageName = session.getParms().get("package");
        byte[] bytes = appChannel.getAppActivitys(packageName).getBytes();
        return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", new ByteArrayInputStream(bytes), bytes.length);
    }
}
