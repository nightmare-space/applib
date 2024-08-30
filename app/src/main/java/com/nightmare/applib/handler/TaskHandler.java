package com.nightmare.applib.handler;

import static com.nightmare.applib.AppServer.appChannel;

import com.nightmare.applib.interfaces.IHTTPHandler;
import com.nightmare.applib.utils.L;
import com.nightmare.applib.utils.TaskUtil;

import fi.iki.elonen.NanoHTTPD;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

public class TaskHandler implements IHTTPHandler {
    @Override
    public String route() {
        return "/tasks";
    }

    @Override
    public NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session) {
//        L.d("TaskHandler handle");
        try {
            return newFixedLengthResponse(
                    NanoHTTPD.Response.Status.OK,
                    "application/json",
                    TaskUtil.getRecentTasksJson(appChannel).toString()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
