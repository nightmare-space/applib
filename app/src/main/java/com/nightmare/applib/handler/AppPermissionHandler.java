package com.nightmare.applib.handler;

import static com.nightmare.applib.AppServer.appChannel;
import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;
import com.nightmare.applib.interfaces.IHTTPHandler;
import fi.iki.elonen.NanoHTTPD;

public class AppPermissionHandler implements IHTTPHandler {
    @Override
    public String route() {
        return "/app_permission";
    }

    @Override
    public NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session) {
        String packageName = session.getParms().get("package");
        String permissions = appChannel.getAppPermissions(packageName);
        return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", permissions);
    }
    // 获取App的权限信息
}
