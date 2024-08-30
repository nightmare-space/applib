package com.nightmare.applib.handler;

import static com.nightmare.applib.AppServer.appChannel;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

import com.nightmare.applib.interfaces.IHTTPHandler;

import java.io.ByteArrayInputStream;

import fi.iki.elonen.NanoHTTPD;

public class AppPermissionHandler implements IHTTPHandler {
    @Override
    public String route() {
        return "/apppermission";
    }

    @Override
    public NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session) {
        String packageName = session.getParms().get("package");
        byte[] bytes = appChannel.getAppPermissions(packageName).getBytes();
        return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", new ByteArrayInputStream(bytes),
                bytes.length);
    }
    // 获取App的权限信息
}
