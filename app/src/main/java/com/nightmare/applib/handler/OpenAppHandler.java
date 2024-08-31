package com.nightmare.applib.handler;

import static com.nightmare.applib.AppServer.appChannel;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

import com.nightmare.applib.interfaces.IHTTPHandler;

import fi.iki.elonen.NanoHTTPD;

public class OpenAppHandler implements IHTTPHandler {
    @Override
    public String route() {
        return "/start_activity";
    }

    @Override
    public NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session) {
        // 要保证参数存在，不然服务可能会崩
        String packageName = session.getParms().get("package");
        String activity = session.getParms().get("activity");
        String id = session.getParms().get("displayId");
        appChannel.openApp(packageName, activity, id);
        return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "text/plain", "success");
    }
}
