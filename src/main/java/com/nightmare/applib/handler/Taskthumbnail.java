package com.nightmare.applib.handler;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

import com.nightmare.applib.interfaces.IHTTPHandler;
import com.nightmare.applib.utils.TaskUtil;

import java.io.ByteArrayInputStream;

import fi.iki.elonen.NanoHTTPD;

// 获取缩略图
public class Taskthumbnail implements IHTTPHandler {
    @Override
    public String route() {
        return "/taskthumbnail";
    }

    @Override
    public NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session) {
        String id = session.getParms().get("id");
        byte[] bytes = null;
        try {
            bytes = TaskUtil.getTaskThumbnail(Integer.parseInt(id));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "image/jpg", new ByteArrayInputStream(bytes), bytes.length);
    }
}
