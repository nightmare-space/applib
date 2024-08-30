package com.nightmare.applib.handler;

import static com.nightmare.applib.AppServer.appChannel;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

import com.nightmare.applib.interfaces.IHTTPHandler;
import com.nightmare.applib.utils.L;

import java.io.ByteArrayInputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

public class IconHandler implements IHTTPHandler {
    @Override
    public String route() {
        return "/icon";
    }

    @Override
    public NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session) {
        String url = session.getUri();
        String path = session.getParms().get("path");
        L.d("icon get path -> " + path);
        byte[] bytes = null;
        if (path != null) {
            try {
                bytes = appChannel.getApkBitmapBytes(path);
            } catch (InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                String packageName = url.substring("/icon/".length());
                int dotIndex = packageName.lastIndexOf('.'); // 找到 '.' 的位置
                String result;
                if (dotIndex != -1) {
                    result = packageName.substring(0, dotIndex); // 获取 '.' 前的部分
                } else {
                    result = packageName; // 如果没有 '.'，返回原字符串
                }
                packageName = result;
                L.d("package -> " + packageName);
                bytes = appChannel.getBitmapBytes(packageName);
            } catch (InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        NanoHTTPD.Response response = newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "image/png", new ByteArrayInputStream(bytes), bytes.length);
//        response.addHeader("Access-Control-Allow-Origin", "*");
//        response.addHeader("Access-Control-Allow-Methods", "*");
//        response.addHeader("Access-Control-Allow-Headers","*");
//        response.addHeader("Accept-Ranges","bytes");
        return response;
    }
}
