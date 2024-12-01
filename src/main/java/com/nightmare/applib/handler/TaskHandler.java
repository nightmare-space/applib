package com.nightmare.applib.handler;


import com.nightmare.applib.interfaces.IHTTPHandler;
import com.nightmare.applib.utils.L;
import com.nightmare.applib.utils.TaskUtil;

import fi.iki.elonen.NanoHTTPD;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class TaskHandler extends IHTTPHandler {
    @Override
    public String route() {
        return "/tasks";
    }

    @Override
    public NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session) {
//        return newFixedLengthResponse(
//                    NanoHTTPD.Response.Status.OK,
//                    "application/json",
//                    ""
//            );
        L.d("TaskHandler handle");
        try {
            return newFixedLengthResponse(
                    NanoHTTPD.Response.Status.OK,
                    "application/json",
                    TaskUtil.getRecentTasksJson().toString()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
