package com.nightmare.applib.handler;

import static com.nightmare.applib.AppServer.appChannel;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

import com.nightmare.applib.interfaces.IHTTPHandler;

import java.io.ByteArrayInputStream;
import java.util.List;

import fi.iki.elonen.NanoHTTPD;

// 获取指定App列表的信息
public class AppInfoHandler implements IHTTPHandler {
    @Override
    public String route() {
        return "appinfos";
    }


    @Override
    public NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session) {
        List<String> packages = session.getParameters().get("apps");
        byte[] bytes = appChannel.getAppInfos(packages).getBytes();
        return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", new ByteArrayInputStream(bytes), bytes.length);
    }
}
