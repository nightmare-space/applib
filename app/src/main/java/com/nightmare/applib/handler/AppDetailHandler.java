package com.nightmare.applib.handler;

import static com.nightmare.applib.AppServer.appChannel;
import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;
import com.nightmare.applib.interfaces.IHTTPHandler;
import fi.iki.elonen.NanoHTTPD;

// 获取单个App的详细信息
public class AppDetailHandler implements IHTTPHandler {
    @Override
    public String route() {
        return "/appdetail";
    }

    @Override
    public NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session) {
        String packageName = session.getParms().get("package");
        String detail = appChannel.getAppDetail(packageName);
        return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", detail);

    }
}
