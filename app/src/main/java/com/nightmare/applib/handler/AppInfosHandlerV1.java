package com.nightmare.applib.handler;

import static com.nightmare.applib.AppServer.appChannel;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

import com.nightmare.applib.interfaces.IHTTPHandler;

import java.io.ByteArrayInputStream;
import java.lang.reflect.InvocationTargetException;

import fi.iki.elonen.NanoHTTPD;

public class AppInfosHandlerV1 implements IHTTPHandler {
    @Override
    public String route() {
        return "/allappinfo";
    }

    @Override
    public NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session) {
        // 获取所有的应用信息
        // 包含被隐藏的，被冻结的
        boolean isSystemApp = false;
        String line = session.getParms().get("is_system_app");
        isSystemApp = Boolean.parseBoolean(line);
        byte[] bytes = null;
        try {
            bytes = appChannel.getAllAppInfo(isSystemApp).getBytes();
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", new ByteArrayInputStream(bytes), bytes.length);
    }
}
